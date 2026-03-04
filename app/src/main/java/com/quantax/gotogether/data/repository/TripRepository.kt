package com.quantax.gotogether.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.quantax.gotogether.domain.model.Trip
import kotlinx.coroutines.tasks.await

class TripRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val tripsCollection = firestore.collection("trips")

    suspend fun getTrips(): Result<List<Trip>> {
        return try {
            val snapshot = tripsCollection
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            val trips = snapshot.toObjects(Trip::class.java)

            // Fetch host profiles by document ID to get current profile images
            val hostIds = trips.map { it.hostId }.distinct().filter { it.isNotBlank() }
            val hostProfiles = mutableMapOf<String, com.quantax.gotogether.domain.model.UserProfile>()
            val usersCollection = firestore.collection("users")
            hostIds.forEach { uid ->
                try {
                    val doc = usersCollection.document(uid).get().await()
                    val profile = doc.toObject(com.quantax.gotogether.domain.model.UserProfile::class.java)
                    if (profile != null) {
                        hostProfiles[profile.uid] = profile
                    }
                } catch (_: Exception) {
                    // Skip if individual profile fetch fails
                }
            }

            // Override hostImageUrl and hostVerified with live data
            val updatedTrips = trips.map { trip ->
                val hostProfile = hostProfiles[trip.hostId]
                if (hostProfile != null) {
                    trip.copy(
                        hostImageUrl = hostProfile.profileImageUrl.ifBlank { trip.hostImageUrl },
                        hostVerified = hostProfile.isVerified
                    )
                } else {
                    trip
                }
            }

            Result.success(updatedTrips)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createTrip(trip: Trip): Result<String> {
        return try {
            val documentRef = tripsCollection.document()
            val newTrip = trip.copy(id = documentRef.id)
            documentRef.set(newTrip).await()
            Result.success(newTrip.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserTrips(userId: String): Result<List<Trip>> {
        return try {
            val snapshot = tripsCollection
                .whereEqualTo("hostId", userId)
                .get()
                .await()
            val trips = snapshot.toObjects(Trip::class.java)
                .sortedByDescending { it.createdAt }
            Result.success(trips)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteTrip(tripId: String): Result<Unit> {
        return try {
            tripsCollection.document(tripId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTripById(tripId: String): Result<Trip?> {
        return try {
            val snapshot = tripsCollection.document(tripId).get().await()
            val trip = snapshot.toObject(Trip::class.java)
            Result.success(trip)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
