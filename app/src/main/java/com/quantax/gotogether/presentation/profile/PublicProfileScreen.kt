package com.quantax.gotogether.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.quantax.gotogether.ui.theme.GoTogetherOrange
import com.quantax.gotogether.ui.theme.GoTogetherTheme
import com.quantax.gotogether.ui.theme.VerifiedCyan
import com.quantax.gotogether.ui.theme.WarningYellow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicProfileScreen(
    userId: String,
    onNavigateBack: () -> Unit,
    viewModel: PublicProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showFullScreenViewer by remember { mutableStateOf(false) }
    var fullScreenInitialIndex by remember { mutableStateOf(0) }

    LaunchedEffect(userId) {
        viewModel.loadProfile(userId)
    }

    Box(modifier = Modifier.fillMaxSize().background(GoTogetherTheme.colors.background)) {
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GoTogetherOrange, strokeWidth = 3.dp)
                }
            }

            state.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                        Text(
                            state.error!!,
                            color = GoTogetherTheme.colors.textSecondary,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadProfile(userId) },
                            colors = ButtonDefaults.buttonColors(containerColor = GoTogetherOrange),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "Retry",
                                color = GoTogetherTheme.colors.textOnPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            else -> {
                val profile = state.profile
                val backgroundPhotos = profile?.backgroundPhotoUrls?.filter { it.isNotBlank() }.orEmpty()
                val hasSlidingPhotos = backgroundPhotos.isNotEmpty()
                val pagerCount = (backgroundPhotos.size).coerceAtLeast(1)

                val bannerHeight = 267.dp
                val photoSize = 120.dp
                val photoHalf = photoSize / 2

                // 1. Banner: same size and behaviour as ProfileScreen
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(bannerHeight)
                        .align(Alignment.TopCenter)
                ) {
                    val pagerState = rememberPagerState(pageCount = { pagerCount }, initialPage = 0)
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        userScrollEnabled = true
                    ) { page ->
                        Box(modifier = Modifier.fillMaxSize()) {
                            if (backgroundPhotos.isNotEmpty() && page < backgroundPhotos.size) {
                                SubcomposeAsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(backgroundPhotos[page])
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Background photo ${page + 1}",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                    loading = {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    GoTogetherTheme.colors.surface
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(color = GoTogetherOrange, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
                                        }
                                    },
                                    error = {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    GoTogetherTheme.colors.surface),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Person, contentDescription = null, tint = GoTogetherTheme.colors.textSecondary, modifier = Modifier.size(48.dp))
                                        }
                                    }
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    GoTogetherOrange.copy(alpha = 0.25f),
                                                    GoTogetherTheme.colors.background
                                                )
                                            )
                                        )
                                )
                            }
                            if (hasSlidingPhotos) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable(
                                            role = Role.Button,
                                            onClick = {
                                                fullScreenInitialIndex = page
                                                showFullScreenViewer = true
                                            }
                                        )
                                )
                            }
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.2f),
                                        Color.Black.copy(alpha = 0.4f)
                                    ),
                                    startY = 0f,
                                    endY = Float.POSITIVE_INFINITY
                                )
                            )
                    )
                    if (backgroundPhotos.isNotEmpty()) {
                        val count = backgroundPhotos.size
                        Row(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(start = 8.dp, end = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            repeat(count) { index ->
                                val isSelected = pagerState.currentPage == index
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(3.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(
                                            if (isSelected) Color.White
                                            else Color.White.copy(alpha = 0.45f)
                                        )
                                )
                            }
                        }
                    }
                }

                // 2. Scrollable content: same layout as ProfileScreen (avatar space, name, username, bio, verification, cards)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = bannerHeight)
                        .verticalScroll(rememberScrollState())
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(bottom = 100.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(photoHalf))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (profile?.name?.isNotBlank() == true) profile.name else "No name set",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoTogetherTheme.colors.textPrimary
                    )
                    if (profile?.username?.isNotBlank() == true) {
                        Text(text = "@${profile.username}", fontSize = 15.sp, color = GoTogetherTheme.colors.textSecondary)
                    }
                    if (profile?.bio?.isNotBlank() == true) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = profile.bio,
                            fontSize = 14.sp,
                            color = GoTogetherTheme.colors.textSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    if (profile != null) {
                        Row(
                            modifier = Modifier
                                .background(
                                    if (profile.isVerified) VerifiedCyan.copy(alpha = 0.12f) else WarningYellow.copy(
                                            alpha = 0.12f
                                        ),
                                    RoundedCornerShape(50)
                                )
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                if (profile.isVerified) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (profile.isVerified) VerifiedCyan else WarningYellow,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (profile.isVerified) "Verified" else "Not Verified",
                                color = if (profile.isVerified) VerifiedCyan else WarningYellow,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(28.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        PublicProfileInfoCard(icon = Icons.Default.LocationOn, label = "Address", value = profile?.address ?: "")
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // 3. Profile photo overlapping banner (same size and style as ProfileScreen)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = bannerHeight - photoHalf)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (profile != null && profile.profileImageUrl.isNotBlank()) {
                        SubcomposeAsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(profile.profileImageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Profile Photo",
                            modifier = Modifier
                                .size(photoSize)
                                .clip(CircleShape)
                                .border(4.dp, Color.White, CircleShape)
                                .border(4.dp, GoTogetherOrange, CircleShape),
                            contentScale = ContentScale.Crop,
                            loading = {
                                Box(
                                    modifier = Modifier.fillMaxSize().background(GoTogetherTheme.colors.surface, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = GoTogetherOrange, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
                                }
                            },
                            error = {
                                Box(
                                    modifier = Modifier.fillMaxSize().background(GoTogetherTheme.colors.surface, CircleShape).border(4.dp, GoTogetherOrange, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = "No Photo", tint = GoTogetherTheme.colors.textSecondary, modifier = Modifier.size(48.dp))
                                }
                            }
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(photoSize)
                                .clip(CircleShape)
                                .background(GoTogetherTheme.colors.surface)
                                .border(4.dp, Color.White, CircleShape)
                                .border(4.dp, GoTogetherOrange, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = "No Photo", tint = GoTogetherTheme.colors.textSecondary, modifier = Modifier.size(48.dp))
                        }
                    }
                }

                // 4. TopAppBar (transparent, back only — like ProfileScreen but with back instead of edit)
                TopAppBar(
                    title = {
                        Text("Profile", fontWeight = FontWeight.Bold, color = GoTogetherTheme.colors.textPrimary)
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = GoTogetherTheme.colors.textPrimary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        }

        if (state.profile != null) {
            val photos = state.profile!!.backgroundPhotoUrls.filter { it.isNotBlank() }
            if (showFullScreenViewer && photos.isNotEmpty()) {
                FullScreenImageViewer(
                    photoUrls = photos,
                    initialIndex = fullScreenInitialIndex,
                    onDismiss = { showFullScreenViewer = false }
                )
            }
        }
    }
}

@Composable
private fun FullScreenImageViewer(
    photoUrls: List<String>,
    initialIndex: Int,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.95f))
        ) {
            val pagerState = rememberPagerState(
                pageCount = { photoUrls.size },
                initialPage = initialIndex.coerceIn(0, (photoUrls.size - 1).coerceAtLeast(0))
            )
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = true
            ) { page ->
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(photoUrls[page])
                        .crossfade(true)
                        .build(),
                    contentDescription = "Photo ${page + 1}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                    loading = {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = GoTogetherOrange, strokeWidth = 2.dp, modifier = Modifier.size(32.dp))
                        }
                    },
                    error = {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(64.dp))
                        }
                    }
                )
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
private fun PublicProfileInfoCard(
    icon: ImageVector,
    label: String,
    value: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = GoTogetherTheme.colors.card)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(GoTogetherOrange.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = GoTogetherOrange,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(label, color = GoTogetherTheme.colors.textSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = value.ifBlank { "Not provided" },
                    color = if (value.isNotBlank()) GoTogetherTheme.colors.textPrimary else GoTogetherTheme.colors.textSecondary.copy(
                        alpha = 0.5f
                    ),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
