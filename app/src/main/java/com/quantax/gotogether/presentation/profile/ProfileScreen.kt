package com.quantax.gotogether.presentation.profile

import com.quantax.gotogether.ui.theme.*

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.quantax.gotogether.ui.theme.*
import com.quantax.gotogether.presentation.profile.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToEditProfile: () -> Unit,
    onSignOut: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    
    // Reload profile each time screen is displayed
    LaunchedEffect(Unit) {
        viewModel.loadProfile()
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
                actions = {
                    IconButton(onClick = onNavigateToEditProfile) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = GoTogetherOrange)
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
                            onClick = { viewModel.loadProfile() },
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
                    // Gradient header — starts from absolute top edge behind TopAppBar
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
                            .padding(bottom = 100.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Profile avatar area (transparent — gradient shows through)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                        // Profile Image
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

                    // Info Cards
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ProfileInfoCard(
                            icon = Icons.Default.Email,
                            label = "Email",
                            value = profile?.email ?: ""
                        )
                        ProfileInfoCard(
                            icon = Icons.Default.Phone,
                            label = "Phone",
                            value = profile?.phone ?: ""
                        )
                        ProfileInfoCard(
                            icon = Icons.Default.LocationOn,
                            label = "Address",
                            value = profile?.address ?: ""
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Edit Profile Button
                    Button(
                        onClick = onNavigateToEditProfile,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .height(60.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GoTogetherOrange),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Edit Profile", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Sign Out Button
                    OutlinedButton(
                        onClick = {
                            viewModel.signOut()
                            onSignOut()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .height(60.dp),
                        shape = RoundedCornerShape(24.dp),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                            brush = androidx.compose.ui.graphics.SolidColor(Color.Red.copy(alpha = 0.4f))
                        )
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = null,
                            tint = Color.Red.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Sign Out",
                            color = Color.Red.copy(alpha = 0.7f),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
                } // end Column
            } // end outer Box
        }
    }
}

@Composable
private fun ProfileInfoCard(
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
