package com.quantax.gotogether.presentation.messages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.quantax.gotogether.data.repository.ChatRepository
import com.quantax.gotogether.domain.model.Chatroom
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MessagesViewModel : ViewModel() {
    private val chatRepository = ChatRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _chatrooms = MutableStateFlow<List<Chatroom>>(emptyList())
    val chatrooms: StateFlow<List<Chatroom>> = _chatrooms.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadChatrooms()
    }

    private fun loadChatrooms() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            chatRepository.getUserChatrooms(uid).collect { rooms ->
                _chatrooms.value = rooms
                _isLoading.value = false
            }
        }
    }
}
