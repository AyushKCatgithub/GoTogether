package com.quantax.gotogether.domain.model

data class Notification(
    val id: String = "",
    val recipientId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderImageUrl: String? = null,
    val type: String = TYPE_REQUEST_APPROVED,
    val tripId: String = "",
    val tripTitle: String = "",
    val chatroomId: String = "",
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
) {
    companion object {
        const val TYPE_REQUEST_APPROVED = "REQUEST_APPROVED"
        const val TYPE_TRIP_UPDATE = "TRIP_UPDATE" // Future extensibility
    }
}
