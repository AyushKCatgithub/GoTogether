package com.quantax.gotogether.presentation.mytrips

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quantax.gotogether.auth.AuthManager
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
}

