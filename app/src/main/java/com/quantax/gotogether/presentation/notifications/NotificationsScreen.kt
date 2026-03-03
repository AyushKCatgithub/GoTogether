package com.quantax.gotogether.presentation.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.quantax.gotogether.domain.model.JoinRequest
import com.quantax.gotogether.domain.model.Notification
import com.quantax.gotogether.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    viewModel: NotificationsViewModel = viewModel(),
    onNavigateToChatroom: (String) -> Unit = {}
) {
    val allNotifications by viewModel.allNotifications.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val actionMessage by viewModel.actionMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(actionMessage) {
        actionMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearActionMessage()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.markAllAsRead()
    }

    Scaffold(
        containerColor = GoTogetherTheme.colors.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Notifications",
                        fontWeight = FontWeight.Bold,
                        color = GoTogetherTheme.colors.textPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = GoTogetherOrange)
                    }
                }
                allNotifications.isEmpty() -> {
                    EmptyNotifications()
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(allNotifications, key = { it.id }) { item ->
                            when (item) {
                                is NotificationItem.JoinRequestItem -> {
                                    JoinRequestCard(
                                        request = item.request,
                                        onApprove = { viewModel.approveRequest(item.request) },
                                        onDeny = { viewModel.denyRequest(item.request) }
                                    )
                                }
                                is NotificationItem.GeneralNotificationItem -> {
                                    GeneralNotificationCard(
                                        notification = item.notification,
                                        onNavigateToChatroom = {
                                            viewModel.markAsRead(item.notification.id)
                                            onNavigateToChatroom(item.notification.chatroomId)
                                        }
                                    )
                                }
                            }
                        }
                        // Bottom spacer for nav bar
                        item { Spacer(modifier = Modifier.height(100.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun JoinRequestCard(
    request: JoinRequest,
    onApprove: () -> Unit,
    onDeny: () -> Unit
) {
    val timeFormat = remember { SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = GoTogetherTheme.colors.card)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(request.requesterImageUrl ?: "https://ui-avatars.com/api/?name=${request.requesterName}&background=E8632B&color=fff&size=80")
                    .crossfade(true)
                    .build(),
                contentDescription = "Requester Avatar",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
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
                            request.requesterName.take(1).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = request.requesterName,
                    color = GoTogetherTheme.colors.textPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Wants to join: ${request.tripTitle}",
                    color = GoTogetherTheme.colors.textSecondary,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = timeFormat.format(Date(request.timestamp)),
                    color = GoTogetherTheme.colors.textSecondary.copy(alpha = 0.6f),
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Action buttons
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Deny button
                IconButton(
                    onClick = onDeny,
                    modifier = Modifier
                        .size(38.dp)
                        .background(
                            Color(0xFFFF5252).copy(alpha = 0.12f),
                            CircleShape
                        )
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Deny",
                        tint = Color(0xFFFF5252),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Approve button
                IconButton(
                    onClick = onApprove,
                    modifier = Modifier
                        .size(38.dp)
                        .background(
                            AccentGreen.copy(alpha = 0.12f),
                            CircleShape
                        )
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Approve",
                        tint = AccentGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun GeneralNotificationCard(
    notification: Notification,
    onNavigateToChatroom: () -> Unit
) {
    val timeFormat = remember { SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()) }
    val backgroundColor = if (!notification.isRead) {
        GoTogetherOrange.copy(alpha = 0.05f)
    } else {
        GoTogetherTheme.colors.card
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(notification.senderImageUrl ?: "https://ui-avatars.com/api/?name=${notification.senderName}&background=E8632B&color=fff&size=80")
                    .crossfade(true)
                    .build(),
                contentDescription = "Sender Avatar",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
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
                            notification.senderName.take(1).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Request Approved",
                    color = GoTogetherTheme.colors.textPrimary,
                    fontWeight = if (!notification.isRead) FontWeight.Bold else FontWeight.SemiBold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${notification.senderName} approved your request for ${notification.tripTitle}.",
                    color = GoTogetherTheme.colors.textSecondary,
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = timeFormat.format(Date(notification.timestamp)),
                    color = GoTogetherTheme.colors.textSecondary.copy(alpha = 0.6f),
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Action button
            if (notification.type == Notification.TYPE_REQUEST_APPROVED) {
                IconButton(
                    onClick = onNavigateToChatroom,
                    modifier = Modifier
                        .size(38.dp)
                        .background(
                            GoTogetherOrange.copy(alpha = 0.12f),
                            CircleShape
                        )
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "Go to Chat",
                        tint = GoTogetherOrange,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyNotifications() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                .background(
                    GoTogetherOrange.copy(alpha = 0.08f),
                    RoundedCornerShape(24.dp)
                ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Notifications,
                    contentDescription = null,
                    tint = GoTogetherOrange.copy(alpha = 0.5f),
                    modifier = Modifier.size(40.dp)
                )
            }
            Text(
                "No notifications yet",
                color = GoTogetherTheme.colors.textPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                "You'll see join requests\nand trip updates here",
                color = GoTogetherTheme.colors.textSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
