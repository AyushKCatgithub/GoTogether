package com.quantax.gotogether.presentation.notifications

import com.quantax.gotogether.domain.model.JoinRequest
import com.quantax.gotogether.domain.model.Notification

sealed interface NotificationItem {
    val timestamp: Long
    val id: String

    data class JoinRequestItem(val request: JoinRequest) : NotificationItem {
        override val timestamp: Long = request.timestamp
        override val id: String = request.id
    }
    data class GeneralNotificationItem(val notification: Notification) : NotificationItem {
        override val timestamp: Long = notification.timestamp
        override val id: String = notification.id
    }
}