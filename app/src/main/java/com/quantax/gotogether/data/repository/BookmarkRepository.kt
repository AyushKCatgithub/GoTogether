package com.quantax.gotogether.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookmarkRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun bookmarksCollection(userId: String) =
        firestore.collection("users").document(userId).collection("bookmarks")

    suspend fun toggleBookmark(
        userId: String,
        tripId: String,
        postTitle: String,
        authorId: String,
        thumbnailUrl: String?
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val docRef = bookmarksCollection(userId).document(tripId)
            val snapshot = docRef.get().await()

            if (snapshot.exists()) {
                docRef.delete().await()
                Result.success(false) // now un-bookmarked
            } else {
                val data = hashMapOf(
                    "savedAt" to Timestamp.now(),
                    "postTitle" to postTitle,
                    "authorId" to authorId,
                    "thumbnailUrl" to (thumbnailUrl ?: "")
                )
                docRef.set(data).await()
                Result.success(true) // now bookmarked
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeBookmarkedIds(userId: String): Flow<Set<String>> = callbackFlow {
        val listener = bookmarksCollection(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val ids = snapshot?.documents?.map { it.id }?.toSet() ?: emptySet()
                trySend(ids)
            }
        awaitClose { listener.remove() }
    }

    suspend fun isBookmarked(userId: String, tripId: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                bookmarksCollection(userId).document(tripId).get().await().exists()
            } catch (e: Exception) {
                false
            }
        }

    suspend fun getBookmarkedTrips(userId: String): Result<List<com.quantax.gotogether.domain.model.Trip>> =
        withContext(Dispatchers.IO) {
            try {
                val bookmarkDocs = bookmarksCollection(userId).get().await()
                val tripIds = bookmarkDocs.documents.map { it.id }.filter { it.isNotBlank() }

                if (tripIds.isEmpty()) return@withContext Result.success(emptyList())

                val tripsCollection = firestore.collection("trips")
                val usersCollection = firestore.collection("users")
                val trips = mutableListOf<com.quantax.gotogether.domain.model.Trip>()

                // Firestore whereIn supports max 30 items per query
                tripIds.chunked(30).forEach { chunk ->
                    val snapshot = tripsCollection
                        .whereIn(
                            com.google.firebase.firestore.FieldPath.documentId(),
                            chunk
                        )
                        .get()
                        .await()
                    trips.addAll(
                        snapshot.toObjects(com.quantax.gotogether.domain.model.Trip::class.java)
                    )
                }

                // Enrich with host profile data (name, image, verified badge)
                val hostIds = trips.map { it.hostId }.distinct().filter { it.isNotBlank() }
                val hostProfiles = mutableMapOf<String, com.quantax.gotogether.domain.model.UserProfile>()

                if (hostIds.isNotEmpty()) {
                    hostIds.chunked(30).forEach { chunk ->
                        try {
                            val usersSnapshot = usersCollection
                                .whereIn(
                                    com.google.firebase.firestore.FieldPath.documentId(),
                                    chunk
                                )
                                .get()
                                .await()
                            usersSnapshot.toObjects(com.quantax.gotogether.domain.model.UserProfile::class.java)
                                .forEach { profile ->
                                    hostProfiles[profile.uid] = profile
                                }
                        } catch (_: Exception) {
                            // Continue even if profile enrichment fails
                        }
                    }
                }

                val enrichedTrips = trips.map { trip ->
                    val profile = hostProfiles[trip.hostId]
                    trip.copy(
                        hostName = profile?.name ?: trip.hostName,
                        hostImageUrl = profile?.profileImageUrl?.takeIf { it.isNotBlank() }
                            ?: trip.hostImageUrl,
                        hostVerified = profile?.isVerified ?: trip.hostVerified
                    )
                }

                Result.success(enrichedTrips.sortedByDescending { it.createdAt })
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
