package com.quantax.gotogether.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.quantax.gotogether.data.repository.ChatRepository
import com.quantax.gotogether.data.repository.UserProfileRepository
import com.quantax.gotogether.domain.model.ChatMessage
import com.quantax.gotogether.domain.model.Chatroom
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {
    private val chatRepository = ChatRepository()
    private val userProfileRepository = UserProfileRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _chatroom = MutableStateFlow<Chatroom?>(null)
    val chatroom: StateFlow<Chatroom?> = _chatroom.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var currentUserName: String = ""
    private var currentUserImageUrl: String? = null

    fun loadChatroom(chatroomId: String) {
        viewModelScope.launch {
            _isLoading.value = true

            // Load current user profile
            val uid = auth.currentUser?.uid ?: return@launch
            userProfileRepository.getProfile(uid).onSuccess { profile ->
                currentUserName = profile?.name ?: (auth.currentUser?.displayName ?: "User")
                currentUserImageUrl = profile?.profileImageUrl
            }

            // Load chatroom info
            chatRepository.getChatroom(chatroomId).onSuccess { room ->
                _chatroom.value = room
            }

            // Start listening to messages
            chatRepository.getMessages(chatroomId).collect { messageList ->
                _messages.value = messageList
                _isLoading.value = false
            }
        }
    }

    fun sendMessage(chatroomId: String, text: String) {
        if (text.isBlank()) return
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            val message = ChatMessage(
                senderId = uid,
                senderName = currentUserName,
                senderImageUrl = currentUserImageUrl,
                text = text.trim(),
                timestamp = System.currentTimeMillis()
            )
            val result = chatRepository.sendMessage(chatroomId, message)
            if (result.isFailure) {
                _errorMessage.value = "Failed to send message: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun getCurrentUserId(): String = auth.currentUser?.uid ?: ""
}
