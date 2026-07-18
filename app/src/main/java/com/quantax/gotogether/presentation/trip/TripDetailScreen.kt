package com.quantax.gotogether.presentation.trip

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ScrollState
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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.quantax.gotogether.domain.model.JoinRequest
import com.quantax.gotogether.domain.model.Trip
import com.quantax.gotogether.domain.model.UserProfile
import com.quantax.gotogether.presentation.components.TripDetailShimmer
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
        containerColor = MaterialTheme.colorScheme.background,
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
                        isBookmarked = currentState.isBookmarked,
                        members = currentState.membersList,
                        scrollState = scrollState,
                        onBackClick = onNavigateBack,
                        onShareClick = shareTrip,
                        onBookmarkClick = { viewModel.toggleBookmark() },
                        onProfileClick = { onNavigateToPublicProfile(it) },
                        paddingValues = paddingValues
                    )

                    // Floating Top Header (Sticky)
                    val headerAlpha = (scrollState.value.toFloat() / 300f).coerceIn(0f, 1f)
                    if (headerAlpha > 0.1f) {
                        FloatingHeader(
                            title = trip.title.ifBlank { trip.destination },
                            alpha = headerAlpha,
                            backgroundColor = MaterialTheme.colorScheme.background,
                            isBookmarked = currentState.isBookmarked,
                            onBackClick = onNavigateBack,
                            onShareClick = shareTrip,
                            onBookmarkClick = { viewModel.toggleBookmark() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TripDetailContent(
    trip: Trip,
    isHost: Boolean,
    requestStatus: String,
    isBookmarked: Boolean,
    members: List<UserProfile>,
    scrollState: ScrollState,
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    onProfileClick: (String) -> Unit,
    paddingValues: PaddingValues
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(MaterialTheme.colorScheme.background)
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
                contentDescription = "Photo of ${trip.destination}",
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FloatingCircleButton(Icons.AutoMirrored.Filled.ArrowBack, onClick = onBackClick)

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FloatingCircleButton(
                        icon = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        tint = if (isBookmarked) MaterialTheme.colorScheme.primary else null,
                        onClick = onBookmarkClick
                    )
                    FloatingCircleButton(Icons.Filled.IosShare, onClick = onShareClick)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            // Title
            Text(
                text = trip.title.ifBlank { trip.destination },
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Budget highlight
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CurrencyRupee,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = trip.budgetRange.ifBlank { "Flexible" },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Location and Date Info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.LocationOn,
                    null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = trip.destination,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (trip.startDate.isNotBlank() && trip.endDate.isNotBlank()) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(12.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(
                        Icons.Default.Schedule,
                        null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${trip.startDate} - ${trip.endDate}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Host Profile Highlight
            // Prefer the host's current uploaded photo (from their live profile in the
            // members list); the trip's stored hostImageUrl is a snapshot that can be
            // stale/blank if the trip was created before the photo was set.
            val hostProfile = members.firstOrNull { it.uid == trip.hostId }
            val hostImageUrl = hostProfile?.profileImageUrl?.takeIf { it.isNotBlank() }
                ?: trip.hostImageUrl?.takeIf { it.isNotBlank() }
            HostSection(
                name = trip.hostName,
                imageUrl = hostImageUrl,
                isVerified = trip.hostVerified,
                isYou = isHost,
                onProfileClick = { onProfileClick(trip.hostId) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (trip.interests.isNotEmpty()) {
                SectionHeader(title = "Trip vibe")
                Spacer(modifier = Modifier.height(16.dp))
                InterestsChips(interests = trip.interests)
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Central Status Card (Trip Info Hub)
            TripInfoHub(trip)

            Spacer(modifier = Modifier.height(24.dp))

            // Description
            SectionHeader(title = "Description")
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = trip.description.ifBlank { "No description provided." },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Location Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionHeader(title = "Location")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Open on maps",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        Icons.Default.ChevronRight,
                        null,
                        tint = MaterialTheme.colorScheme.primary,
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
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Map,
                        null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Travelers
            SectionHeader(title = "Travelers joining")
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
fun FloatingCircleButton(
    icon: ImageVector,
    tint: Color? = null,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(44.dp),
        shape = CircleShape,
        color = Color.Black.copy(alpha = 0.35f)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = tint ?: Color.White
            )
        }
    }
}

@Composable
fun FloatingHeader(
    title: String,
    alpha: Float,
    backgroundColor: Color,
    isBookmarked: Boolean,
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
    onBookmarkClick: () -> Unit
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
                    contentDescription = "Back",
                    tint = if (alpha < 0.5f) Color.White else MaterialTheme.colorScheme.onSurface
                )
            }

            if (alpha > 0.6f) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBookmarkClick,
                    modifier = Modifier.background(
                        if (alpha < 0.5f) Color.Black.copy(alpha = 0.3f) else Color.Transparent,
                        CircleShape
                    )
                ) {
                    Icon(
                        if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = if (alpha < 0.5f) Color.White
                        else if (isBookmarked) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface
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
                        contentDescription = "Share",
                        tint = if (alpha < 0.5f) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun TripInfoHub(trip: Trip) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            InfoItem(Icons.Default.Groups, "${trip.joinedCount}/${trip.groupSize}", "Travelers")
            VerticalDivider()
            InfoItem(Icons.Default.CurrencyRupee, trip.budgetRange.ifBlank { "TBA" }, "Budget")
            VerticalDivider()
            InfoItem(Icons.Default.Map, trip.startingLocation.ifBlank { "TBA" }.take(8), "From")
        }
    }
}

@Composable
fun InfoItem(icon: ImageVector, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            value,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun VerticalDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(40.dp)
            .background(MaterialTheme.colorScheme.outlineVariant)
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
            model = imageUrl?.takeIf { it.isNotBlank() }
                ?: "https://ui-avatars.com/api/?name=$name&background=random",
            contentDescription = "Host $name",
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (isYou) "You (Host)" else name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Golden member",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
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
                        .background(MaterialTheme.colorScheme.outlineVariant, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Verified expert",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InterestsChips(interests: List<String>) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        interests.forEach { tag ->
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    tag,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
fun TravelersList(
    members: List<UserProfile>,
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
                    contentDescription = member.name,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        member.name,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Ready for adventure",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    Icons.Default.ChevronRight,
                    null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
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
        Icon(
            Icons.Default.GroupAdd,
            null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "No travelers yet. Be the first to join!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .navigationBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            if (isHost || requestStatus == JoinRequest.STATUS_APPROVED) {
                Button(
                    onClick = onChatClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Enter chatroom", style = MaterialTheme.typography.titleMedium)
                }
            } else {
                val isPending = requestStatus == JoinRequest.STATUS_PENDING
                if (isPending) {
                    FilledTonalButton(
                        onClick = {},
                        enabled = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Request pending", style = MaterialTheme.typography.titleMedium)
                    }
                } else {
                    Button(
                        onClick = onJoinClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Join trip", style = MaterialTheme.typography.titleMedium)
                    }
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
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Shield, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                "Travel safe policy",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "All members are verified by GoTogether",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface
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
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                message,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 48.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(32.dp))
            FilledTonalButton(onClick = onRetry) {
                Text("Try again")
            }
        }
    }
}
