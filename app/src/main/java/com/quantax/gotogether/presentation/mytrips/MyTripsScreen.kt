package com.quantax.gotogether.presentation.mytrips


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quantax.gotogether.domain.model.Trip
import com.quantax.gotogether.presentation.components.TripCardShimmer
import com.quantax.gotogether.presentation.home.ErrorHomeView
import com.quantax.gotogether.presentation.home.TripCard
import com.quantax.gotogether.ui.theme.GoTogetherOrange
import com.quantax.gotogether.ui.theme.GoTogetherTheme

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
        containerColor = GoTogetherTheme.colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "YOUR ADVENTURES",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                        color = GoTogetherTheme.colors.textPrimary,
                        letterSpacing = 2.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
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

@Composable
private fun TabSelector(
    selectedTab: MyTripsTab,
    onTabSelected: (MyTripsTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TabButton(
            label = "My Posts",
            isSelected = selectedTab == MyTripsTab.MY_POSTS,
            onClick = { onTabSelected(MyTripsTab.MY_POSTS) },
            modifier = Modifier.weight(1f)
        )
        TabButton(
            label = "Saved Posts",
            isSelected = selectedTab == MyTripsTab.SAVED_POSTS,
            onClick = { onTabSelected(MyTripsTab.SAVED_POSTS) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TabButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(46.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) GoTogetherOrange else GoTogetherTheme.colors.card,
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(
            1.5.dp,
            GoTogetherTheme.colors.border
        )
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
                fontSize = 14.sp,
                color = if (isSelected) Color.White else GoTogetherTheme.colors.textSecondary,
                letterSpacing = if (isSelected) 0.5.sp else 0.sp
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
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                items(3) {
                    TripCardShimmer()
                }
            }
        }

        state.error != null -> {
            ErrorHomeView(message = state.error!!, onRetry = onRetry)
        }

        state.trips.isEmpty() -> {
            EmptyStateView(
                icon = Icons.Default.List,
                title = "NO TRIPS YET",
                subtitle = "Start your journey by creating a new trip."
            )
        }

        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                item {
                    Text(
                        "Manage your active and past trips",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GoTogetherTheme.colors.textSecondary,
                        modifier = Modifier.padding(bottom = 8.dp)
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
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                items(3) {
                    TripCardShimmer()
                }
            }
        }

        state.error != null -> {
            ErrorHomeView(message = state.error!!, onRetry = onRetry)
        }

        state.savedTrips.isEmpty() -> {
            EmptyStateView(
                icon = Icons.Default.BookmarkBorder,
                title = "NO SAVED TRIPS",
                subtitle = "Bookmark trips from the home feed to see them here."
            )
        }

        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                item {
                    Text(
                        "Trips you've saved for later",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GoTogetherTheme.colors.textSecondary,
                        modifier = Modifier.padding(bottom = 8.dp)
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
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
            modifier = Modifier.size(120.dp),
            shape = RoundedCornerShape(32.dp),
            color = GoTogetherTheme.colors.surface
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = GoTogetherOrange,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = GoTogetherTheme.colors.textPrimary,
            letterSpacing = 1.sp
        )
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = GoTogetherTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
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
        // Wrap the card to give it a "float" feel
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            shadowElevation = 0.dp, // Minimal shadow for Gen-Z clean look
            color = Color.Transparent
        ) {
            TripCard(
                trip = trip,
                onClick = onTripClick,
                onHostClick = onHostClick
            )
        }

        // High-contrast Delete Button
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(20.dp)
                .size(40.dp),
            shape = CircleShape,
            color = Color.Black, // Sharp Contrast
            onClick = { showDeleteDialog = true }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
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
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            shadowElevation = 0.dp,
            color = Color.Transparent
        ) {
            TripCard(
                trip = trip,
                onClick = onTripClick,
                onHostClick = onHostClick,
                isBookmarked = true,
                onBookmarkClick = { showRemoveDialog = true }
            )
        }

        if (showRemoveDialog) {
            AlertDialog(
                onDismissRequest = { showRemoveDialog = false },
                title = {
                    Text(
                        "REMOVE BOOKMARK?",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                },
                text = {
                    Text(
                        "Remove '${trip.title}' from your saved trips?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GoTogetherTheme.colors.textSecondary
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onRemoveBookmark()
                            showRemoveDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoTogetherOrange),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("YES, REMOVE", fontWeight = FontWeight.Black, color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRemoveDialog = false }) {
                        Text("CANCEL", fontWeight = FontWeight.Bold, color = Color.Gray)
                    }
                },
                containerColor = GoTogetherTheme.colors.background,
                shape = RoundedCornerShape(28.dp)
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
        title = {
            Text(
                "DELETE TRIP?",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        },
        text = {
            Text(
                "This will permanently erase '$tripTitle'. No take-backs.",
                style = MaterialTheme.typography.bodyMedium,
                color = GoTogetherTheme.colors.textSecondary
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("YES, DELETE", fontWeight = FontWeight.Black, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", fontWeight = FontWeight.Bold, color = Color.Gray)
            }
        },
        containerColor = GoTogetherTheme.colors.background,
        shape = RoundedCornerShape(28.dp)
    )
}
