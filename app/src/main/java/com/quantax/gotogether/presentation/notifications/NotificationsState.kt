package com.quantax.gotogether.presentation.notifications

data class NotificationsState(
    val allNotifications: List<NotificationItem> = emptyList(),
    val unreadCount: Int = 0,
    val isLoading: Boolean = true,
    val actionMessage: String? = null
)