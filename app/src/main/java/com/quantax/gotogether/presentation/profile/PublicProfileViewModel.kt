package com.quantax.gotogether.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quantax.gotogether.data.repository.UserProfileRepository
import com.quantax.gotogether.domain.model.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PublicProfileState(
    val isLoading: Boolean = false,
    val profile: UserProfile? = null,
    val error: String? = null
)

@HiltViewModel
class PublicProfileViewModel @Inject constructor(
    private val repository: UserProfileRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PublicProfileState())
    val state: StateFlow<PublicProfileState> = _state.asStateFlow()

    fun loadProfile(userId: String) {
        if (userId.isBlank()) {
            _state.value = _state.value.copy(error = "Invalid user")
            return
        }

        _state.value = _state.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                val result = repository.getProfile(userId)
                if (result.isSuccess) {
                    val profile = result.getOrNull()
                    if (profile != null) {
                        _state.value = _state.value.copy(isLoading = false, profile = profile)
                    } else {
                        _state.value = _state.value.copy(isLoading = false, error = "User not found")
                    }
                } else {
                    val errorMsg = result.exceptionOrNull()?.message ?: ""
                    if (errorMsg.contains("PERMISSION_DENIED", ignoreCase = true)) {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = "This user's profile is private"
                        )
                    } else {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = result.exceptionOrNull()?.localizedMessage ?: "Failed to load profile"
                        )
                    }
                }
            } catch (e: Exception) {
                val errorMsg = e.message ?: ""
                if (errorMsg.contains("PERMISSION_DENIED", ignoreCase = true)) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "This user's profile is private"
                    )
                } else {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = e.localizedMessage ?: "Failed to load profile"
                    )
                }
            }
        }
    }
}
