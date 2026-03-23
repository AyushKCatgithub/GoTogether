package com.quantax.gotogether.presentation.messages

import com.quantax.gotogether.domain.model.Chatroom

data class MessagesState(
    val chatrooms: List<Chatroom> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)