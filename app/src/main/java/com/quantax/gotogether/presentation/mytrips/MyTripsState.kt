package com.quantax.gotogether.presentation.mytrips

import com.quantax.gotogether.domain.model.Trip

data class MyTripsState(
    val trips: List<Trip> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)