package com.quantax.gotogether.presentation.mytrips

import com.quantax.gotogether.domain.model.*

import com.quantax.gotogether.ui.theme.*

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.quantax.gotogether.presentation.mytrips.MyTripsViewModel
import com.quantax.gotogether.presentation.home.TripCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTripsScreen(
    onNavigateToDetail: (String) -> Unit,
    viewModel: MyTripsViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    // Reload trips when screen becomes visible
    LaunchedEffect(Unit) {
        viewModel.loadMyTrips()
    }

    Scaffold(
        containerColor = GoTogetherTheme.colors.background,
        topBar = {
            TopAppBar(
                title = { Text("My Trips", fontWeight = FontWeight.Bold, color = GoTogetherTheme.colors.textPrimary) },
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
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = GoTogetherTheme.colors.textPrimary
                    )
                }
                state.error != null -> {
                    Text(
                        text = state.error!!,
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center),
                        textAlign = TextAlign.Center
                    )
                }
                state.trips.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = Color.White.copy(alpha = 0.05f),
                            modifier = Modifier.size(80.dp)
                        ) {
                            Icon(
                                Icons.Default.List,
                                contentDescription = null,
                                tint = GoTogetherTheme.colors.textSecondary,
                                modifier = Modifier
                                    .padding(20.dp)
                                    .fillMaxSize()
                            )
                        }
                        Text(
                            "You haven't posted any trips yet.",
                            color = GoTogetherTheme.colors.textSecondary,
                            fontSize = 16.sp
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(state.trips) { trip ->
                            Box {
                                TripCard(
                                    trip = trip,
                                    showBookmark = false,
                                    onClick = { onNavigateToDetail(trip.id) }
                                )
                                
                                // Floating Delete Button over the trip card
                                var showDeleteDialog by remember { mutableStateOf(false) }
                                
                                IconButton(
                                    onClick = { showDeleteDialog = true },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete Trip",
                                        tint = WarningYellow
                                    )
                                }
                                
                                if (showDeleteDialog) {
                                    AlertDialog(
                                        onDismissRequest = { showDeleteDialog = false },
                                        title = { Text("Delete Trip") },
                                        text = { Text("Are you sure you want to delete '${trip.title}'?") },
                                        confirmButton = {
                                            TextButton(
                                                onClick = {
                                                    viewModel.deleteTrip(trip.id)
                                                    showDeleteDialog = false
                                                }
                                            ) {
                                                Text("Delete", color = WarningYellow)
                                            }
                                        },
                                        dismissButton = {
                                            TextButton(onClick = { showDeleteDialog = false }) {
                                                Text("Cancel", color = GoTogetherTheme.colors.textOnPrimary)
                                            }
                                        },
                                        containerColor = GoTogetherTheme.colors.background,
                                        titleContentColor = Color.White,
                                        textContentColor = TextGray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
