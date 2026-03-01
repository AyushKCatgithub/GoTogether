package com.quantax.gotogether.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.quantax.gotogether.domain.model.JoinRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class JoinRequestRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val requestsCollection = firestore.collection("join_requests")
    private val tripsCollection = firestore.collection("trips")

    suspend fun createRequest(request: JoinRequest): Result<Unit> {
        return try {
            val docRef = requestsCollection.document()
            val newRequest = request.copy(id = docRef.id)
            docRef.set(newRequest).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getRequestsForHost(hostId: String): Flow<List<JoinRequest>> = callbackFlow {
        val listener = requestsCollection
            .whereEqualTo("hostId", hostId)
            .whereEqualTo("status", JoinRequest.STATUS_PENDING)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val requests = snapshot?.toObjects(JoinRequest::class.java) ?: emptyList()
                trySend(requests)
            }
        awaitClose { listener.remove() }
    }

    suspend fun approveRequest(requestId: String, tripId: String, userId: String): Result<Unit> {
        return try {
            // Update request status
            requestsCollection.document(requestId).update("status", JoinRequest.STATUS_APPROVED).await()

            // Add user to trip members
            tripsCollection.document(tripId).update(
                "members", FieldValue.arrayUnion(userId)
            ).await()

            // Increment joinedCount
            tripsCollection.document(tripId).update(
                "joinedCount", FieldValue.increment(1)
            ).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun denyRequest(requestId: String): Result<Unit> {
        return try {
            requestsCollection.document(requestId).update("status", JoinRequest.STATUS_DENIED).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserRequestStatus(tripId: String, userId: String): String? {
        return try {
            val snapshot = requestsCollection
                .whereEqualTo("tripId", tripId)
                .whereEqualTo("requesterId", userId)
                .get()
                .await()
            snapshot.documents.firstOrNull()?.getString("status")
        } catch (e: Exception) {
            null
        }
    }
}
