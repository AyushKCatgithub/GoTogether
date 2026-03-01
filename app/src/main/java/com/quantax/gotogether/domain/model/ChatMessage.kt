package com.quantax.gotogether.domain.model

data class ChatMessage(
    val id: String = "",
    val chatroomId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderImageUrl: String? = null,
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
