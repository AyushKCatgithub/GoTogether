package com.quantax.gotogether.data.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.core.content.getSystemService

/**
 * Centralized notification channel + payload-type definitions. Channels are required
 * on Android 8+ and must exist before any [android.app.Notification] is posted against
 * them. The TYPE_* values double as the persisted `Notification.type` field in
 * Firestore — do not change them lightly.
 */
object NotificationChannels {

    const val CHAT = "channel_chat"
    const val JOIN_REQUESTS = "channel_join_requests"
    const val TRIP_UPDATES = "channel_trip_updates"

    /** Extras keys carried on the [android.app.PendingIntent] that launches MainActivity. */
    const val EXTRA_TYPE = "extra_notification_type"
    const val EXTRA_TRIP_ID = "extra_notification_trip_id"
    const val EXTRA_CHATROOM_ID = "extra_notification_chatroom_id"

    const val TYPE_CHAT = "CHAT"
    const val TYPE_JOIN_REQUEST = "JOIN_REQUEST"
    const val TYPE_REQUEST_APPROVED = "REQUEST_APPROVED"
    const val TYPE_TRIP_UPDATE = "TRIP_UPDATE"

    fun ensureCreated(context: Context) {
        val manager = context.getSystemService<NotificationManager>() ?: return
        manager.createNotificationChannels(
            listOf(
                NotificationChannel(
                    CHAT,
                    "Messages",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply { description = "New chat messages from your trip groups." },
                NotificationChannel(
                    JOIN_REQUESTS,
                    "Join Requests",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply { description = "When a traveler asks to join your trip." },
                NotificationChannel(
                    TRIP_UPDATES,
                    "Trip Updates",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply { description = "Approvals and updates for trips you joined." }
            )
        )
    }

    fun channelFor(type: String?): String = when (type) {
        TYPE_CHAT -> CHAT
        TYPE_JOIN_REQUEST -> JOIN_REQUESTS
        TYPE_REQUEST_APPROVED, TYPE_TRIP_UPDATE -> TRIP_UPDATES
        else -> TRIP_UPDATES
    }

    /**
     * Launches the system settings page for this app so the user can re-enable
     * POST_NOTIFICATIONS after a permanent denial.
     */
    fun openAppNotificationSettings(context: Context) {
        val intent = Intent().apply {
            action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        runCatching { context.startActivity(intent) }.onFailure {
            val fallback = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            runCatching { context.startActivity(fallback) }
        }
    }
}
