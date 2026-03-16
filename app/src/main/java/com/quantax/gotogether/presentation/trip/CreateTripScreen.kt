package com.quantax.gotogether.presentation.trip

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.quantax.gotogether.presentation.components.LocationSearchDialog
import com.quantax.gotogether.ui.theme.GoTogetherOrange
import com.quantax.gotogether.ui.theme.GoTogetherOrangeLight
import com.quantax.gotogether.ui.theme.GoTogetherTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTripScreen(
    onNavigateBack: () -> Unit,
    viewModel: CreateTripViewModel = hiltViewModel()
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = GoTogetherTheme.colors.textPrimary)
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
        Text("Group Size", color = GoTogetherTheme.colors.textSecondary, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp), fontSize = 14.sp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(GoTogetherTheme.colors.card, RoundedCornerShape(20.dp))
                .border(1.dp, GoTogetherTheme.colors.surfaceVariant, RoundedCornerShape(20.dp))
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { if (groupSize > 0) onValueChange(groupSize - 1) },
                modifier = Modifier
                    .size(48.dp)
                    .background(if (groupSize > 0) GoTogetherOrange.copy(alpha = 0.1f) else GoTogetherTheme.colors.surface, RoundedCornerShape(16.dp))
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = if (groupSize > 0) GoTogetherOrange else GoTogetherTheme.colors.textSecondary.copy(alpha=0.5f))
            }
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    if (groupSize == 0) "Any Size" else "$groupSize", 
                    fontSize = 20.sp, 
                    fontWeight = FontWeight.Bold,
                    color = GoTogetherTheme.colors.textPrimary
                )
                if (groupSize > 0) {
                    Text("Travelers", fontSize = 11.sp, color = GoTogetherTheme.colors.textSecondary)
                }
            }

            IconButton(
                onClick = { onValueChange(groupSize + 1) },
                modifier = Modifier
                    .size(48.dp)
                    .background(GoTogetherOrange.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            ) {
                Icon(Icons.Default.Add, contentDescription = "Increase", tint = GoTogetherOrange)
            }
        }
    }
}

@Composable
fun InterestsSection(interests: List<String>, onAdd: (String) -> Unit, onRemove: (String) -> Unit) {
    Column {
        Text("Interests", color = GoTogetherTheme.colors.textSecondary, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp), fontSize = 14.sp)
        
        if(interests.isNotEmpty()) {
            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                interests.forEach { interest ->
                    Box(
                        modifier = Modifier
                            .background(GoTogetherOrange.copy(alpha = 0.15f), RoundedCornerShape(50))
                            .border(1.dp, GoTogetherOrange.copy(alpha = 0.5f), RoundedCornerShape(50))
                            .clickable { onRemove(interest) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(interest, color = GoTogetherOrangeLight, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Default.Close, "Remove", tint = GoTogetherOrangeLight, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }
        
        var newInterest by remember { mutableStateOf("") }
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = newInterest,
                onValueChange = { newInterest = it },
                modifier = Modifier.weight(1f).height(60.dp),
                placeholder = { Text("e.g. Hiking, Food, Museums", color = GoTogetherTheme.colors.textSecondary) },
                colors = textFieldColors(),
                shape = RoundedCornerShape(20.dp),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(12.dp))
            IconButton(
                onClick = { if(newInterest.isNotBlank()) { onAdd(newInterest.trim()); newInterest = "" } },
                modifier = Modifier
                    .size(60.dp)
                    .background(
                        if (newInterest.isNotBlank()) GoTogetherOrange else GoTogetherTheme.colors.surfaceVariant,
                        RoundedCornerShape(20.dp)
                    )
            ) {
                Icon(
                    Icons.Default.Add, 
                    contentDescription = "Add", 
                    tint = if (newInterest.isNotBlank()) Color.White else GoTogetherTheme.colors.textSecondary
                )
            }
        }
    }
}

@Composable
fun PhotoSection(uri: android.net.Uri?, onPickerLaunch: () -> Unit, onRemove: () -> Unit) {
    Column {
        Text("Destination Cover Photo", color = GoTogetherTheme.colors.textSecondary, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp), fontSize = 14.sp)
        if (uri != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .clickable(onClick = onPickerLaunch)
            ) {
                AsyncImage(model = uri, contentDescription = "Selected photo", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                
                // Remove Button
                IconButton(
                    onClick = onRemove, 
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        .size(36.dp)
                ) {
                    Icon(Icons.Default.Close, "Remove photo", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(GoTogetherTheme.colors.card, RoundedCornerShape(24.dp))
                    .border(2.dp, GoTogetherTheme.colors.surfaceVariant, RoundedCornerShape(24.dp))
                    .clickable(onClick = onPickerLaunch), 
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(GoTogetherTheme.colors.surfaceVariant, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, "Camera", tint = GoTogetherOrange, modifier = Modifier.size(32.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Tap to upload cover photo", color = GoTogetherTheme.colors.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("High quality images make trips stand out", color = GoTogetherTheme.colors.textSecondary, fontSize = 13.sp)
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
