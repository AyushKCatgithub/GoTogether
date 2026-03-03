package com.quantax.gotogether.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.quantax.gotogether.domain.model.Trip
import com.quantax.gotogether.presentation.home.HomeViewModel
import com.quantax.gotogether.presentation.components.LocationSearchDialog
import com.quantax.gotogether.ui.theme.GoTogetherTheme
import com.quantax.gotogether.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToCreateTrip: () -> Unit,
    onNavigateToTripDetail: (String) -> Unit = {},
    onNavigateToMessages: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val searchFrom by viewModel.searchFrom.collectAsState()
    val searchTo by viewModel.searchTo.collectAsState()
    val searchBudget by viewModel.searchBudget.collectAsState()
    val searchGroupSize by viewModel.searchGroupSize.collectAsState()
    
    val appliedFrom by viewModel.appliedFrom.collectAsState()
    val appliedTo by viewModel.appliedTo.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    
    var showFilterSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    // Pickers State
    var showingLocationPickerFor by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true || 
                      permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            viewModel.fetchLocation(context)
        } else {
            viewModel.setLocationError("Location permission denied")
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadTrips()
        permissionLauncher.launch(
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    Scaffold(
        containerColor = GoTogetherTheme.colors.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreateTrip,
                containerColor = GoTogetherOrange,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(end = 8.dp, bottom = 68.dp)
                    .shadow(8.dp, RoundedCornerShape(16.dp), ambientColor = GoTogetherOrange.copy(alpha = 0.4f), spotColor = GoTogetherOrange.copy(alpha = 0.6f))
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Trip")
            }
        },
        topBar = {
            TopAppBar(
                title = { 
                    if (state is HomeState.Success) {
                        val locationText = (state as HomeState.Success).locationText
                        
                        val titleText = if (appliedFrom.isNotBlank() && appliedTo.isNotBlank()) {
                            "$appliedFrom --> $appliedTo"
                        } else if (appliedFrom.isNotBlank()) {
                            "$appliedFrom --> Anywhere"
                        } else if (appliedTo.isNotBlank()) {
                            "Anywhere --> $appliedTo"
                        } else {
                            locationText
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = "Location", tint = GoTogetherOrange, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(titleText, color = GoTogetherTheme.colors.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                },
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .padding(12.dp)
                            .size(40.dp)
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        GoTogetherOrange,
                                        GoTogetherOrangeLight
                                    )
                                ),
                                CircleShape
                            )
                            .clickable { viewModel.loadTrips() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.Explore, 
                            contentDescription = "Logo", 
                            tint = GoTogetherTheme.colors.textPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToMessages) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.White.copy(alpha = 0.05f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.ChatBubbleOutline, 
                                contentDescription = "Messages", 
                                tint = GoTogetherTheme.colors.textPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
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
        ) {
            // Search Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = viewModel::onSearchQueryChange,
                    placeholder = { Text("Search trips, destinations...", color = GoTogetherTheme.colors.textSecondary) },
                    leadingIcon = { Icon(Icons.Outlined.Search, "Search", tint = GoTogetherTheme.colors.textSecondary) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoTogetherOrange,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = GoTogetherTheme.colors.textPrimary,
                        unfocusedTextColor = GoTogetherTheme.colors.textPrimary,
                        unfocusedContainerColor = GoTogetherTheme.colors.card,
                        focusedContainerColor = GoTogetherTheme.colors.card
                    ),
                    shape = RoundedCornerShape(20.dp),
                    singleLine = true
                )
                
                IconButton(
                    onClick = { showFilterSheet = true },
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            Brush.linearGradient(listOf(GoTogetherTheme.colors.card, GoTogetherTheme.colors.surfaceVariant)),
                            RoundedCornerShape(20.dp)
                        )
                ) {
                    Icon(Icons.Outlined.Tune, "Filter", tint = if (showFilterSheet) Color.White else GoTogetherOrange)
                }
            }
            
            if (showingLocationPickerFor != null) {
                LocationSearchDialog(
                    onDismissRequest = { showingLocationPickerFor = null },
                    onLocationSelected = { selectedLocation ->
                        if (showingLocationPickerFor == "from") {
                            viewModel.onSearchFromChange(selectedLocation)
                        } else if (showingLocationPickerFor == "to") {
                            viewModel.onSearchToChange(selectedLocation)
                        }
                        showingLocationPickerFor = null
                    }
                )
            }
            
            if (showFilterSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showFilterSheet = false },
                    sheetState = sheetState,
                    containerColor = GoTogetherTheme.colors.background,
                    dragHandle = { BottomSheetDefaults.DragHandle() }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Text("Filters", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = GoTogetherTheme.colors.textPrimary)
                        
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = searchFrom,
                                onValueChange = { },
                                readOnly = true,
                                enabled = false,
                                placeholder = { Text("Leaving from...", color = GoTogetherTheme.colors.textSecondary) },
                                leadingIcon = { Icon(Icons.Default.LocationOn, "From", tint = GoTogetherTheme.colors.textSecondary) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledBorderColor = Color.Transparent,
                                    disabledTextColor = GoTogetherTheme.colors.textPrimary,
                                    disabledContainerColor = GoTogetherTheme.colors.card,
                                    disabledPlaceholderColor = GoTogetherTheme.colors.textSecondary,
                                    disabledLeadingIconColor = GoTogetherTheme.colors.textSecondary
                                ),
                                shape = RoundedCornerShape(20.dp),
                                singleLine = true
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable { showingLocationPickerFor = "from" }
                            )
                        }
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = searchTo,
                                onValueChange = { },
                                readOnly = true,
                                enabled = false,
                                placeholder = { Text("Going to...", color = GoTogetherTheme.colors.textSecondary) },
                                leadingIcon = { Icon(Icons.Default.LocationOn, "To", tint = GoTogetherTheme.colors.textSecondary) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledBorderColor = Color.Transparent,
                                    disabledTextColor = GoTogetherTheme.colors.textPrimary,
                                    disabledContainerColor = GoTogetherTheme.colors.card,
                                    disabledPlaceholderColor = GoTogetherTheme.colors.textSecondary,
                                    disabledLeadingIconColor = GoTogetherTheme.colors.textSecondary
                                ),
                                shape = RoundedCornerShape(20.dp),
                                singleLine = true
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable { showingLocationPickerFor = "to" }
                            )
                        }
                        
                        OutlinedTextField(
                            value = searchBudget,
                            onValueChange = viewModel::onSearchBudgetChange,
                            placeholder = { Text("Max Budget (e.g., 5000₹₹₹)", color = GoTogetherTheme.colors.textSecondary) },
                            leadingIcon = { Icon(Icons.Default.AttachMoney, "Budget", tint = GoTogetherTheme.colors.textSecondary) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoTogetherOrange,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = GoTogetherTheme.colors.textPrimary,
                                unfocusedTextColor = GoTogetherTheme.colors.textPrimary,
                                unfocusedContainerColor = GoTogetherTheme.colors.card,
                                focusedContainerColor = GoTogetherTheme.colors.card
                            ),
                            shape = RoundedCornerShape(20.dp),
                            singleLine = true
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Groups, contentDescription = "Group", tint = GoTogetherTheme.colors.textSecondary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (searchGroupSize == 0) "Max Group Size (Any)" else "Max Group Size (${searchGroupSize})", color = GoTogetherTheme.colors.textPrimary, fontSize = 16.sp)
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { if (searchGroupSize > 0) viewModel.onSearchGroupSizeChange(searchGroupSize - 1) }) {
                                    Icon(Icons.Default.RemoveCircleOutline, "Decrease", tint = if (searchGroupSize > 0) GoTogetherOrange else GoTogetherTheme.colors.textSecondary, modifier = Modifier.size(32.dp))
                                }
                                Text("$searchGroupSize", color = GoTogetherTheme.colors.textPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                                IconButton(onClick = { viewModel.onSearchGroupSizeChange(searchGroupSize + 1) }) {
                                    Icon(Icons.Default.AddCircleOutline, "Increase", tint = GoTogetherOrange, modifier = Modifier.size(32.dp))
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedButton(
                                onClick = { 
                                    viewModel.clearFilters()
                                    showFilterSheet = false
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(60.dp),
                                shape = RoundedCornerShape(24.dp),
                                border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(GoTogetherOrange.copy(alpha = 0.5f))
                                )
                            ) {
                                Text("Clear", color = GoTogetherOrange, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = {
                                    viewModel.applyFilters()
                                    showFilterSheet = false
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(60.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GoTogetherOrange),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Text("Apply Filters", color = GoTogetherTheme.colors.textOnPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }

            // Feed Content
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { viewModel.loadTrips() },
                modifier = Modifier.weight(1f)
            ) {
                when (val currentState = state) {
                    is HomeState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = GoTogetherOrange, strokeWidth = 3.dp)
                        }
                    }
                    is HomeState.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(currentState.message, color = GoTogetherTheme.colors.textSecondary, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { viewModel.loadTrips() },
                                    colors = ButtonDefaults.buttonColors(containerColor = GoTogetherOrange),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Retry", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    is HomeState.Success -> {
                        if (currentState.trips.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Outlined.Explore,
                                        contentDescription = null,
                                        tint = GoTogetherTheme.colors.textSecondary.copy(alpha = 0.4f),
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("No trips found", color = GoTogetherTheme.colors.textPrimary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                                    Text("Create one to get started!", color = GoTogetherTheme.colors.textSecondary, fontSize = 14.sp)
                                }
                            }
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 100.dp),
                                verticalArrangement = Arrangement.spacedBy(24.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(currentState.trips) { trip ->
                                    TripCard(
                                        trip = trip,
                                        onClick = { onNavigateToTripDetail(trip.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TripCard(
    trip: Trip,
    showBookmark: Boolean = true,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .shadow(
                12.dp,
                RoundedCornerShape(32.dp),
                ambientColor = Color.Black.copy(alpha = 0.5f),
                spotColor = Color.Black.copy(alpha = 0.5f)
            ),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = GoTogetherTheme.colors.card)
    ) {
        Column {
            // Hero Image with gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            ) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(trip.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Destination Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    loading = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(GoTogetherTheme.colors.surface),
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
                                .background(
                                    Brush.linearGradient(
                                        listOf(GoTogetherTheme.colors.surface, GoTogetherTheme.colors.surfaceVariant)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.Explore,
                                contentDescription = null,
                                tint = GoTogetherTheme.colors.textSecondary.copy(alpha = 0.3f),
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                )
                
                // Bottom gradient overlay for text readability
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                            )
                        )
                )

                // Destination title on image
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(
                        text = trip.title,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${trip.startDate} - ${trip.endDate}",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
                
                // Host Badge
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopStart)
                        .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(50))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(trip.hostImageUrl ?: "https://ui-avatars.com/api/?name=${trip.hostName}&background=E8632B&color=fff&size=80")
                            .crossfade(true)
                            .build(),
                        contentDescription = "Host Avatar",
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, GoTogetherOrange, CircleShape),
                        contentScale = ContentScale.Crop,
                        loading = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(GoTogetherTheme.colors.surface, CircleShape)
                            )
                        },
                        error = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(GoTogetherOrange, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    trip.hostName.take(1).uppercase(),
                                    color = GoTogetherTheme.colors.textPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = trip.hostName,
                        color = GoTogetherTheme.colors.textPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                    if (trip.hostVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.CheckCircle, "Verified", tint = VerifiedCyan, modifier = Modifier.size(14.dp))
                    }
                }

                // Bookmark
                if (showBookmark) {
                    IconButton(
                        onClick = { },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                            .size(36.dp)
                            .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                    ) {
                        Icon(
                            Icons.Default.BookmarkBorder,
                            contentDescription = "Save",
                            tint = GoTogetherTheme.colors.textPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Details Section
            Column(modifier = Modifier.padding(16.dp)) {
                // Description
                Text(
                    text = trip.description,
                    fontSize = 14.sp,
                    color = GoTogetherTheme.colors.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Tags
                if (trip.interests.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        trip.interests.take(3).forEach { tag ->
                            Box(
                                modifier = Modifier
                                    .background(
                                        GoTogetherOrange.copy(alpha = 0.1f),
                                        RoundedCornerShape(50)
                                    )
                                    .border(
                                        1.dp,
                                        GoTogetherOrange.copy(alpha = 0.3f),
                                        RoundedCornerShape(50)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(tag, color = GoTogetherOrangeLight, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }
                
                // Bottom row: travelers + connect
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Groups, contentDescription = null, tint = GoTogetherTheme.colors.textSecondary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${trip.joinedCount}/${trip.groupSize} travelers",
                            fontSize = 13.sp,
                            color = GoTogetherTheme.colors.textSecondary
                        )
                    }
                    
                    Button(
                        onClick = onClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoTogetherOrange
                        ),
                        shape = CircleShape,
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp)
                    ) {
                        Text("View Trip", color = GoTogetherTheme.colors.textOnPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
