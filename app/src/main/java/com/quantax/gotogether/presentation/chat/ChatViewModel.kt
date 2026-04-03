package com.quantax.gotogether.presentation.chat

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quantax.gotogether.auth.AuthManager
import com.quantax.gotogether.data.repository.ChatRepository
import com.quantax.gotogether.data.repository.StorageService
import com.quantax.gotogether.data.repository.UserProfileRepository
import com.quantax.gotogether.domain.model.ChatMessage
import com.quantax.gotogether.domain.model.Chatroom
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
data class ChatUIState(
    val messages: List<ChatMessage> = emptyList(),
    val chatroom: Chatroom? = null,
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val userProfileRepository: UserProfileRepository,
    private val storageService: StorageService,
    private val authManager: AuthManager
) : ViewModel() {

    private val _state = MutableStateFlow(ChatUIState())
    val state: StateFlow<ChatUIState> = _state.asStateFlow()

    private var currentUserName: String = ""
    private var currentUserImageUrl: String? = null
    private var chatJob: Job? = null

    fun loadChatroom(chatroomId: String) {
        if (chatJob?.isActive == true) return
        
        chatJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            // Load current user profile if not already loaded
            if (currentUserName.isEmpty()) {
                val uid = authManager.currentUser?.uid ?: return@launch
                userProfileRepository.getProfile(uid).onSuccess { profile ->
                    currentUserName = profile?.name ?: (authManager.currentUser?.displayName ?: "User")
                    currentUserImageUrl = profile?.profileImageUrl
                }
            }

            // Load chatroom info
            chatRepository.getChatroom(chatroomId).onSuccess { room ->
                _state.update { it.copy(chatroom = room) }
            }.onFailure { e ->
                _state.update { it.copy(error = "Failed to load chat: ${e.message}") }
            }

            // Start listening to messages
            chatRepository.getMessages(chatroomId)
                .catch { e ->
                    _state.update { it.copy(isLoading = false, error = e.message) }
                }
                .collect { messageList ->
                    // Filter out messages soft-deleted for the current user
                    val uid = authManager.currentUser?.uid
                    val filtered = if (uid != null) {
                        messageList.filter { !it.deletedFor.contains(uid) }
                    } else {
                        messageList
                    }
                    _state.update { it.copy(messages = filtered, isLoading = false) }
                }
        }
    }

    fun sendMessage(chatroomId: String, text: String) {
        val trimmedText = text.trim()
        if (trimmedText.isBlank()) return
        val uid = authManager.currentUser?.uid ?: return

        viewModelScope.launch {
            _state.update { it.copy(isSending = true) }

            val message = ChatMessage(
                senderId = uid,
                senderName = currentUserName,
                senderImageUrl = currentUserImageUrl,
                text = trimmedText,
                timestamp = System.currentTimeMillis()
            )

            val result = chatRepository.sendMessage(chatroomId, message)

            _state.update { it.copy(isSending = false) }

            if (result.isFailure) {
                _state.update { it.copy(error = "Failed to send message: ${result.exceptionOrNull()?.message}") }
            }
        }
    }

    fun sendImageMessage(chatroomId: String, imageUri: Uri) {
        val uid = authManager.currentUser?.uid ?: return

        viewModelScope.launch {
            _state.update { it.copy(isSending = true, error = null) }

            val upload = storageService.uploadImage(imageUri, "chat_attachments")
            if (upload.isFailure) {
                _state.update {
                    it.copy(
                        isSending = false,
                        error = upload.exceptionOrNull()?.message ?: "Failed to upload image"
                    )
                }
                return@launch
            }

            val url = upload.getOrNull() ?: run {
                _state.update { it.copy(isSending = false, error = "Upload failed") }
                return@launch
            }

            val message = ChatMessage(
                senderId = uid,
                senderName = currentUserName,
                senderImageUrl = currentUserImageUrl,
                text = "",
                imageUrl = url,
                timestamp = System.currentTimeMillis()
            )

            val result = chatRepository.sendMessage(chatroomId, message)
            _state.update { it.copy(isSending = false) }

            if (result.isFailure) {
                _state.update { it.copy(error = "Failed to send photo: ${result.exceptionOrNull()?.message}") }
            }
        }
    }

    fun clearErrorMessage() {
        _state.update { it.copy(error = null) }
    }

    fun getCurrentUserId(): String = authManager.currentUser?.uid ?: ""

    fun deleteMessageForMe(chatroomId: String, messageId: String) {
        val uid = authManager.currentUser?.uid ?: return
        viewModelScope.launch {
            chatRepository.deleteMessageForMe(chatroomId, messageId, uid).onFailure { e ->
                _state.update { it.copy(error = "Failed to delete: ${e.message}") }
            }
        }
    }

    fun deleteMessageForAll(chatroomId: String, messageId: String) {
        viewModelScope.launch {
            chatRepository.deleteMessageForAll(chatroomId, messageId).onFailure { e ->
                _state.update { it.copy(error = "Failed to delete: ${e.message}") }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        chatJob?.cancel()
    }
}

