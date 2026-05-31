package com.quantax.gotogether.data.repository

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.quantax.gotogether.domain.model.ChatMessage
import com.quantax.gotogether.domain.model.Chatroom
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val database: FirebaseDatabase
) {
    private val chatroomsCollection = firestore.collection("chatrooms")

    suspend fun createChatroom(chatroom: Chatroom): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            chatroomsCollection.document(chatroom.id).set(chatroom).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error creating chatroom", e)
            Result.failure(e)
        }
    }

    suspend fun getChatroom(chatroomId: String): Result<Chatroom?> = withContext(Dispatchers.IO) {
        try {
            val snapshot = chatroomsCollection.document(chatroomId).get().await()
            Result.success(snapshot.toObject(Chatroom::class.java))
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error fetching chatroom $chatroomId", e)
            Result.failure(e)
        }
    }

    fun getUserChatrooms(userId: String): Flow<List<Chatroom>> = callbackFlow {
        val listener = chatroomsCollection
            .whereArrayContains("memberIds", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ChatRepository", "Failed to load user chatrooms", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val chatrooms = snapshot?.toObjects(Chatroom::class.java) ?: emptyList()
                // Sort locally to avoid needing a composite index for whereArrayContains + orderBy
                val sortedChatrooms = chatrooms.sortedByDescending { it.lastMessageTimestamp }
                trySend(sortedChatrooms)
            }
        awaitClose { listener.remove() }
    }.flowOn(Dispatchers.IO)

    fun getMessages(chatroomId: String): Flow<List<ChatMessage>> = callbackFlow {
        val databaseRef = database.getReference("chatrooms").child(chatroomId)
            .child("messages")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messagesList = mutableListOf<ChatMessage>()
                for (childSnapshot in snapshot.children) {
                    val msg = childSnapshot.getValue(ChatMessage::class.java)
                    if (msg != null) {
                        messagesList.add(msg)
                    }
                }
                // Newer messages at the bottom for normal list, 
                // but since UI uses reverseLayout = true, we sort descending
                messagesList.sortByDescending { it.timestamp }
                trySend(messagesList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(
                    "ChatRepository",
                    "Listen failed for messages in $chatroomId",
                    error.toException()
                )
                // Don't close the flow, just send empty list or handle error in UI
                trySend(emptyList())
            }
        }
        val query = databaseRef.limitToLast(100)
        query.addValueEventListener(listener)
        awaitClose { query.removeEventListener(listener) }
    }.flowOn(Dispatchers.IO)

    suspend fun sendMessage(chatroomId: String, message: ChatMessage): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val databaseRef =
                    database.getReference("chatrooms").child(chatroomId).child("messages").push()
                val messageId = databaseRef.key
                    ?: return@withContext Result.failure(Exception("Could not generate message ID"))

                val newMessage = message.copy(id = messageId, chatroomId = chatroomId)

                // Upload message to Realtime Database
                databaseRef.setValue(newMessage).await()

                // Update chatroom summary in Firestore for the list view
                val lastPreview = when {
                    message.text.isNotBlank() -> message.text
                    !message.imageUrl.isNullOrBlank() -> "📷 Photo"
                    else -> ""
                }
                val updates = mapOf(
                    "lastMessage" to lastPreview,
                    "lastMessageTimestamp" to message.timestamp,
                    "lastMessageSenderName" to message.senderName,
                    "lastMessageSenderId" to message.senderId
                )

                chatroomsCollection.document(chatroomId).set(updates, SetOptions.merge()).await()

                Result.success(Unit)
            } catch (e: Exception) {
                Log.e("ChatRepository", "Error sending message to $chatroomId", e)
                Result.failure(e)
            }
        }

    suspend fun addMemberToChatroom(chatroomId: String, userId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                chatroomsCollection.document(chatroomId).update(
                    "memberIds",
                    com.google.firebase.firestore.FieldValue.arrayUnion(userId)
                ).await()
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e("ChatRepository", "Error adding member $userId to $chatroomId", e)
                Result.failure(e)
            }
        }

    /**
     * Soft-delete a message for a specific user by adding their userId
     * to the message's `deletedFor` list in Realtime Database.
     */
    suspend fun deleteMessageForMe(
        chatroomId: String,
        messageId: String,
        userId: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val messageRef = database.getReference("chatrooms")
                .child(chatroomId).child("messages").child(messageId)
            // Read the current deletedFor list and append
            val snapshot = messageRef.child("deletedFor").get().await()
            val currentList = mutableListOf<String>()
            snapshot.children.forEach { child ->
                child.getValue(String::class.java)?.let { currentList.add(it) }
            }
            if (!currentList.contains(userId)) {
                currentList.add(userId)
            }
            messageRef.child("deletedFor").setValue(currentList).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error deleting message for user $userId", e)
            Result.failure(e)
        }
    }

    /**
     * Permanently delete a message for all users by removing the
     * message node from Realtime Database, then update the chatroom's
     * lastMessage summary in Firestore to reflect the new latest message.
     */
    suspend fun deleteMessageForAll(
        chatroomId: String,
        messageId: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val messagesRef = database.getReference("chatrooms")
                .child(chatroomId).child("messages")

            // Remove the message
            messagesRef.child(messageId).removeValue().await()

            // Fetch the latest remaining message to update Firestore chatroom summary
            // Push keys are chronologically sorted, so limitToLast(1) gives the newest
            val latestSnapshot = messagesRef
                .limitToLast(1)
                .get()
                .await()

            val updates: Map<String, Any> = if (latestSnapshot.hasChildren()) {
                val latestMsg = latestSnapshot.children.first()
                    .getValue(ChatMessage::class.java)
                val preview = when {
                    latestMsg?.text?.isNotBlank() == true -> latestMsg.text
                    !latestMsg?.imageUrl.isNullOrBlank() -> "📷 Photo"
                    else -> ""
                }
                mapOf(
                    "lastMessage" to preview,
                    "lastMessageTimestamp" to (latestMsg?.timestamp ?: 0L),
                    "lastMessageSenderName" to (latestMsg?.senderName ?: ""),
                    "lastMessageSenderId" to (latestMsg?.senderId ?: "")
                )
            } else {
                // No messages left in the chatroom
                mapOf(
                    "lastMessage" to "",
                    "lastMessageTimestamp" to 0L,
                    "lastMessageSenderName" to "",
                    "lastMessageSenderId" to ""
                )
            }

            chatroomsCollection.document(chatroomId)
                .set(updates, SetOptions.merge()).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error deleting message for all", e)
            Result.failure(e)
        }
    }
}
