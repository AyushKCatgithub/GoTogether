package com.quantax.gotogether.presentation.profile

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import coil.compose.SubcomposeAsyncImage
import com.quantax.gotogether.domain.model.UserProfile
import com.quantax.gotogether.ui.theme.GoTogetherOrange
import com.quantax.gotogether.ui.theme.GoTogetherTheme
import com.quantax.gotogether.ui.theme.VerifiedCyan

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToEditProfile: () -> Unit,
    onSignOut: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    var showFullScreenViewer by remember { mutableStateOf(false) }
    var fullScreenInitialIndex by remember { mutableStateOf(0) }
    val backgroundPhotos = (state.profile?.verificationPhotoUrls?.filter { it.isNotBlank() } ?: emptyList()).take(4)
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onEvent(ProfileEvent.LoadProfile)
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
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .background(
                                    WarningYellow.copy(alpha = 0.1f),
                                    RoundedCornerShape(26.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Warning,
                                contentDescription = null,
                                tint = WarningYellow.copy(alpha = 0.8f),
                                modifier = Modifier.size(45.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            "Profile Unavailable",
                            color = GoTogetherTheme.colors.textPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            state.error!!,
                            color = GoTogetherTheme.colors.textSecondary,
                            fontSize = 15.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 22.sp
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(
                            onClick = { viewModel.onEvent(ProfileEvent.LoadProfile) },
                            colors = ButtonDefaults.buttonColors(containerColor = GoTogetherOrange),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.height(54.dp).fillMaxWidth(0.7f)
                        ) {
                            Text("Try Again", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
            else -> {
                val profile = state.profile
                val pagerCount = (backgroundPhotos.size).coerceAtLeast(1)

                //Background section: banner only (profile photo sits half-over in next layer)
                val bannerHeight = 267.dp
                val photoSize = 120.dp
                val photoHalf = photoSize / 2

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
                                            modifier = Modifier.fillMaxSize().background(GoTogetherTheme.colors.surface),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(color = GoTogetherOrange, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
                                        }
                                    },
                                    error = {
                                        Box(
                                            modifier = Modifier.fillMaxSize().background(GoTogetherTheme.colors.surface),
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
                            if (backgroundPhotos.isNotEmpty()) {
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
                    // Stick indicators, divided as per the number of images uploaded by user
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
                    IconButton(
                        onClick = onNavigateToEditProfile,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = "Add or edit background photos",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
    Scaffold(
        containerColor = Color.White,
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {


            when {
                state.isLoading -> LoadingState()
                state.error != null -> ErrorState(state.error!!) { viewModel.onEvent(ProfileEvent.LoadProfile) }
                else -> ProfileContent(
                    profile = state.profile,
                    onEditClick = onNavigateToEditProfile,
                    onSignOutClick = {
                        viewModel.onEvent(ProfileEvent.SignOut)
                        onSignOut()
                    },
                    modifier = Modifier // Removed paddingValues here to handle overlap correctly
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProfileContent(
    profile: UserProfile?,
    onEditClick: () -> Unit,
    onSignOutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Premium Cover Image + Profile Pic Overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
        ) {
            // Dark, cinematic cover image
            SubcomposeAsyncImage(
                model = "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&q=80&w=1000",
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )

            // Gradient Overlay for readability
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.4f))
                        )
                    )
            )

            // Overlapping Avatar
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = 10.dp)
            ) {
                Surface(
                    modifier = Modifier.size(130.dp),
                    shape = CircleShape,
                    border = androidx.compose.foundation.BorderStroke(
                        6.dp,
                        Color.White
                    ), // Thick white border for separation
                    shadowElevation = 16.dp
                ) {
                    SubcomposeAsyncImage(
                        model = profile?.profileImageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        loading = {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(40.dp),
                                color = GoTogetherOrange
                            )
                        },
                        error = {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.padding(30.dp),
                                tint = Color.LightGray
                            )
                        }
                    )
                }

                if (profile?.isVerified == true) {
                    Surface(
                        modifier = Modifier
                            .size(34.dp)
                            .align(Alignment.BottomEnd)
                            .offset(x = (-4).dp, y = (-4).dp),
                        shape = CircleShape,
                        color = VerifiedCyan,
                        border = androidx.compose.foundation.BorderStroke(3.dp, Color.White)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.padding(6.dp)
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Name & Rank
            Text(
                text = profile?.name?.ifBlank { "Traveler" }?.uppercase() ?: "...",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                color = Color.Black,
                textAlign = TextAlign.Center,
                letterSpacing = (-1).sp
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (profile?.username?.isNotBlank() == true) "@${profile.username}" else "ACTIVE WANDERER",
                    style = MaterialTheme.typography.titleSmall,
                    color = GoTogetherOrange,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )

            }

            Spacer(modifier = Modifier.height(32.dp))

            // Premium Stats Card - Refined
            Surface(
                color = Color(0xFFF8F8F8),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    CleanStat("12", "TRIPS", Icons.Default.Star)
                    CleanStat("2.5K", "MILES", Icons.Default.CheckCircle)
                    CleanStat("4.9", "RATING", Icons.Default.Person)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Vibes (Interests) Section
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "MY VIBES",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val vibes =
                        listOf("Adventure", "Trekking", "Solo Traveler", "Foodie", "Mountains")
                    vibes.forEach { vibe ->
                        Surface(
                            color = Color.White,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                Color.Black.copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(50.dp)
                        ) {
                            Text(
                                vibe.uppercase(),
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        }
                // 2. Scrollable content: starts below banner; first spacer leaves room for overlapping avatar
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
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Bio Section - Refined
            if (profile?.bio?.isNotBlank() == true) {
                Spacer(modifier = Modifier.height(32.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "ABOUT ME",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = Color(0xFFF8F8F8),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = profile.bio,
                            fontSize = 14.sp,
                            color = GoTogetherTheme.colors.textSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    if (profile != null) {
                        Row(
                            modifier = Modifier
                                .background(
                                    if (profile.isVerified) VerifiedCyan.copy(alpha = 0.12f) else WarningYellow.copy(alpha = 0.12f),
                                    RoundedCornerShape(50)
                                )
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.padding(20.dp),
                            lineHeight = 22.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Action Buttons - High Contrast
            Button(
                onClick = onEditClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    "EDIT PROFILE",
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    letterSpacing = 1.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Share & Rate - Modern Pill Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    onClick = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "Join me on GoTogether! Let's travel the world together. Download here: https://play.google.com/store/apps/details?id=${context.packageName}"
                            )
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, null)
                        context.startActivity(shareIntent)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF1F1F1)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (profile.isVerified) "Verified" else "Not Verified",
                                color = if (profile.isVerified) VerifiedCyan else WarningYellow,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        if (!profile.isVerified) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Complete your profile to get verified",
                                color = GoTogetherTheme.colors.textSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(28.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ProfileInfoCard(icon = Icons.Default.Email, label = "Email", value = profile?.email ?: "")
                        ProfileInfoCard(icon = Icons.Default.Phone, label = "Phone", value = profile?.phone ?: "")
                        ProfileInfoCard(icon = Icons.Default.LocationOn, label = "Address", value = profile?.address ?: "")
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onNavigateToEditProfile,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(60.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GoTogetherOrange),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Edit Profile", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "SHARE",
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }

                Surface(
                    onClick = {
                        val uri = Uri.parse("market://details?id=${context.packageName}")
                        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
                        try {
                            context.startActivity(goToMarket)
                        } catch (e: Exception) {
                            context.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("http://play.google.com/store/apps/details?id=${context.packageName}")
                                )
                            )
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF1F1F1)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = GoTogetherOrange,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "RATE",
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp,
                                letterSpacing = 1.sp
                            )
                        }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = {
                            viewModel.onEvent(ProfileEvent.SignOut)
                            onSignOut()
                        },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(60.dp),
                        shape = RoundedCornerShape(24.dp),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                            brush = androidx.compose.ui.graphics.SolidColor(Color.Red.copy(alpha = 0.4f))
                        )
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sign Out", color = Color.Red.copy(alpha = 0.7f), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sign Out in simple text or minimal button
            androidx.compose.material3.TextButton(
                onClick = onSignOutClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "SIGN OUT",
                    color = Color.Red,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Version Number
            Text(
                text = "VERSION 1.0",
                style = MaterialTheme.typography.labelSmall,
                color = GoTogetherTheme.colors.textSecondary.copy(alpha = 0.5f),
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(24.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 3. Profile photo straddling banner and content (half above, half below — Telegram-style)
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

                // 4. TopAppBar on top of everything
                TopAppBar(
                    title = {
                        Text("Profile", fontWeight = FontWeight.Bold, color = GoTogetherTheme.colors.textPrimary)
                    },
                    actions = {
                        IconButton(onClick = onNavigateToEditProfile) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = GoTogetherOrange)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        }

        // Full-screen image viewer (Dialog)
        if (showFullScreenViewer && backgroundPhotos.isNotEmpty()) {
            FullScreenImageViewer(
                photoUrls = backgroundPhotos,
                initialIndex = fullScreenInitialIndex,
                onDismiss = { showFullScreenViewer = false }
            )
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
private fun CleanStat(
    value: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            icon,
            contentDescription = null,
            tint = GoTogetherOrange.copy(alpha = 0.6f),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = GoTogetherTheme.colors.textPrimary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = GoTogetherTheme.colors.textSecondary,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun LoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = GoTogetherOrange)
    }
}

@Composable
private fun ErrorState(error: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Warning,
            contentDescription = null,
            tint = Color.Red,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(error, textAlign = TextAlign.Center, color = GoTogetherTheme.colors.textPrimary)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry) { Text("Retry") }
    }
}
