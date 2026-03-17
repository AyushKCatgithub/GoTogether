package com.quantax.gotogether.presentation.trip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quantax.gotogether.auth.AuthManager
import com.quantax.gotogether.data.repository.JoinRequestRepository
import com.quantax.gotogether.data.repository.TripRepository
import com.quantax.gotogether.data.repository.UserProfileRepository
import com.quantax.gotogether.domain.model.JoinRequest
import com.quantax.gotogether.domain.model.Trip
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class TripDetailState {
    object Loading : TripDetailState()
    data class Success(
        val trip: Trip,
        val isHost: Boolean,
        val requestStatus: String,
        val membersList: List<com.quantax.gotogether.domain.model.UserProfile> = emptyList()
    ) : TripDetailState()
    data class Error(val message: String) : TripDetailState()
}

@HiltViewModel
class TripDetailViewModel @Inject constructor(
    private val tripRepository: TripRepository,
    private val joinRequestRepository: JoinRequestRepository,
    private val userProfileRepository: UserProfileRepository,
    private val authManager: AuthManager
) : ViewModel() {

    private val _state = MutableStateFlow<TripDetailState>(TripDetailState.Loading)
    val state: StateFlow<TripDetailState> = _state.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    fun loadTrip(tripId: String) {
        viewModelScope.launch {
            _state.value = TripDetailState.Loading
            tripRepository.getTripById(tripId).onSuccess { loadedTrip ->
                if (loadedTrip != null) {
                    val uid = authManager.currentUser?.uid ?: ""
                    val isHost = loadedTrip.hostId == uid
                    
                    var requestStatus = "NONE"
                    if (!isHost) {
                        if (loadedTrip.members.contains(uid)) {
                            requestStatus = JoinRequest.STATUS_APPROVED
                        } else {
                            val status = joinRequestRepository.getUserRequestStatus(tripId, uid)
                            requestStatus = status ?: "NONE"
                        }
                    }

                    // Fetch member profiles in batch
                    var memberProfiles = emptyList<com.quantax.gotogether.domain.model.UserProfile>()
                    if (loadedTrip.members.isNotEmpty()) {
                        userProfileRepository.getProfiles(loadedTrip.members).onSuccess { profiles ->
                            memberProfiles = profiles
                        }
                    }

                    _state.value = TripDetailState.Success(
                        trip = loadedTrip,
                        isHost = isHost,
                        requestStatus = requestStatus,
                        membersList = memberProfiles
                    )
                } else {
                    _state.value = TripDetailState.Error("Trip not found")
                }
            }.onFailure {
                _state.value = TripDetailState.Error(it.localizedMessage ?: "Failed to load trip")
            }
        }
    }

    fun requestToJoin() {
        val currentState = _state.value
        if (currentState !is TripDetailState.Success) return
        
        val currentTrip = currentState.trip
        val uid = authManager.currentUser?.uid ?: return

        viewModelScope.launch {
            userProfileRepository.getProfile(uid).onSuccess { profile ->
                val request = JoinRequest(
                    tripId = currentTrip.id,
                    tripTitle = currentTrip.title,
                    requesterId = uid,
                    requesterName = profile?.name ?: (authManager.currentUser?.displayName ?: "User"),
                    requesterImageUrl = profile?.profileImageUrl,
                    hostId = currentTrip.hostId
                )
                
                joinRequestRepository.createRequest(request).onSuccess {
                    _actionMessage.value = "Join request sent to host!"
                    // Update state to pending
                    _state.value = currentState.copy(requestStatus = JoinRequest.STATUS_PENDING)
                }.onFailure {
                    _actionMessage.value = "Failed to send join request."
                }
            }
        }
    }

    fun clearActionMessage() {
        _actionMessage.value = null
    }
}
