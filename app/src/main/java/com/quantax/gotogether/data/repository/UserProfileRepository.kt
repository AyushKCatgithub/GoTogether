package com.quantax.gotogether.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.quantax.gotogether.domain.model.UserProfile
import kotlinx.coroutines.tasks.await

class UserProfileRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")

    suspend fun getProfile(uid: String): Result<UserProfile?> {
        return try {
            val snapshot = usersCollection.document(uid).get().await()
            if (snapshot.exists()) {
                val profile = snapshot.toObject(UserProfile::class.java)
                Result.success(profile)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveProfile(profile: UserProfile): Result<Unit> {
        return try {
            usersCollection.document(profile.uid).set(profile).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
