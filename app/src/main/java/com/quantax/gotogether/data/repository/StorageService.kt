package com.quantax.gotogether.data.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class StorageService {
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()

    /**
     * Uploads an image URI to Firebase Storage and returns the public download URL.
     * @param uri The local content:// URI of the image
     * @param folder The folder name in Storage (e.g., "profile_images" or "trip_images")
     * @return The public HTTP download URL, or null if upload fails
     */
    suspend fun uploadImage(uri: Uri, folder: String): String? {
        val user = auth.currentUser ?: return null
        
        return try {
            // Create a unique filename: timestamp_uuid.jpg
            val fileName = "${System.currentTimeMillis()}_${UUID.randomUUID()}.jpg"
            
            // Path: folder/userId/fileName
            val storageRef = storage.reference.child("$folder/${user.uid}/$fileName")
            
            // Upload the file
            storageRef.putFile(uri).await()
            
            // Get the download URL
            val downloadUrl = storageRef.downloadUrl.await()
            downloadUrl.toString()
        } catch (e: Exception) {
            android.util.Log.e("StorageService", "Failed to upload image: ${e.message}", e)
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Helper to check if a URL is a local content URI
     */
    fun isLocalUri(url: String?): Boolean {
        return url?.startsWith("content://") == true || url?.startsWith("file://") == true
    }
}
