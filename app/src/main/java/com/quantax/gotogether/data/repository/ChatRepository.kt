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
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ChatRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val chatroomsCollection = firestore.collection("chatrooms")

    suspend fun createChatroom(chatroom: Chatroom): Result<Unit> {
        return try {
            chatroomsCollection.document(chatroom.id).set(chatroom).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getChatroom(chatroomId: String): Result<Chatroom?> {
        return try {
            val snapshot = chatroomsCollection.document(chatroomId).get().await()
            val chatroom = snapshot.toObject(Chatroom::class.java)
            Result.success(chatroom)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getUserChatrooms(userId: String): Flow<List<Chatroom>> = callbackFlow {
        val listener = chatroomsCollection
            .whereArrayContains("memberIds", userId)
            // Removed orderBy because combining whereArrayContains and orderBy 
            // requires a custom Firestore composite index, which causes silent failures.
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ChatRepository", "Failed to load user chatrooms", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val chatrooms = snapshot?.toObjects(Chatroom::class.java) ?: emptyList()
                // Sort locally instead to avoid the index requirement
                val sortedChatrooms = chatrooms.sortedByDescending { it.lastMessageTimestamp }
                trySend(sortedChatrooms)
            }
        awaitClose { listener.remove() }
    }

    fun getMessages(chatroomId: String): Flow<List<ChatMessage>> = callbackFlow {
        val databaseRef = FirebaseDatabase.getInstance().getReference("chatrooms").child(chatroomId).child("messages")
        
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messagesList = mutableListOf<ChatMessage>()
                for (childSnapshot in snapshot.children) {
                    val msg = childSnapshot.getValue(ChatMessage::class.java)
                    if (msg != null) {
                        messagesList.add(msg)
                    }
                }
                messagesList.sortByDescending { it.timestamp }
                trySend(messagesList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatRepository", "Listen failed for messages in $chatroomId", error.toException())
                trySend(emptyList())
            }
        }
        databaseRef.addValueEventListener(listener)
        awaitClose { databaseRef.removeEventListener(listener) }
    }

    suspend fun sendMessage(chatroomId: String, message: ChatMessage): Result<Unit> {
        return try {
            val databaseRef = FirebaseDatabase.getInstance().getReference("chatrooms").child(chatroomId).child("messages").push()
            val newMessage = message.copy(id = databaseRef.key ?: "", chatroomId = chatroomId)
            databaseRef.setValue(newMessage).await()

            // Update last message on chatroom doc safely creating it if missing
            chatroomsCollection.document(chatroomId).set(
                mapOf(
                    "lastMessage" to message.text,
                    "lastMessageTimestamp" to message.timestamp,
                    "lastMessageSenderName" to message.senderName
                ),
                SetOptions.merge()
            ).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error sending message", e)
            Result.failure(e)
        }
    }

    suspend fun addMemberToChatroom(chatroomId: String, userId: String): Result<Unit> {
        return try {
            chatroomsCollection.document(chatroomId).update(
                "memberIds",
                com.google.firebase.firestore.FieldValue.arrayUnion(userId)
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
