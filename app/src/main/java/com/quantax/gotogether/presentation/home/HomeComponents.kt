package com.quantax.gotogether.presentation.home

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.ImageNotSupported
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.quantax.gotogether.domain.model.Trip
import com.quantax.gotogether.presentation.components.ShimmerAnimation
import com.quantax.gotogether.ui.theme.GoTogetherOrange
import com.quantax.gotogether.ui.theme.GoTogetherOrangeLight
import com.quantax.gotogether.ui.theme.GoTogetherTheme
import com.quantax.gotogether.ui.theme.VerifiedCyan

// ─── Senior UI Extensions ───

fun Modifier.bounceClick() = composed {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isPressed) 0.96f else 1f, label = "bounce")

    this
        .graphicsLayer(scaleX = scale, scaleY = scale)
        .pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    awaitFirstDown(false)
                    isPressed = true
                    waitForUpOrCancellation()
                    isPressed = false
                }
            }
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopAppBar(
    locationText: String,
    unreadMessageCount: Int = 0,
    onLogoClick: () -> Unit,
    onMessagesClick: () -> Unit
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { onLogoClick() }
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = "Location",
                    tint = GoTogetherOrange,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = locationText,
                    color = GoTogetherTheme.colors.textPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        navigationIcon = {
            Box(
                modifier = Modifier
                    .padding(start = 16.dp, end = 8.dp)
                    .size(40.dp)
                    .background(
                        Brush.linearGradient(listOf(GoTogetherOrange, GoTogetherOrangeLight)),
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { onLogoClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Explore,
                    contentDescription = "Logo",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        },
        actions = {
            Box(
                modifier = Modifier
                    .padding(end = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .border(
                            1.dp,
                            GoTogetherTheme.colors.border.copy(alpha = 0.5f),
                            RoundedCornerShape(12.dp)
                        )
                        .background(GoTogetherTheme.colors.card, RoundedCornerShape(12.dp))
                        .clickable { onMessagesClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "Messages",
                        tint = GoTogetherTheme.colors.textPrimary,
                        modifier = Modifier.size(20.dp)
                    )

                }
                // Activity indicator dot — only visible when there are unread messages
                if (unreadMessageCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color.Red, CircleShape)
                            .align(Alignment.TopEnd)
                            .padding(2.dp)
                    )
                }
            }

        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
    )
}

@Composable
fun HomeSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = {
                Text(
                    "Search trips, destinations...",
                    color = GoTogetherTheme.colors.textSecondary,
                    fontSize = 15.sp
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Outlined.Search,
                    contentDescription = "Search",
                    tint = GoTogetherTheme.colors.textSecondary,
                    modifier = Modifier.size(22.dp)
                )
            },
            modifier = Modifier.weight(1f),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GoTogetherOrange.copy(alpha = 0.5f),
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = GoTogetherTheme.colors.textPrimary,
                unfocusedTextColor = GoTogetherTheme.colors.textPrimary,
                unfocusedContainerColor = GoTogetherTheme.colors.card,
                focusedContainerColor = GoTogetherTheme.colors.card
            ),
            shape = RoundedCornerShape(18.dp),
            singleLine = true
        )

        Surface(
            onClick = onFilterClick,
            modifier = Modifier
                .size(54.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(16.dp),
                    ambientColor = GoTogetherOrange.copy(alpha = 0.1f),
                    spotColor = GoTogetherOrange.copy(alpha = 0.2f)
                ),
            shape = RoundedCornerShape(16.dp),
            color = GoTogetherTheme.colors.card,
            border = BorderStroke(1.dp, GoTogetherTheme.colors.border.copy(alpha = 0.3f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Outlined.Tune,
                    contentDescription = "Filter",
                    tint = GoTogetherOrange,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
fun CategoryChips(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(categories) { category ->
            val isSelected = category == selectedCategory
            Surface(
                onClick = { onCategorySelected(category) },
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) GoTogetherOrange else GoTogetherTheme.colors.card,
                border = if (isSelected) null else BorderStroke(
                    2.dp,
                    GoTogetherTheme.colors.border
                ),
                modifier = Modifier.bounceClick()
            ) {
                Text(
                    text = category,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else GoTogetherTheme.colors.textSecondary,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun TripCard(
    trip: Trip,
    onClick: () -> Unit,
    onHostClick: () -> Unit = {},
    isBookmarked: Boolean = false,
    onBookmarkClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
        Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(32.dp),
                spotColor = GoTogetherTheme.colors.overlay.copy(alpha = 0.35f)
            ),
        shape = RoundedCornerShape(32.dp),
        color = GoTogetherTheme.colors.card,
    ) {
        Column {
            TripCardHero(
                trip = trip,
                onHostClick = onHostClick,
                isBookmarked = isBookmarked,
                onBookmarkClick = onBookmarkClick
            )
            TripCardDetails(trip = trip, onActionClick = onClick)
        }
    }
}

@Composable
private fun TripCardHero(
    trip: Trip,
    onHostClick: () -> Unit,
    isBookmarked: Boolean = false,
    onBookmarkClick: () -> Unit = {}
) {
    // Bounce animation on bookmark toggle
    val bookmarkScale by animateFloatAsState(
        targetValue = if (isBookmarked) 1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "bookmarkScale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
    ) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(trip.imageUrl)
                .crossfade(true)
                .diskCachePolicy(CachePolicy.ENABLED)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .build(),
            contentDescription = "Destination Image",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
                .clip(RoundedCornerShape(20.dp)),
            loading = {
                ShimmerAnimation(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(20.dp)
                )
            },
            error = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(GoTogetherTheme.colors.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.ImageNotSupported,
                        contentDescription = null,
                        tint = GoTogetherTheme.colors.textSecondary.copy(alpha = 0.3f),
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        )

        // Subtle gradient just over the image area to make badges visible
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.35f),
                            Color.Transparent,
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.5f)
                        )
                    )
                )
        )

        // Host Badge
        HostBadge(
            name = trip.hostName,
            imageUrl = trip.hostImageUrl,
            isVerified = trip.hostVerified,
            onClick = onHostClick,
            modifier = Modifier
                .padding(20.dp)
                .align(Alignment.TopStart)
        )

        // Bookmark
        Surface(
            onClick = onBookmarkClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(20.dp)
                .size(38.dp)
                .graphicsLayer(
                    scaleX = bookmarkScale,
                    scaleY = bookmarkScale
                ),
            color = if (isBookmarked) GoTogetherOrange.copy(alpha = 0.85f)
                    else Color.Black.copy(alpha = 0.3f),
            shape = CircleShape
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (isBookmarked) Icons.Default.Bookmark
                                  else Icons.Default.BookmarkBorder,
                    contentDescription = if (isBookmarked) "Remove bookmark" else "Bookmark",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }


    }
}

@Composable
private fun HostBadge(
    name: String,
    imageUrl: String?,
    isVerified: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        color = Color.Black.copy(alpha = 0.4f),
        shape = CircleShape,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(
                        imageUrl
                            ?: "https://ui-avatars.com/api/?name=$name&background=E8632B&color=fff"
                    )
                    .crossfade(true)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .build(),
                contentDescription = "Host Avatar",
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                loading = {
                    ShimmerAnimation(
                        modifier = Modifier.fillMaxSize(),
                        shape = CircleShape
                    )
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            if (isVerified) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    Icons.Default.CheckCircle,
                    "Verified",
                    tint = VerifiedCyan,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun TripCardDetails(
    trip: Trip,
    onActionClick: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
        // Destination & Date Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = trip.title.uppercase(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = GoTogetherTheme.colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    letterSpacing = (-0.5).sp
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = GoTogetherOrange,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = trip.destination,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GoTogetherTheme.colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "•",
                        color = GoTogetherTheme.colors.textSecondary.copy(alpha = 0.5f),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = trip.startDate,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = GoTogetherTheme.colors.textSecondary,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            if (trip.budgetRange.isNotBlank()) {
                Surface(
                    color = MaterialTheme.colorScheme.inverseSurface,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = trip.budgetRange,
                        color = MaterialTheme.colorScheme.inverseOnSurface,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        if (trip.description.isNotEmpty()) {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = trip.description,
                fontSize = 14.sp,
                color = GoTogetherTheme.colors.textSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Normal
            )
        }

        // Interests Chips (Limited to 2-3)
        if (trip.interests.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                trip.interests.take(2).forEach { interest ->
                    Surface(
                        color = GoTogetherTheme.colors.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, GoTogetherTheme.colors.border)
                    ) {
                        Text(
                            text = interest,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            color = GoTogetherTheme.colors.textSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                if (trip.interests.size > 2) {
                    Text(
                        "+${trip.interests.size - 2}",
                        fontSize = 11.sp,
                        color = GoTogetherTheme.colors.textSecondary,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Actions & Availability Footer
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(GoTogetherTheme.colors.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Icon(
                    Icons.Default.Groups,
                    contentDescription = null,
                    tint = GoTogetherOrange,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${trip.joinedCount}/${trip.groupSize}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = GoTogetherTheme.colors.textPrimary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "joined",
                    fontSize = 11.sp,
                    color = GoTogetherTheme.colors.textSecondary
                )
            }

            Button(
                onClick = onActionClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                modifier = Modifier.bounceClick(),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp
                )
            ) {
                Text(
                    "JOIN THE VIBE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
fun EmptyTripsView() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Outlined.Explore,
                contentDescription = null,
                tint = GoTogetherTheme.colors.textSecondary.copy(alpha = 0.3f),
                modifier = Modifier.size(100.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "No expeditions found",
                color = GoTogetherTheme.colors.textPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Try adjusting your filters or search query.",
                color = GoTogetherTheme.colors.textSecondary,
                fontSize = 15.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ErrorHomeView(message: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Outlined.ErrorOutline,
                contentDescription = null,
                tint = Color.Red.copy(alpha = 0.6f),
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Connection Issue",
                color = GoTogetherTheme.colors.textPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                message,
                color = GoTogetherTheme.colors.textSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = GoTogetherOrange),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(54.dp)
            ) {
                Text("Retry", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

@Composable
fun FilterSection(
    searchFrom: String,
    searchTo: String,
    searchBudget: String,
    searchGroupSize: Int,
    onSearchFromClick: () -> Unit,
    onSearchToClick: () -> Unit,
    onSearchBudgetChange: (String) -> Unit,
    onSearchGroupSizeChange: (Int) -> Unit,
    onClearFilters: () -> Unit,
    onApplyFilters: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            "Filter Journeys",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = GoTogetherTheme.colors.textPrimary
        )

        FilterItem(
            value = searchFrom,
            placeholder = "Leaving from...",
            icon = Icons.Default.LocationOn,
            onClick = onSearchFromClick
        )

        FilterItem(
            value = searchTo,
            placeholder = "Going to...",
            icon = Icons.Default.LocationOn,
            onClick = onSearchToClick
        )

        OutlinedTextField(
            value = searchBudget,
            onValueChange = onSearchBudgetChange,
            placeholder = {
                Text(
                    "Max Budget (e.g., 5000)",
                    color = GoTogetherTheme.colors.textSecondary
                )
            },
            leadingIcon = { Icon(Icons.Default.AttachMoney, "Budget", tint = GoTogetherOrange) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GoTogetherOrange,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = GoTogetherTheme.colors.textPrimary,
                unfocusedTextColor = GoTogetherTheme.colors.textPrimary,
                unfocusedContainerColor = GoTogetherTheme.colors.card,
                focusedContainerColor = GoTogetherTheme.colors.card
            ),
            shape = RoundedCornerShape(20.dp),
            singleLine = true
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Column {
                Text(
                    "Group Size",
                    color = GoTogetherTheme.colors.textPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    if (searchGroupSize == 0) "Any size" else "Up to $searchGroupSize people",
                    color = GoTogetherTheme.colors.textSecondary,
                    fontSize = 13.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { if (searchGroupSize > 0) onSearchGroupSizeChange(searchGroupSize - 1) },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.RemoveCircleOutline,
                        "Decrease",
                        tint = if (searchGroupSize > 0) GoTogetherOrange else GoTogetherTheme.colors.textSecondary
                    )
                }
                Text(
                    "$searchGroupSize",
                    color = GoTogetherTheme.colors.textPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                IconButton(
                    onClick = { onSearchGroupSizeChange(searchGroupSize + 1) },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(Icons.Default.AddCircleOutline, "Increase", tint = GoTogetherOrange)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TextButton(
                onClick = onClearFilters,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    "Reset All",
                    color = GoTogetherTheme.colors.textSecondary,
                    fontWeight = FontWeight.Bold
                )
            }
            Button(
                onClick = onApplyFilters,
                modifier = Modifier
                    .weight(1.5f)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoTogetherOrange),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Text("See Results", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun FilterItem(
    value: String,
    placeholder: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(GoTogetherTheme.colors.card)
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = null,
                tint = GoTogetherOrange,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = value.ifBlank { placeholder },
                color = if (value.isBlank()) GoTogetherTheme.colors.textSecondary else GoTogetherTheme.colors.textPrimary,
                fontSize = 15.sp
            )
        }
    }
}
