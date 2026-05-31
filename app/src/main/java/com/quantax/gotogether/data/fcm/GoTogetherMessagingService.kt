package com.quantax.gotogether.data.fcm

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

/**
 * Receives FCM messages and refreshed tokens. Cloud-Function-emitted pushes
 * are expected to carry their semantics in the `data` payload, with `type`
 * matching one of [NotificationChannels.TYPE_CHAT], [NotificationChannels.TYPE_JOIN_REQUEST],
 * [NotificationChannels.TYPE_REQUEST_APPROVED], [NotificationChannels.TYPE_TRIP_UPDATE]:
 *
 * ```
 * {
 *   "title": "...",          // optional
 *   "body":  "...",
 *   "type":  "CHAT" | "JOIN_REQUEST" | "REQUEST_APPROVED" | "TRIP_UPDATE",
 *   "tripId":     "<tripId>",     // optional
 *   "chatroomId": "<chatroomId>"  // optional
 * }
 * ```
 *
 * Token registration is delegated to [FcmTokenRepository]; display is delegated
 * to [SystemNotificationDisplayer]. This class only translates between the two.
 */
@AndroidEntryPoint
class GoTogetherMessagingService : FirebaseMessagingService() {

    @Inject lateinit var tokenRepository: FcmTokenRepository
    @Inject lateinit var displayer: SystemNotificationDisplayer
    @Inject lateinit var auth: FirebaseAuth

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val uid = auth.currentUser?.uid ?: return
        // FirebaseMessagingService dispatches onNewToken on a background worker thread
        // and keeps the service alive only until this method returns. Block here so
        // the Firestore write completes before the service can be torn down.
        runBlocking { tokenRepository.saveToken(uid, token) }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val data = message.data
        val title = message.notification?.title ?: data["title"] ?: "GoTogether"
        val body = message.notification?.body ?: data["body"].orEmpty()
        val type = data["type"] ?: NotificationChannels.TYPE_TRIP_UPDATE
        val tripId = data["tripId"]
        val chatroomId = data["chatroomId"] ?: data["chatId"]

        displayer.display(
            SystemNotificationDisplayer.Payload(
                title = title,
                body = body,
                type = type,
                tripId = tripId,
                chatroomId = chatroomId,
                tag = chatroomId ?: tripId ?: type
            )
        )
    }
}
