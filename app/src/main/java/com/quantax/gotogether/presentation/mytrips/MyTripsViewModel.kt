package com.quantax.gotogether.presentation.mytrips

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quantax.gotogether.auth.AuthManager
import com.quantax.gotogether.data.repository.BookmarkRepository
import com.quantax.gotogether.data.repository.TripRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class MyTripsViewModel @Inject constructor(
    private val repository: TripRepository,
    private val bookmarkRepository: BookmarkRepository,
    private val authManager: AuthManager
) : ViewModel() {

    private val _state = MutableStateFlow(MyTripsState())
    val state: StateFlow<MyTripsState> = _state.asStateFlow()

    fun loadMyTrips() {
        val user = authManager.currentUser
        if (user == null) {
            _state.update { it.copy(error = "User not logged in", isLoading = false) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.getUserTrips(user.uid).onSuccess { trips ->
                _state.update { it.copy(isLoading = false, trips = trips) }
            }.onFailure { e ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.localizedMessage ?: "Failed to load your trips"
                    )
                }
            }
        }
    }

    fun loadSavedTrips() {
        val user = authManager.currentUser
        if (user == null) {
            _state.update { it.copy(error = "User not logged in", isSavedLoading = false) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSavedLoading = true, error = null) }
            bookmarkRepository.getBookmarkedTrips(user.uid).onSuccess { trips ->
                _state.update { it.copy(isSavedLoading = false, savedTrips = trips) }
            }.onFailure { e ->
                _state.update {
                    it.copy(
                        isSavedLoading = false,
                        error = e.localizedMessage ?: "Failed to load saved trips"
                    )
                }
            }
        }
    }

    fun selectTab(tab: MyTripsTab) {
        _state.update { it.copy(selectedTab = tab) }
        // Load the relevant data if not yet loaded
        when (tab) {
            MyTripsTab.MY_POSTS -> {
                if (_state.value.trips.isEmpty() && !_state.value.isLoading) {
                    loadMyTrips()
                }
            }
            MyTripsTab.SAVED_POSTS -> {
                // Always refresh saved trips when switching to tab
                loadSavedTrips()
            }
        }
    }

    fun deleteTrip(tripId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            repository.deleteTrip(tripId).onSuccess {
                _state.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        trips = currentState.trips.filter { it.id != tripId }
                    )
                }
            }.onFailure { e ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.localizedMessage ?: "Failed to delete trip"
                    )
                }
            }
        }
    }

    fun removeBookmark(tripId: String) {
        val user = authManager.currentUser ?: return
        viewModelScope.launch {
            // Optimistically remove from UI
            _state.update { currentState ->
                currentState.copy(
                    savedTrips = currentState.savedTrips.filter { it.id != tripId }
                )
            }
            // Remove from Firestore
            val trip = _state.value.savedTrips.find { it.id == tripId }
            bookmarkRepository.toggleBookmark(
                userId = user.uid,
                tripId = tripId,
                postTitle = trip?.title ?: "",
                authorId = trip?.hostId ?: "",
                thumbnailUrl = trip?.imageUrl
            )
        }
    }
}
