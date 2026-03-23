package com.quantax.gotogether.presentation.messages

sealed class MessagesEvent {
    object LoadChatrooms : MessagesEvent()
}