package com.quantax.gotogether.domain.model

import com.google.firebase.firestore.PropertyName
import com.quantax.gotogether.data.fcm.NotificationChannels

data class Notification(
    val id: String = "",
    val recipientId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderImageUrl: String? = null,
    val type: String = NotificationChannels.TYPE_REQUEST_APPROVED,
    val tripId: String = "",
    val tripTitle: String = "",
    val chatroomId: String = "",
    @get:PropertyName("isRead") @set:PropertyName("isRead") var isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
