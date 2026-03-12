package com.quantax.gotogether.presentation.notifications

import com.quantax.gotogether.domain.model.JoinRequest

sealed class NotificationsEvent {
    object LoadNotifications : NotificationsEvent()
    data class ApproveRequest(val request: JoinRequest) : NotificationsEvent()
    data class DenyRequest(val request: JoinRequest) : NotificationsEvent()
    data class MarkAsRead(val notificationId: String) : NotificationsEvent()
    object MarkAllAsRead : NotificationsEvent()
    object ClearActionMessage : NotificationsEvent()
}