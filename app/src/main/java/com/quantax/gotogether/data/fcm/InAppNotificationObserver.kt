package com.quantax.gotogether.data.fcm

import com.quantax.gotogether.data.repository.ChatRepository
import com.quantax.gotogether.data.repository.NotificationRepository
import com.quantax.gotogether.domain.model.Chatroom
import com.quantax.gotogether.domain.model.Notification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Foreground bridge between Firestore/RTDB state and the system notification tray.
 *
 * Without a server-side push pipeline, FCM only fires when something external
 * pushes to it. To keep the app useful end-to-end, this observer watches:
 *
 *  - The user's chatrooms — emits a system notification when `lastMessageTimestamp`
 *    moves forward (i.e. a new message arrived), except for the chatroom currently
 *    open in the UI ([ActiveChatroomTracker]) or messages sent by the user themself.
 *  - The user's `notifications` collection — emits a system notification for each
 *    newly-arrived Firestore notification (e.g. a join-request approval).
 *
 * Owns its own process-scoped coroutine scope so the observer keeps running across
 * Activity recreations. Start/stop is driven by Firebase auth state — the observer
 * latches onto the current user and resets baselines when the user switches.
 */
@Singleton
class InAppNotificationObserver @Inject constructor(
    private val chatRepository: ChatRepository,
    private val notificationRepository: NotificationRepository,
    private val displayer: SystemNotificationDisplayer,
    private val activeChatroomTracker: ActiveChatroomTracker
) {

    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var job: Job? = null
    private var currentUserId: String? = null

    private val lastChatTimestamps = ConcurrentHashMap<String, Long>()
    private val seenNotificationIds = ConcurrentHashMap.newKeySet<String>()
    private val startTime = System.currentTimeMillis()

    /**
     * Start (or re-start) the observer for [userId]. Safe to call multiple times —
     * a duplicate call for the same user is a no-op; switching users resets
     * per-user baselines so we don't notify against the previous account's state.
     */
    @Synchronized
    fun start(userId: String) {
        if (userId.isBlank()) return
        if (currentUserId == userId && job?.isActive == true) return

        if (currentUserId != userId) {
            lastChatTimestamps.clear()
            seenNotificationIds.clear()
        }
        currentUserId = userId
        job?.cancel()
        job = scope.launch {
            launch { observeChatrooms(userId) }
            launch { observeNotifications(userId) }
        }
    }

    @Synchronized
    fun stop() {
        job?.cancel()
        job = null
        currentUserId = null
        lastChatTimestamps.clear()
        seenNotificationIds.clear()
    }

    private suspend fun observeChatrooms(currentUserId: String) {
        chatRepository.getUserChatrooms(currentUserId).collect { rooms ->
            rooms.forEach { room -> maybeNotifyForChatroom(currentUserId, room) }
        }
    }

    private fun maybeNotifyForChatroom(currentUserId: String, room: Chatroom) {
        if (room.id.isBlank()) return
        val ts = room.lastMessageTimestamp
        val previous = lastChatTimestamps.put(room.id, ts) ?: -1L

        // First sighting: seed baseline only. Historical messages must never replay.
        if (previous == -1L) return
        if (ts <= previous) return
        if (ts < startTime) return
        if (room.lastMessage.isBlank()) return
        // The user's own send updates the chatroom summary too; skip our own writes.
        if (room.lastMessageSenderId == currentUserId) return
        // The user is staring at this chatroom — the message is already on screen.
        if (activeChatroomTracker.isActive(room.id)) return

        val sender = room.lastMessageSenderName.ifBlank { "New message" }
        displayer.display(
            SystemNotificationDisplayer.Payload(
                title = room.tripTitle.ifBlank { sender },
                body = "$sender: ${room.lastMessage}",
                type = NotificationChannels.TYPE_CHAT,
                tripId = room.id,
                chatroomId = room.id,
                tag = "chat-${room.id}"
            )
        )
    }

    private suspend fun observeNotifications(currentUserId: String) {
        notificationRepository.getNotificationsForUser(currentUserId).collect { items ->
            items.forEach { maybeNotifyForNotification(it) }
        }
    }

    private fun maybeNotifyForNotification(notification: Notification) {
        if (notification.id.isBlank()) return
        if (!seenNotificationIds.add(notification.id)) return
        if (notification.timestamp < startTime) return
        if (notification.isRead) return

        when (notification.type) {
            NotificationChannels.TYPE_REQUEST_APPROVED -> displayer.display(
                SystemNotificationDisplayer.Payload(
                    title = "Request approved",
                    body = "${notification.senderName} approved your request for ${notification.tripTitle}.",
                    type = NotificationChannels.TYPE_REQUEST_APPROVED,
                    tripId = notification.tripId,
                    chatroomId = notification.chatroomId,
                    tag = "notif-${notification.id}"
                )
            )
            NotificationChannels.TYPE_TRIP_UPDATE -> displayer.display(
                SystemNotificationDisplayer.Payload(
                    title = notification.tripTitle.ifBlank { "Trip update" },
                    body = "${notification.senderName} posted an update.",
                    type = NotificationChannels.TYPE_TRIP_UPDATE,
                    tripId = notification.tripId,
                    chatroomId = notification.chatroomId,
                    tag = "notif-${notification.id}"
                )
            )
        }
    }
}
