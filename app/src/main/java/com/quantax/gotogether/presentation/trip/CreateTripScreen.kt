package com.quantax.gotogether.presentation.trip

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.quantax.gotogether.presentation.components.LocationSearchDialog
import com.quantax.gotogether.ui.theme.GoTogetherOrange
import com.quantax.gotogether.ui.theme.GoTogetherTheme
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTripScreen(
    onNavigateBack: () -> Unit,
    viewModel: CreateTripViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            onNavigateBack()
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let { 
            snackbarHostState.showSnackbar(it)
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> viewModel.onImageSelected(uri) }
    )

    var showingLocationPickerFor by remember { mutableStateOf<String?>(null) }
    var showingStartDatePicker by remember { mutableStateOf(false) }
    var showingEndDatePicker by remember { mutableStateOf(false) }

    val startDatePickerState = rememberDatePickerState()
    val endDatePickerState = rememberDatePickerState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create New Trip", fontWeight = FontWeight.Bold, color = GoTogetherTheme.colors.textPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = GoTogetherTheme.colors.textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = GoTogetherTheme.colors.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text("Trip Details", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = GoTogetherTheme.colors.textPrimary)

            // Starting Location
            LocationField(state.startingLocation, "Starting Location") { showingLocationPickerFor = "start" }

            // Destination
            LocationField(state.destination, "Destination") { showingLocationPickerFor = "destination" }

            // Travel Dates
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    DateField(state.startDate, "Start Date") { showingStartDatePicker = true }
                }
                Box(modifier = Modifier.weight(1f)) {
                    DateField(state.endDate, "End Date") { showingEndDatePicker = true }
                }
            }

            // Budget Range
            OutlinedTextField(
                value = state.budgetRange,
                onValueChange = viewModel::onBudgetRangeChange,
                label = { Text("Budget Range (e.g., ₹₹₹, Flexible)", color = GoTogetherTheme.colors.textSecondary) },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Money, contentDescription = null, tint = GoTogetherTheme.colors.textSecondary) },
                colors = textFieldColors(),
                shape = RoundedCornerShape(20.dp)
            )

            // Group Size
            GroupSizeSection(state.groupSize, viewModel::onGroupSizeChange)

            // Interests
            InterestsSection(state.interests, viewModel::addInterest, viewModel::removeInterest)

            // Trip Description
            OutlinedTextField(
                value = state.description,
                onValueChange = viewModel::onDescriptionChange,
                label = { Text("Trip Description", color = GoTogetherTheme.colors.textSecondary) },
                modifier = Modifier.fillMaxWidth().height(150.dp),
                colors = textFieldColors(),
                shape = RoundedCornerShape(20.dp),
                maxLines = 5
            )

            // Photos
            PhotoSection(state.selectedImageUri, 
                onPickerLaunch = { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                onRemove = { viewModel.onImageSelected(null) }
            )

            // Publish Button
            Button(
                onClick = { viewModel.publishTrip() },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoTogetherOrange),
                shape = RoundedCornerShape(24.dp),
                enabled = !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(color = GoTogetherTheme.colors.textOnPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Text("Publish Trip", color = GoTogetherTheme.colors.textOnPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Dialogs
        if (showingLocationPickerFor != null) {
            LocationSearchDialog(
                onDismissRequest = { showingLocationPickerFor = null },
                onLocationSelected = { location ->
                    if (showingLocationPickerFor == "start") viewModel.onStartingLocationChange(location)
                    else viewModel.onDestinationChange(location)
                    showingLocationPickerFor = null
                }
            )
        }

        if (showingStartDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showingStartDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.onStartDateChange(startDatePickerState.selectedDateMillis)
                        showingStartDatePicker = false
                    }) { Text("Confirm") }
                },
                dismissButton = { TextButton(onClick = { showingStartDatePicker = false }) { Text("Cancel") } }
            ) {
                DatePicker(
                    state = startDatePickerState,
                    showModeToggle = false
                )
            }
        }

        if (showingEndDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showingEndDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.onEndDateChange(endDatePickerState.selectedDateMillis)
                        showingEndDatePicker = false
                    }) { Text("Confirm") }
                },
                dismissButton = { TextButton(onClick = { showingEndDatePicker = false }) { Text("Cancel") } }
            ) {
                DatePicker(
                    state = endDatePickerState,
                    showModeToggle = false
                )
            }
        }
    }
}

@Composable
fun LocationField(value: String, placeholder: String, onClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = { },
            readOnly = true,
            enabled = false,
            placeholder = { Text(placeholder, color = GoTogetherTheme.colors.textSecondary) },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = GoTogetherTheme.colors.textSecondary) },
            colors = disabledTextFieldColors(),
            shape = RoundedCornerShape(20.dp)
        )
        Box(modifier = Modifier.matchParentSize().clip(RoundedCornerShape(20.dp)).clickable(onClick = onClick))
    }
}

@Composable
fun DateField(value: String, placeholder: String, onClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = { },
            readOnly = true,
            enabled = false,
            placeholder = { Text(placeholder, color = GoTogetherTheme.colors.textSecondary) },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null, tint = GoTogetherTheme.colors.textSecondary) },
            colors = disabledTextFieldColors(),
            shape = RoundedCornerShape(20.dp)
        )
        Box(modifier = Modifier.matchParentSize().clip(RoundedCornerShape(20.dp)).clickable(onClick = onClick))
    }
}

@Composable
fun GroupSizeSection(groupSize: Int, onValueChange: (Int) -> Unit) {
    Column {
        Text("Group Size", color = GoTogetherTheme.colors.textPrimary, modifier = Modifier.padding(bottom = 8.dp))
        Row(
            modifier = Modifier.fillMaxWidth().background(GoTogetherTheme.colors.card, RoundedCornerShape(20.dp)).padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onValueChange(groupSize - 1) }) {
                Text("-", fontSize = 24.sp, color = GoTogetherTheme.colors.textPrimary)
            }
            Text(groupSize.toString(), fontSize = 18.sp, color = GoTogetherTheme.colors.textPrimary)
            IconButton(onClick = { onValueChange(groupSize + 1) }) {
                Text("+", fontSize = 24.sp, color = GoTogetherTheme.colors.textPrimary)
            }
        }
    }
}

@Composable
fun InterestsSection(interests: List<String>, onAdd: (String) -> Unit, onRemove: (String) -> Unit) {
    Column {
        Text("Interests", color = GoTogetherTheme.colors.textPrimary, modifier = Modifier.padding(bottom = 8.dp))
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            interests.forEach { interest ->
                AssistChip(
                    onClick = { onRemove(interest) },
                    label = { Text(interest, color = GoTogetherTheme.colors.textPrimary) },
                    trailingIcon = { Icon(Icons.Default.Close, "Remove", modifier = Modifier.size(16.dp), tint = GoTogetherTheme.colors.textPrimary) },
                    colors = AssistChipDefaults.assistChipColors(containerColor = GoTogetherTheme.colors.surface),
                    border = null
                )
            }
        }
        var newInterest by remember { mutableStateOf("") }
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = newInterest,
                onValueChange = { newInterest = it },
                modifier = Modifier.weight(1f).height(56.dp),
                placeholder = { Text("Add Interest", color = GoTogetherTheme.colors.textSecondary) },
                colors = textFieldColors(),
                shape = RoundedCornerShape(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = { if(newInterest.isNotBlank()) { onAdd(newInterest); newInterest = "" } },
                modifier = Modifier.background(GoTogetherTheme.colors.surface, RoundedCornerShape(12.dp)).size(56.dp)
            ) { Icon(Icons.Default.Add, contentDescription = "Add", tint = GoTogetherOrange) }
        }
    }
}

@Composable
fun PhotoSection(uri: android.net.Uri?, onPickerLaunch: () -> Unit, onRemove: () -> Unit) {
    Column {
        Text("Destination Photos", color = GoTogetherTheme.colors.textPrimary, modifier = Modifier.padding(bottom = 8.dp))
        if (uri != null) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(12.dp)).clickable(onClick = onPickerLaunch)) {
                AsyncImage(model = uri, contentDescription = "Selected photo", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                IconButton(onClick = onRemove, modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(50)).size(32.dp)) {
                    Icon(Icons.Default.Close, "Remove photo", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxWidth().height(120.dp).background(GoTogetherTheme.colors.card, RoundedCornerShape(20.dp)).clickable(onClick = onPickerLaunch), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CameraAlt, "Camera", tint = GoTogetherTheme.colors.textSecondary, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Tap to upload destination photo", color = GoTogetherTheme.colors.textSecondary, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = GoTogetherOrange,
    unfocusedBorderColor = Color.Transparent,
    focusedTextColor = GoTogetherTheme.colors.textPrimary,
    unfocusedTextColor = GoTogetherTheme.colors.textPrimary,
    unfocusedContainerColor = GoTogetherTheme.colors.card,
    focusedContainerColor = GoTogetherTheme.colors.card,
    cursorColor = GoTogetherOrange
)

@Composable
private fun disabledTextFieldColors() = OutlinedTextFieldDefaults.colors(
    disabledBorderColor = Color.Transparent,
    disabledTextColor = GoTogetherTheme.colors.textPrimary,
    disabledContainerColor = GoTogetherTheme.colors.card,
    disabledPlaceholderColor = GoTogetherTheme.colors.textSecondary,
    disabledLeadingIconColor = GoTogetherTheme.colors.textSecondary
)
