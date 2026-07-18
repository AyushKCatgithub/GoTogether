package com.quantax.gotogether.presentation.profile

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberPlatformOverscrollFactory
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.quantax.gotogether.domain.model.UserProfile
import com.quantax.gotogether.ui.theme.VerifiedCyan

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToEditProfile: () -> Unit,
    onSignOut: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showFullScreenViewer by remember { mutableStateOf(false) }
    var showProfilePhotoPreview by remember { mutableStateOf(false) }
    var fullScreenInitialIndex by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        viewModel.onEvent(ProfileEvent.LoadProfile)
    }

    val scrollState = rememberScrollState()
    val bannerHeight = 240.dp
    val photoSize = 88.dp
    val overscrollFactory = rememberPlatformOverscrollFactory(
        glowColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
    )

    // Derived state to prevent whole-screen recomposition on scroll
    val headerAlpha by remember {
        derivedStateOf {
            val scrollValue = scrollState.value.toFloat()
            val bannerCollapseProgress =
                (scrollValue / (bannerHeight.value * 2.5f)).coerceIn(0f, 1f)
            (bannerCollapseProgress * 2f).coerceIn(0f, 1f)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when {
            state.isLoading -> LoadingState()
            state.error != null -> state.error?.let { error ->
                ErrorState(error = error) {
                    viewModel.onEvent(
                        ProfileEvent.LoadProfile
                    )
                }
            }

            else -> {
                val profile = state.profile
                val backgroundPhotos = remember(profile?.verificationPhotoUrls) {
                    (profile?.verificationPhotoUrls?.filter { it.isNotBlank() }
                        ?: emptyList()).take(4)
                }
                val pagerCount = backgroundPhotos.size.coerceAtLeast(1)

                CompositionLocalProvider(LocalOverscrollFactory provides overscrollFactory) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .navigationBarsPadding()
                    ) {
                        ProfileCover(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(bannerHeight)
                                .graphicsLayer {
                                    translationY = scrollState.value.toFloat() * 0.25f
                                    alpha =
                                        (1f - (scrollState.value.toFloat() / (bannerHeight.toPx() * 0.9f))).coerceIn(
                                            0f,
                                            1f
                                        )
                                },
                            photos = backgroundPhotos,
                            pagerCount = pagerCount,
                            onPhotoClick = { page ->
                                fullScreenInitialIndex = page
                                showFullScreenViewer = true
                            }
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .padding(top = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(18.dp)
                        ) {
                            ProfileSummaryCard(
                                profile = profile,
                                photoSize = photoSize,
                                onPhotoLongPress = { showProfilePhotoPreview = true }
                            )

                            ProfileContent(
                                profile = profile,
                                onEdit = onNavigateToEditProfile,
                                onSignOut = {
                                    viewModel.onEvent(ProfileEvent.SignOut)
                                    onSignOut()
                                }
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }

                ProfileTopBar(
                    headerAlpha = headerAlpha,
                    profileImage = profile?.profileImageUrl,
                    profileName = profile?.name,
                    onEdit = onNavigateToEditProfile
                )
            }
        }

        // Full-screen viewer
        if (showFullScreenViewer) {
            val photos = (state.profile?.verificationPhotoUrls?.filter { it.isNotBlank() }
                ?: emptyList()).take(4)
            if (photos.isNotEmpty()) {
                FullScreenImageViewer(
                    photoUrls = photos,
                    initialIndex = fullScreenInitialIndex,
                    onDismiss = { showFullScreenViewer = false })
            }
        }

        if (showProfilePhotoPreview) {
            ProfilePhotoPreviewDialog(
                photoUrl = state.profile?.profileImageUrl,
                onDismiss = { showProfilePhotoPreview = false }
            )
        }
    }
}

@Composable
private fun ProfileCover(
    modifier: Modifier = Modifier,
    photos: List<String>,
    pagerCount: Int,
    onPhotoClick: (Int) -> Unit
) {
    Box(modifier = modifier) {
        val pagerState = rememberPagerState(pageCount = { pagerCount })
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            if (photos.isNotEmpty()) {
                SubcomposeAsyncImage(
                    model = photos[page],
                    contentDescription = "Profile photo ${page + 1}",
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { onPhotoClick(page) },
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.surface,
                                    MaterialTheme.colorScheme.background
                                )
                            )
                        )
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.08f),
                            Color.Black.copy(alpha = 0.34f)
                        )
                    )
                )
        )

        if (photos.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                repeat(photos.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .size(if (isSelected) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) Color.White
                                else Color.White.copy(alpha = 0.48f)
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileSummaryCard(
    profile: UserProfile?,
    photoSize: Dp,
    onPhotoLongPress: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier
                        .size(photoSize)
                        .combinedClickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {},
                            onLongClick = onPhotoLongPress
                        ),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHighest
                ) {
                    ProfileImage(
                        url = profile?.profileImageUrl,
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = profile?.name ?: "Traveller",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (profile?.isVerified == true) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Verified",
                                tint = VerifiedCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Text(
                        text = if (profile?.username != null) "@${profile.username}" else "GoTogether profile",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (profile?.bio?.isNotBlank() == true) {
                Text(
                    text = profile.bio,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ProfileContent(
    profile: UserProfile?,
    onEdit: () -> Unit,
    onSignOut: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        // Info Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ProfileInfoItem(
                    Icons.Default.VerifiedUser,
                    "Account type",
                    if (profile?.isVerified == true) "Verified member" else "Standard member"
                )
                if (profile?.email?.isNotBlank() == true) ProfileInfoItem(
                    Icons.Default.Email,
                    "Email",
                    profile.email
                )
                if (profile?.address?.isNotBlank() == true) ProfileInfoItem(
                    Icons.Default.LocationOn,
                    "Location",
                    profile.address
                )
            }
        }

        // Action Buttons
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = onEdit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text("Edit profile", style = MaterialTheme.typography.titleSmall)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                val context = LocalContext.current
                SecondaryActionButton(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Share,
                    label = "Share"
                ) {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "Connect with me on GoTogether! Download: https://play.google.com/store/apps/details?id=${context.packageName}"
                        )
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(intent, null))
                }
                SecondaryActionButton(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Star,
                    label = "Rate us"
                ) {
                    try {
                        context.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("market://details?id=${context.packageName}")
                            )
                        )
                    } catch (e: Exception) {
                        context.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("http://play.google.com/store/apps/details?id=${context.packageName}")
                            )
                        )
                    }
                }
            }
        }

        // Sign Out
        SecondaryActionButton(
            modifier = Modifier.fillMaxWidth(),
            icon = Icons.AutoMirrored.Filled.Logout,
            label = "Sign out",
            contentColor = MaterialTheme.colorScheme.error,
            onClick = onSignOut
        )
    }
}

@Composable
private fun ProfileTopBar(
    headerAlpha: Float,
    profileImage: String?,
    profileName: String?,
    onEdit: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        color = MaterialTheme.colorScheme.background.copy(alpha = headerAlpha),
        tonalElevation = if (headerAlpha > 0.9f) 4.dp else 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (headerAlpha > 0.8f) {
                    Surface(
                        modifier = Modifier.size(36.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHighest
                    ) {
                        ProfileImage(profileImage)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        profileName ?: "Profile",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            IconButton(
                onClick = onEdit,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(y = (-16).dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Edit profile", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun ProfileImage(
    url: String?,
    contentScale: ContentScale = ContentScale.Crop
) {
    if (url?.isNotBlank() == true) {
        AsyncImage(
            model = url,
            contentDescription = "Profile picture",
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape),
            contentScale = contentScale
        )
    } else {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                Icons.Default.Person,
                null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ProfileInfoItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHighest
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun SecondaryActionButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    contentColor: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = contentColor)
    ) {
        Icon(icon, null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.labelLarge)
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
        properties = DialogProperties(usePlatformDefaultWidth = false)
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
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current).data(photoUrls[page])
                        .crossfade(true).build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                    loading = {
                        Box(
                            Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 2.dp
                            )
                        }
                    },
                    error = {
                        Box(
                            Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Person,
                                null,
                                tint = Color.White,
                                modifier = Modifier.size(64.dp)
                            )
                        }
                    }
                )
            }
            // Dot indicator
            if (photoUrls.size > 1) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    repeat(photoUrls.size) { index ->
                        val isSelected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .size(if (isSelected) 8.dp else 6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) Color.White
                                    else Color.White.copy(alpha = 0.4f)
                                )
                        )
                    }
                }
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Close, "Close", tint = Color.White, modifier = Modifier.size(28.dp))
            }
        }
    }
}

@Composable
private fun ProfilePhotoPreviewDialog(
    photoUrl: String?,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.68f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.size(230.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                shadowElevation = 12.dp
            ) {
                ProfileImage(
                    url = photoUrl,
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, strokeWidth = 3.dp) }
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
        Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Profile unavailable",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            error,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        FilledTonalButton(onClick = onRetry) {
            Text("Try again")
        }
    }
}
