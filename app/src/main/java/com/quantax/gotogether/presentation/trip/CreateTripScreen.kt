package com.quantax.gotogether.presentation.trip

import com.quantax.gotogether.domain.model.*

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Money
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import com.quantax.gotogether.presentation.trip.CreateTripViewModel
import com.quantax.gotogether.ui.theme.GoTogetherTheme
import com.quantax.gotogether.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTripScreen(
    onNavigateBack: () -> Unit,
    viewModel: CreateTripViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    
    // Handle Navigation on Success
    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            onNavigateBack()
        }
    }

    // Photo Picker Launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> viewModel.onImageSelected(uri) }
    )

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
            Text(
                "Trip Details", 
                fontSize = 20.sp, 
                fontWeight = FontWeight.Bold,
                color = GoTogetherTheme.colors.textPrimary
            )

            // Starting Location
            OutlinedTextField(
                value = state.startingLocation,
                onValueChange = viewModel::onStartingLocationChange,
                label = { Text("Starting Location", color = GoTogetherTheme.colors.textSecondary) },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = GoTogetherTheme.colors.textSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoTogetherOrange,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = GoTogetherTheme.colors.textPrimary,
                    unfocusedTextColor = GoTogetherTheme.colors.textPrimary,
                    unfocusedContainerColor = GoTogetherTheme.colors.card,
                    focusedContainerColor = GoTogetherTheme.colors.card,
                    cursorColor = GoTogetherOrange
                ),
                shape = RoundedCornerShape(20.dp)
            )

            // Destination
            OutlinedTextField(
                value = state.destination,
                onValueChange = viewModel::onDestinationChange,
                label = { Text("Destination", color = GoTogetherTheme.colors.textSecondary) },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = GoTogetherTheme.colors.textSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoTogetherOrange,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = GoTogetherTheme.colors.textPrimary,
                    unfocusedTextColor = GoTogetherTheme.colors.textPrimary,
                    unfocusedContainerColor = GoTogetherTheme.colors.card,
                    focusedContainerColor = GoTogetherTheme.colors.card,
                    cursorColor = GoTogetherOrange
                ),
                shape = RoundedCornerShape(20.dp)
            )

            // Travel Dates
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = state.startDate,
                    onValueChange = viewModel::onStartDateChange,
                    label = {  Text("Start Date", color = GoTogetherTheme.colors.textSecondary) },
                    modifier = Modifier.weight(1f),
                    leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null, tint = GoTogetherTheme.colors.textSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoTogetherOrange,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = GoTogetherTheme.colors.textPrimary,
                        unfocusedTextColor = GoTogetherTheme.colors.textPrimary,
                        unfocusedContainerColor = GoTogetherTheme.colors.card,
                        focusedContainerColor = GoTogetherTheme.colors.card,
                        cursorColor = GoTogetherOrange
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                OutlinedTextField(
                    value = state.endDate,
                    onValueChange = viewModel::onEndDateChange,
                    label = { Text("End Date", color = GoTogetherTheme.colors.textSecondary) },
                    modifier = Modifier.weight(1f),
                    leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null, tint = GoTogetherTheme.colors.textSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoTogetherOrange,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = GoTogetherTheme.colors.textPrimary,
                        unfocusedTextColor = GoTogetherTheme.colors.textPrimary,
                        unfocusedContainerColor = GoTogetherTheme.colors.card,
                        focusedContainerColor = GoTogetherTheme.colors.card,
                        cursorColor = GoTogetherOrange
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
            }

            // Budget Range
            OutlinedTextField(
                value = state.budgetRange,
                onValueChange = viewModel::onBudgetRangeChange,
                label = { Text("Budget Range (e.g., ₹₹₹, Flexible)", color = GoTogetherTheme.colors.textSecondary) },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Money, contentDescription = null, tint = GoTogetherTheme.colors.textSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoTogetherOrange,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = GoTogetherTheme.colors.textPrimary,
                    unfocusedTextColor = GoTogetherTheme.colors.textPrimary,
                    unfocusedContainerColor = GoTogetherTheme.colors.card,
                    focusedContainerColor = GoTogetherTheme.colors.card,
                    cursorColor = GoTogetherOrange
                ),
                shape = RoundedCornerShape(20.dp)
            )

            // Group Size
            Column {
                Text("Group Size", color = GoTogetherTheme.colors.textPrimary, modifier = Modifier.padding(bottom = 8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(GoTogetherTheme.colors.card, RoundedCornerShape(20.dp))
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.onGroupSizeChange(state.groupSize - 1) }) {
                        Text("-", fontSize = 24.sp, color = GoTogetherTheme.colors.textPrimary)
                    }
                    Text(state.groupSize.toString(), fontSize = 18.sp, color = GoTogetherTheme.colors.textPrimary)
                    IconButton(onClick = { viewModel.onGroupSizeChange(state.groupSize + 1) }) {
                        Text("+", fontSize = 24.sp, color = GoTogetherTheme.colors.textPrimary)
                    }
                }
            }

            // Interests/Trip Type
            Column {
                Text("Interests", color = GoTogetherTheme.colors.textPrimary, modifier = Modifier.padding(bottom = 8.dp))
                // Note: Using a Row wrapped in horizontalScroll for simplicity over experimental FlowRow here
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.interests.forEach { interest ->
                        AssistChip(
                            onClick = { viewModel.removeInterest(interest) },
                            label = { Text(interest, color = GoTogetherTheme.colors.textPrimary) },
                            trailingIcon = { Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(16.dp), tint = GoTogetherTheme.colors.textPrimary) },
                            colors = AssistChipDefaults.assistChipColors(containerColor = GoTogetherTheme.colors.surface),
                            border = AssistChipDefaults.assistChipBorder(enabled = true, borderColor = Color.Transparent)
                        )
                    }
                }
                
                var newInterest by remember { mutableStateOf("") }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newInterest,
                        onValueChange = { newInterest = it },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        placeholder = { Text("Add Interest", color = GoTogetherTheme.colors.textSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoTogetherOrange,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = GoTogetherTheme.colors.textPrimary,
                            unfocusedTextColor = GoTogetherTheme.colors.textPrimary,
                            unfocusedContainerColor = GoTogetherTheme.colors.card,
                            focusedContainerColor = GoTogetherTheme.colors.card,
                            cursorColor = GoTogetherOrange
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { 
                            if(newInterest.isNotBlank()) {
                                viewModel.addInterest(newInterest)
                                newInterest = ""
                            }
                        },
                        modifier = Modifier
                            .background(GoTogetherTheme.colors.surface, RoundedCornerShape(12.dp))
                            .size(56.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = GoTogetherOrange)
                    }
                }
            }

            // Trip Description
            OutlinedTextField(
                value = state.description,
                onValueChange = viewModel::onDescriptionChange,
                label = { Text("Trip Description", color = GoTogetherTheme.colors.textSecondary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoTogetherOrange,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = GoTogetherTheme.colors.textPrimary,
                    unfocusedTextColor = GoTogetherTheme.colors.textPrimary,
                    unfocusedContainerColor = GoTogetherTheme.colors.card,
                    focusedContainerColor = GoTogetherTheme.colors.card,
                    cursorColor = GoTogetherOrange
                ),
                shape = RoundedCornerShape(20.dp),
                maxLines = 5
            )

            // Destination Photos Placeholder
            Column {
                Text("Destination Photos", color = GoTogetherTheme.colors.textPrimary, modifier = Modifier.padding(bottom = 8.dp))
                if (state.selectedImageUri != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                    ) {
                        AsyncImage(
                            model = state.selectedImageUri,
                            contentDescription = "Selected destination photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { viewModel.onImageSelected(null) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(50))
                                .size(32.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Remove photo", tint = GoTogetherTheme.colors.textPrimary, modifier = Modifier.size(16.dp))
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .background(GoTogetherTheme.colors.card, RoundedCornerShape(20.dp))
                            .clickable { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CameraAlt, contentDescription = "Camera", tint = GoTogetherTheme.colors.textSecondary, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Tap to upload destination photos", color = GoTogetherTheme.colors.textSecondary, fontSize = 14.sp)
                        }
                    }
                }
            }

            if (state.error != null) {
                Text(state.error!!, color = Color.Red, fontSize = 14.sp)
            }

            // Publish Button
            Button(
                onClick = { viewModel.publishTrip() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 4.dp)
                    .height(60.dp),
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
    }
}
