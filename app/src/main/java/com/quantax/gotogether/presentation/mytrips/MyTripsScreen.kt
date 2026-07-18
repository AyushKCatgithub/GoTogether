package com.quantax.gotogether.presentation.mytrips


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quantax.gotogether.domain.model.Trip
import com.quantax.gotogether.presentation.components.TripCardShimmer
import com.quantax.gotogether.presentation.home.ErrorHomeView
import com.quantax.gotogether.presentation.home.TripCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTripsScreen(
    onNavigateToDetail: (String) -> Unit,
    onNavigateToPublicProfile: (String) -> Unit = {},
    viewModel: MyTripsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadMyTrips()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "My trips",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // ─── Tab Selector ───
            TabSelector(
                selectedTab = state.selectedTab,
                onTabSelected = { viewModel.selectTab(it) }
            )

            // ─── Content ───
            Box(modifier = Modifier.fillMaxSize()) {
                when (state.selectedTab) {
                    MyTripsTab.MY_POSTS -> MyPostsContent(
                        state = state,
                        onTripClick = onNavigateToDetail,
                        onHostClick = onNavigateToPublicProfile,
                        onDeleteClick = { viewModel.deleteTrip(it) },
                        onRetry = { viewModel.loadMyTrips() }
                    )

                    MyTripsTab.SAVED_POSTS -> SavedPostsContent(
                        state = state,
                        onTripClick = onNavigateToDetail,
                        onHostClick = onNavigateToPublicProfile,
                        onRemoveBookmark = { viewModel.removeBookmark(it) },
                        onRetry = { viewModel.loadSavedTrips() }
                    )
                }
            }
        }
    }
}

// ─── Tab Selector ───

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TabSelector(
    selectedTab: MyTripsTab,
    onTabSelected: (MyTripsTab) -> Unit
) {
    val tabs = listOf(
        MyTripsTab.MY_POSTS to "My trips",
        MyTripsTab.SAVED_POSTS to "Saved"
    )
    SingleChoiceSegmentedButtonRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        tabs.forEachIndexed { index, (tab, label) ->
            SegmentedButton(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = tabs.size),
                label = { Text(label) }
            )
        }
    }
}

// ─── My Posts Content ───

@Composable
private fun MyPostsContent(
    state: MyTripsState,
    onTripClick: (String) -> Unit,
    onHostClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit,
    onRetry: () -> Unit
) {
    when {
        state.isLoading -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(3) {
                    TripCardShimmer()
                }
            }
        }

        state.error != null -> {
            ErrorHomeView(message = state.error, onRetry = onRetry)
        }

        state.trips.isEmpty() -> {
            EmptyStateView(
                icon = Icons.Default.List,
                title = "No trips yet",
                subtitle = "Start your journey by creating a new trip."
            )
        }

        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        "Manage your active and past trips",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                items(state.trips, key = { it.id }) { trip ->
                    TripItemContainer(
                        trip = trip,
                        onTripClick = { onTripClick(trip.id) },
                        onHostClick = { onHostClick(trip.hostId) },
                        onDeleteClick = { onDeleteClick(trip.id) }
                    )
                }
            }
        }
    }
}

// ─── Saved Posts Content ───

@Composable
private fun SavedPostsContent(
    state: MyTripsState,
    onTripClick: (String) -> Unit,
    onHostClick: (String) -> Unit,
    onRemoveBookmark: (String) -> Unit,
    onRetry: () -> Unit
) {
    when {
        state.isSavedLoading -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(3) {
                    TripCardShimmer()
                }
            }
        }

        state.error != null -> {
            ErrorHomeView(message = state.error, onRetry = onRetry)
        }

        state.savedTrips.isEmpty() -> {
            EmptyStateView(
                icon = Icons.Default.BookmarkBorder,
                title = "No saved trips",
                subtitle = "Bookmark trips from the home feed to see them here."
            )
        }

        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        "Trips you've saved for later",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                items(state.savedTrips, key = { it.id }) { trip ->
                    SavedTripItemContainer(
                        trip = trip,
                        onTripClick = { onTripClick(trip.id) },
                        onHostClick = { onHostClick(trip.hostId) },
                        onRemoveBookmark = { onRemoveBookmark(trip.id) }
                    )
                }
            }
        }
    }
}

// ─── Shared Components ───

@Composable
private fun EmptyStateView(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(112.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHighest
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun TripItemContainer(
    trip: Trip,
    onTripClick: () -> Unit,
    onHostClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        TripCard(
            trip = trip,
            onClick = onTripClick,
            onHostClick = onHostClick
        )

        // Delete action overlaid on the card hero
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(20.dp)
                .size(40.dp),
            shape = CircleShape,
            color = Color.Black.copy(alpha = 0.4f),
            onClick = { showDeleteDialog = true }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete trip",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        if (showDeleteDialog) {
            DeleteConfirmationDialog(
                tripTitle = trip.title,
                onConfirm = {
                    onDeleteClick()
                    showDeleteDialog = false
                },
                onDismiss = { showDeleteDialog = false }
            )
        }
    }
}

@Composable
private fun SavedTripItemContainer(
    trip: Trip,
    onTripClick: () -> Unit,
    onHostClick: () -> Unit,
    onRemoveBookmark: () -> Unit
) {
    var showRemoveDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        TripCard(
            trip = trip,
            onClick = onTripClick,
            onHostClick = onHostClick,
            isBookmarked = true,
            onBookmarkClick = { showRemoveDialog = true }
        )

        if (showRemoveDialog) {
            AlertDialog(
                onDismissRequest = { showRemoveDialog = false },
                title = { Text("Remove bookmark?") },
                text = {
                    Text("Remove \"${trip.title}\" from your saved trips?")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onRemoveBookmark()
                            showRemoveDialog = false
                        }
                    ) {
                        Text("Remove")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRemoveDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun DeleteConfirmationDialog(
    tripTitle: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete trip?") },
        text = {
            Text("This will permanently delete \"$tripTitle\". This can't be undone.")
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
