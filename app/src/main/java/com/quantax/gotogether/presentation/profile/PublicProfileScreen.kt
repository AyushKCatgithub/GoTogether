package com.quantax.gotogether.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.quantax.gotogether.domain.model.UserProfile
import com.quantax.gotogether.ui.theme.VerifiedCyan

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

    val scrollState = rememberScrollState()
    val bannerHeight = 320.dp
    val photoSize = 110.dp

    // Derived state for fade effects
    val headerAlpha by remember {
        derivedStateOf {
            val scrollValue = scrollState.value.toFloat()
            val progress = (scrollValue / (bannerHeight.value * 2.5f)).coerceIn(0f, 1f)
            (progress * 2f).coerceIn(0f, 1f)
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
                ErrorState(error = error) { viewModel.loadProfile(userId) }
            }
            else -> {
                val profile = state.profile
                val backgroundPhotos = remember(profile?.backgroundPhotoUrls) {
                    profile?.backgroundPhotoUrls?.filter { it.isNotBlank() }.orEmpty()
                }
                val pagerCount = backgroundPhotos.size.coerceAtLeast(1)

                // ─── 1. Background Layer (Parallax Header) ──────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(bannerHeight)
                        .graphicsLayer {
                            translationY = -scrollState.value.toFloat() * 0.5f
                            alpha = (1f - (scrollState.value.toFloat() / (bannerHeight.toPx() * 0.8f))).coerceIn(0f, 1f)
                        }
                ) {
                    val pagerState = rememberPagerState(pageCount = { pagerCount })
                    HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                        if (backgroundPhotos.isNotEmpty()) {
                            SubcomposeAsyncImage(
                                model = backgroundPhotos[page],
                                contentDescription = "Photo ${page + 1}",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable {
                                        fullScreenInitialIndex = page
                                        showFullScreenViewer = true
                                    },
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
                            .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.4f))))
                    )
                }

                // ─── 2. Scrollable Content ───────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .navigationBarsPadding()
                ) {
                    Spacer(modifier = Modifier.height(bannerHeight))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.background,
                        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Spacer(modifier = Modifier.height(photoSize / 2 + 16.dp))

                            // Name & Verification
                            PublicProfileHeadline(profile)

                            Spacer(modifier = Modifier.height(28.dp))

                            // Info Section Card
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerLow
                            ) {
                                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    ProfileInfoItem(Icons.Default.LocationOn, "From", profile?.address?.ifBlank { "Unspecified" } ?: "Unspecified")
                                    ProfileInfoItem(Icons.Default.CheckCircle, "Status", if (profile?.isVerified == true) "Verified member" else "Exploring")
                                }
                            }

                            Spacer(modifier = Modifier.height(32.dp))

                            // Primary Action
                            Button(
                                onClick = { /* Future: Message feature */ },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text("Say hello", style = MaterialTheme.typography.titleMedium)
                            }

                            Spacer(modifier = Modifier.height(100.dp))
                        }
                    }
                }

                // ─── 3. Floating Avatar ──────────────────────────────────────
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = bannerHeight - photoSize / 2)
                        .graphicsLayer {
                            val scrollValue = scrollState.value.toFloat()
                            val scale = (1f - ((scrollValue / bannerHeight.toPx()) * 0.4f)).coerceIn(0.6f, 1f)
                            scaleX = scale
                            scaleY = scale
                            translationY = (-scrollValue * 0.4f).coerceIn(-(bannerHeight.toPx() / 4), 0f)
                        }
                ) {
                    Surface(
                        modifier = Modifier.size(photoSize),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        shadowElevation = 8.dp,
                        border = androidx.compose.foundation.BorderStroke(4.dp, MaterialTheme.colorScheme.background)
                    ) {
                        ProfileImage(profile?.profileImageUrl)
                    }
                }

                // ─── 4. Top Bar with Back ────────────────────────────────────
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
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        IconButton(onClick = onNavigateBack) {
                            Surface(
                                modifier = Modifier.size(36.dp),
                                shape = CircleShape,
                                color = if (headerAlpha > 0.2f) MaterialTheme.colorScheme.surfaceContainerHighest else Color.Black.copy(alpha = 0.3f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        modifier = Modifier.size(18.dp),
                                        tint = if (headerAlpha > 0.2f) MaterialTheme.colorScheme.onSurface else Color.White
                                    )
                                }
                            }
                        }

                        if (headerAlpha > 0.8f) {
                            Row(modifier = Modifier.align(Alignment.Center), verticalAlignment = Alignment.CenterVertically) {
                                Surface(modifier = Modifier.size(32.dp), shape = CircleShape, color = MaterialTheme.colorScheme.surfaceContainerHighest) {
                                    ProfileImage(profile?.profileImageUrl)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(profile?.name ?: "Profile", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }
        }

        if (showFullScreenViewer) {
            val photos = state.profile?.backgroundPhotoUrls?.filter { it.isNotBlank() }.orEmpty()
            if (photos.isNotEmpty()) {
                FullScreenImageViewer(photoUrls = photos, initialIndex = fullScreenInitialIndex, onDismiss = { showFullScreenViewer = false })
            }
        }
    }
}

@Composable
private fun PublicProfileHeadline(profile: UserProfile?) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = profile?.name ?: "Traveller",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (profile?.isVerified == true) {
                Spacer(modifier = Modifier.width(6.dp))
                Icon(Icons.Default.CheckCircle, "Verified", tint = VerifiedCyan, modifier = Modifier.size(20.dp))
            }
        }
        Text(
            text = if (profile?.username != null) "@${profile.username}" else "Exploring the world",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (profile?.bio?.isNotBlank() == true) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = profile.bio,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ProfileImage(url: String?) {
    if (url?.isNotBlank() == true) {
        AsyncImage(model = url, contentDescription = "Profile picture", modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
    } else {
        Box(contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Person, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ProfileInfoItem(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Surface(modifier = Modifier.size(40.dp), shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceContainerHighest) {
            Box(contentAlignment = Alignment.Center) { Icon(icon, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary) }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun FullScreenImageViewer(photoUrls: List<String>, initialIndex: Int, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.95f))) {
            val pagerState = rememberPagerState(pageCount = { photoUrls.size }, initialPage = initialIndex.coerceIn(0, (photoUrls.size - 1).coerceAtLeast(0)))
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current).data(photoUrls[page]).crossfade(true).build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                    loading = { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, strokeWidth = 2.dp) } },
                    error = { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(64.dp)) } }
                )
            }
            IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)) {
                Icon(Icons.Default.Close, "Close", tint = Color.White, modifier = Modifier.size(28.dp))
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, strokeWidth = 3.dp) }
}

@Composable
private fun ErrorState(error: String, onRetry: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text("Profile unavailable", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(6.dp))
        Text(error, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(24.dp))
        FilledTonalButton(onClick = onRetry) {
            Text("Try again")
        }
    }
}
