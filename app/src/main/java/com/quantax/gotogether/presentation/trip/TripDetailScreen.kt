package com.quantax.gotogether.presentation.trip

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.quantax.gotogether.domain.model.JoinRequest
import com.quantax.gotogether.domain.model.Trip
import com.quantax.gotogether.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailScreen(
    tripId: String,
    onNavigateBack: () -> Unit,
    onNavigateToChatroom: (String) -> Unit = {},
    onNavigateToPublicProfile: (String) -> Unit = {},
    viewModel: TripDetailViewModel = viewModel()
) {
    val trip by viewModel.trip.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isHost by viewModel.isHost.collectAsState()
    val requestStatus by viewModel.requestStatus.collectAsState()
    val actionMessage by viewModel.actionMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(tripId) {
        viewModel.loadTrip(tripId)
    }

    LaunchedEffect(actionMessage) {
        actionMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearActionMessage()
        }
    }

    Scaffold(
        containerColor = GoTogetherTheme.colors.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
        if (isLoading && trip == null) {
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
        } else if (trip != null) {
            val t = trip!!
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
                            .data(t.imageUrl)
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
                            text = "${t.startingLocation} to ${t.destination}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            lineHeight = 28.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${t.startDate} - ${t.endDate}",
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
                        .padding(horizontal = 16.dp)
                        .clickable { onNavigateToPublicProfile(t.hostId) },
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
                                .data(t.hostImageUrl ?: "https://ui-avatars.com/api/?name=${t.hostName}&background=E8632B&color=fff&size=80")
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
                                        t.hostName.take(1).uppercase(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isHost) "You're the Host" else "Hosted by ${t.hostName}",
                                color = GoTogetherTheme.colors.textPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            if (t.hostVerified) {
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
                        text = t.description.ifBlank { "No description provided." },
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
                        value = t.budgetRange.ifBlank { "Not specified" }
                    )
                    TripInfoRow(
                        icon = Icons.Default.LocationOn,
                        label = "From",
                        value = t.startingLocation.ifBlank { "Not specified" }
                    )
                    TripInfoRow(
                        icon = Icons.Default.Flag,
                        label = "To",
                        value = t.destination.ifBlank { "Not specified" }
                    )
                    TripInfoRow(
                        icon = Icons.Default.Groups,
                        label = "Group Size",
                        value = "${t.joinedCount}/${t.groupSize} travelers"
                    )
                }

                // Interests
                if (t.interests.isNotEmpty()) {
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
                            t.interests.forEach { tag ->
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
                        text = "Travelers Joining (${t.joinedCount})",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoTogetherTheme.colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    if (t.joinedCount > 0) {
                        Row(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
                            repeat(minOf(t.joinedCount, 5)) { idx ->
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
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${t.joinedCount} people have already joined this trip.",
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

                // Action Buttons Area
                if (isHost || requestStatus == JoinRequest.STATUS_APPROVED) {
                    Button(
                        onClick = { onNavigateToChatroom(t.id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open Trip Chat", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    // "Request to Join" flow for non-hosts
                    Button(
                        onClick = { viewModel.requestToJoin() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (requestStatus == JoinRequest.STATUS_PENDING) GoTogetherTheme.colors.surfaceVariant else GoTogetherOrange,
                            disabledContainerColor = GoTogetherTheme.colors.surfaceVariant
                        ),
                        shape = RoundedCornerShape(16.dp),
                        enabled = requestStatus == "NONE" && !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        } else {
                            Text(
                                text = when (requestStatus) {
                                    JoinRequest.STATUS_PENDING -> "Request Pending"
                                    JoinRequest.STATUS_DENIED -> "Request Denied"
                                    else -> "Request to Join"
                                },
                                color = if (requestStatus == JoinRequest.STATUS_PENDING) GoTogetherTheme.colors.textSecondary else Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    if (requestStatus == JoinRequest.STATUS_PENDING) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Waiting for host approval.",
                            color = GoTogetherTheme.colors.textSecondary,
                            fontSize = 13.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
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
