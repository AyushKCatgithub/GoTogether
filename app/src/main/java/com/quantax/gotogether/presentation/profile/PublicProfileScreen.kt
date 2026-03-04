package com.quantax.gotogether.presentation.profile

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicProfileScreen(
    userId: String,
    onNavigateBack: () -> Unit,
    viewModel: PublicProfileViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(userId) {
        viewModel.loadProfile(userId)
    }

    Scaffold(
        containerColor = GoTogetherTheme.colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Profile",
                        fontWeight = FontWeight.Bold,
                        color = GoTogetherTheme.colors.textPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = GoTogetherTheme.colors.textPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GoTogetherOrange, strokeWidth = 3.dp)
                }
            }
            state.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.error!!, color = GoTogetherTheme.colors.textSecondary, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadProfile(userId) },
                            colors = ButtonDefaults.buttonColors(containerColor = GoTogetherOrange),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Retry", color = GoTogetherTheme.colors.textOnPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            else -> {
                val profile = state.profile
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Gradient header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        GoTogetherOrange.copy(alpha = 0.18f),
                                        GoTogetherTheme.colors.background
                                    )
                                )
                            )
                    )

                    // Scrollable content
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .verticalScroll(rememberScrollState())
                            .windowInsetsPadding(WindowInsets.navigationBars)
                            .padding(bottom = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Profile avatar area
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
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
                                        .size(120.dp)
                                        .clip(CircleShape)
                                        .border(3.dp, GoTogetherOrange, CircleShape),
                                    contentScale = ContentScale.Crop,
                                    loading = {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(GoTogetherTheme.colors.surface, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(
                                                color = GoTogetherOrange,
                                                strokeWidth = 2.dp,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    },
                                    error = {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(GoTogetherTheme.colors.surface, CircleShape)
                                                .border(3.dp, GoTogetherOrange, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.Person,
                                                contentDescription = "No Photo",
                                                tint = GoTogetherTheme.colors.textSecondary,
                                                modifier = Modifier.size(48.dp)
                                            )
                                        }
                                    }
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(120.dp)
                                        .clip(CircleShape)
                                        .background(GoTogetherTheme.colors.surface)
                                        .border(3.dp, GoTogetherOrange, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = "No Photo",
                                        tint = GoTogetherTheme.colors.textSecondary,
                                        modifier = Modifier.size(48.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Name
                        Text(
                            text = if (profile?.name?.isNotBlank() == true) profile.name else "No name set",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoTogetherTheme.colors.textPrimary
                        )

                        // Username
                        if (profile?.username?.isNotBlank() == true) {
                            Text(
                                text = "@${profile.username}",
                                fontSize = 15.sp,
                                color = GoTogetherTheme.colors.textSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Verification Badge
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

                        // Bio
                        if (profile?.bio?.isNotBlank() == true) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = profile.bio,
                                fontSize = 14.sp,
                                color = GoTogetherTheme.colors.textSecondary,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        // Info Cards — only non-sensitive info
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            PublicProfileInfoCard(
                                icon = Icons.Default.LocationOn,
                                label = "Address",
                                value = profile?.address ?: ""
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
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
                Icon(icon, contentDescription = null, tint = GoTogetherOrange, modifier = Modifier.size(20.dp))
            }
            Column {
                Text(label, color = GoTogetherTheme.colors.textSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = value.ifBlank { "Not provided" },
                    color = if (value.isNotBlank()) GoTogetherTheme.colors.textPrimary else GoTogetherTheme.colors.textSecondary.copy(alpha = 0.5f),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
