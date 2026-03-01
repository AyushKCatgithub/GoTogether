package com.quantax.gotogether.presentation.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.quantax.gotogether.data.repository.ChatRepository
import com.quantax.gotogether.data.repository.JoinRequestRepository
import com.quantax.gotogether.data.repository.NotificationRepository
import com.quantax.gotogether.domain.model.JoinRequest
import com.quantax.gotogether.domain.model.Notification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface NotificationItem {
    val timestamp: Long
    val id: String
    
    data class JoinRequestItem(val request: JoinRequest) : NotificationItem {
        override val timestamp: Long = request.timestamp
        override val id: String = request.id
    }
    
    data class GeneralNotificationItem(val notification: Notification) : NotificationItem {
        override val timestamp: Long = notification.timestamp
        override val id: String = notification.id
    }
}

class NotificationsViewModel : ViewModel() {
    private val joinRequestRepository = JoinRequestRepository()
    private val chatRepository = ChatRepository()
    private val notificationRepository = NotificationRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _pendingRequests = MutableStateFlow<List<JoinRequest>>(emptyList())
    // Keep for backward compatibility or specific uses
    val pendingRequests: StateFlow<List<JoinRequest>> = _pendingRequests.asStateFlow()

    private val _generalNotifications = MutableStateFlow<List<Notification>>(emptyList())
    
    val allNotifications: StateFlow<List<NotificationItem>> = combine(
        _pendingRequests,
        _generalNotifications
    ) { requests, notifications ->
        val items = mutableListOf<NotificationItem>()
        items.addAll(requests.map { NotificationItem.JoinRequestItem(it) })
        items.addAll(notifications.map { NotificationItem.GeneralNotificationItem(it) })
        items.sortedByDescending { it.timestamp }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadCount: StateFlow<Int> = combine(
        _pendingRequests,
        _generalNotifications
    ) { requests, notifications ->
        requests.size + notifications.count { !it.isRead }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    init {
        loadRequestsAndNotifications()
    }

    private fun loadRequestsAndNotifications() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            launch {
                joinRequestRepository.getRequestsForHost(uid).collect { requests ->
                    _pendingRequests.value = requests
                    updateLoadingState()
                }
            }
            launch {
                notificationRepository.getNotificationsForUser(uid).collect { notifications ->
                    _generalNotifications.value = notifications
                    updateLoadingState()
                }
            }
        }
    }

    private fun updateLoadingState() {
        if (_isLoading.value) {
            _isLoading.value = false
        }
    }

    fun approveRequest(request: JoinRequest) {
        viewModelScope.launch {
            joinRequestRepository.approveRequest(request.id, request.tripId, request.requesterId)
                .onSuccess {
                    // Also add to chatroom
                    chatRepository.addMemberToChatroom(request.tripId, request.requesterId)
                    
                    // Create an approval notification for the requester
                    val currentUser = auth.currentUser
                    val notification = Notification(
                        recipientId = request.requesterId,
                        senderId = currentUser?.uid ?: "",
                        senderName = currentUser?.displayName ?: "Host",
                        senderImageUrl = currentUser?.photoUrl?.toString(),
                        type = Notification.TYPE_REQUEST_APPROVED,
                        tripId = request.tripId,
                        tripTitle = request.tripTitle,
                        chatroomId = request.tripId // chatroomId is the same as tripId
                    )
                    notificationRepository.createNotification(notification)

                    _actionMessage.value = "${request.requesterName} has been approved!"
                }
                .onFailure {
                    _actionMessage.value = "Failed to approve request"
                }
        }
    }

    fun denyRequest(request: JoinRequest) {
        viewModelScope.launch {
            joinRequestRepository.denyRequest(request.id)
                .onSuccess {
                    _actionMessage.value = "${request.requesterName}'s request denied"
                }
                .onFailure {
                    _actionMessage.value = "Failed to deny request"
                }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            notificationRepository.markAsRead(notificationId)
        }
    }
    
    fun markAllAsRead() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            notificationRepository.markAllAsRead(uid)
        }
    }

    fun clearActionMessage() {
        _actionMessage.value = null
    }
}
