package com.quantax.gotogether.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

// A mock database of popular travel destinations (India Centric)
private val popularDestinations = listOf(
    "Agra, Uttar Pradesh", "Ahmedabad, Gujarat", "Amritsar, Punjab", "Andaman Islands",
    "Aurangabad, Maharashtra", "Bangalore, Karnataka", "Bhopal, Madhya Pradesh", 
    "Bhubaneswar, Odisha", "Bikaner, Rajasthan", "Chandigarh", "Chennai, Tamil Nadu", 
    "Coimbatore, Tamil Nadu", "Coorg, Karnataka", "Darjeeling, West Bengal", 
    "Dehradun, Uttarakhand", "Delhi", "Dharamshala, Himachal Pradesh", "Gangtok, Sikkim", 
    "Goa", "Guwahati, Assam", "Gwalior, Madhya Pradesh", "Hampi, Karnataka", 
    "Haridwar, Uttarakhand", "Hyderabad, Telangana", "Indore, Madhya Pradesh", 
    "Jaipur, Rajasthan", "Jaisalmer, Rajasthan", "Jodhpur, Rajasthan", "Kanpur, Uttar Pradesh", 
    "Kanyakumari, Tamil Nadu", "Kochi, Kerala", "Kolkata, West Bengal", 
    "Kovalam, Kerala", "Kullu, Himachal Pradesh", "Leh, Ladakh", "Lucknow, Uttar Pradesh", 
    "Madurai, Tamil Nadu", "Mahabaleshwar, Maharashtra", "Manali, Himachal Pradesh", 
    "Mangalore, Karnataka", "Mumbai, Maharashtra", "Munnar, Kerala", "Mussoorie, Uttarakhand", 
    "Mysore, Karnataka", "Nagpur, Maharashtra", "Nainital, Uttarakhand", "Ooty, Tamil Nadu", 
    "Patna, Bihar", "Pondicherry", "Pune, Maharashtra", "Puri, Odisha", 
    "Pushkar, Rajasthan", "Ranchi, Jharkhand", "Rishikesh, Uttarakhand", "Shimla, Himachal Pradesh", 
    "Srinagar, Jammu and Kashmir", "Surat, Gujarat", "Thiruvananthapuram, Kerala", 
    "Tirupati, Andhra Pradesh", "Udaipur, Rajasthan", "Vadodara, Gujarat", 
    "Varanasi, Uttar Pradesh", "Visakhapatnam, Andhra Pradesh", "Wayanad, Kerala"
).sorted()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationSearchDialog(
    onDismissRequest: () -> Unit,
    onLocationSelected: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredDestinations = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            popularDestinations
        } else {
            popularDestinations.filter { it.contains(searchQuery, ignoreCase = true) }
        }
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false) // Allow filling screen width
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header with Close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Select location",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismissRequest) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search for a city…") },
                    leadingIcon = {
                        Icon(Icons.Outlined.Search, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Transparent,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Auto-suggest row (Dynamic input if not found in list)
                if (searchQuery.isNotBlank() && !filteredDestinations.any { it.equals(searchQuery, ignoreCase = true) }) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onLocationSelected(searchQuery.trim())
                            }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Use \"${searchQuery.trim()}\"",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }

                // Results List
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    if (filteredDestinations.isEmpty() && searchQuery.isBlank()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Start typing to search…",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else if (filteredDestinations.isEmpty()) {
                        // The auto-suggest row above handles custom inputs so we just show an info message here
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No matching popular destinations.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(filteredDestinations) { location ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onLocationSelected(location)
                                    }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = location,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        }
                    }
                }
            }
        }
    }
}
