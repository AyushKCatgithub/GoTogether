package com.quantax.gotogether.presentation.trip

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quantax.gotogether.auth.AuthManager
import com.quantax.gotogether.data.repository.TripRepository
import com.quantax.gotogether.data.repository.UserProfileRepository
import com.quantax.gotogether.domain.model.Trip
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CreateTripState(
    val startingLocation: String = "",
    val destination: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val budgetRange: String = "",
    val groupSize: Int = 1,
    val interests: List<String> = emptyList(),
    val description: String = "",
    val destinationPhotoUrl: String? = null,
    val selectedImageUri: Uri? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

class CreateTripViewModel : ViewModel() {
    private val repository = TripRepository()
    private val userProfileRepository = UserProfileRepository()
    private val authManager = AuthManager()
    private val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    private val _state = MutableStateFlow(CreateTripState())
    val state: StateFlow<CreateTripState> = _state.asStateFlow()

    fun onStartingLocationChange(value: String) { _state.value = _state.value.copy(startingLocation = value) }
    fun onDestinationChange(value: String) { _state.value = _state.value.copy(destination = value) }
    
    fun onStartDateChange(value: String) { 
        _state.value = _state.value.copy(startDate = value) 
    }
    
    fun onEndDateChange(value: String) { 
        _state.value = _state.value.copy(endDate = value)
    }

    fun onBudgetRangeChange(value: String) { _state.value = _state.value.copy(budgetRange = value) }
    fun onGroupSizeChange(value: Int) { _state.value = _state.value.copy(groupSize = value.coerceAtLeast(1)) }
    
    fun addInterest(interest: String) {
        val current = _state.value.interests.toMutableList()
        if (!current.contains(interest) && interest.isNotBlank()) {
            current.add(interest.trim())
            _state.value = _state.value.copy(interests = current)
        }
    }
    
    fun removeInterest(interest: String) {
        val current = _state.value.interests.toMutableList()
        current.remove(interest)
        _state.value = _state.value.copy(interests = current)
    }

    fun onDescriptionChange(value: String) { _state.value = _state.value.copy(description = value) }
    
    fun onImageSelected(uri: Uri?) {
        _state.value = _state.value.copy(selectedImageUri = uri)
    }
    
    fun publishTrip() {
        val currentState = _state.value
        if (currentState.startingLocation.isBlank() || currentState.destination.isBlank()) {
            _state.value = currentState.copy(error = "Location and destination are required")
            return
        }

        val currentUser = authManager.currentUser
        if (currentUser == null) {
            _state.value = currentState.copy(error = "You must be signed in to create a trip")
            return
        }

        viewModelScope.launch {
            _state.value = currentState.copy(isLoading = true, error = null)
            
            // Fetch profile to determine verification status
            val profileResult = userProfileRepository.getProfile(currentUser.uid)
            val profile = profileResult.getOrNull()
            val isVerified = profile?.isVerified ?: false
            val hostName = profile?.name?.ifBlank { null } ?: currentUser.displayName ?: "Traveler"
            val hostImageUrl = profile?.profileImageUrl?.ifBlank { null } ?: currentUser.photoUrl?.toString()
            
            // For MVP, we will use a dummy image if none provided
            val dummyImages = listOf(
                "https://images.unsplash.com/photo-1499856871958-5b9627545d1a?auto=format&fit=crop&q=80&w=2020", // Paris
                "https://images.unsplash.com/photo-1512453979436-5a50cce119fb?auto=format&fit=crop&q=80&w=2070", // Kyoto
                "https://images.unsplash.com/photo-1533319417894-6f53ca9b0fcd?auto=format&fit=crop&q=80&w=2070", // Alps
                "https://images.unsplash.com/photo-1537996194471-e657df975ab4?auto=format&fit=crop&q=80&w=2138", // Bali
                "https://images.unsplash.com/photo-1513635269975-5969336cd1f5?auto=format&fit=crop&q=80&w=2070"  // London
            )
            
            // Handle image upload if a local URI was selected
            var finalImageUrl: String? = null
            if (currentState.selectedImageUri != null) {
                val storageService = com.quantax.gotogether.data.repository.StorageService()
                val uploadedUrl = storageService.uploadImage(currentState.selectedImageUri, "trip_images")
                if (uploadedUrl != null) {
                    finalImageUrl = uploadedUrl
                } else {
                    _state.value = currentState.copy(
                        isLoading = false,
                        error = "Failed to upload trip image. Please try again."
                    )
                    return@launch
                }
            } else {
                finalImageUrl = dummyImages.random()
            }
            
            val trip = Trip(
                hostId = currentUser.uid,
                hostName = hostName,
                hostImageUrl = hostImageUrl,
                hostVerified = isVerified,
                title = "${currentState.destination}: An Exciting Trip",
                startingLocation = currentState.startingLocation,
                destination = currentState.destination,
                startDate = currentState.startDate,
                endDate = currentState.endDate,
                budgetRange = currentState.budgetRange,
                groupSize = currentState.groupSize,
                interests = currentState.interests,
                description = currentState.description,
                imageUrl = finalImageUrl
            )

            val result = repository.createTrip(trip)
            if (result.isSuccess) {
                _state.value = currentState.copy(isLoading = false, isSuccess = true)
            } else {
                _state.value = currentState.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to publish trip"
                )
            }
        }
    }
}

