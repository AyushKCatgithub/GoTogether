package com.quantax.gotogether.presentation.chat

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.Mood
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import kotlin.math.absoluteValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.quantax.gotogether.domain.model.ChatMessage
import com.quantax.gotogether.domain.model.Chatroom
import com.quantax.gotogether.ui.theme.GoTogetherOrange
import com.quantax.gotogether.ui.theme.GoTogetherOrangeLight
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
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val currentUserId = viewModel.getCurrentUserId()
    val snackbarHostState = remember { SnackbarHostState() }
    // Use TextFieldValue for proper cursor control (emoji fix)
    var messageTextFieldValue by remember { mutableStateOf(TextFieldValue("")) }
    var showEmojiPicker by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val emojiSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Full-screen image viewer state
    var fullScreenImageUrl by remember { mutableStateOf<String?>(null) }

    // Delete dialog state
    var messageToDelete by remember { mutableStateOf<ChatMessage?>(null) }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { viewModel.sendImageMessage(chatroomId, it) }
    }

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

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = GoTogetherTheme.colors.background,
            contentWindowInsets = WindowInsets.safeDrawing.only(
                WindowInsetsSides.Horizontal + WindowInsetsSides.Top
            ),
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
                } else if (uiState.messages.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyChatState()
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
                            .padding(horizontal = 8.dp),
                        contentPadding = PaddingValues(bottom = 8.dp, top = 8.dp),
                        reverseLayout = true
                    ) {
                        groupedMessages.forEach { (dateLabel, messages) ->
                            items(messages, key = { it.id }) { message ->
                                SwipeableMessageItem(
                                    message = message,
                                    isCurrentUser = message.senderId == currentUserId,
                                    onImageClick = { url -> fullScreenImageUrl = url },
                                    onDeleteRequested = { messageToDelete = it }
                                )
                            }
                            item {
                                DateHeader(dateLabel)
                            }
                        }
                    }
                }

                ChatInputBar(
                    messageFieldValue = messageTextFieldValue,
                    onMessageChange = { messageTextFieldValue = it },
                    onSendClick = {
                        if (messageTextFieldValue.text.isNotBlank()) {
                            viewModel.sendMessage(chatroomId, messageTextFieldValue.text)
                            messageTextFieldValue = TextFieldValue("")
                        }
                    },
                    onAttachClick = {
                        pickImageLauncher.launch(
                            PickVisualMediaRequest(PickVisualMedia.ImageOnly)
                        )
                    },
                    onEmojiClick = { showEmojiPicker = true },
                    isSending = uiState.isSending
                )
            }
        }

        // ─── Emoji Picker Sheet ───
        if (showEmojiPicker) {
            ModalBottomSheet(
                onDismissRequest = { showEmojiPicker = false },
                sheetState = emojiSheetState,
                containerColor = GoTogetherTheme.colors.background,
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                EmojiPickerGrid(
                    currentText = messageTextFieldValue.text,
                    onEmojiSelected = { emoji ->
                        // Append emoji and keep cursor at the end
                        val currentText = messageTextFieldValue.text
                        val newText = currentText + emoji
                        messageTextFieldValue = TextFieldValue(
                            text = newText,
                            selection = TextRange(newText.length)
                        )
                        // Do NOT close the sheet — user can pick more emojis
                    },
                    onDone = { showEmojiPicker = false }
                )
            }
        }

        // ─── Full-Screen Image Viewer ───
        if (fullScreenImageUrl != null) {
            FullScreenImageViewer(
                imageUrl = fullScreenImageUrl!!,
                onDismiss = { fullScreenImageUrl = null }
            )
        }

        // ─── Delete Confirmation Dialog ───
        if (messageToDelete != null) {
            DeleteMessageDialog(
                message = messageToDelete!!,
                isOwnMessage = messageToDelete!!.senderId == currentUserId,
                onDeleteForMe = {
                    viewModel.deleteMessageForMe(chatroomId, messageToDelete!!.id)
                    messageToDelete = null
                },
                onDeleteForAll = {
                    viewModel.deleteMessageForAll(chatroomId, messageToDelete!!.id)
                    messageToDelete = null
                },
                onDismiss = { messageToDelete = null }
            )
        }
    }
}

// ─── Swipeable Message Item ───

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableMessageItem(
    message: ChatMessage,
    isCurrentUser: Boolean,
    onImageClick: (String) -> Unit,
    onDeleteRequested: (ChatMessage) -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDeleteRequested(message)
            }
            // Always return false so the item snaps back
            false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 3.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Box(
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .size(40.dp)
                        .background(Color.Red.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.Red,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(GoTogetherTheme.colors.background)
        ) {
            MessageItem(
                message = message,
                isCurrentUser = isCurrentUser,
                onImageClick = onImageClick
            )
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

// ─── Delete Confirmation Dialog ───

@Composable
private fun DeleteMessageDialog(
    message: ChatMessage,
    isOwnMessage: Boolean,
    onDeleteForMe: () -> Unit,
    onDeleteForAll: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "DELETE MESSAGE",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        },
        text = {
            Column {
                val preview = when {
                    message.text.isNotBlank() -> "\"${message.text.take(50)}${if (message.text.length > 50) "…" else ""}\""
                    !message.imageUrl.isNullOrBlank() -> "📷 Photo"
                    else -> "this message"
                }
                Text(
                    "Delete $preview?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GoTogetherTheme.colors.textSecondary
                )
            }
        },
        confirmButton = {
            Column(horizontalAlignment = Alignment.End) {
                if (isOwnMessage) {
                    Button(
                        onClick = onDeleteForAll,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.9f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("DELETE FOR ALL", fontWeight = FontWeight.Black, color = Color.White, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Button(
                    onClick = onDeleteForMe,
                    colors = ButtonDefaults.buttonColors(containerColor = GoTogetherTheme.colors.surfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "DELETE FOR ME",
                        fontWeight = FontWeight.Black,
                        color = GoTogetherTheme.colors.textPrimary,
                        fontSize = 12.sp
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", fontWeight = FontWeight.Bold, color = Color.Gray)
            }
        },
        containerColor = GoTogetherTheme.colors.background,
        shape = RoundedCornerShape(28.dp)
    )
}

// ─── Full-Screen Image Viewer ───

@Composable
private fun FullScreenImageViewer(
    imageUrl: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            var scale by remember { mutableFloatStateOf(1f) }
            var offset by remember { mutableStateOf(Offset.Zero) }

            AsyncImage(
                model = imageUrl,
                contentDescription = "Full-screen image",
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    )
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 5f)
                            if (scale > 1f) {
                                offset = Offset(
                                    x = offset.x + pan.x,
                                    y = offset.y + pan.y
                                )
                            } else {
                                offset = Offset.Zero
                            }
                        }
                    },
                contentScale = ContentScale.Fit
            )

            // Close button
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(44.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

// ─── Existing Components (updated) ───

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
                            border = BorderStroke(
                                2.dp,
                                GoTogetherTheme.colors.background
                            ),
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
                            border = BorderStroke(
                                2.dp,
                                GoTogetherTheme.colors.background
                            ),
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
    isCurrentUser: Boolean,
    onImageClick: (String) -> Unit = {}
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val maxBubble = maxWidth * 0.82f
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.Bottom
        ) {
            if (!isCurrentUser) {
                val nameColor = senderColorForGroup(message.senderName)
                Surface(
                    modifier = Modifier
                        .size(36.dp)
                        .offset(y = 2.dp),
                    shape = CircleShape,
                    color = GoTogetherTheme.colors.surface,
                    border = BorderStroke(1.dp, GoTogetherTheme.colors.border)
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
                                message.senderName.take(1).uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = nameColor
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            MessageBubble(
                message = message,
                isCurrentUser = isCurrentUser,
                maxBubbleWidth = maxBubble,
                onImageClick = onImageClick
            )
        }
    }
}

@Composable
private fun MessageBubble(
    message: ChatMessage,
    isCurrentUser: Boolean,
    maxBubbleWidth: Dp,
    onImageClick: (String) -> Unit = {}
) {
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val isDark = GoTogetherTheme.colors.isDark
    val sentBubble = whatsAppSentBubbleColor(isDark)
    val receivedBubble = whatsAppReceivedBubbleColor(isDark)
    val sentText = whatsAppSentBubbleText(isDark)
    val receivedText = whatsAppReceivedBubbleText(isDark)
    val timeColorSent = if (isDark) Color(0xFFE8C4B0) else Color(0xFF667781)
    val timeColorReceived = if (isDark) Color(0xFF8696A0) else Color(0xFF667781)
    val checkTint = if (isDark) GoTogetherOrangeLight else GoTogetherOrange.copy(alpha = 0.88f)

    Column(
        modifier = Modifier.widthIn(max = maxBubbleWidth),
        horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start
    ) {
        if (!isCurrentUser) {
            Text(
                text = message.senderName.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = senderColorForGroup(message.senderName),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 12.dp, bottom = 4.dp),
                letterSpacing = 0.5.sp
            )
        }
        Surface(
            color = if (isCurrentUser) sentBubble else receivedBubble,
            shape = RoundedCornerShape(
                topStart = 12.dp,
                topEnd = 12.dp,
                bottomStart = if (isCurrentUser) 12.dp else 2.dp,
                bottomEnd = if (isCurrentUser) 2.dp else 12.dp
            ),
            modifier = if (isCurrentUser) {
                Modifier.shadow(
                    2.dp,
                    RoundedCornerShape(12.dp),
                    spotColor = GoTogetherTheme.colors.overlay.copy(alpha = 0.25f)
                )
            } else {
                Modifier.shadow(
                    1.dp,
                    RoundedCornerShape(12.dp),
                    spotColor = GoTogetherTheme.colors.overlay.copy(alpha = 0.15f)
                )
            }
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                if (!message.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = message.imageUrl,
                        contentDescription = "Attached image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onImageClick(message.imageUrl!!) },
                        contentScale = ContentScale.Crop
                    )
                    if (message.text.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                if (message.text.isNotBlank()) {
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isCurrentUser) sentText else receivedText,
                        lineHeight = 22.sp
                    )
                }
                Row(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = timeFormat.format(Date(message.timestamp)),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isCurrentUser) timeColorSent else timeColorReceived,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp
                    )
                    if (isCurrentUser) {
                        Icon(
                            imageVector = Icons.Filled.DoneAll,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = checkTint
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatInputBar(
    messageFieldValue: TextFieldValue,
    onMessageChange: (TextFieldValue) -> Unit,
    onSendClick: () -> Unit,
    onAttachClick: () -> Unit,
    onEmojiClick: () -> Unit,
    isSending: Boolean
) {
    val sendAccent = GoTogetherOrange
    val barBg = GoTogetherTheme.colors.inputBackground
    val placeholderColor = GoTogetherTheme.colors.textSecondary.copy(alpha = 0.65f)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 8.dp)
            .padding(bottom = 2.dp),
        color = GoTogetherTheme.colors.background,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            IconButton(
                onClick = onAttachClick,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    Icons.Outlined.AttachFile,
                    contentDescription = "Attach photo",
                    tint = GoTogetherTheme.colors.textSecondary
                )
            }
            IconButton(
                onClick = onEmojiClick,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    Icons.Outlined.Mood,
                    contentDescription = "Emoji",
                    tint = GoTogetherTheme.colors.textSecondary
                )
            }
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .border(
                        width = 1.dp,
                        color = GoTogetherTheme.colors.border,
                        shape = RoundedCornerShape(24.dp)
                    ),
                shape = RoundedCornerShape(24.dp),
                color = barBg
            ) {
                OutlinedTextField(
                    value = messageFieldValue,
                    onValueChange = onMessageChange,
                    placeholder = {
                        Text(
                            "Message",
                            style = MaterialTheme.typography.bodyLarge,
                            color = placeholderColor,
                            fontWeight = FontWeight.Normal
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = GoTogetherTheme.colors.textPrimary,
                        unfocusedTextColor = GoTogetherTheme.colors.textPrimary,
                        cursorColor = sendAccent
                    ),
                    maxLines = 6,
                    singleLine = false
                )
            }
            IconButton(
                onClick = onSendClick,
                enabled = messageFieldValue.text.isNotBlank() && !isSending,
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (messageFieldValue.text.isNotBlank()) sendAccent else GoTogetherTheme.colors.surfaceVariant,
                        CircleShape
                    )
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (messageFieldValue.text.isNotBlank()) Color.White else GoTogetherTheme.colors.textSecondary.copy(alpha = 0.35f),
                        modifier = Modifier.size(22.dp)
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
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = GoTogetherTheme.colors.surfaceVariant.copy(alpha = if (GoTogetherTheme.colors.isDark) 0.85f else 1f),
            shape = RoundedCornerShape(50)
        ) {
            Text(
                text = dateLabel.uppercase(),
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = GoTogetherTheme.colors.textSecondary,
                letterSpacing = 0.8.sp,
                fontSize = 11.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EmojiPickerGrid(
    currentText: String = "",
    onEmojiSelected: (String) -> Unit,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Pick an emoji",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = GoTogetherTheme.colors.textPrimary
            )
            Surface(
                onClick = onDone,
                shape = RoundedCornerShape(12.dp),
                color = GoTogetherOrange
            ) {
                Text(
                    "Done",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        // ─── Live text preview ───
        if (currentText.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                color = GoTogetherTheme.colors.inputBackground,
                border = BorderStroke(1.dp, GoTogetherTheme.colors.border)
            ) {
                Text(
                    text = currentText,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = GoTogetherTheme.colors.textPrimary,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(48.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 380.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(CHAT_EMOJI_PICKER_ITEMS) { emoji ->
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onEmojiSelected(emoji) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = emoji, fontSize = 26.sp)
                }
            }
        }
    }
}

@Composable
private fun EmptyChatState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
    ) {
        Text(
            text = "NO MESSAGES YET",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = GoTogetherTheme.colors.textPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "Be the first to drop a vibe! 👋",
            style = MaterialTheme.typography.bodyLarge,
            color = GoTogetherTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )
    }
}

/** Sent message bubble — orangish, aligned with GoTogether brand. */
private fun whatsAppSentBubbleColor(isDark: Boolean): Color =
    if (isDark) Color(0xFF6B2A0F) else Color(0xFFFFEDE3)

private fun whatsAppReceivedBubbleColor(isDark: Boolean): Color =
    if (isDark) Color(0xFF1F2C34) else Color(0xFFFFFFFF)

private fun whatsAppSentBubbleText(isDark: Boolean): Color =
    if (isDark) Color(0xFFFFF8F5) else Color(0xFF111B21)

private fun whatsAppReceivedBubbleText(isDark: Boolean): Color =
    if (isDark) Color(0xFFE9EDEF) else Color(0xFF111B21)

private fun senderColorForGroup(name: String): Color {
    val palette = listOf(
        Color(0xFF53BDEB),
        GoTogetherOrange,
        Color(0xFF6B88C5),
        Color(0xFFEBBC42),
        Color(0xFFD65A5A),
        Color(0xFFAC8ED7),
        GoTogetherOrangeLight,
        Color(0xFFD28A62)
    )
    val idx = name.hashCode().absoluteValue % palette.size
    return palette[idx]
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
