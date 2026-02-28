package com.quantax.gotogether.presentation.profile

import com.quantax.gotogether.domain.model.*

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.quantax.gotogether.data.repository.StorageService
import com.quantax.gotogether.data.repository.UserProfileRepository
import com.quantax.gotogether.domain.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileState(
    val isLoading: Boolean = false,
    val profile: UserProfile? = null,
    val error: String? = null,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false
)

class ProfileViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val repository = UserProfileRepository()
    private val storageService = StorageService()

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        val user = auth.currentUser
        if (user == null) {
            _state.value = _state.value.copy(error = "User not logged in")
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

    fun saveProfile(profile: UserProfile) {
        val user = auth.currentUser
        if (user == null) {
            _state.value = _state.value.copy(error = "User not logged in")
            return
        }

        _state.value = _state.value.copy(isSaving = true, saveSuccess = false, error = null)

        viewModelScope.launch {
            try {
                var updatedProfile = profile.copy(uid = user.uid)
                
                // If the profile image is a new local content URI, upload it to Storage first
                if (storageService.isLocalUri(profile.profileImageUrl)) {
                    val uri = Uri.parse(profile.profileImageUrl)
                    val publicUrl = storageService.uploadImage(uri, "profile_images")
                    
                    if (publicUrl != null) {
                        updatedProfile = updatedProfile.copy(profileImageUrl = publicUrl)
                    } else {
                        // If upload fails, fallback to keeping the original (or throwing an error)
                        _state.value = _state.value.copy(
                            isSaving = false, 
                            error = "Failed to upload profile photo. Please try again."
                        )
                        return@launch
                    }
                }

                repository.saveProfile(updatedProfile)
                _state.value = _state.value.copy(
                    isSaving = false, 
                    saveSuccess = true, 
                    profile = updatedProfile
                )
            } catch (e: Exception) {
                e.printStackTrace()
                _state.value = _state.value.copy(isSaving = false, error = e.localizedMessage ?: "Failed to save profile")
            }
        }
    }

    fun resetSaveSuccess() {
        _state.value = _state.value.copy(saveSuccess = false)
    }

    fun signOut() {
        auth.signOut()
    }
}
