package com.quantax.gotogether.domain.model

data class Chatroom(
    val id: String = "",
    val tripTitle: String = "",
    val tripImageUrl: String? = null,
    val hostId: String = "",
    val memberIds: List<String> = emptyList(),
    val lastMessage: String = "",
    val lastMessageTimestamp: Long = 0L,
    val lastMessageSenderName: String = "",
    val lastMessageSenderId: String = ""
)
