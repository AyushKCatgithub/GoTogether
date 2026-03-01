package com.quantax.gotogether.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.quantax.gotogether.domain.model.Notification
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class NotificationRepository {
    private val firestore = FirebaseFirestore.getInstance()
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
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val notifications = snapshot?.toObjects(Notification::class.java) ?: emptyList()
                trySend(notifications)
            }
        awaitClose { listener.remove() }
    }

    suspend fun markAsRead(notificationId: String): Result<Unit> {
        return try {
            notificationsCollection.document(notificationId).update("isRead", true).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun markAllAsRead(userId: String): Result<Unit> {
        return try {
            val unreadNotifications = notificationsCollection
                .whereEqualTo("recipientId", userId)
                .whereEqualTo("isRead", false)
                .get()
                .await()
            
            firestore.runBatch { batch ->
                for (doc in unreadNotifications.documents) {
                    batch.update(doc.reference, "isRead", true)
                }
            }.await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
