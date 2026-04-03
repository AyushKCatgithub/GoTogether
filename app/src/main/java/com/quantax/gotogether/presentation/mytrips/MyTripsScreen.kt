package com.quantax.gotogether.presentation.mytrips

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
                    ErrorHomeView(message = state.error!!, onRetry = { viewModel.loadMyTrips() })
                }

                state.trips.isEmpty() -> {
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
                                    Icons.Default.List,
                                    contentDescription = null,
                                    tint = GoTogetherOrange,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                        Text(
                            "NO TRIPS YET",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            color = GoTogetherTheme.colors.textPrimary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            "Start your journey by creating a new trip.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = GoTogetherTheme.colors.textSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
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
                                onTripClick = { onNavigateToDetail(trip.id) },
                                onDeleteClick = { viewModel.deleteTrip(trip.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TripItemContainer(
    trip: Trip,
    onTripClick: () -> Unit,
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
                onClick = onTripClick
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


