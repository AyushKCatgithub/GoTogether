package com.quantax.gotogether.presentation.trip

import com.quantax.gotogether.ui.theme.*

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.quantax.gotogether.domain.model.Trip
import com.quantax.gotogether.ui.theme.*
import com.quantax.gotogether.presentation.home.HomeViewModel
import com.quantax.gotogether.presentation.home.HomeState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailScreen(
    tripId: String,
    onNavigateBack: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    
    // Find the trip from the loaded trips
    val trip = when (val s = state) {
        is HomeState.Success -> s.trips.find { it.id == tripId }
        else -> null
    }

    Scaffold(
        containerColor = GoTogetherTheme.colors.background,
        topBar = {
            TopAppBar(
                title = { Text("Trip Details", fontWeight = FontWeight.Bold, color = GoTogetherTheme.colors.textPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = GoTogetherTheme.colors.textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        if (trip == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = GoTogetherOrange)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Loading trip details...", color = GoTogetherTheme.colors.textSecondary)
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // Hero Image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                ) {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(trip.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Trip Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)),
                        loading = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(GoTogetherTheme.colors.surface),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = GoTogetherOrange, strokeWidth = 2.dp)
                            }
                        },
                        error = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.linearGradient(listOf(GoTogetherTheme.colors.surface, GoTogetherTheme.colors.surfaceVariant))
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.Landscape,
                                    contentDescription = null,
                                    tint = GoTogetherTheme.colors.textSecondary.copy(alpha = 0.3f),
                                    modifier = Modifier.size(64.dp)
                                )
                            }
                        }
                    )
                    
                    // Gradient overlay
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .align(Alignment.BottomCenter)
                            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))
                                )
                            )
                    )

                    // Trip title + dates on image
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "${trip.startingLocation} to ${trip.destination}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoTogetherTheme.colors.textPrimary,
                            lineHeight = 28.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${trip.startDate} - ${trip.endDate}",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Host Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = GoTogetherTheme.colors.card)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SubcomposeAsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(trip.hostImageUrl ?: "https://ui-avatars.com/api/?name=${trip.hostName}&background=E8632B&color=fff&size=80")
                                .crossfade(true)
                                .build(),
                            contentDescription = "Host Avatar",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .border(2.dp, GoTogetherOrange, CircleShape),
                            contentScale = ContentScale.Crop,
                            loading = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(GoTogetherTheme.colors.surface, CircleShape)
                                )
                            },
                            error = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(GoTogetherOrange, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        trip.hostName.take(1).uppercase(),
                                        color = GoTogetherTheme.colors.textPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Hosted by ${trip.hostName}",
                                color = GoTogetherTheme.colors.textPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            if (trip.hostVerified) {
                                Row(
                                    modifier = Modifier
                                        .background(VerifiedCyan.copy(alpha = 0.12f), RoundedCornerShape(50))
                                        .padding(horizontal = 10.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, "Verified", tint = VerifiedCyan, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Verified Host", color = VerifiedCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Text("Host", color = GoTogetherTheme.colors.textSecondary, fontSize = 13.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // About This Trip
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "About This Trip",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoTogetherTheme.colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = trip.description.ifBlank { "No description provided." },
                        fontSize = 15.sp,
                        color = GoTogetherTheme.colors.textPrimary,
                        lineHeight = 24.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Trip Info Cards
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TripInfoRow(
                        icon = Icons.Default.AttachMoney,
                        label = "Budget",
                        value = trip.budgetRange.ifBlank { "Not specified" }
                    )
                    TripInfoRow(
                        icon = Icons.Default.LocationOn,
                        label = "From",
                        value = trip.startingLocation.ifBlank { "Not specified" }
                    )
                    TripInfoRow(
                        icon = Icons.Default.Flag,
                        label = "To",
                        value = trip.destination.ifBlank { "Not specified" }
                    )
                    TripInfoRow(
                        icon = Icons.Default.Groups,
                        label = "Group Size",
                        value = "${trip.joinedCount}/${trip.groupSize} travelers"
                    )
                }

                // Interests
                if (trip.interests.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            text = "Interests",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoTogetherTheme.colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            trip.interests.forEach { tag ->
                                Box(
                                    modifier = Modifier
                                        .background(
                                            GoTogetherOrange.copy(alpha = 0.1f),
                                            RoundedCornerShape(50)
                                        )
                                        .border(1.dp, GoTogetherOrange.copy(alpha = 0.3f), RoundedCornerShape(50))
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Text(tag, color = GoTogetherOrangeLight, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }

                // Travelers Joining section
                Spacer(modifier = Modifier.height(24.dp))
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "Travelers Joining (${trip.joinedCount})",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoTogetherTheme.colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    if (trip.joinedCount > 0) {
                        Row(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
                            repeat(minOf(trip.joinedCount, 5)) { idx ->
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(
                                            listOf(
                                                GoTogetherOrange,
                                                AccentBlue,
                                                AccentPurple,
                                                AccentGreen,
                                                VerifiedCyan
                                            )[idx % 5],
                                            CircleShape
                                        )
                                        .border(2.dp, GoTogetherTheme.colors.background, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "${idx + 1}",
                                        color = GoTogetherTheme.colors.textPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${trip.joinedCount} people have already joined this trip.",
                            color = GoTogetherTheme.colors.textSecondary,
                            fontSize = 13.sp
                        )
                    } else {
                        Text(
                            text = "Be the first to join this trip!",
                            color = GoTogetherTheme.colors.textSecondary,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Request to Join Button
                Button(
                    onClick = { /* TODO */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoTogetherOrange),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Request to Join", color = GoTogetherTheme.colors.textOnPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun TripInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = GoTogetherTheme.colors.card)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(GoTogetherOrange.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = GoTogetherOrange, modifier = Modifier.size(20.dp))
            }
            Column {
                Text(label, color = GoTogetherTheme.colors.textSecondary, fontSize = 12.sp)
                Text(
                    text = value,
                    color = GoTogetherTheme.colors.textPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
