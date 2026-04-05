package com.quantax.gotogether.presentation.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.quantax.gotogether.ui.theme.GoTogetherOrange
import com.quantax.gotogether.ui.theme.GoTogetherTheme
import com.quantax.gotogether.ui.theme.VerifiedCyan
import com.quantax.gotogether.ui.theme.WarningYellow

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
    var backgroundPhotoUrls by remember(profile) {
        mutableStateOf(
            (profile?.verificationPhotoUrls?.filter { it.isNotBlank() } ?: emptyList())
                .let { list -> list + List((4 - list.size).coerceAtLeast(0)) { "" } }
                .take(4)
        )
    }
    var pickerForBackgroundSlot by remember { mutableStateOf<Int?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let { u ->
                val slot = pickerForBackgroundSlot
                if (slot == null) {
                    profileImageUrl = u.toString()
                } else {
                    backgroundPhotoUrls =
                        backgroundPhotoUrls.toMutableList().apply { set(slot, u.toString()) }
                    pickerForBackgroundSlot = null
                }
            }
        }
    )

    val pickMultiplePhotosLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(4),
        onResult = { uris ->
            if (uris.isNotEmpty()) {
                val newList = uris.take(4).map { it.toString() } +
                        List((4 - uris.size).coerceAtLeast(0)) { "" }
                backgroundPhotoUrls = newList.take(4)
            }
        }
    )

    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) {
            viewModel.onEvent(ProfileEvent.ResetSaveSuccess)
            onNavigateBack()
        }
    }

    // Verification: min 3 of 4 background photos
    val backgroundCount = backgroundPhotoUrls.count { it.isNotBlank() }
    val wouldBeVerified = backgroundCount >= 3
    val missingFields = buildList {
        if (backgroundCount < 3) add("Background photos (min 3 of 4)")
        if (name.isBlank()) add("Name")
        if (email.isBlank()) add("Email")
        if (phone.isBlank()) add("Phone")
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
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
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
                            pickerForBackgroundSlot = null
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
                        contentDescription = "Change Photo",
                        tint = GoTogetherTheme.colors.textPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Text(
                "Tap to change photo",
                color = GoTogetherTheme.colors.textSecondary,
                fontSize = 13.sp
            )

            // Background photos (up to 4) for profile header and verification
            Text(
                "Background photos (up to 4)",
                color = GoTogetherTheme.colors.textPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                "Add at least 3 to get verified. Shown on your profile header.",
                color = GoTogetherTheme.colors.textSecondary,
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedButton(
                onClick = {
                    pickMultiplePhotosLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = GoTogetherOrange)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Select up to 4 photos at once")
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                repeat(4) { index ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(80.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(GoTogetherTheme.colors.surface)
                            .border(
                                1.dp,
                                GoTogetherTheme.colors.textSecondary.copy(alpha = 0.3f),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                pickerForBackgroundSlot = index
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (backgroundPhotoUrls.getOrNull(index).orEmpty().isNotBlank()) {
                            AsyncImage(
                                model = backgroundPhotoUrls[index],
                                contentDescription = "Background ${index + 1}",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            IconButton(
                                onClick = {
                                    backgroundPhotoUrls =
                                        backgroundPhotoUrls.toMutableList().apply { set(index, "") }
                                },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                )
                            }
                        } else {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Add photo",
                                tint = GoTogetherTheme.colors.textSecondary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }

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
                    onValueChange = {
                        username = it
                        if (state.error != null) viewModel.onEvent(ProfileEvent.ClearError)
                    },
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
                    onValueChange = { phone = it.filter { char -> char.isDigit() || char == '+' } },
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
                    val verificationUrls = backgroundPhotoUrls.filter { it.isNotBlank() }
                    val updatedProfile = profile?.copy(
                        name = name,
                        username = username,
                        email = email,
                        phone = phone,
                        address = address,
                        bio = bio,
                        profileImageUrl = profileImageUrl,
                        verificationPhotoUrls = verificationUrls
                    ) ?: com.quantax.gotogether.domain.model.UserProfile(
                        name = name,
                        username = username,
                        email = email,
                        phone = phone,
                        address = address,
                        bio = bio,
                        profileImageUrl = profileImageUrl,
                        verificationPhotoUrls = verificationUrls
                    )
                    viewModel.onEvent(
                        ProfileEvent.SaveProfile(
                            updatedProfile
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                enabled = !state.isSaving && name.isNotBlank()
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
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
            modifier = Modifier
                .fillMaxWidth()
                .height(if (singleLine) 60.dp else 120.dp),
            placeholder = {
                Text(
                    placeholder,
                    color = GoTogetherTheme.colors.textSecondary.copy(alpha = 0.5f)
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GoTogetherOrange,
                unfocusedBorderColor = androidx.compose.ui.graphics.Color.LightGray.copy(alpha = 0.3f),
                focusedContainerColor = GoTogetherTheme.colors.surface,
                unfocusedContainerColor = GoTogetherTheme.colors.surface,
                cursorColor = GoTogetherTheme.colors.textPrimary
            ),
            shape = RoundedCornerShape(16.dp),
            singleLine = singleLine,
            minLines = if (singleLine) 1 else 3
        )
    }
}


