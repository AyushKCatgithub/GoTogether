package com.quantax.gotogether.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.quantax.gotogether.domain.model.JoinRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JoinRequestRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
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
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val requests = snapshot?.toObjects(JoinRequest::class.java) ?: emptyList()
                trySend(requests.sortedByDescending { it.timestamp })
            }
        awaitClose { listener.remove() }
    }

    /**
     * Atomically approves a join request using a Firestore batch write.
     * Updates the request status, adds the user to trip members, and increments joinedCount
     * all in a single atomic operation — no partial state possible.
     */
    suspend fun approveRequest(requestId: String, tripId: String, userId: String): Result<Unit> {
        return try {
            val batch = firestore.batch()

            // 1. Mark request as approved
            val requestRef = requestsCollection.document(requestId)
            batch.update(requestRef, "status", JoinRequest.STATUS_APPROVED)

            // 2. Add user to trip members array
            val tripRef = tripsCollection.document(tripId)
            batch.update(tripRef, "members", FieldValue.arrayUnion(userId))

            // 3. Increment joinedCount
            batch.update(tripRef, "joinedCount", FieldValue.increment(1))

            // Commit all three writes atomically
            batch.commit().await()
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

    fun getUserRequestStatusFlow(tripId: String, userId: String): Flow<String?> = callbackFlow {
        if (tripId.isBlank() || userId.isBlank()) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val listener = requestsCollection
            .whereEqualTo("tripId", tripId)
            .whereEqualTo("requesterId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val status = snapshot?.documents?.firstOrNull()?.getString("status")
                trySend(status)
            }
        awaitClose { listener.remove() }
    }

    /**
     * Deletes every join request tied to a trip. Used when a trip is removed
     * (manually or because it expired) so no orphaned requests are left behind.
     */
    suspend fun deleteRequestsForTrip(tripId: String): Result<Unit> {
        if (tripId.isBlank()) return Result.failure(IllegalArgumentException("Trip ID cannot be blank"))
        return try {
            val snapshot = requestsCollection.whereEqualTo("tripId", tripId).get().await()
            if (!snapshot.isEmpty) {
                val batch = firestore.batch()
                snapshot.documents.forEach { batch.delete(it.reference) }
                batch.commit().await()
            }
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
