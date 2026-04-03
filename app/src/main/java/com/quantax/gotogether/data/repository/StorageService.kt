package com.quantax.gotogether.data.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageService @Inject constructor(
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth
) {


    /**
     * Uploads an image URI to Firebase Storage and returns the public download URL.
     * @param uri The local content:// URI of the image
     * @param folder The folder name in Storage (e.g., "profile_images" or "trip_images")
     * @return Result containing the public HTTP download URL
     */
    suspend fun uploadImage(uri: Uri, folder: String): Result<String> =
        withContext(Dispatchers.IO) {
            val user = auth.currentUser
                ?: return@withContext Result.failure(Exception("User not authenticated"))

            try {
                // Create a unique filename: timestamp_uuid.jpg
                val fileName = "${System.currentTimeMillis()}_${UUID.randomUUID()}.jpg"

                // Path: folder/userId/fileName
                val storageRef = storage.reference.child("$folder/${user.uid}/$fileName")

                // Upload the file
                storageRef.putFile(uri).await()

                // Get the download URL
                val downloadUrl = storageRef.downloadUrl.await()
                Result.success(downloadUrl.toString())
            } catch (e: Exception) {
                android.util.Log.e("StorageService", "Failed to upload image: ${e.message}", e)
                Result.failure(e)
            }
        }

    /**
     * Deletes an image from Firebase Storage by its URL.
     * @param imageUrl The public download URL of the image
     */
    suspend fun deleteImage(imageUrl: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val storageRef = storage.getReferenceFromUrl(imageUrl)
            storageRef.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("StorageService", "Failed to delete image: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Helper to check if a URL is a local content URI
     */
    fun isLocalUri(url: String?): Boolean {
        return url?.startsWith("content://") == true || url?.startsWith("file://") == true
    }
}
