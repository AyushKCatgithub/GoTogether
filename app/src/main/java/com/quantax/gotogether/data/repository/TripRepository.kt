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
            Result.success(trips)
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
