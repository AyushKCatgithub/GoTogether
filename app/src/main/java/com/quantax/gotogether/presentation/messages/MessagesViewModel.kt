package com.quantax.gotogether.presentation.messages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quantax.gotogether.auth.AuthManager
import com.quantax.gotogether.domain.usecase.chat.GetChatroomsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class MessagesViewModel @Inject constructor(
    private val getChatroomsUseCase: GetChatroomsUseCase,
    private val authManager: AuthManager
) : ViewModel() {

    private val _state = MutableStateFlow(MessagesState())
    val state: StateFlow<MessagesState> = _state.asStateFlow()

    init {
        onEvent(MessagesEvent.LoadChatrooms)
    }

    fun onEvent(event: MessagesEvent) {
        when (event) {
            is MessagesEvent.LoadChatrooms -> loadChatrooms()
        }
    }

    private fun loadChatrooms() {
        val uid = authManager.currentUser?.uid
        if (uid == null) {
            _state.update { it.copy(isLoading = false, error = "User not logged in") }
            return
        }
        
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                getChatroomsUseCase(uid)
                    .catch { e -> 
                        _state.update { it.copy(isLoading = false, error = e.localizedMessage) }
                    }
                    .collect { rooms ->
                        _state.update { it.copy(chatrooms = rooms, isLoading = false) }
                    }
            } catch (e: Exception) {
                 _state.update { it.copy(isLoading = false, error = e.localizedMessage) }
            }
        }
    }
}
