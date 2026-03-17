package com.quantax.gotogether.presentation.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quantax.gotogether.auth.AuthManager
import com.quantax.gotogether.data.repository.StorageService
import com.quantax.gotogether.data.repository.UserProfileRepository
import com.quantax.gotogether.domain.model.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileState(
    val isLoading: Boolean = false,
    val profile: UserProfile? = null,
    val error: String? = null,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false
)

sealed class ProfileEvent {
    object LoadProfile : ProfileEvent()
    data class SaveProfile(val profile: UserProfile) : ProfileEvent()
    object ResetSaveSuccess : ProfileEvent()
    object SignOut : ProfileEvent()
    object ClearError : ProfileEvent()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: UserProfileRepository,
    private val storageService: StorageService,
    private val authManager: AuthManager
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        val user = authManager.currentUser
        if (user != null) {
            _state.value = _state.value.copy(isLoading = true)
            repository.getProfileFlow(user.uid)
                .onEach { profile ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        profile = profile ?: UserProfile(uid = user.uid, email = user.email ?: ""),
                        error = null
                    )
                    // Self-backfill usernameLower when missing so uniqueness checks work
                    if (profile != null && profile.username.isNotBlank() && profile.usernameLower.isBlank()) {
                        viewModelScope.launch {
                            repository.updateUsernameLowerIfNeeded(profile.uid, profile.username)
                        }
                    }
                }
                .catch { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = e.localizedMessage ?: "Failed to load profile"
                    )
                }
                .launchIn(viewModelScope)
        } else {
            _state.value = _state.value.copy(error = "User not logged in", isLoading = false)
        }
    }

    fun onEvent(event: ProfileEvent) {
        when (event) {
            is ProfileEvent.LoadProfile -> loadProfile()
            is ProfileEvent.SaveProfile -> saveProfile(event.profile)
            is ProfileEvent.ResetSaveSuccess -> resetSaveSuccess()
            is ProfileEvent.SignOut -> signOut()
            is ProfileEvent.ClearError -> _state.value = _state.value.copy(error = null)
        }
    }

    private fun loadProfile() {
        val user = authManager.currentUser
        if (user == null) {
            _state.value = _state.value.copy(error = "User not logged in", isLoading = false)
            return
        }

        _state.value = _state.value.copy(isLoading = true, error = null)
        
        viewModelScope.launch {
            try {
                val profileResult = repository.getProfile(user.uid)
                if (profileResult.isSuccess) {
                    val profile = profileResult.getOrNull()
                    if (profile != null) {
                        _state.value = _state.value.copy(isLoading = false, profile = profile)
                    } else {
                        // Create an empty profile for new users
                        val newProfile = UserProfile(uid = user.uid, email = user.email ?: "")
                        _state.value = _state.value.copy(isLoading = false, profile = newProfile)
                    }
                } else {
                    _state.value = _state.value.copy(isLoading = false, error = profileResult.exceptionOrNull()?.localizedMessage ?: "Failed to load profile")
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.localizedMessage ?: "Failed to load profile")
            }
        }
    }

    private fun saveProfile(profile: UserProfile) {
        val user = authManager.currentUser
        if (user == null) {
            _state.value = _state.value.copy(error = "User not logged in", isSaving = false)
            return
        }

        _state.value = _state.value.copy(isSaving = true, saveSuccess = false, error = null)

        viewModelScope.launch {
            try {
                var updatedProfile = profile.copy(uid = user.uid)

                // Upload profile image if it's a local content URI
                if (storageService.isLocalUri(profile.profileImageUrl)) {
                    val uri = Uri.parse(profile.profileImageUrl)
                    val publicUrl = storageService.uploadImage(uri, "profile_images")
                    if (publicUrl != null) {
                        updatedProfile = updatedProfile.copy(profileImageUrl = publicUrl)
                    } else {
                        _state.value = _state.value.copy(
                            isSaving = false,
                            error = "Failed to upload profile photo. Please try again."
                        )
                        return@launch
                    }
                }

                // Upload any local verification/background photo URIs and collect public URLs
                val verificationUrls = profile.verificationPhotoUrls.map { url ->
                    if (storageService.isLocalUri(url)) {
                        val uri = Uri.parse(url)
                        storageService.uploadImage(uri, "verification_photos") ?: url
                    } else {
                        url
                    }
                }
                updatedProfile = updatedProfile.copy(verificationPhotoUrls = verificationUrls)

                val oldUsername = _state.value.profile?.username
                val saveResult = repository.saveProfile(updatedProfile, oldUsername)
                saveResult.fold(
                    onSuccess = {
                        _state.value = _state.value.copy(
                            isSaving = false,
                            saveSuccess = true,
                            profile = updatedProfile
                        )
                    },
                    onFailure = { e ->
                        _state.value = _state.value.copy(
                            isSaving = false,
                            error = e.message ?: e.localizedMessage ?: "Failed to save profile"
                        )
                    }
                )
            } catch (e: Exception) {
                e.printStackTrace()
                _state.value = _state.value.copy(isSaving = false, error = e.message ?: e.localizedMessage ?: "Failed to save profile")
            }
        }
    }

    private fun resetSaveSuccess() {
        _state.value = _state.value.copy(saveSuccess = false)
    }

    private fun signOut() {
        authManager.signOut()
    }
}
