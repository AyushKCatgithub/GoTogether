package com.quantax.gotogether.presentation.mytrips

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quantax.gotogether.auth.AuthManager
import com.quantax.gotogether.data.repository.TripRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
            _state.value = _state.value.copy(error = "User not logged in")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val result = repository.getUserTrips(user.uid)
            
            if (result.isSuccess) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    trips = result.getOrDefault(emptyList())
                )
            } else {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to load your trips"
                )
            }
        }
    }

    fun deleteTrip(tripId: String) {
        viewModelScope.launch {
            val result = repository.deleteTrip(tripId)
            if (result.isSuccess) {
                // Remove from local state
                val updatedTrips = _state.value.trips.filter { it.id != tripId }
                _state.value = _state.value.copy(trips = updatedTrips)
            } else {
                _state.value = _state.value.copy(
                    error = result.exceptionOrNull()?.message ?: "Failed to delete trip"
                )
            }
        }
    }
}
