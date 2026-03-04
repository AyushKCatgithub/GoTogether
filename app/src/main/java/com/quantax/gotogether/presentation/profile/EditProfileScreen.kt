package com.quantax.gotogether.presentation.profile

import com.quantax.gotogether.ui.theme.*

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.quantax.gotogether.ui.theme.*
import com.quantax.gotogether.presentation.profile.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val profile = state.profile

    var name by remember(profile) { mutableStateOf(profile?.name ?: "") }
    var username by remember(profile) { mutableStateOf(profile?.username ?: "") }
    var email by remember(profile) { mutableStateOf(profile?.email ?: "") }
    var phone by remember(profile) { mutableStateOf(profile?.phone ?: "") }
    var address by remember(profile) { mutableStateOf(profile?.address ?: "") }
    var bio by remember(profile) { mutableStateOf(profile?.bio ?: "") }
    var profileImageUrl by remember(profile) { mutableStateOf(profile?.profileImageUrl ?: "") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let { profileImageUrl = it.toString() }
        }
    )

    // Navigate back on successful save
    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) {
            viewModel.resetSaveSuccess()
            onNavigateBack()
        }
    }

    // Compute live verification status
    val wouldBeVerified = name.isNotBlank() && email.isNotBlank() && phone.isNotBlank() && profileImageUrl.isNotBlank()
    val missingFields = buildList {
        if (profileImageUrl.isBlank()) add("Profile Photo")
        if (name.isBlank()) add("Name")
        if (email.isBlank()) add("Email")
        if (phone.isBlank()) add("Phone")
    }

    Scaffold(
        containerColor = GoTogetherTheme.colors.background,
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile", fontWeight = FontWeight.Bold, color = GoTogetherTheme.colors.textPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = GoTogetherTheme.colors.textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Profile Photo Picker
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(CircleShape)
                    .clickable {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                if (profileImageUrl.isNotBlank()) {
                    AsyncImage(
                        model = profileImageUrl,
                        contentDescription = "Profile Photo",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .border(3.dp, GoTogetherOrange, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(GoTogetherTheme.colors.surface, CircleShape)
                            .border(3.dp, GoTogetherOrange, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = "Add Photo",
                            tint = GoTogetherTheme.colors.textSecondary,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
                // Overlay edit badge
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(36.dp)
                        .background(GoTogetherOrange, CircleShape)
                        .border(2.dp, GoTogetherTheme.colors.background, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Change Photo",
                        tint = GoTogetherTheme.colors.textPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Text("Tap to change photo", color = GoTogetherTheme.colors.textSecondary, fontSize = 13.sp)

            // Verification Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (wouldBeVerified)
                        VerifiedCyan.copy(alpha = 0.08f) else WarningYellow.copy(alpha = 0.08f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        if (wouldBeVerified) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (wouldBeVerified) VerifiedCyan else WarningYellow,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            if (wouldBeVerified) "Profile will be verified ✓" else "Missing fields for verification",
                            color = if (wouldBeVerified) VerifiedCyan else WarningYellow,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        if (!wouldBeVerified) {
                            Text(
                                "Missing: ${missingFields.joinToString(", ")}",
                                color = GoTogetherTheme.colors.textSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Form Fields
            ProfileTextField(
                value = name,
                onValueChange = { name = it },
                label = "Full Name *",
                icon = Icons.Default.Person
            )

            ProfileTextField(
                value = username,
                onValueChange = { username = it },
                label = "Username",
                icon = Icons.Default.AlternateEmail
            )

            ProfileTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email *",
                icon = Icons.Default.Email
            )

            ProfileTextField(
                value = bio,
                onValueChange = { bio = it },
                label = "Bio / Description",
                icon = Icons.Default.Info,
                singleLine = false
            )

            ProfileTextField(
                value = phone,
                onValueChange = { phone = it },
                label = "Phone *",
                icon = Icons.Default.Phone
            )

            ProfileTextField(
                value = address,
                onValueChange = { address = it },
                label = "Address",
                icon = Icons.Default.LocationOn
            )

            // Error
            if (state.error != null) {
                Text(state.error!!, color = Color.Red, fontSize = 14.sp)
            }

            // Save Button
            Button(
                onClick = {
                    val updatedProfile = profile?.copy(
                        name = name,
                        username = username,
                        email = email,
                        phone = phone,
                        address = address,
                        bio = bio,
                        profileImageUrl = profileImageUrl
                    ) ?: com.quantax.gotogether.domain.model.UserProfile(
                        name = name,
                        username = username,
                        email = email,
                        phone = phone,
                        address = address,
                        bio = bio,
                        profileImageUrl = profileImageUrl
                    )
                    viewModel.saveProfile(updatedProfile)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoTogetherOrange),
                shape = RoundedCornerShape(14.dp),
                enabled = !state.isSaving && name.isNotBlank()
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(color = GoTogetherTheme.colors.textOnPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Text("Save Profile", color = GoTogetherTheme.colors.textOnPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = GoTogetherTheme.colors.textSecondary) },
        modifier = Modifier.fillMaxWidth(),
        leadingIcon = { Icon(icon, contentDescription = null, tint = GoTogetherTheme.colors.textSecondary) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = GoTogetherOrange,
            unfocusedBorderColor = GoTogetherTheme.colors.surface,
            focusedTextColor = GoTogetherTheme.colors.textPrimary,
            unfocusedTextColor = GoTogetherTheme.colors.textPrimary,
            cursorColor = GoTogetherOrange
        ),
        shape = RoundedCornerShape(12.dp),
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 3,
        maxLines = if (singleLine) 1 else 5
    )
}
