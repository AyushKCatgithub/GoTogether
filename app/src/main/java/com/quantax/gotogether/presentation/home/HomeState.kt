package com.quantax.gotogether.presentation.home

import com.quantax.gotogether.domain.model.Trip

sealed class HomeState {
    object Loading : HomeState()
    data class Success(val trips: List<Trip>, val locationText: String = "Current Location, Destination") : HomeState()
    data class Error(val message: String) : HomeState()
}