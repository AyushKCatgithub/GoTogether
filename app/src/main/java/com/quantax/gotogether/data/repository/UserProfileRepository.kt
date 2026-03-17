package com.quantax.gotogether.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.quantax.gotogether.domain.model.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserProfileRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val usersCollection = firestore.collection("users")

    suspend fun getProfile(uid: String): Result<UserProfile?> = withContext(Dispatchers.IO) {
        try {
            val snapshot = usersCollection.document(uid).get().await()
            Result.success(snapshot.toObject(UserProfile::class.java))
        } catch (e: Exception) {
            android.util.Log.e("UserProfileRepository", "Error getting profile for $uid", e)
            Result.failure(e)
        }
    }

    suspend fun saveProfile(profile: UserProfile): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            usersCollection.document(profile.uid).set(profile).await()
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("UserProfileRepository", "Error saving profile for ${profile.uid}", e)
            Result.failure(e)
        }
    }

    suspend fun getProfiles(uids: List<String>): Result<List<UserProfile>> = withContext(Dispatchers.IO) {
        if (uids.isEmpty()) return@withContext Result.success(emptyList())
        
        try {
            val profiles = mutableListOf<UserProfile>()
            uids.distinct().chunked(30).forEach { chunk ->
                val snapshot = usersCollection
                    .whereIn(com.google.firebase.firestore.FieldPath.documentId(), chunk)
                    .get()
                    .await()
                profiles.addAll(snapshot.toObjects(UserProfile::class.java))
            }
            Result.success(profiles)
        } catch (e: Exception) {
            android.util.Log.e("UserProfileRepository", "Error fetching multiple profiles", e)
            Result.failure(e)
        }
    }
}
