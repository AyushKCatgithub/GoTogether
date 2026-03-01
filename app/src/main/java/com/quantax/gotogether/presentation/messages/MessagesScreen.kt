package com.quantax.gotogether.presentation.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.quantax.gotogether.domain.model.Chatroom
import com.quantax.gotogether.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToChatroom: (String) -> Unit = {},
    viewModel: MessagesViewModel = viewModel()
) {
    val chatrooms by viewModel.chatrooms.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        containerColor = GoTogetherTheme.colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Messages",
                        fontWeight = FontWeight.Bold,
                        color = GoTogetherTheme.colors.textPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = GoTogetherTheme.colors.textPrimary
                        )
                    }
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
                chatrooms.isEmpty() -> {
                    EmptyMessages()
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(chatrooms, key = { it.id }) { chatroom ->
                            ChatroomListItem(
                                chatroom = chatroom,
                                onClick = { onNavigateToChatroom(chatroom.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatroomListItem(
    chatroom: Chatroom,
    onClick: () -> Unit
) {
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val dateFormat = remember { SimpleDateFormat("MMM dd", Locale.getDefault()) }

    val timestampText = if (chatroom.lastMessageTimestamp > 0) {
        val now = System.currentTimeMillis()
        val diff = now - chatroom.lastMessageTimestamp
        if (diff < 24 * 60 * 60 * 1000) {
            timeFormat.format(Date(chatroom.lastMessageTimestamp))
        } else {
            dateFormat.format(Date(chatroom.lastMessageTimestamp))
        }
    } else ""

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = GoTogetherTheme.colors.card)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Trip/Chatroom image
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(chatroom.tripImageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Trip Image",
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp)),
                contentScale = ContentScale.Crop,
                loading = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(GoTogetherTheme.colors.surface, RoundedCornerShape(14.dp))
                    )
                },
                error = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(listOf(GoTogetherOrange, GoTogetherOrangeDark)),
                                RoundedCornerShape(14.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            chatroom.tripTitle.take(1).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = chatroom.tripTitle,
                        color = GoTogetherTheme.colors.textPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (timestampText.isNotEmpty()) {
                        Text(
                            text = timestampText,
                            color = GoTogetherTheme.colors.textSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                if (chatroom.lastMessage.isNotEmpty()) {
                    Text(
                        text = "${chatroom.lastMessageSenderName}: ${chatroom.lastMessage}",
                        color = GoTogetherTheme.colors.textSecondary,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Text(
                        text = "No messages yet",
                        color = GoTogetherTheme.colors.textSecondary.copy(alpha = 0.6f),
                        fontSize = 13.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyMessages() {
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
                        AccentBlue.copy(alpha = 0.08f),
                        RoundedCornerShape(24.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.ChatBubbleOutline,
                    contentDescription = null,
                    tint = AccentBlue.copy(alpha = 0.5f),
                    modifier = Modifier.size(40.dp)
                )
            }
            Text(
                "No messages yet",
                color = GoTogetherTheme.colors.textPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                "Join trips to start\nchatting with travelers",
                color = GoTogetherTheme.colors.textSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
