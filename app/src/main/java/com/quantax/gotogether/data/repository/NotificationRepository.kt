package com.quantax.gotogether.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.quantax.gotogether.domain.model.Notification
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val notificationsCollection = firestore.collection("notifications")

    suspend fun createNotification(notification: Notification): Result<Unit> {
        return try {
            val docRef = notificationsCollection.document()
            val newNotification = notification.copy(id = docRef.id)
            docRef.set(newNotification).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getNotificationsForUser(userId: String): Flow<List<Notification>> = callbackFlow {
        val listener = notificationsCollection
            .whereEqualTo("recipientId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val notifications = snapshot?.toObjects(Notification::class.java) ?: emptyList()
                trySend(notifications.sortedByDescending { it.timestamp })
            }
        awaitClose { listener.remove() }
    }

    suspend fun markAsRead(notificationId: String): Result<Unit> {
        return try {
            notificationsCollection.document(notificationId).update(
                mapOf(
                    "isRead" to true,
                    "read" to true
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun markAllAsRead(userId: String): Result<Unit> {
        return try {
            val allNotifications = notificationsCollection
                .whereEqualTo("recipientId", userId)
                .get()
                .await()
            
            firestore.runBatch { batch ->
                for (doc in allNotifications.documents) {
                    val isRead = doc.getBoolean("isRead") ?: doc.getBoolean("read") ?: false
                    if (!isRead) {
                        batch.update(doc.reference, mapOf("isRead" to true, "read" to true))
                    }
                }
            }.await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
