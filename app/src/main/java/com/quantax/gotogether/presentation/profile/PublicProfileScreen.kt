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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
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

    val scrollState = rememberScrollState()
    val bannerHeight = 320.dp
    val photoSize = 110.dp
    
    // 🔥 Optimized: Derived state for fade effects
    val headerAlpha by remember {
        derivedStateOf {
            val scrollValue = scrollState.value.toFloat()
            val progress = (scrollValue / (bannerHeight.value * 2.5f)).coerceIn(0f, 1f)
            (progress * 2f).coerceIn(0f, 1f)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(GoTogetherTheme.colors.background)) {
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
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize().clickable {
                                    fullScreenInitialIndex = page
                                    showFullScreenViewer = true
                                },
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxSize().background(
                                Brush.verticalGradient(listOf(GoTogetherOrange.copy(alpha = 0.2f), GoTogetherTheme.colors.background))
                            ))
                        }
                    }
                    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.4f)))))
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
                        color = GoTogetherTheme.colors.background,
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
                                color = GoTogetherTheme.colors.surface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, GoTogetherTheme.colors.textSecondary.copy(alpha = 0.05f))
                            ) {
                                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    ProfileInfoItem(Icons.Default.LocationOn, "From", profile?.address?.ifBlank { "Unspecified" } ?: "Unspecified")
                                    ProfileInfoItem(Icons.Default.CheckCircle, "Status", if (profile?.isVerified == true) "Verified Member" else "Exploring")
                                }
                            }

                            Spacer(modifier = Modifier.height(32.dp))

                            // Primary Action
                            Button(
                                onClick = { /* Future: Message feature */ },
                                modifier = Modifier.fillMaxWidth().height(60.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GoTogetherOrange),
                                shape = RoundedCornerShape(20.dp),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                            ) {
                                Text("Say Hello", color = Color.White, fontWeight = FontWeight.Bold)
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
                        color = GoTogetherTheme.colors.background,
                        shadowElevation = 8.dp,
                        border = androidx.compose.foundation.BorderStroke(4.dp, GoTogetherTheme.colors.background)
                    ) {
                        ProfileImage(profile?.profileImageUrl)
                    }
                }

                // ─── 4. Top Bar with Back ────────────────────────────────────
                Surface(
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    color = GoTogetherTheme.colors.background.copy(alpha = headerAlpha),
                    tonalElevation = if (headerAlpha > 0.9f) 4.dp else 0.dp
                ) {
                    Box(modifier = Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 8.dp), contentAlignment = Alignment.CenterStart) {
                        IconButton(onClick = onNavigateBack) {
                            Surface(
                                modifier = Modifier.size(36.dp), 
                                shape = CircleShape, 
                                color = if (headerAlpha > 0.2f) GoTogetherTheme.colors.surface else Color.Black.copy(alpha = 0.2f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowBack, 
                                        null, 
                                        modifier = Modifier.size(18.dp), 
                                        tint = GoTogetherTheme.colors.textPrimary.copy(alpha = (headerAlpha + 0.4f).coerceIn(0.4f, 1f))
                                    )
                                }
                            }
                        }
                        
                        if (headerAlpha > 0.8f) {
                            Row(modifier = Modifier.align(Alignment.Center), verticalAlignment = Alignment.CenterVertically) {
                                Surface(modifier = Modifier.size(32.dp), shape = CircleShape, color = GoTogetherTheme.colors.surface) {
                                    ProfileImage(profile?.profileImageUrl)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(profile?.name ?: "Profile", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
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
private fun PublicProfileHeadline(profile: com.quantax.gotogether.domain.model.UserProfile?) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = profile?.name ?: "Traveller",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = GoTogetherTheme.colors.textPrimary
            )
            if (profile?.isVerified == true) {
                Spacer(modifier = Modifier.width(6.dp))
                Icon(Icons.Default.CheckCircle, null, tint = VerifiedCyan, modifier = Modifier.size(20.dp))
            }
        }
        Text(
            text = if (profile?.username != null) "@${profile.username}" else "Exploring the world",
            style = MaterialTheme.typography.bodyMedium,
            color = GoTogetherTheme.colors.textSecondary
        )
        if (profile?.bio?.isNotBlank() == true) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = profile.bio,
                style = MaterialTheme.typography.bodyLarge,
                color = GoTogetherTheme.colors.textPrimary.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ProfileImage(url: String?) {
    if (url?.isNotBlank() == true) {
        AsyncImage(model = url, contentDescription = null, modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
    } else {
        Box(contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Person, null, modifier = Modifier.size(48.dp), tint = GoTogetherTheme.colors.textSecondary.copy(alpha = 0.3f))
        }
    }
}

@Composable
private fun ProfileInfoItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Surface(modifier = Modifier.size(36.dp), shape = RoundedCornerShape(10.dp), color = GoTogetherTheme.colors.background) {
            Box(contentAlignment = Alignment.Center) { Icon(icon, null, modifier = Modifier.size(16.dp), tint = GoTogetherOrange) }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = GoTogetherTheme.colors.textSecondary)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = GoTogetherTheme.colors.textPrimary)
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
                    loading = { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = GoTogetherOrange, strokeWidth = 2.dp) } },
                    error = { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(64.dp)) } }
                )
            }
            IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)) {
                Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(28.dp))
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = GoTogetherOrange, strokeWidth = 3.dp) }
}

@Composable
private fun ErrorState(error: String, onRetry: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Default.Warning, null, tint = WarningYellow, modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text("Profile Unavailable", color = GoTogetherTheme.colors.textPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(error, textAlign = TextAlign.Center, color = GoTogetherTheme.colors.textSecondary, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = GoTogetherOrange), shape = RoundedCornerShape(16.dp), modifier = Modifier.height(54.dp)) {
            Text("Try Again", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}
