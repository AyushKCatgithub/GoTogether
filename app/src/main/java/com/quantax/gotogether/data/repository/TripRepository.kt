package com.quantax.gotogether.data.repository

import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.quantax.gotogether.domain.model.Trip
import com.quantax.gotogether.domain.model.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TripRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val tripsCollection = firestore.collection("trips")
    private val usersCollection = firestore.collection("users")

    suspend fun getTrips(): Result<List<Trip>> = withContext(Dispatchers.IO) {
        try {
            val snapshot = tripsCollection
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .await()
            val trips = snapshot.toObjects(Trip::class.java)

            if (trips.isEmpty()) return@withContext Result.success(emptyList())

            // Optimize: Fetch host profiles in batches (Firestore 'whereIn' supports up to 30 items)
            val hostIds = trips.map { it.hostId }.distinct().filter { it.isNotBlank() }
            val hostProfiles = mutableMapOf<String, UserProfile>()

            if (hostIds.isNotEmpty()) {
                hostIds.chunked(30).forEach { chunk ->
                    try {
                        val usersSnapshot = usersCollection
                            .whereIn(com.google.firebase.firestore.FieldPath.documentId(), chunk)
                            .get()
                            .await()
                        usersSnapshot.toObjects(UserProfile::class.java).forEach { profile ->
                            hostProfiles[profile.uid] = profile
                        }
                    } catch (e: Exception) {
                        // Continue even if profile enrichment fails for some users
                    }
                }
            }

            // Map trips with live host data
            val updatedTrips = trips.map { trip ->
                val profile = hostProfiles[trip.hostId]
                trip.copy(
                    hostName = profile?.name ?: trip.hostName,
                    hostImageUrl = profile?.profileImageUrl?.takeIf { it.isNotBlank() } ?: trip.hostImageUrl,
                    hostVerified = profile?.isVerified ?: trip.hostVerified
                )
            }

            Result.success(updatedTrips)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createTrip(trip: Trip): Result<String> = withContext(Dispatchers.IO) {
        if (trip.title.isBlank() || trip.destination.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Title and destination are required"))
        }

        try {
            val docRef = tripsCollection.document()
            val finalTrip = trip.copy(id = docRef.id)
            docRef.set(finalTrip).await()
            Result.success(finalTrip.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserTrips(userId: String): Result<List<Trip>> = withContext(Dispatchers.IO) {
        if (userId.isBlank()) return@withContext Result.failure(IllegalArgumentException("User ID cannot be blank"))
        
        try {
            val snapshot = tripsCollection
                .whereEqualTo("hostId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            val trips = snapshot.toObjects(Trip::class.java)
            Result.success(trips)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteTrip(tripId: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (tripId.isBlank()) return@withContext Result.failure(IllegalArgumentException("Trip ID cannot be blank"))

        try {
            tripsCollection.document(tripId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTripById(tripId: String): Result<Trip?> = withContext(Dispatchers.IO) {
        if (tripId.isBlank()) return@withContext Result.success(null)

        try {
            val doc = tripsCollection.document(tripId).get().await()
            val trip = doc.toObject(Trip::class.java)
            Result.success(trip)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
