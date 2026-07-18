package com.quantax.gotogether.presentation.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.quantax.gotogether.ui.theme.VerifiedCyan

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val profile = state.profile

    // Form State
    var name by remember(profile) { mutableStateOf(profile?.name ?: "") }
    var username by remember(profile) { mutableStateOf(profile?.username ?: "") }
    var email by remember(profile) { mutableStateOf(profile?.email ?: "") }
    var phone by remember(profile) { mutableStateOf(profile?.phone ?: "") }
    var address by remember(profile) { mutableStateOf(profile?.address ?: "") }
    var bio by remember(profile) { mutableStateOf(profile?.bio ?: "") }
    var profileImageUrl by remember(profile) { mutableStateOf(profile?.profileImageUrl ?: "") }

    // Background Photos State (ensuring exactly 4 slots)
    var backgroundPhotoUrls by remember(profile) {
        mutableStateOf(
            (profile?.verificationPhotoUrls?.filter { it.isNotBlank() }.orEmpty() + List(4) { "" }).take(4)
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
                    backgroundPhotoUrls = backgroundPhotoUrls.toMutableList().apply { set(slot, u.toString()) }
                }
            }
            pickerForBackgroundSlot = null
        }
    )

    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) {
            viewModel.onEvent(ProfileEvent.ResetSaveSuccess)
            onNavigateBack()
        }
    }

    val backgroundCount by remember { derivedStateOf { backgroundPhotoUrls.count { it.isNotBlank() } } }
    val isVerified = backgroundCount >= 3
    val canSave = name.isNotBlank() && !state.isSaving

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Edit profile", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        enabled = canSave,
                        onClick = {
                            val updatedProfile = (profile ?: com.quantax.gotogether.domain.model.UserProfile()).copy(
                                name = name,
                                username = username,
                                email = email,
                                phone = phone,
                                address = address,
                                bio = bio,
                                profileImageUrl = profileImageUrl,
                                verificationPhotoUrls = backgroundPhotoUrls.filter { it.isNotBlank() }
                            )
                            viewModel.onEvent(ProfileEvent.SaveProfile(updatedProfile))
                        }
                    ) {
                        Text("Save", style = MaterialTheme.typography.labelLarge)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
                .navigationBarsPadding()
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Avatar Section
            EditableAvatar(profileImageUrl) {
                pickerForBackgroundSlot = null
                photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Form Sections
            val focusManager = LocalFocusManager.current

            ProfileSection("Personal details") {
                ProfileTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Full name",
                    placeholder = "Your travel name",
                    imeAction = ImeAction.Next,
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                )
                ProfileTextField(
                    value = username,
                    onValueChange = {
                        username = it
                        if (state.error != null) viewModel.onEvent(ProfileEvent.ClearError)
                    },
                    label = "Username",
                    placeholder = "@wanderer",
                    imeAction = ImeAction.Next,
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                )
            }

            ProfileSection("Contact info") {
                ProfileTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email address",
                    placeholder = "hello@world.com",
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                )
                ProfileTextField(
                    value = phone,
                    onValueChange = { phone = it.filter { c -> c.isDigit() || c == '+' } },
                    label = "Phone number",
                    placeholder = "+91 999 000 1122",
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next,
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                )
            }

            ProfileSection("About me") {
                ProfileTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = "Bio",
                    placeholder = "Life's short, travel far…",
                    singleLine = false,
                    minLines = 3,
                    imeAction = ImeAction.Default
                )
                ProfileTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = "Location",
                    placeholder = "City, Country",
                    imeAction = ImeAction.Done,
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                )
            }

            ProfileSection("Verification photos") {
                Text(
                    "Upload at least 3 photos to earn your verified badge and build trust.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                VerificationPhotoGrid(backgroundPhotoUrls) { index ->
                    pickerForBackgroundSlot = index
                    photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }

                Spacer(modifier = Modifier.height(16.dp))

                VerificationStatusCard(isVerified, 3 - backgroundCount)
            }

            state.error?.let { error ->
                Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(16.dp))
            }

            if (state.isSaving) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(vertical = 24.dp))
            }
        }
    }
}

@Composable
private fun EditableAvatar(imageUrl: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.BottomEnd) {
            Surface(
                modifier = Modifier
                    .size(110.dp)
                    .clickable(onClick = onClick),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHighest
            ) {
                if (imageUrl.isNotBlank()) {
                    AsyncImage(model = imageUrl, contentDescription = "Profile picture", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Surface(modifier = Modifier.size(32.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primary) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp))
                }
            }
        }
        Text("Tap to change avatar", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 12.dp))
    }
}

@Composable
private fun VerificationPhotoGrid(urls: List<String>, onPick: (Int) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(4) { index ->
            val url = urls.getOrNull(index).orEmpty()
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(90.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .clickable { onPick(index) },
                contentAlignment = Alignment.Center
            ) {
                if (url.isNotBlank()) {
                    AsyncImage(model = url, contentDescription = "Verification photo ${index + 1}", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Icon(Icons.Default.Add, contentDescription = "Add photo", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}

@Composable
private fun VerificationStatusCard(isVerified: Boolean, photosNeeded: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(
                if (isVerified) Icons.Default.CheckCircle else Icons.Default.Warning,
                null,
                tint = if (isVerified) VerifiedCyan else MaterialTheme.colorScheme.primary
            )
            Column {
                Text(
                    if (isVerified) "Ready for verification" else "Complete your profile",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    if (isVerified) "You have enough photos to be verified." else "Add ${photosNeeded.coerceAtLeast(1)} more photos to qualify.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ProfileSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) { content() }
    }
}

@Composable
private fun ProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    singleLine: Boolean = true,
    minLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        singleLine = singleLine,
        minLines = minLines,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        keyboardActions = keyboardActions,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    )
}
