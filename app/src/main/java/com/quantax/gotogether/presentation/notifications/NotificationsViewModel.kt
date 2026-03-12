package com.quantax.gotogether.presentation.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.quantax.gotogether.data.repository.ChatRepository
import com.quantax.gotogether.data.repository.JoinRequestRepository
import com.quantax.gotogether.data.repository.NotificationRepository
import com.quantax.gotogether.domain.model.JoinRequest
import com.quantax.gotogether.domain.model.Notification
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val joinRequestRepository: JoinRequestRepository,
    private val chatRepository: ChatRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    private val _pendingRequests = MutableStateFlow<List<JoinRequest>>(emptyList())
    private val _generalNotifications = MutableStateFlow<List<Notification>>(emptyList())
    private val _isLoading = MutableStateFlow(true)
    private val _actionMessage = MutableStateFlow<String?>(null)

    val state: StateFlow<NotificationsState> = combine(
        _pendingRequests,
        _generalNotifications,
        _isLoading,
        _actionMessage
    ) { requests, notifications, loading, message ->
        val items = mutableListOf<NotificationItem>()
        items.addAll(requests.map { NotificationItem.JoinRequestItem(it) })
        items.addAll(notifications.map { NotificationItem.GeneralNotificationItem(it) })
        items.sortByDescending { it.timestamp }

        val count = requests.size + notifications.count { !it.isRead }

        NotificationsState(
            allNotifications = items,
            unreadCount = count,
            isLoading = loading,
            actionMessage = message
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NotificationsState())

    init {
        onEvent(NotificationsEvent.LoadNotifications)
    }

    fun onEvent(event: NotificationsEvent) {
        when (event) {
            is NotificationsEvent.LoadNotifications -> loadRequestsAndNotifications()
            is NotificationsEvent.ApproveRequest -> approveRequest(event.request)
            is NotificationsEvent.DenyRequest -> denyRequest(event.request)
            is NotificationsEvent.MarkAsRead -> markAsRead(event.notificationId)
            is NotificationsEvent.MarkAllAsRead -> markAllAsRead()
            is NotificationsEvent.ClearActionMessage -> clearActionMessage()
        }
    }

    private fun loadRequestsAndNotifications() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            _isLoading.value = false
            _actionMessage.value = "User not logged in"
            return
        }

        _isLoading.value = true
        _actionMessage.value = null

        viewModelScope.launch {
            launch {
                try {
                    joinRequestRepository.getRequestsForHost(uid)
                        .catch { e ->
                            _actionMessage.value = e.localizedMessage ?: "Failed to load requests"
                            updateLoadingState()
                        }
                        .collect { requests ->
                            _pendingRequests.value = requests
                            updateLoadingState()
                        }
                } catch (e: Exception) {
                    _actionMessage.value = e.localizedMessage ?: "Failed to load requests"
                    updateLoadingState()
                }
            }
            launch {
                try {
                    notificationRepository.getNotificationsForUser(uid)
                        .catch { e ->
                            _actionMessage.value =
                                e.localizedMessage ?: "Failed to load notifications"
                            updateLoadingState()
                        }
                        .collect { notifications ->
                            _generalNotifications.value = notifications
                            updateLoadingState()
                        }
                } catch (e: Exception) {
                    _actionMessage.value = e.localizedMessage ?: "Failed to load notifications"
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

    private fun approveRequest(request: JoinRequest) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _actionMessage.value = "User not logged in"
            return
        }

        viewModelScope.launch {
            try {
                joinRequestRepository.approveRequest(
                    request.id,
                    request.tripId,
                    request.requesterId
                )
                    .onSuccess {
                        // Also add to chatroom
                        try {
                            chatRepository.addMemberToChatroom(request.tripId, request.requesterId)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                        // Create an approval notification for the requester
                        try {
                            val notification = Notification(
                                recipientId = request.requesterId,
                                senderId = currentUser.uid,
                                senderName = currentUser.displayName ?: "Host",
                                senderImageUrl = currentUser.photoUrl?.toString(),
                                type = Notification.TYPE_REQUEST_APPROVED,
                                tripId = request.tripId,
                                tripTitle = request.tripTitle,
                                chatroomId = request.tripId // chatroomId is the same as tripId
                            )
                            notificationRepository.createNotification(notification)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                        _actionMessage.value = "${request.requesterName} has been approved!"
                    }
                    .onFailure { e ->
                        _actionMessage.value = e.localizedMessage ?: "Failed to approve request"
                    }
            } catch (e: Exception) {
                _actionMessage.value = e.localizedMessage ?: "Failed to approve request"
            }
        }
    }

    private fun denyRequest(request: JoinRequest) {
        if (auth.currentUser == null) {
            _actionMessage.value = "User not logged in"
            return
        }

        viewModelScope.launch {
            try {
                joinRequestRepository.denyRequest(request.id)
                    .onSuccess {
                        _actionMessage.value = "${request.requesterName}'s request denied"
                    }
                    .onFailure { e ->
                        _actionMessage.value = e.localizedMessage ?: "Failed to deny request"
                    }
            } catch (e: Exception) {
                _actionMessage.value = e.localizedMessage ?: "Failed to deny request"
            }
        }
    }

    private fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            try {
                notificationRepository.markAsRead(notificationId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun markAllAsRead() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                notificationRepository.markAllAsRead(uid)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun clearActionMessage() {
        _actionMessage.value = null
    }
}
