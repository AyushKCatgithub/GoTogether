package com.quantax.gotogether.presentation.mytrips

import com.quantax.gotogether.domain.model.Trip

enum class MyTripsTab {
    MY_POSTS,
    SAVED_POSTS
}

data class MyTripsState(
    val trips: List<Trip> = emptyList(),
    val savedTrips: List<Trip> = emptyList(),
    val isLoading: Boolean = false,
    val isSavedLoading: Boolean = false,
    val error: String? = null,
    val selectedTab: MyTripsTab = MyTripsTab.MY_POSTS
)