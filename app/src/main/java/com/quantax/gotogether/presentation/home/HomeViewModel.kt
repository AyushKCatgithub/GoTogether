package com.quantax.gotogether.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quantax.gotogether.data.repository.TripRepository
import com.quantax.gotogether.domain.model.Trip
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: TripRepository
) : ViewModel() {

    private val _allTrips = MutableStateFlow<List<Trip>?>(null)
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _searchFrom = MutableStateFlow("")
    val searchFrom = _searchFrom.asStateFlow()

    private val _searchTo = MutableStateFlow("")
    val searchTo = _searchTo.asStateFlow()

    private val _searchBudget = MutableStateFlow("")
    val searchBudget = _searchBudget.asStateFlow()

    private val _searchGroupSize = MutableStateFlow(0)
    val searchGroupSize = _searchGroupSize.asStateFlow()

    private val _locationText = MutableStateFlow("Detecting location...")
    private val _currentCity = MutableStateFlow<String?>(null)

    // Main state combining trips and filters
    val state: StateFlow<HomeState> = combine(
        _allTrips,
        _searchQuery.debounce(300L),
        _searchFrom,
        _searchTo,
        _searchBudget,
        _searchGroupSize,
        _locationText,
        _currentCity,
    ) { args: Array<Any?> ->
        val trips = args[0] as? List<Trip>
        val query = args[1] as String
        val from = args[2] as String
        val to = args[3] as String
        val budget = args[4] as String
        val size = args[5] as Int
        val loc = args[6] as String
        val currentCity = args[7] as String?

        if (trips == null) return@combine HomeState.Loading

        val displayTitle = when {
            from.isNotBlank() && to.isNotBlank() -> "$from → $to"
            from.isNotBlank() -> "$from → Anywhere"
            to.isNotBlank() -> "Anywhere → $to"
            loc != "Detecting location..." && loc != "Location permission denied" -> loc
            else -> "Search journeys..."
        }

        // Sort trips by current location if no specific 'from' filter is applied
        val sortedTrips = if (from.isBlank() && _currentCity.value != null) {
            val current = _currentCity.value!!
            trips.sortedWith(compareByDescending<Trip> { 
                it.startingLocation.contains(current, ignoreCase = true) || current.contains(it.startingLocation, ignoreCase = true)
            }.thenByDescending { it.createdAt })
        } else {
            trips
        }

        val filtered = sortedTrips.filter { trip ->
            trip.matchesFilter(query, from, to, budget, size)
        }

        HomeState.Success(filtered, displayTitle)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeState.Loading
    )

    init {
        loadTrips()
    }

    fun loadTrips() {
        viewModelScope.launch {
            _isRefreshing.value = true
            repository.getTrips().fold(
                onSuccess = { trips ->
                    _allTrips.value = trips
                },
                onFailure = { error ->

                }
            )
            _isRefreshing.value = false
        }
    }

    fun onSearchQueryChange(query: String) { _searchQuery.value = query }
    fun onSearchFromChange(query: String) { _searchFrom.value = query }
    fun onSearchToChange(query: String) { _searchTo.value = query }
    fun onSearchBudgetChange(query: String) { _searchBudget.value = query }
    fun onSearchGroupSizeChange(size: Int) { if (size >= 0) _searchGroupSize.value = size }

    fun clearFilters() {
        _searchFrom.value = ""
        _searchTo.value = ""
        _searchBudget.value = ""
        _searchGroupSize.value = 0
    }

    fun applyFilters() {
        // combine handles this reactively now
    }

    fun updateLocationText(text: String, cityOnly: String? = null) {
        _locationText.value = text
        cityOnly?.let { _currentCity.value = it }
    }

    private fun Trip.matchesFilter(query: String, from: String, to: String, budget: String, size: Int): Boolean {
        val matchesFrom = from.isEmpty() || 
                startingLocation.contains(from.split(",")[0].trim(), ignoreCase = true) || 
                from.contains(startingLocation, ignoreCase = true)
        val matchesTo = to.isEmpty() || 
                destination.contains(to.split(",")[0].trim(), ignoreCase = true) || 
                to.contains(destination, ignoreCase = true)
        val matchesSearch = query.isEmpty() || (
            destination.contains(query, ignoreCase = true) ||
            startingLocation.contains(query, ignoreCase = true) ||
            title.contains(query, ignoreCase = true)
        )
        val matchesBudget = budget.isEmpty() || matchesBudgetRange(budget)
        val matchesGroupSize = size == 0 || (groupSize <= size)

        return matchesFrom && matchesTo && matchesSearch && matchesBudget && matchesGroupSize
    }

    private fun Trip.matchesBudgetRange(maxBudgetStr: String): Boolean {
        val maxBudget = maxBudgetStr.toIntOrNull() ?: return true
        val numbers = "\\d+".toRegex().findAll(budgetRange)
        val tripMaxBudget = if (numbers.any()) numbers.maxOf { it.value.toInt() } else null
        
        return tripMaxBudget?.let { it <= maxBudget } ?: (
            budgetRange.isEmpty() || 
            budgetRange.contains("flex", ignoreCase = true) || 
            budgetRange.contains("free", ignoreCase = true)
        )
    }
}
