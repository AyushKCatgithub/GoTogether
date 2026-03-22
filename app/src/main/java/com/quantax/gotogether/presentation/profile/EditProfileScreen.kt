package com.quantax.gotogether.presentation.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.quantax.gotogether.ui.theme.GoTogetherOrange
import com.quantax.gotogether.ui.theme.GoTogetherTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
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

    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) {
            viewModel.onEvent(ProfileEvent.ResetSaveSuccess)
            onNavigateBack()
        }
    }

    Scaffold(
        containerColor = GoTogetherTheme.colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "SETTINGS",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Bold Photo Picker
            Box(contentAlignment = Alignment.BottomEnd) {
                Surface(
                    modifier = Modifier
                        .size(120.dp)
                        .clickable {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    shape = CircleShape,
                    border = androidx.compose.foundation.BorderStroke(4.dp, GoTogetherOrange),
                    color = GoTogetherTheme.colors.surface
                ) {
                    if (profileImageUrl.isNotBlank()) {
                        AsyncImage(
                            model = profileImageUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.padding(32.dp),
                            tint = Color.LightGray
                        )
                    }
                }
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = CircleShape,
                    color = Color.Black
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.padding(8.dp),
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Minimalist Form
            Column(
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                GenZTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "NAME",
                    placeholder = "Your legal name"
                )
                GenZTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = "USERNAME",
                    placeholder = "vibe_checker"
                )
                GenZTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "EMAIL",
                    placeholder = "hello@travel.com"
                )
                GenZTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = "PHONE",
                    placeholder = "+1 234 ..."
                )
                GenZTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = "BIO",
                    placeholder = "Tell your story...",
                    singleLine = false
                )
                GenZTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = "LOCATION",
                    placeholder = "Earth"
                )
            }

            if (state.error != null) {
                Text(
                    state.error!!,
                    color = Color.Red,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Bold Save Button
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
                    viewModel.onEvent(ProfileEvent.SaveProfile(updatedProfile))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                enabled = !state.isSaving && name.isNotBlank()
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        "SAVE CHANGES",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun GenZTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    singleLine: Boolean = true
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Black,
            color = GoTogetherTheme.colors.textPrimary,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = Color.LightGray) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GoTogetherOrange,
                unfocusedBorderColor = Color.LightGray.copy(alpha = 0.3f),
                focusedContainerColor = GoTogetherTheme.colors.surface,
                unfocusedContainerColor = GoTogetherTheme.colors.surface,
                cursorColor = Color.Black
            ),
            shape = RoundedCornerShape(16.dp),
            singleLine = singleLine,
            minLines = if (singleLine) 1 else 3
        )
    }
}


