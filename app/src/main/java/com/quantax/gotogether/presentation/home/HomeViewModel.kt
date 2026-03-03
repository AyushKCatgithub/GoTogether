package com.quantax.gotogether.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quantax.gotogether.data.repository.TripRepository
import com.quantax.gotogether.domain.model.Trip
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class HomeState {
    object Loading : HomeState()
    data class Success(val trips: List<Trip>, val locationText: String = "Current Location, Destination") : HomeState()
    data class Error(val message: String) : HomeState()
}

class HomeViewModel : ViewModel() {
    private val repository = TripRepository()
    private val _state = MutableStateFlow<HomeState>(HomeState.Loading)
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private var allTrips: List<Trip> = emptyList()

    private val _searchFrom = MutableStateFlow("")
    val searchFrom: StateFlow<String> = _searchFrom.asStateFlow()

    private val _searchTo = MutableStateFlow("")
    val searchTo: StateFlow<String> = _searchTo.asStateFlow()

    private val _searchBudget = MutableStateFlow("")
    val searchBudget: StateFlow<String> = _searchBudget.asStateFlow()

    private val _searchGroupSize = MutableStateFlow(0)
    val searchGroupSize: StateFlow<Int> = _searchGroupSize.asStateFlow()

    private val _appliedFrom = MutableStateFlow("")
    val appliedFrom: StateFlow<String> = _appliedFrom.asStateFlow()

    private val _appliedTo = MutableStateFlow("")
    val appliedTo: StateFlow<String> = _appliedTo.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadTrips()
    }

    fun loadTrips() {
        viewModelScope.launch {
            if (_state.value is HomeState.Success) {
                _isRefreshing.value = true
            } else {
                _state.value = HomeState.Loading
            }
            val result = repository.getTrips()
            result.fold(
                onSuccess = { trips ->
                    allTrips = trips
                    applyFilters()
                    _isRefreshing.value = false
                },
                onFailure = { error ->
                    _state.value = HomeState.Error(error.message ?: "Failed to fetch trips")
                    _isRefreshing.value = false
                }
            )
        }
    }

    fun onSearchFromChange(query: String) {
        _searchFrom.value = query
    }

    fun onSearchToChange(query: String) {
        _searchTo.value = query
    }

    fun onSearchBudgetChange(query: String) {
        _searchBudget.value = query
    }

    fun onSearchGroupSizeChange(size: Int) {
        if (size >= 0) {
            _searchGroupSize.value = size
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        applyFilters()
    }

    fun applyFilters() {
        val fromQuery = _searchFrom.value.trim()
        val toQuery = _searchTo.value.trim()
        val budgetQuery = _searchBudget.value.trim()
        val groupSizeQuery = _searchGroupSize.value
        val searchQuery = _searchQuery.value.trim()

        val filtered = allTrips.filter { trip ->
            val matchesFrom = fromQuery.isEmpty() || trip.startingLocation.contains(fromQuery, ignoreCase = true)
            val matchesTo = toQuery.isEmpty() || trip.destination.contains(toQuery, ignoreCase = true)
            
            val matchesSearch = searchQuery.isEmpty() || 
                    trip.destination.contains(searchQuery, ignoreCase = true) || 
                    trip.startingLocation.contains(searchQuery, ignoreCase = true) ||
                    trip.title.contains(searchQuery, ignoreCase = true)

            val maxBudget = budgetQuery.toIntOrNull()
            val matchesBudget = if (maxBudget != null) {
                val tripMaxBudget = "\\d+".toRegex().findAll(trip.budgetRange).map { it.value.toInt() }.maxOrNull()
                if (tripMaxBudget != null) {
                    tripMaxBudget <= maxBudget
                } else {
                    trip.budgetRange.isEmpty() || trip.budgetRange.contains("flex", ignoreCase = true) || trip.budgetRange.contains("free", ignoreCase = true)
                }
            } else if (budgetQuery.isNotEmpty()) {
                trip.budgetRange.contains(budgetQuery, ignoreCase = true)
            } else {
                true
            }

            val matchesGroupSize = groupSizeQuery == 0 || (trip.groupSize in 1..groupSizeQuery)
            matchesFrom && matchesTo && matchesBudget && matchesGroupSize && matchesSearch
        }
        
        _appliedFrom.value = fromQuery
        _appliedTo.value = toQuery

        val currentState = _state.value
        if (currentState is HomeState.Success) {
            _state.value = currentState.copy(trips = filtered)
        } else {
            _state.value = HomeState.Success(filtered)
        }
    }

    fun clearFilters() {
        _searchFrom.value = ""
        _searchTo.value = ""
        _searchBudget.value = ""
        _searchGroupSize.value = 0
        applyFilters()
    }

    @android.annotation.SuppressLint("MissingPermission")
    fun fetchLocation(context: android.content.Context) {
        val currentState = _state.value
        if (currentState is HomeState.Success) {
            _state.value = currentState.copy(locationText = "Fetching location...")
        }

        val fusedLocationClient = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                try {
                    val geocoder = android.location.Geocoder(context, java.util.Locale.getDefault())
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        geocoder.getFromLocation(location.latitude, location.longitude, 1) { addresses ->
                            val address = addresses.firstOrNull()
                            updateLocationText(address?.locality, address?.countryName)
                        }
                    } else {
                        @Suppress("DEPRECATION")
                        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                        val address = addresses?.firstOrNull()
                        updateLocationText(address?.locality, address?.countryName)
                    }
                } catch (e: Exception) {
                    updateLocationText(null, null)
                }
            } else {
                setLocationError("Location unavailable")
            }
        }.addOnFailureListener {
            setLocationError("Failed to get location")
        }
    }

    private fun updateLocationText(city: String?, country: String?) {
        val text = if (city != null && country != null) {
            "$city, $country"
        } else if (city != null) {
            city
        } else if (country != null) {
            country
        } else {
            "Location unavailable"
        }
        val currentState = _state.value
        if (currentState is HomeState.Success) {
            _state.value = currentState.copy(locationText = text)
        }
    }
    
    fun setLocationError(message: String) {
        val currentState = _state.value
        if (currentState is HomeState.Success) {
            _state.value = currentState.copy(locationText = message)
        }
    }
}
