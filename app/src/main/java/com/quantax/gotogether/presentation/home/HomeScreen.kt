package com.quantax.gotogether.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quantax.gotogether.presentation.components.LocationSearchDialog
import com.quantax.gotogether.presentation.components.TripCardShimmer
import com.quantax.gotogether.utils.PermissionUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToCreateTrip: () -> Unit,
    onNavigateToTripDetail: (String) -> Unit = {},
    onNavigateToMessages: () -> Unit,
    onNavigateToPublicProfile: (String) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val searchFrom by viewModel.searchFrom.collectAsStateWithLifecycle()
    val searchTo by viewModel.searchTo.collectAsStateWithLifecycle()
    val searchBudget by viewModel.searchBudget.collectAsStateWithLifecycle()
    val searchGroupSize by viewModel.searchGroupSize.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val bookmarkedIds by viewModel.bookmarkedIds.collectAsStateWithLifecycle()

    var showFilterSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showingLocationPickerFor by remember { mutableStateOf<String?>(null) }
    val categories =
        listOf("All", "Adventure", "Relaxation", "Cultural", "Budget Friendly", "Solo", "Group")

    val context = LocalContext.current

    // Handle Location Permission through Utility
    val permissionLauncher = PermissionUtils.rememberLocationPermissionLauncher { granted ->
        if (granted) {
            PermissionUtils.fetchLocation(
                context = context,
                onLocationDetailsFetched = { full, city ->
                    viewModel.updateLocationText(
                        full, city
                    )
                },
                onError = { viewModel.updateLocationText(it) })
        } else {
            viewModel.updateLocationText("Location permission denied")
        }
    }

    LaunchedEffect(Unit) {
        if (PermissionUtils.hasLocationPermission(context)) {
            PermissionUtils.fetchLocation(
                context = context,
                onLocationDetailsFetched = { full, city ->
                    viewModel.updateLocationText(
                        full, city
                    )
                },
                onError = { viewModel.updateLocationText("Location not available") })
        } else {
            permissionLauncher.launch(PermissionUtils.locationPermissions)
        }
    }

    val connectivityStatus by viewModel.isConnected.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            val locationText = when (val s = state) {
                is HomeState.Success -> s.locationText
                else -> "Loading location..."
            }
            Column {
                HomeTopAppBar(
                    locationText = locationText,
                    onLogoClick = { viewModel.loadTrips() },
                    onMessagesClick = onNavigateToMessages
                )
                // Premium "No Connection" bar
                androidx.compose.animation.AnimatedVisibility(
                    visible = connectivityStatus != com.quantax.gotogether.utils.ConnectivityObserver.Status.Available,
                    enter = androidx.compose.animation.expandVertically(),
                    exit = androidx.compose.animation.shrinkVertically()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No internet connection — showing cached data",
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.loadTrips() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                // 1. Premium Greeting & Search
                item {
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                        Text(
                            "DISCOVER YOUR",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "Next vibe",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                item {
                    HomeSearchBar(
                        query = searchQuery,
                        onQueryChange = viewModel::onSearchQueryChange,
                        onFilterClick = { showFilterSheet = true })
                }

                // 2. Interactive Category Filter
                item {
                    CategoryChips(
                        categories = categories,
                        selectedCategory = selectedCategory,
                        onCategorySelected = viewModel::onCategorySelected
                    )
                }

                item {
                    SectionHeader(
                        title = if (selectedCategory == "All") "Upcoming trips" else "$selectedCategory trips",
                        hideSeeAll = true
                    )
                }

                // 5. Success / Loading / Error States
                when (val currentState = state) {
                    is HomeState.Loading -> {
                        items(3) {
                            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                                TripCardShimmer()
                            }
                        }
                    }

                    is HomeState.Error -> {
                        item {
                            ErrorHomeView(
                                message = currentState.message, onRetry = { viewModel.loadTrips() })
                        }
                    }

                    is HomeState.Success -> {
                        if (currentState.trips.isEmpty()) {
                            item { EmptyTripsView() }
                        } else {
                            itemsIndexed(
                                currentState.trips, key = { _, trip -> trip.id }
                            ) { index, trip ->
                                // Premium staggered animation effect
                                val animatedVisibility = remember { mutableStateOf(false) }
                                LaunchedEffect(Unit) { animatedVisibility.value = true }

                                androidx.compose.animation.AnimatedVisibility(
                                    visible = animatedVisibility.value,
                                    enter = androidx.compose.animation.fadeIn(
                                        animationSpec = androidx.compose.animation.core.tween(
                                            500,
                                            delayMillis = index * 80
                                        )
                                    ) + androidx.compose.animation.scaleIn(
                                        initialScale = 0.9f,
                                        animationSpec = androidx.compose.animation.core.tween(
                                            500,
                                            delayMillis = index * 80
                                        )
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier.padding(
                                            horizontal = 16.dp, vertical = 12.dp
                                        )
                                    ) {
                                        TripCard(
                                            trip = trip,
                                            onClick = { onNavigateToTripDetail(trip.id) },
                                            onHostClick = { onNavigateToPublicProfile(trip.hostId) },
                                            isBookmarked = bookmarkedIds.contains(trip.id),
                                            onBookmarkClick = { viewModel.toggleBookmark(trip) })
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialogs & Sheets
    if (showingLocationPickerFor != null) {
        LocationSearchDialog(
            onDismissRequest = { showingLocationPickerFor = null },
            onLocationSelected = { selectedLocation ->
                if (showingLocationPickerFor == "from") {
                    viewModel.onSearchFromChange(selectedLocation)
                } else if (showingLocationPickerFor == "to") {
                    viewModel.onSearchToChange(selectedLocation)
                }
                showingLocationPickerFor = null
            })
    }

    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            dragHandle = { BottomSheetDefaults.DragHandle() }) {
            FilterSection(
                searchFrom = searchFrom,
                searchTo = searchTo,
                searchBudget = searchBudget,
                searchGroupSize = searchGroupSize,
                onSearchFromClick = { showingLocationPickerFor = "from" },
                onSearchToClick = { showingLocationPickerFor = "to" },
                onSearchBudgetChange = viewModel::onSearchBudgetChange,
                onSearchGroupSizeChange = viewModel::onSearchGroupSizeChange,
                onClearFilters = { viewModel.clearFilters() },
                onApplyFilters = {
                    viewModel.applyFilters()
                    showFilterSheet = false
                })
        }
    }
}

@Composable
private fun SectionHeader(
    title: String, onSeeAllClick: (() -> Unit)? = null, hideSeeAll: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 16.dp, end = 16.dp, top = 24.dp, bottom = 16.dp
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (!hideSeeAll && onSeeAllClick != null) {
            TextButton(onClick = onSeeAllClick) {
                Text("See all")
            }
        }
    }
}
