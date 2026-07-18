package com.quantax.gotogether.presentation.trip

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.quantax.gotogether.presentation.components.LocationSearchDialog
import java.util.Calendar
import java.util.TimeZone

/**
 * Allows only today and future dates to be picked; past dates are disabled.
 * DatePicker supplies each candidate day as UTC midnight, so we compare against
 * today's UTC midnight.
 */
@OptIn(ExperimentalMaterial3Api::class)
private object PresentOrFutureSelectableDates : SelectableDates {
    private val todayUtcMillis: Long = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis >= todayUtcMillis

    override fun isSelectableYear(year: Int): Boolean =
        year >= Calendar.getInstance().get(Calendar.YEAR)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTripScreen(
    onNavigateBack: () -> Unit,
    viewModel: CreateTripViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) onNavigateBack()
    }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> viewModel.onImageSelected(uri) }
    )

    var showingLocationPickerFor by remember { mutableStateOf<String?>(null) }
    var showingStartDatePicker by remember { mutableStateOf(false) }
    var showingEndDatePicker by remember { mutableStateOf(false) }

    val startDatePickerState = rememberDatePickerState(selectableDates = PresentOrFutureSelectableDates)
    // End picker is keyed on the chosen start date so it rebuilds — and re-greys days —
    // whenever the start date changes. Any day on or before the start date is disabled;
    // if no start date is picked yet, it falls back to blocking only past dates.
    val endDatePickerState = key(state.startDateMillis) {
        val startMillis = state.startDateMillis
        rememberDatePickerState(
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                    if (startMillis != null) utcTimeMillis > startMillis
                    else PresentOrFutureSelectableDates.isSelectableDate(utcTimeMillis)

                override fun isSelectableYear(year: Int): Boolean =
                    PresentOrFutureSelectableDates.isSelectableYear(year)
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Plan a trip",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            // ─── Photo Header ───────────────────────────────────────────
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                PhotoSection(
                    state.selectedImageUri,
                    onPickerLaunch = { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    onRemove = { viewModel.onImageSelected(null) }
                )
            }

            // ─── Destination Section ─────────────────────────────────────
            TripSection("Where to?") {
                LocationCard(state.startingLocation, "Starting from", Icons.Default.LocationOn) { showingLocationPickerFor = "start" }
                LocationCard(state.destination, "Going to", Icons.Default.LocationOn) { showingLocationPickerFor = "destination" }
            }

            // ─── Date Section ────────────────────────────────────────────
            TripSection("When?") {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DateCard(modifier = Modifier.weight(1f), date = state.startDate, label = "Starts", icon = Icons.Default.CalendarMonth) { showingStartDatePicker = true }
                    DateCard(modifier = Modifier.weight(1f), date = state.endDate, label = "Ends", icon = Icons.Default.CalendarMonth) { showingEndDatePicker = true }
                }
            }

            // ─── Details Section ─────────────────────────────────────────
            TripSection("Details") {
                TripField(
                    value = state.budgetRange,
                    onValueChange = viewModel::onBudgetRangeChange,
                    label = "Budget range",
                    placeholder = "e.g. ₹5k – ₹10k, Flexible",
                    icon = Icons.Default.Money,
                    imeAction = ImeAction.Next,
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                )

                GroupSizeSection(state.groupSize, viewModel::onGroupSizeChange)
            }

            // ─── Interests Section ───────────────────────────────────────
            TripSection("Vibe & interests") {
                InterestsSection(state.interests, viewModel::addInterest, viewModel::removeInterest)
            }

            // ─── Description Section ─────────────────────────────────────
            TripSection("Trip bio") {
                TripField(
                    value = state.description,
                    onValueChange = viewModel::onDescriptionChange,
                    label = "Tell them more",
                    placeholder = "What's the plan? Food, sightseeing, or just relaxing?",
                    singleLine = false,
                    minLines = 4
                )
            }

            // ─── Publish Button ──────────────────────────────────────────
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Button(
                    onClick = { viewModel.publishTrip() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !state.isLoading
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Publish trip", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }

        // ─── Dialogs ──────────────────────────────────────────────────
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
            TripDatePicker(
                state = startDatePickerState,
                onDismiss = { showingStartDatePicker = false },
                onConfirm = {
                    viewModel.onStartDateChange(startDatePickerState.selectedDateMillis)
                    showingStartDatePicker = false
                }
            )
        }

        if (showingEndDatePicker) {
            TripDatePicker(
                state = endDatePickerState,
                onDismiss = { showingEndDatePicker = false },
                onConfirm = {
                    viewModel.onEndDateChange(endDatePickerState.selectedDateMillis)
                    showingEndDatePicker = false
                }
            )
        }
    }
}

// ─── UI COMPONENTS ──────────────────────────────────────────────────────────

@Composable
private fun TripSection(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) { content() }
    }
}

@Composable
private fun LocationCard(value: String, label: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(40.dp), shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceContainerHighest) {
                Box(contentAlignment = Alignment.Center) { Icon(icon, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary) }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    if (value.isBlank()) "Tap to select" else value,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (value.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun DateCard(modifier: Modifier = Modifier, date: String, label: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(6.dp))
                Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                if (date.isBlank()) "Select" else date,
                style = MaterialTheme.typography.bodyLarge,
                color = if (date.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun TripField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: ImageVector? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    modifier: Modifier = Modifier
) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    var isFocused by remember { mutableStateOf(false) }
    // Keep the focused field fully above the keyboard. Re-requesting on every IME
    // inset change (the keyboard animates in over several frames) and on every text
    // change (the cursor moves to new lines) — a one-shot request on focus fires
    // mid-animation and under-scrolls, so lines past the first couple slip behind
    // the keyboard.
    val imeBottom = WindowInsets.ime.getBottom(LocalDensity.current)
    LaunchedEffect(isFocused, value, imeBottom) {
        if (isFocused) {
            bringIntoViewRequester.bringIntoView()
        }
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        leadingIcon = icon?.let { { Icon(it, contentDescription = null) } },
        singleLine = singleLine,
        minLines = minLines,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        keyboardActions = keyboardActions,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .bringIntoViewRequester(bringIntoViewRequester)
            .onFocusEvent { isFocused = it.isFocused }
    )
}

@Composable
private fun GroupSizeSection(groupSize: Int, onValueChange: (Int) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            FilledTonalIconButton(
                onClick = { if (groupSize > 0) onValueChange(groupSize - 1) },
                enabled = groupSize > 0,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(Icons.Default.Remove, "Decrease group size")
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    if (groupSize == 0) "Any size" else "$groupSize",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    if (groupSize == 1) "Traveler" else "Travelers",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            FilledTonalIconButton(
                onClick = { onValueChange(groupSize + 1) },
                modifier = Modifier.size(44.dp)
            ) {
                Icon(Icons.Default.Add, "Increase group size")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun InterestsSection(interests: List<String>, onAdd: (String) -> Unit, onRemove: (String) -> Unit) {
    Column {
        if (interests.isNotEmpty()) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                interests.forEach { interest ->
                    InputChip(
                        selected = false,
                        onClick = { onRemove(interest) },
                        label = { Text(interest) },
                        trailingIcon = {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Remove $interest",
                                modifier = Modifier.size(InputChipDefaults.IconSize)
                            )
                        }
                    )
                }
            }
        }

        var newInterest by remember { mutableStateOf("") }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = newInterest,
                onValueChange = { newInterest = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Add e.g. Hiking, Food") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    if (newInterest.isNotBlank()) { onAdd(newInterest.trim()); newInterest = "" }
                })
            )
            FilledTonalIconButton(
                onClick = { if (newInterest.isNotBlank()) { onAdd(newInterest.trim()); newInterest = "" } },
                enabled = newInterest.isNotBlank(),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add interest")
            }
        }
    }
}

@Composable
private fun PhotoSection(uri: android.net.Uri?, onPickerLaunch: () -> Unit, onRemove: () -> Unit) {
    Surface(
        onClick = onPickerLaunch,
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        if (uri != null) {
            Box {
                AsyncImage(model = uri, contentDescription = "Selected cover photo", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                Surface(
                    onClick = onRemove,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .size(32.dp),
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.5f)
                ) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Close, "Remove photo", tint = Color.White, modifier = Modifier.size(16.dp)) }
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(modifier = Modifier.size(56.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.AddPhotoAlternate, null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(24.dp)) }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("Add cover photo", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                Text("Make your trip stand out", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TripDatePicker(state: androidx.compose.material3.DatePickerState, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onConfirm) { Text("Confirm") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    ) {
        DatePicker(state = state, showModeToggle = false)
    }
}
