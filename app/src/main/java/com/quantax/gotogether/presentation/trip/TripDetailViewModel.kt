package com.quantax.gotogether.presentation.trip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quantax.gotogether.auth.AuthManager
import com.quantax.gotogether.data.repository.BookmarkRepository
import com.quantax.gotogether.data.repository.JoinRequestRepository
import com.quantax.gotogether.data.repository.TripRepository
import com.quantax.gotogether.data.repository.UserProfileRepository
import com.quantax.gotogether.domain.model.JoinRequest
import com.quantax.gotogether.domain.model.Trip
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class TripDetailState {
    object Loading : TripDetailState()
    data class Success(
        val trip: Trip,
        val isHost: Boolean,
        val requestStatus: String,
        val isBookmarked: Boolean = false,
        val membersList: List<com.quantax.gotogether.domain.model.UserProfile> = emptyList()
    ) : TripDetailState()

    data class Error(val message: String) : TripDetailState()
}

@HiltViewModel
class TripDetailViewModel @Inject constructor(
    private val tripRepository: TripRepository,
    private val joinRequestRepository: JoinRequestRepository,
    private val userProfileRepository: UserProfileRepository,
    private val bookmarkRepository: BookmarkRepository,
    private val authManager: AuthManager
) : ViewModel() {

    private val _state = MutableStateFlow<TripDetailState>(TripDetailState.Loading)
    val state: StateFlow<TripDetailState> = _state.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    private var tripLoadJob: Job? = null
    private var currentProfilesJob: kotlinx.coroutines.Job? = null

    fun loadTrip(tripId: String) {
        tripLoadJob?.cancel()
        currentProfilesJob?.cancel()
        _state.value = TripDetailState.Loading
        val uid = authManager.currentUser?.uid ?: ""

        val tripFlow = tripRepository.getTripByIdFlow(tripId).distinctUntilChanged()
        val statusFlow = joinRequestRepository.getUserRequestStatusFlow(tripId, uid).distinctUntilChanged()
        val bookmarkFlow = if (uid.isNotBlank()) {
            bookmarkRepository.observeBookmarkedIds(uid)
        } else {
            flowOf(emptySet())
        }.distinctUntilChanged()

        tripLoadJob = combine(tripFlow, statusFlow, bookmarkFlow) { loadedTrip, status, bookmarks ->
            if (loadedTrip != null) {
                val isHost = loadedTrip.hostId == uid
                val requestStatus = if (isHost) "NONE"
                else if (loadedTrip.members.contains(uid)) JoinRequest.STATUS_APPROVED
                else status ?: "NONE"
                
                val isBookmarked = bookmarks.contains(loadedTrip.id)

                // Fetch profiles asynchronously while maintaining state
                currentProfilesJob?.cancel()
                currentProfilesJob = viewModelScope.launch {
                    val profiles = userProfileRepository.getProfiles(loadedTrip.members).getOrDefault(emptyList())
                    _state.update { 
                        TripDetailState.Success(
                            trip = loadedTrip,
                            isHost = isHost,
                            requestStatus = requestStatus,
                            isBookmarked = isBookmarked,
                            membersList = profiles
                        )
                    }
                }
            } else {
                _state.value = TripDetailState.Error("Trip not found")
            }
        }.catch { e ->
            _state.value = TripDetailState.Error(e.localizedMessage ?: "Critical flow error")
        }.launchIn(viewModelScope)
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
                    requesterName = profile?.name ?: (authManager.currentUser?.displayName
                        ?: "User"),
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

    fun toggleBookmark() {
        val currentState = _state.value
        if (currentState !is TripDetailState.Success) return
        val trip = currentState.trip
        val uid = authManager.currentUser?.uid ?: return

        viewModelScope.launch {
            bookmarkRepository.toggleBookmark(
                userId = uid,
                tripId = trip.id,
                postTitle = trip.title,
                authorId = trip.hostId,
                thumbnailUrl = trip.imageUrl
            )
        }
    }

    fun clearActionMessage() {
        _actionMessage.value = null
    }
}
