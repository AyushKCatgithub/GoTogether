package com.quantax.gotogether.domain.model

data class ChatMessage(
    val id: String = "",
    val chatroomId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderImageUrl: String? = null,
    val text: String = "",
    /** Optional image attachment (Firebase Storage URL). */
    val imageUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    /** User IDs for whom this message is soft-deleted ("delete for me"). */
    val deletedFor: List<String> = emptyList()
)
