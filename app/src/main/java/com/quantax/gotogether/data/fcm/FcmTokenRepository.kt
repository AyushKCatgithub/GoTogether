package com.quantax.gotogether.data.fcm

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Synchronizes the device's FCM registration token with the signed-in user's
 * Firestore document. Tokens are stored as an array on `users/{uid}.fcmTokens`
 * so a single user can be reached on multiple devices.
 */
@Singleton
class FcmTokenRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val messaging: FirebaseMessaging
) {
    private val usersCollection = firestore.collection("users")

    /**
     * Fetches the current device token and attaches it to the given user.
     * Safe to call on every app launch — Firestore's arrayUnion is idempotent.
     */
    suspend fun registerCurrentToken(uid: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (uid.isBlank()) return@withContext Result.failure(IllegalArgumentException("Empty uid"))
        try {
            val token = messaging.token.await()
            saveToken(uid, token)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch FCM token", e)
            Result.failure(e)
        }
    }

    suspend fun saveToken(uid: String, token: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (uid.isBlank() || token.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Missing uid or token"))
        }
        try {
            usersCollection.document(uid)
                .set(mapOf("fcmTokens" to FieldValue.arrayUnion(token)), com.google.firebase.firestore.SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save FCM token for $uid", e)
            Result.failure(e)
        }
    }

    /**
     * Removes the current device's token from the user document and deletes the
     * underlying FCM registration. Called on sign-out so the device stops receiving
     * pushes targeted at this account.
     */
    suspend fun clearCurrentToken(uid: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (uid.isBlank()) return@withContext Result.success(Unit)
        try {
            val token = runCatching { messaging.token.await() }.getOrNull()
            if (!token.isNullOrBlank()) {
                usersCollection.document(uid)
                    .update("fcmTokens", FieldValue.arrayRemove(token))
                    .await()
            }
            messaging.deleteToken().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear FCM token for $uid", e)
            Result.failure(e)
        }
    }

    private companion object {
        const val TAG = "FcmTokenRepository"
    }
}
