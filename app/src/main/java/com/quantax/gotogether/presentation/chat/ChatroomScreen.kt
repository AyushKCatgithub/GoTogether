package com.quantax.gotogether.presentation.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.quantax.gotogether.domain.model.ChatMessage
import com.quantax.gotogether.domain.model.Chatroom
import com.quantax.gotogether.ui.theme.GoTogetherOrange
import com.quantax.gotogether.ui.theme.GoTogetherTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatroomScreen(
    chatroomId: String,
    onNavigateBack: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.state.collectAsState()
    val currentUserId = viewModel.getCurrentUserId()
    val snackbarHostState = remember { SnackbarHostState() }
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(chatroomId) {
        viewModel.loadChatroom(chatroomId)
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearErrorMessage()
        }
    }

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    Scaffold(
        containerColor = GoTogetherTheme.colors.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            ChatTopBar(
                chatroom = uiState.chatroom,
                onBackClick = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
        ) {
            if (uiState.isLoading && uiState.messages.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GoTogetherOrange)
                }
            } else {
                val groupedMessages = remember(uiState.messages) {
                    groupMessagesByDate(uiState.messages)
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    contentPadding = PaddingValues(bottom = 16.dp, top = 24.dp),
                    reverseLayout = true
                ) {
                    groupedMessages.forEach { (dateLabel, messages) ->
                        items(messages, key = { it.id }) { message ->
                            MessageItem(
                                message = message,
                                isCurrentUser = message.senderId == currentUserId
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                        item {
                            DateHeader(dateLabel)
                        }
                    }

                    if (uiState.messages.isEmpty() && !uiState.isLoading) {
                        item {
                            EmptyChatState()
                        }
                    }
                }
            }

            ChatInputBar(
                messageText = messageText,
                onMessageChange = { messageText = it },
                onSendClick = {
                    if (messageText.isNotBlank()) {
                        viewModel.sendMessage(chatroomId, messageText)
                        messageText = ""
                    }
                },
                isSending = uiState.isSending
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatTopBar(
    chatroom: Chatroom?,
    onBackClick: () -> Unit
) {
    Surface(
        color = GoTogetherTheme.colors.background,
        shadowElevation = 0.dp
    ) {
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Overlapping avatars for "Vibers"
                    Box(modifier = Modifier.width(60.dp)) {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = CircleShape,
                            border = androidx.compose.foundation.BorderStroke(2.dp, Color.White),
                            color = GoTogetherTheme.colors.surface
                        ) {
                            if (chatroom?.tripImageUrl != null) {
                                AsyncImage(
                                    model = chatroom.tripImageUrl,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                        Surface(
                            modifier = Modifier
                                .size(40.dp)
                                .offset(x = 20.dp),
                            shape = CircleShape,
                            border = androidx.compose.foundation.BorderStroke(2.dp, Color.White),
                            color = GoTogetherOrange
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    "${chatroom?.memberIds?.size ?: 0}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                    
                    Column {
                        Text(
                            chatroom?.tripTitle?.uppercase() ?: "CHAT",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Black,
                            color = GoTogetherTheme.colors.textPrimary,
                            letterSpacing = 1.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            "ACTIVE NOW",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF4CAF50), // Vibrant Green for "Active"
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
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
}

@Composable
private fun MessageItem(
    message: ChatMessage,
    isCurrentUser: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isCurrentUser) {
            Surface(
                modifier = Modifier
                    .size(32.dp)
                    .offset(y = 4.dp),
                shape = CircleShape,
                color = GoTogetherTheme.colors.surface
            ) {
                if (message.senderImageUrl != null) {
                    AsyncImage(
                        model = message.senderImageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            message.senderName.take(1),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
        }

        MessageBubble(message = message, isCurrentUser = isCurrentUser)
    }
}

@Composable
private fun MessageBubble(
    message: ChatMessage,
    isCurrentUser: Boolean
) {
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    Column(
        modifier = Modifier.widthIn(max = 280.dp),
        horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start
    ) {
        if (!isCurrentUser) {
            Text(
                text = message.senderName.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = GoTogetherTheme.colors.textSecondary,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(start = 12.dp, bottom = 4.dp),
                letterSpacing = 0.5.sp
            )
        }
        Surface(
            color = if (isCurrentUser) Color.Black else Color.White,
            shape = RoundedCornerShape(
                topStart = 28.dp,
                topEnd = 28.dp,
                bottomStart = if (isCurrentUser) 28.dp else 4.dp,
                bottomEnd = if (isCurrentUser) 4.dp else 28.dp
            ),
            modifier = if (isCurrentUser) {
                Modifier.shadow(8.dp, RoundedCornerShape(28.dp), spotColor = Color.Black.copy(alpha = 0.2f))
            } else {
                Modifier.shadow(2.dp, RoundedCornerShape(28.dp), spotColor = Color.Gray.copy(alpha = 0.1f))
            }
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isCurrentUser) Color.White else Color.Black,
                    lineHeight = 24.sp
                )
                Text(
                    text = timeFormat.format(Date(message.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isCurrentUser) Color.White.copy(alpha = 0.6f) else Color.Gray,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 6.dp),
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
private fun ChatInputBar(
    messageText: String,
    onMessageChange: (String) -> Unit,
    onSendClick: () -> Unit,
    isSending: Boolean
) {
    Surface(
        color = Color.Transparent,
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 20.dp)
            .shadow(24.dp, RoundedCornerShape(32.dp), spotColor = Color.Black.copy(alpha = 0.1f))
            .background(Color.White, RoundedCornerShape(32.dp))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(4.dp)
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = onMessageChange,
                placeholder = {
                    Text(
                        "Say something cool...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Medium
                    )
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(32.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    cursorColor = GoTogetherOrange
                ),
                maxLines = 4,
                singleLine = false
            )

            IconButton(
                onClick = onSendClick,
                enabled = messageText.isNotBlank() && !isSending,
                modifier = Modifier
                    .padding(4.dp)
                    .size(48.dp)
                    .background(
                        if (messageText.isNotBlank()) Color.Black else Color.Gray.copy(alpha = 0.1f),
                        CircleShape
                    )
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (messageText.isNotBlank()) Color.White else Color.Gray.copy(alpha = 0.4f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DateHeader(dateLabel: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = GoTogetherTheme.colors.surface,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = dateLabel.uppercase(),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                color = GoTogetherTheme.colors.textSecondary,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
private fun EmptyChatState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 80.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "NO MESSAGES YET",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = GoTogetherTheme.colors.textPrimary
            )
            Text(
                "Be the first to drop a vibe! 👋",
                style = MaterialTheme.typography.bodyLarge,
                color = GoTogetherTheme.colors.textSecondary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

private fun isCurrentUser(senderId: String, currentUserId: String): Boolean {
    return senderId == currentUserId
}

private fun groupMessagesByDate(messages: List<ChatMessage>): Map<String, List<ChatMessage>> {
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val today = Calendar.getInstance()
    val yesterday = Calendar.getInstance().apply { add(Calendar.DATE, -1) }

    return messages.groupBy {
        val cal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
        when {
            isSameDay(cal, today) -> "Today"
            isSameDay(cal, yesterday) -> "Yesterday"
            else -> dateFormat.format(Date(it.timestamp))
        }
    }
}

private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}



