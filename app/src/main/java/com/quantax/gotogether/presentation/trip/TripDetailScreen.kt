package com.quantax.gotogether.presentation.trip

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.quantax.gotogether.domain.model.JoinRequest
import com.quantax.gotogether.presentation.components.TripDetailShimmer
import com.quantax.gotogether.ui.theme.AccentBlue
import com.quantax.gotogether.ui.theme.GoTogetherOrange
import com.quantax.gotogether.ui.theme.GoTogetherTheme
import com.quantax.gotogether.ui.theme.VerifiedCyan

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TripDetailScreen(
    tripId: String,
    onNavigateBack: () -> Unit,
    onNavigateToChatroom: (String) -> Unit = {},
    onNavigateToPublicProfile: (String) -> Unit = {},
    viewModel: TripDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val actionMessage by viewModel.actionMessage.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    BackHandler {
        onNavigateBack()
    }

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
        bottomBar = {
            if (state is TripDetailState.Success) {
                val successState = state as TripDetailState.Success
                ActionBottomBar(
                    isHost = successState.isHost,
                    requestStatus = successState.requestStatus,
                    onJoinClick = { viewModel.requestToJoin() },
                    onChatClick = { onNavigateToChatroom(successState.trip.id) }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Content
            when (val currentState = state) {
                is TripDetailState.Loading -> {
                    TripDetailShimmer()
                }

                is TripDetailState.Error -> {
                    ErrorState(
                        message = currentState.message,
                        onRetry = { viewModel.loadTrip(tripId) })
                }

                is TripDetailState.Success -> {
                    val trip = currentState.trip
                    val shareTrip = {
                        val sendIntent = android.content.Intent().apply {
                            action = android.content.Intent.ACTION_SEND
                            putExtra(
                                android.content.Intent.EXTRA_TEXT,
                                "Join me for '${trip.title}' in ${trip.destination}! Check it out on GoTogether."
                            )
                            type = "text/plain"
                        }
                        context.startActivity(
                            android.content.Intent.createChooser(
                                sendIntent,
                                null
                            )
                        )
                    }

                    TripDetailContent(
                        trip = trip,
                        isHost = currentState.isHost,
                        requestStatus = currentState.requestStatus,
                        members = currentState.membersList,
                        scrollState = scrollState,
                        onBackClick = onNavigateBack,
                        onShareClick = shareTrip,
                        onProfileClick = { onNavigateToPublicProfile(it) },
                        paddingValues = paddingValues
                    )

                    // Floating Top Header (Sticky)
                    val headerAlpha = (scrollState.value.toFloat() / 300f).coerceIn(0f, 1f)
                    if (headerAlpha > 0.1f) {
                        FloatingHeader(
                            title = trip.title.ifBlank { trip.destination },
                            alpha = headerAlpha,
                            backgroundColor = GoTogetherTheme.colors.background,
                            onBackClick = onNavigateBack,
                            onShareClick = shareTrip
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TripDetailContent(
    trip: com.quantax.gotogether.domain.model.Trip,
    isHost: Boolean,
    requestStatus: String,
    members: List<com.quantax.gotogether.domain.model.UserProfile>,
    scrollState: androidx.compose.foundation.ScrollState,
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
    onProfileClick: (String) -> Unit,
    paddingValues: PaddingValues
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(GoTogetherTheme.colors.background)
            .padding(bottom = paddingValues.calculateBottomPadding())
    ) {
        // Hero Image Card Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .padding(16.dp)
                .clip(RoundedCornerShape(28.dp))
        ) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(trip.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        translationY = scrollState.value * 0.4f
                    }
            )

            // Overlays on top of image
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Black.copy(alpha = 0.2f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.4f)
                            )
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FloatingCircleButton(Icons.AutoMirrored.Filled.ArrowBack, onClick = onBackClick)
                FloatingCircleButton(Icons.Filled.IosShare, onClick = onShareClick)
            }


        }

        Spacer(modifier = Modifier.height(12.dp))

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            // Title and Pricing Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = trip.title.ifBlank { trip.destination }.uppercase(),
                    modifier = Modifier.weight(1f),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = GoTogetherTheme.colors.textPrimary,
                    letterSpacing = (-1).sp,
                    lineHeight = 32.sp
                )
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = trip.budgetRange,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoTogetherTheme.colors.textPrimary
                    )
                    Text(
                        text = "/Trip",
                        fontSize = 12.sp,
                        color = GoTogetherTheme.colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Location and Date Info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.LocationOn,
                    null,
                    tint = GoTogetherTheme.colors.textSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = trip.destination,
                    fontSize = 14.sp,
                    color = GoTogetherTheme.colors.textSecondary
                )
                if (trip.startDate.isNotBlank() && trip.endDate.isNotBlank()) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(12.dp)
                            .background(GoTogetherTheme.colors.divider)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(
                        Icons.Default.Schedule,
                        null,
                        tint = GoTogetherTheme.colors.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${trip.startDate} - ${trip.endDate}",
                        fontSize = 14.sp,
                        color = GoTogetherTheme.colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Host Profile Highlight
            HostSection(
                name = trip.hostName,
                imageUrl = trip.hostImageUrl,
                isVerified = trip.hostVerified,
                isYou = isHost,
                onProfileClick = { onProfileClick(trip.hostId) }
            )

            Spacer(modifier = Modifier.height(24.dp))
            
            if (trip.interests.isNotEmpty()) {
                SectionHeader(title = "Trip Vibe")
                Spacer(modifier = Modifier.height(16.dp))
                InterestsChips(interests = trip.interests)
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Central Status Card (Trip Info Hub)
            TripInfoHub(trip)

            Spacer(modifier = Modifier.height(24.dp))

            // Description
            Text(
                text = "Description",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = GoTogetherTheme.colors.textPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = trip.description.ifBlank { "No description provided." },
                fontSize = 14.sp,
                lineHeight = 22.sp,
                color = GoTogetherTheme.colors.textSecondary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Location Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Location",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoTogetherTheme.colors.textPrimary
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Open on maps",
                        fontSize = 14.sp,
                        color = Color(0xFF6A8E4E),
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        Icons.Default.ChevronRight,
                        null,
                        tint = Color(0xFF6A8E4E),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Mock Map Preview
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                shape = RoundedCornerShape(20.dp),
                color = GoTogetherTheme.colors.surfaceVariant
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Map,
                        null,
                        modifier = Modifier.size(48.dp),
                        tint = GoTogetherTheme.colors.textSecondary.copy(alpha = 0.35f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Travelers
            SectionHeader(title = "Travelers Joining")
            Spacer(modifier = Modifier.height(16.dp))
            if (members.isNotEmpty()) {
                TravelersList(members, onProfileClick)
            } else {
                EmptyTravelersPlaceholder()
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun FloatingCircleButton(icon: ImageVector, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(44.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
        shadowElevation = 4.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun FloatingHeader(
    title: String,
    alpha: Float,
    backgroundColor: Color,
    onBackClick: () -> Unit,
    onShareClick: () -> Unit
) {
    Surface(
        color = backgroundColor.copy(alpha = alpha.coerceIn(0f, 0.95f)),
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .zIndex(10f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.background(
                    if (alpha < 0.5f) Color.Black.copy(alpha = 0.3f) else Color.Transparent,
                    CircleShape
                )
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = if (alpha < 0.5f) Color.White else GoTogetherTheme.colors.textPrimary
                )
            }

            if (alpha > 0.6f) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = GoTogetherTheme.colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            IconButton(
                onClick = onShareClick,
                modifier = Modifier.background(
                    if (alpha < 0.5f) Color.Black.copy(alpha = 0.3f) else Color.Transparent,
                    CircleShape
                )
            ) {
                Icon(
                    Icons.Default.Share,
                    contentDescription = null,
                    tint = if (alpha < 0.5f) Color.White else GoTogetherTheme.colors.textPrimary
                )
            }
        }
    }
}

@Composable
fun TripInfoHub(trip: com.quantax.gotogether.domain.model.Trip) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = GoTogetherTheme.colors.surfaceVariant.copy(alpha = 0.4f)
        ),
        border = BorderStroke(1.dp, GoTogetherTheme.colors.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            InfoItem(Icons.Default.Groups, "${trip.joinedCount}/${trip.groupSize}", "Travelers")
            VerticalDivider()
            InfoItem(Icons.Default.AttachMoney, trip.budgetRange, "Budget")
            VerticalDivider()
            InfoItem(Icons.Default.Map, trip.startingLocation.ifBlank { "TBA" }.take(8), "From")
        }
    }
}

@Composable
fun InfoItem(icon: ImageVector, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = GoTogetherOrange, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = GoTogetherTheme.colors.textPrimary)
        Text(label, fontSize = 11.sp, color = GoTogetherTheme.colors.textSecondary)
    }
}

@Composable
fun VerticalDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(40.dp)
            .background(GoTogetherTheme.colors.surfaceVariant)
    )
}

@Composable
fun HostSection(
    name: String,
    imageUrl: String?,
    isVerified: Boolean,
    isYou: Boolean,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onProfileClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        SubcomposeAsyncImage(
            model = imageUrl ?: "https://ui-avatars.com/api/?name=$name&background=random",
            contentDescription = null,
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .border(2.dp, GoTogetherOrange.copy(alpha = 0.3f), CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (isYou) "You (Host)" else name,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = GoTogetherTheme.colors.textPrimary
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Golden Member", fontSize = 12.sp, color = GoTogetherOrange)
                if (isVerified) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Default.Verified,
                        null,
                        tint = VerifiedCyan,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(3.dp)
                        .background(GoTogetherTheme.colors.divider, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Verified Expert",
                    fontSize = 12.sp,
                    color = GoTogetherTheme.colors.textSecondary
                )
            }
        }
        Icon(Icons.Default.ChevronRight, null, tint = GoTogetherTheme.colors.textSecondary)
    }
}

@Composable
fun InterestsChips(interests: List<String>) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        interests.forEach { tag ->
            Surface(
                color = GoTogetherTheme.colors.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, GoTogetherTheme.colors.surfaceVariant)
            ) {
                Text(
                    tag,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = GoTogetherTheme.colors.textPrimary
                )
            }
        }
    }
}

@Composable
fun TravelersList(
    members: List<com.quantax.gotogether.domain.model.UserProfile>,
    onProfileClick: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        members.forEach { member ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onProfileClick(member.uid) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                SubcomposeAsyncImage(
                    model = member.profileImageUrl
                        ?: "https://ui-avatars.com/api/?name=${member.name}",
                    contentDescription = null,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(member.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = GoTogetherTheme.colors.textPrimary)
                    Text("Ready for adventure", fontSize = 12.sp, color = GoTogetherTheme.colors.textSecondary)
                }
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    Icons.Default.ChevronRight,
                    null,
                    tint = GoTogetherTheme.colors.textSecondary.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyTravelersPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.GroupAdd, null, modifier = Modifier.size(48.dp), tint = GoTogetherTheme.colors.textSecondary.copy(alpha = 0.4f))
        Spacer(modifier = Modifier.height(12.dp))
        Text("No travelers yet. Be the first to join!", color = GoTogetherTheme.colors.textSecondary, fontSize = 14.sp)
    }
}

@Composable
fun ActionBottomBar(
    isHost: Boolean,
    requestStatus: String,
    onJoinClick: () -> Unit,
    onChatClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 0.dp // Reference image has it very flat or subtle
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .navigationBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            if (isHost || requestStatus == JoinRequest.STATUS_APPROVED) {
                Button(
                    onClick = onChatClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text(
                        "ENTER CHATROOM",
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        letterSpacing = 1.sp
                    )
                }
            } else {
                val isPending = requestStatus == JoinRequest.STATUS_PENDING
                Button(
                    onClick = onJoinClick,
                    enabled = !isPending,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPending) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text(
                        if (isPending) "REQUEST PENDING" else "JOIN THE VIBE",
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

@Composable
fun TripPolicyItem() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(GoTogetherTheme.colors.surfaceVariant.copy(alpha = 0.2f))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Shield, null, tint = AccentBlue, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text("Travel Safe Policy", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(
                "All members are verified by GoTogether",
                fontSize = 12.sp,
                color = GoTogetherTheme.colors.textSecondary
            )
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = GoTogetherTheme.colors.textPrimary,
        letterSpacing = (-0.5).sp
    )
}

@Composable
fun ErrorState(message: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.ErrorOutline,
                null,
                modifier = Modifier.size(64.dp),
                tint = Color.Red.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                message,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 48.dp),
                color = GoTogetherTheme.colors.textSecondary,
                lineHeight = 22.sp
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onRetry,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoTogetherOrange)
            ) {
                Text("Try Again", modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
    }
}
