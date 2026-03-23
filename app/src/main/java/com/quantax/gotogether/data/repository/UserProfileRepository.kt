package com.quantax.gotogether.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.quantax.gotogether.domain.model.UserProfile
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
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
    private val usernamesCollection = firestore.collection("usernames")

    /** Normalize for uniqueness: lowercase, trim, letters/digits/underscore/dot only (Instagram-style). */
    private fun normalizeUsername(username: String): String =
        username.trim().lowercase().filter { it.isLetterOrDigit() || it == '_' || it == '.' }

    /**
     * Returns true if the username is available for use.
     * Checks the dedicated `usernames` collection.
     */
    suspend fun isUsernameAvailable(username: String, excludeUid: String): Result<Boolean> = withContext(Dispatchers.IO) {
        val normalized = normalizeUsername(username)
        if (normalized.isBlank()) return@withContext Result.success(true)
        try {
            val doc = usernamesCollection.document(normalized).get().await()
            if (!doc.exists()) {
                Result.success(true)
            } else {
                val ownerUid = doc.getString("uid")
                Result.success(ownerUid == excludeUid)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Backfills usernameLower for the given user document so future uniqueness checks work.
     * Call when profile has username set but usernameLower blank (e.g. after loading profile).
     */
    suspend fun updateUsernameLowerIfNeeded(uid: String, username: String): Result<Unit> {
        if (username.isBlank()) return Result.success(Unit)
        return try {
            val normalized = normalizeUsername(username)
            usersCollection.document(uid).update("usernameLower", normalized).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Real-time flow of profile updates. Emits whenever the profile document changes.
     */
    fun getProfileFlow(uid: String): Flow<UserProfile?> = callbackFlow {
        val listener = usersCollection.document(uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(null)
                return@addSnapshotListener
            }
            val profile = snapshot?.toObject(UserProfile::class.java)
            trySend(profile)
        }
        awaitClose { listener.remove() }
    }

    suspend fun getProfile(uid: String): Result<UserProfile?> = withContext(Dispatchers.IO) {
        try {
            val snapshot = usersCollection.document(uid).get().await()
            Result.success(snapshot.toObject(UserProfile::class.java))
        } catch (e: Exception) {
            android.util.Log.e("UserProfileRepository", "Error getting profile for $uid", e)
            Result.failure(e)
        }
    }

    /**
     * Saves the profile and enforces unique username.
     * Uses a Firestore batched write to update the user profile and the reserved username document atomically.
     */
    suspend fun saveProfile(profile: UserProfile, oldUsername: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val newNormalized = normalizeUsername(profile.username)
            val oldNormalized = oldUsername?.let { normalizeUsername(it) } ?: ""

            val profileToSave = profile.copy(
                usernameLower = if (profile.username.isNotBlank()) newNormalized else ""
            )

            if (newNormalized.isBlank() || newNormalized == oldNormalized) {
                // No username change or cleared username. Just save profile.
                val batch = firestore.batch()
                batch.set(usersCollection.document(profileToSave.uid), profileToSave)
                if (oldNormalized.isNotBlank() && oldNormalized != newNormalized) {
                    batch.delete(usernamesCollection.document(oldNormalized))
                }
                batch.commit().await()
                return@withContext Result.success(Unit)
            }

            // Transactional update to safely claim the new username
            firestore.runTransaction { transaction ->
                val newUsernameRef = usernamesCollection.document(newNormalized)
                val newUsernameDoc = transaction.get(newUsernameRef)

                if (newUsernameDoc.exists()) {
                    val ownerUid = newUsernameDoc.getString("uid")
                    if (ownerUid != profile.uid) {
                        throw Exception("This username is already taken.")
                    }
                }

                // Claim the new username
                transaction.set(newUsernameRef, mapOf("uid" to profile.uid, "username" to profile.username))
                
                // Release the old username if it changed
                if (oldNormalized.isNotBlank() && oldNormalized != newNormalized) {
                    transaction.delete(usernamesCollection.document(oldNormalized))
                }

                // Save profile
                transaction.set(usersCollection.document(profileToSave.uid), profileToSave)
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("UserProfileRepository", "Error saving profile for \${profile.uid}", e)
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
