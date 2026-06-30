const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();

const db = admin.firestore();
const auth = admin.auth();

/**
 * Triggered when a new message is pushed to:
 *   /chatrooms/{chatroomId}/messages/{messageId}
 *
 * Reads the chatroom's member list from Firestore, looks up each
 * recipient's FCM tokens, and sends a data-only push so the app's
 * own notification displayer handles presentation consistently in
 * both foreground and background.
 */
exports.onNewChatMessage = functions.database
  .ref("/chatrooms/{chatroomId}/messages/{messageId}")
  .onCreate(async (snapshot, context) => {
    const { chatroomId } = context.params;
    const message = snapshot.val();
    if (!message) {
      functions.logger.warn("Empty message snapshot – skipping");
      return;
    }

    const senderId = message.senderId;
    const senderName = message.senderName || "Someone";
    const text = (message.text || "").trim();
    const imageUrl = message.imageUrl || "";

    const preview = text || (imageUrl ? "📷 Photo" : "");
    if (!preview) {
      functions.logger.info("Nothing to notify about (blank message)");
      return;
    }

    // Look up the chatroom in Firestore for member list and trip title.
    let chatroomSnapshot;
    try {
      chatroomSnapshot = await db.collection("chatrooms").doc(chatroomId).get();
    } catch (e) {
      functions.logger.error(`Failed to read chatroom ${chatroomId}`, e);
      return;
    }

    if (!chatroomSnapshot.exists) {
      functions.logger.warn(`Chatroom ${chatroomId} not found in Firestore`);
      return;
    }

    const chatroom = chatroomSnapshot.data();
    const memberIds = chatroom.memberIds || [];
    const tripTitle = chatroom.tripTitle || "Trip Chat";

    // Notify everyone except the sender.
    const recipients = memberIds.filter((id) => id !== senderId);
    if (recipients.length === 0) return;

    const payload = {
      data: {
        type: "CHAT",
        title: tripTitle,
        body: `${senderName}: ${preview}`,
        chatroomId: chatroomId,
        tripId: chatroomId,
      },
    };

    const results = await Promise.allSettled(
      recipients.map((uid) => deliverToUser(uid, payload, chatroomId)),
    );

    for (const result of results) {
      if (result.status === "rejected") {
        functions.logger.error("Delivery failed for a recipient", result.reason);
      }
    }
  });

/**
 * Reads the user's FCM tokens from Firestore and sends the payload to each.
 * Invalid tokens are automatically cleaned up from the user document.
 */
async function deliverToUser(uid, payload, chatroomId) {
  let userSnapshot;
  try {
    userSnapshot = await db.collection("users").doc(uid).get();
  } catch (e) {
    functions.logger.error(`Failed to read user ${uid}`, e);
    return;
  }

  if (!userSnapshot.exists) return;

  const tokens = userSnapshot.data().fcmTokens;
  if (!tokens || !Array.isArray(tokens) || tokens.length === 0) return;

  const sendPromises = tokens.map((token) =>
    admin
      .messaging()
      .send({ ...payload, token })
      .catch((err) => handleSendError(uid, token, err)),
  );

  await Promise.allSettled(sendPromises);
}

/**
 * Handles FCM send errors – removes stale tokens from the user document.
 */
async function handleSendError(uid, token, err) {
  if (
    err.code === "messaging/invalid-registration-token" ||
    err.code === "messaging/registration-token-not-registered"
  ) {
    functions.logger.warn(`Removing stale token for user ${uid}`);
    try {
      await db
        .collection("users")
        .doc(uid)
        .update({ fcmTokens: admin.firestore.FieldValue.arrayRemove(token) });
    } catch (e) {
      functions.logger.error(`Failed to remove stale token for ${uid}`, e);
    }
  } else {
    functions.logger.error(`FCM send error for user ${uid}`, err);
  }
}
