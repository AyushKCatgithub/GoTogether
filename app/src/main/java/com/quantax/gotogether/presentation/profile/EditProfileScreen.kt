package com.quantax.gotogether.presentation.profile

import android.util.Log
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.quantax.gotogether.ui.theme.GoTogetherOrange
import com.quantax.gotogether.ui.theme.GoTogetherTheme
import com.quantax.gotogether.ui.theme.VerifiedCyan
import com.quantax.gotogether.ui.theme.WarningYellow
import kotlin.math.log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val profile = state.profile

    // 🔥 Form State
    var name by remember(profile) { mutableStateOf(profile?.name ?: "") }
    var username by remember(profile) { mutableStateOf(profile?.username ?: "") }
    var email by remember(profile) { mutableStateOf(profile?.email ?: "") }
    var phone by remember(profile) { mutableStateOf(profile?.phone ?: "") }
    var address by remember(profile) { mutableStateOf(profile?.address ?: "") }
    var bio by remember(profile) { mutableStateOf(profile?.bio ?: "") }
    var profileImageUrl by remember(profile) { mutableStateOf(profile?.profileImageUrl ?: "") }
    
    // 🔥 Background Photos State (ensuring exactly 4 slots)
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
        containerColor = GoTogetherTheme.colors.background,
        topBar = {
            TopAppBar(
                title = { Text("EDIT PROFILE", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Surface(shape = CircleShape, color = GoTogetherTheme.colors.surface, modifier = Modifier.size(36.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                },
                actions = {
                    Text(
                        text = "SAVE",
                        color = if (canSave) GoTogetherOrange else GoTogetherTheme.colors.textSecondary.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .clickable(enabled = canSave) {
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
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GoTogetherTheme.colors.background)
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

            ProfileSection("PERSONAL DETAILS") {
                PremiumTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Full Name",
                    placeholder = "Your travel name",
                    imeAction = ImeAction.Next,
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                )
                PremiumTextField(
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

            ProfileSection("CONTACT INFO") {
                PremiumTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email Address",
                    placeholder = "hello@world.com",
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                )
                PremiumTextField(
                    value = phone,
                    onValueChange = { phone = it.filter { c -> c.isDigit() || c == '+' } },
                    label = "Phone Number",
                    placeholder = "+91 999 000 1122",
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next,
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                )
            }

            ProfileSection("ABOUT ME") {
                PremiumTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = "Bio",
                    placeholder = "Life's short, travel far...",
                    singleLine = false,
                    imeAction = ImeAction.Default
                )
                PremiumTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = "Location",
                    placeholder = "City, Country",
                    imeAction = ImeAction.Done,
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                )
            }

            ProfileSection("VERIFICATION PHOTOS") {
                Text(
                    "Upload at least 3 photos of your's to earn your verified badge and build trust.",
                    style = MaterialTheme.typography.bodySmall,
                    color = GoTogetherTheme.colors.textSecondary,
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
                CircularProgressIndicator(color = GoTogetherOrange, modifier = Modifier.padding(vertical = 24.dp))
            }
        }
    }
}

@Composable
private fun EditableAvatar(imageUrl: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.BottomEnd) {
            Surface(
                modifier = Modifier.size(110.dp).clickable(onClick = onClick),
                shape = CircleShape,
                color = GoTogetherTheme.colors.surface,
                shadowElevation = 4.dp
            ) {
                if (imageUrl.isNotBlank()) {
                    AsyncImage(model = imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(32.dp), tint = GoTogetherTheme.colors.textSecondary.copy(alpha = 0.5f))
                    }
                }
            }
            Surface(modifier = Modifier.size(32.dp), shape = CircleShape, color = GoTogetherOrange, shadowElevation = 2.dp) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }
        Text("Tap to change avatar", style = MaterialTheme.typography.labelMedium, color = GoTogetherTheme.colors.textSecondary, modifier = Modifier.padding(top = 12.dp))
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
                    .background(GoTogetherTheme.colors.surface)
                    .clickable { onPick(index) },
                contentAlignment = Alignment.Center
            ) {
                if (url.isNotBlank()) {
                    AsyncImage(model = url, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Icon(Icons.Default.Add, null, tint = GoTogetherTheme.colors.textSecondary.copy(alpha = 0.3f), modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}

@Composable
private fun VerificationStatusCard(isVerified: Boolean, photosNeeded: Int) {
    Log.i("EditProfileScreen.kt","isVerified -> $isVerified");
    Log.i("EditProfileScreen.kt", "photosNeeded -> $photosNeeded");
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = (if (isVerified) VerifiedCyan else WarningYellow).copy(alpha = 0.1f)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(if (isVerified) Icons.Default.CheckCircle else Icons.Default.Warning, null, tint = if (isVerified) VerifiedCyan else WarningYellow)
            Column {
                Text(if (isVerified) "Ready for Verification" else "Complete your profile", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = if (isVerified) VerifiedCyan else WarningYellow)
                Text(if (isVerified) "You have enough photos to be verified." else "Add ${photosNeeded.coerceAtLeast(1)} more photos to qualify.", style = MaterialTheme.typography.bodySmall, color = (if (isVerified) VerifiedCyan else WarningYellow).copy(alpha = 0.8f))
            }
        }
    }
}

@Composable
private fun ProfileSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)) {
        Text(text = title, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = GoTogetherTheme.colors.textSecondary, letterSpacing = 1.sp, modifier = Modifier.padding(start = 4.dp, bottom = 12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) { content() }
    }
}

@Composable
private fun PremiumTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    singleLine: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = GoTogetherTheme.colors.textPrimary.copy(alpha = 0.6f), modifier = Modifier.padding(start = 4.dp, bottom = 6.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = GoTogetherTheme.colors.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, GoTogetherTheme.colors.textSecondary.copy(alpha = 0.05f))
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = GoTogetherTheme.colors.textPrimary),
                cursorBrush = SolidColor(GoTogetherOrange),
                singleLine = singleLine,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
                keyboardActions = keyboardActions,
                decorationBox = { innerTextField ->
                    Box {
                        if (value.isEmpty()) Text(placeholder, style = MaterialTheme.typography.bodyLarge, color = GoTogetherTheme.colors.textSecondary.copy(alpha = 0.4f))
                        innerTextField()
                    }
                }
            )
        }
    }
}
