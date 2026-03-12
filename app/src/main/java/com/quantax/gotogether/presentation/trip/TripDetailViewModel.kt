package com.quantax.gotogether.presentation.trip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.quantax.gotogether.data.repository.JoinRequestRepository
import com.quantax.gotogether.data.repository.TripRepository
import com.quantax.gotogether.data.repository.UserProfileRepository
import com.quantax.gotogether.domain.model.JoinRequest
import com.quantax.gotogether.domain.model.Trip
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TripDetailViewModel @Inject constructor(
    private val tripRepository: TripRepository,
    private val joinRequestRepository: JoinRequestRepository,
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    private val _trip = MutableStateFlow<Trip?>(null)
    val trip: StateFlow<Trip?> = _trip.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isHost = MutableStateFlow(false)
    val isHost: StateFlow<Boolean> = _isHost.asStateFlow()

    // Request status: "NONE", "PENDING", "APPROVED", "DENIED"
    private val _requestStatus = MutableStateFlow("NONE")
    val requestStatus: StateFlow<String> = _requestStatus.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    fun loadTrip(tripId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            tripRepository.getTripById(tripId).onSuccess { loadedTrip ->
                _trip.value = loadedTrip
                if (loadedTrip != null) {
                    val uid = auth.currentUser?.uid ?: ""
                    _isHost.value = loadedTrip.hostId == uid

                    if (!_isHost.value) {
                        if (loadedTrip.members.contains(uid)) {
                            _requestStatus.value = JoinRequest.STATUS_APPROVED
                        } else {
                            val status = joinRequestRepository.getUserRequestStatus(tripId, uid)
                            _requestStatus.value = status ?: "NONE"
                        }
                    }
                }
            }
            _isLoading.value = false
        }
    }

    fun requestToJoin() {
        val currentTrip = _trip.value ?: return
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            _isLoading.value = true
            userProfileRepository.getProfile(uid).onSuccess { profile ->
                val request = JoinRequest(
                    tripId = currentTrip.id,
                    tripTitle = currentTrip.title,
                    requesterId = uid,
                    requesterName = profile?.name ?: (auth.currentUser?.displayName ?: "User"),
                    requesterImageUrl = profile?.profileImageUrl,
                    hostId = currentTrip.hostId
                )
                
                joinRequestRepository.createRequest(request).onSuccess {
                    _requestStatus.value = JoinRequest.STATUS_PENDING
                    _actionMessage.value = "Join request sent to host!"
                }.onFailure {
                    _actionMessage.value = "Failed to send join request."
                }
            }
            _isLoading.value = false
        }
    }

    fun clearActionMessage() {
        _actionMessage.value = null
    }
}
