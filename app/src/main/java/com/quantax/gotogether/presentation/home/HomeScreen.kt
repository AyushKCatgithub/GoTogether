package com.quantax.gotogether.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quantax.gotogether.presentation.components.LocationSearchDialog
import com.quantax.gotogether.presentation.components.TripCardShimmer
import com.quantax.gotogether.ui.theme.GoTogetherOrange
import com.quantax.gotogether.ui.theme.GoTogetherTheme
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

    Scaffold(containerColor = GoTogetherTheme.colors.background, topBar = {
        val locationText = when (val s = state) {
            is HomeState.Success -> s.locationText
            else -> "Loading location..."
        }
        HomeTopAppBar(
            locationText = locationText,
            onLogoClick = { viewModel.loadTrips() },
            onMessagesClick = onNavigateToMessages
        )
    }, floatingActionButton = {
        FloatingActionButton(
            onClick = onNavigateToCreateTrip,
            containerColor = GoTogetherOrange,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.shadow(
                12.dp,
                RoundedCornerShape(18.dp),
                ambientColor = GoTogetherOrange.copy(alpha = 0.5f),
                spotColor = GoTogetherOrange.copy(alpha = 0.8f)
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create Trip", fontWeight = FontWeight.ExtraBold)
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
                    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                        Text(
                            "DISCOVER YOUR",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = GoTogetherOrange,
                            letterSpacing = 2.sp
                        )
                        Text(
                            "NEXT VIBE",
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onBackground,
                            lineHeight = 46.sp,
                            letterSpacing = (-1).sp
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
                        title = if (selectedCategory == "All") "UPCOMING EXPEDITIONS" else "${selectedCategory.uppercase()} TRIPS",
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
                            items(
                                currentState.trips, key = { it.id }, contentType = { "TripCard" }
                            ) { trip ->
                                Box(
                                    modifier = Modifier.padding(
                                        horizontal = 16.dp, vertical = 12.dp
                                    )
                                ) {
                                    TripCard(
                                        trip = trip,
                                        onClick = { onNavigateToTripDetail(trip.id) },
                                        onHostClick = { onNavigateToPublicProfile(trip.hostId) })
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
            containerColor = GoTogetherTheme.colors.background,
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
                tint = GoTogetherOrange,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = GoTogetherTheme.colors.textPrimary
            )
        }

        if (!hideSeeAll && onSeeAllClick != null) {
            TextButton(onClick = onSeeAllClick) {
                Text(
                    "See All",
                    color = GoTogetherOrange,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}
