package com.quantax.gotogether.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.quantax.gotogether.data.repository.BookmarkRepository
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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: TripRepository,
    private val bookmarkRepository: BookmarkRepository,
    private val auth: FirebaseAuth
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

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _locationText = MutableStateFlow("Detecting location...")
    private val _currentCity = MutableStateFlow<String?>(null)
    private val _error = MutableStateFlow<String?>(null)

    // ─── Bookmarks ───
    val bookmarkedIds: StateFlow<Set<String>> = run {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            bookmarkRepository.observeBookmarkedIds(userId)
                .catch { emit(emptySet()) }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())
        } else {
            MutableStateFlow(emptySet())
        }
    }

    fun toggleBookmark(trip: Trip) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            bookmarkRepository.toggleBookmark(
                userId = userId,
                tripId = trip.id,
                postTitle = trip.title,
                authorId = trip.hostId,
                thumbnailUrl = trip.imageUrl
            )
        }
    }

    // Main state combining trips and filters
    val state: StateFlow<HomeState> = combine(
        _allTrips,
        _searchQuery.debounce(300L),
        _searchFrom.debounce(300L),
        _searchTo.debounce(300L),
        _searchBudget.debounce(300L),
        _searchGroupSize,
        _locationText,
        _currentCity,
        _selectedCategory,
        _error
    ) { args: Array<Any?> ->
        val trips = args[0] as? List<Trip>
        val query = args[1] as String
        val from = args[2] as String
        val to = args[3] as String
        val budget = args[4] as String
        val size = args[5] as Int
        val loc = args[6] as String
        val currentCity = args[7] as String?
        val category = args[8] as String
        val errorMsg = args[9] as String?

        if (errorMsg != null) return@combine HomeState.Error(errorMsg)
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
                it.startingLocation.contains(
                    current,
                    ignoreCase = true
                ) || current.contains(it.startingLocation, ignoreCase = true)
            }.thenByDescending { it.createdAt })
        } else {
            trips
        }

        val filtered = sortedTrips.filter { trip ->
            val matchesSearch = trip.matchesFilter(query, from, to, budget, size)
            val matchesCategory = category == "All" ||
                    trip.title.contains(category, ignoreCase = true) ||
                    trip.description.contains(category, ignoreCase = true) ||
                    trip.interests.any { it.contains(category, ignoreCase = true) }

            matchesSearch && matchesCategory
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
        _error.value = null
        _isRefreshing.value = true
        repository.getTripsFlow()
            .onEach { trips ->
                _allTrips.value = trips
                _isRefreshing.value = false
            }
            .catch { error ->
                _error.value = error.localizedMessage ?: "Failed to load trips"
                _isRefreshing.value = false
            }
            .launchIn(viewModelScope)
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
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
        if (size >= 0) _searchGroupSize.value = size
    }

    fun onCategorySelected(category: String) {
        _selectedCategory.value = category
    }

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

    private fun Trip.matchesFilter(
        query: String,
        from: String,
        to: String,
        budget: String,
        size: Int
    ): Boolean {
        val matchesFrom = from.isBlank() ||
                startingLocation.contains(
                    from.split(",").getOrNull(0)?.trim() ?: from,
                    ignoreCase = true
                ) ||
                from.contains(startingLocation, ignoreCase = true)
        val matchesTo = to.isBlank() ||
                destination.contains(to.split(",").getOrNull(0)?.trim() ?: to, ignoreCase = true) ||
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

    private val budgetRegex = "\\d+".toRegex()

    private fun Trip.matchesBudgetRange(maxBudgetStr: String): Boolean {
        val maxBudget = maxBudgetStr.toIntOrNull() ?: return true

        // Performance: Reusing regex and avoiding expensive operations
        val tripMaxBudget = budgetRegex.findAll(budgetRange)
            .map { it.value.toIntOrNull() }
            .filterNotNull()
            .maxOrNull()

        return tripMaxBudget?.let { it <= maxBudget } ?: (
                budgetRange.isEmpty() ||
                        budgetRange.contains("flex", ignoreCase = true) ||
                        budgetRange.contains("free", ignoreCase = true)
                )
    }
}
