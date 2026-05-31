package com.quantax.gotogether.data.fcm

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.quantax.gotogether.MainActivity
import com.quantax.gotogether.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Posts Android system-tray notifications. The single funnel for both
 * remote FCM payloads and in-app foreground events (new chat, approvals).
 */
@Singleton
class SystemNotificationDisplayer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val permissionState: NotificationPermissionState
) {

    data class Payload(
        val title: String,
        val body: String,
        val type: String,
        val tripId: String? = null,
        val chatroomId: String? = null,
        /** Stable tag — passing the same value collapses repeats (e.g. multiple messages in one chat). */
        val tag: String? = null
    )

    fun display(payload: Payload) {
        if (!hasPostPermission()) {
            permissionState.setDenied(true)
            Log.w(TAG, "Dropped notification (POST_NOTIFICATIONS not granted): type=${payload.type}")
            return
        }
        permissionState.setDenied(false)

        val channelId = NotificationChannels.channelFor(payload.type)
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(NotificationChannels.EXTRA_TYPE, payload.type)
            payload.tripId?.let { putExtra(NotificationChannels.EXTRA_TRIP_ID, it) }
            payload.chatroomId?.let { putExtra(NotificationChannels.EXTRA_CHATROOM_ID, it) }
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            (payload.tag ?: payload.type).hashCode(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(payload.title)
            .setContentText(payload.body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(payload.body))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val notifId = (payload.tag ?: "${payload.type}-${System.nanoTime()}").hashCode()
        NotificationManagerCompat.from(context).notify(payload.tag, notifId, notification)
    }

    private fun hasPostPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private companion object {
        const val TAG = "SystemNotificationDisplayer"
    }
}
