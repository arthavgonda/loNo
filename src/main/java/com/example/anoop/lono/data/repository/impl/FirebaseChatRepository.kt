package com.example.anoop.lono.data.repository.impl

import com.example.anoop.lono.data.model.Chat
import com.example.anoop.lono.data.model.Message
import com.example.anoop.lono.data.model.ChatReaction
import com.example.anoop.lono.data.model.MessageType
import com.example.anoop.lono.data.repository.ChatRepository
import com.example.anoop.lono.data.util.FirestoreConverters
import com.example.anoop.lono.data.util.FirestoreConverters.toObjectWithDates
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.tasks.await
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import org.threeten.bp.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.firestore.SetOptions

@Singleton
class FirebaseChatRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage
) : ChatRepository {
    companion object {
        private const val TAG = "FirebaseChatRepo"
        private const val CHATS_COLLECTION = "chats"
        private const val MESSAGES_COLLECTION = "messages"
    }

    private val chatsCollection = firestore.collection(CHATS_COLLECTION)
    private val messagesCollection = firestore.collection(MESSAGES_COLLECTION)

    override suspend fun get(id: String): Chat? {
        return try {
            chatsCollection.document(id).get().await().toObjectWithDates<Chat>()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getAll(): List<Chat> {
        return try {
            chatsCollection.get().await().documents.mapNotNull { it.toObjectWithDates<Chat>() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun create(item: Chat): String {
        return try {
            val chatMap = mutableMapOf<String, Any>(
                "id" to item.id,
                "participants" to (item.participants as List<String>),
                "createdAt" to FirestoreConverters.fromLocalDateTime(item.createdAt),
                "updatedAt" to FirestoreConverters.fromLocalDateTime(item.updatedAt),
                "unreadCount" to item.unreadCount,
                "isArchived" to item.isArchived
            )

            item.lastMessage?.let { message ->
                chatMap["lastMessage"] = mutableMapOf<String, Any>(
                    "id" to message.id,
                    "content" to message.content,
                    "senderId" to message.senderId,
                    "senderName" to message.senderName,
                    "timestamp" to FirestoreConverters.fromLocalDateTime(message.timestamp),
                    "type" to message.type.name,
                    "isRead" to message.isRead,
                    "isDeleted" to message.isDeleted
                ).apply {
                    message.mediaUrl?.let { this["mediaUrl"] = it }
                }
            }

            item.lastMessageTime?.let {
                chatMap["lastMessageTime"] = FirestoreConverters.fromLocalDateTime(it)
            }

            val docRef = chatsCollection.document(item.id)
            docRef.set(chatMap).await()
            item.id
        } catch (e: Exception) {
            Log.e(TAG, "Error creating chat: ${e.message}", e)
            throw e
        }
    }

    override suspend fun update(item: Chat): Boolean {
        return try {
            val updates = mutableMapOf<String, Any>(
                "participants" to (item.participants as List<String>),
                "updatedAt" to FirestoreConverters.fromLocalDateTime(item.updatedAt),
                "unreadCount" to item.unreadCount,
                "isArchived" to item.isArchived
            )

            item.lastMessage?.let { message ->
                updates["lastMessage"] = mutableMapOf<String, Any>(
                    "id" to message.id,
                    "content" to message.content,
                    "senderId" to message.senderId,
                    "senderName" to message.senderName,
                    "timestamp" to FirestoreConverters.fromLocalDateTime(message.timestamp),
                    "type" to message.type.name,
                    "isRead" to message.isRead,
                    "isDeleted" to message.isDeleted
                ).apply {
                    message.mediaUrl?.let { this["mediaUrl"] = it }
                }
            }

            item.lastMessageTime?.let {
                updates["lastMessageTime"] = FirestoreConverters.fromLocalDateTime(it)
            }

            val docRef = chatsCollection.document(item.id)
            docRef.set(updates, SetOptions.merge()).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating chat: ${e.message}", e)
            false
        }
    }

    private suspend fun getChatFromSnapshot(snapshot: DocumentSnapshot): Chat? {
        return try {
            snapshot.toObjectWithDates<Chat>()
        } catch (e: Exception) {
            Log.e(TAG, "Error converting chat snapshot: ${e.message}")
            null
        }
    }

    override suspend fun delete(id: String): Boolean {
        return try {
            chatsCollection.document(id).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun exists(id: String): Boolean {
        return try {
            chatsCollection.document(id).get().await().exists()
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getChatByParticipants(participantIds: List<String>): Chat? {
        return try {
            chatsCollection
                .whereEqualTo("participants", participantIds)
                .get()
                .await()
                .documents
                .firstOrNull()
                ?.toObjectWithDates<Chat>()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getMessages(chatId: String, limit: Int, before: LocalDateTime?): List<Message> {
        return try {
            var query = messagesCollection
                .whereEqualTo("chatId", chatId)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit.toLong())

            before?.let {
                query = query.startAfter(FirestoreConverters.fromLocalDateTime(it))
            }

            query.get().await().documents.mapNotNull { it.toObjectWithDates<Message>() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun sendMessage(chatId: String, message: Message): String {
        return try {
            val messageMap = mutableMapOf<String, Any>(
                "id" to message.id,
                "chatId" to chatId,
                "senderId" to message.senderId,
                "senderName" to message.senderName,
                "content" to message.content,
                "timestamp" to FirestoreConverters.fromLocalDateTime(message.timestamp),
                "type" to message.type.name,
                "isRead" to message.isRead,
                "isDeleted" to message.isDeleted,
                "reactions" to (message.reactions as Map<String, String>)
            )

            message.mediaUrl?.let {
                messageMap["mediaUrl"] = it
            }

            val chatUpdates = mutableMapOf<String, Any>(
                "lastMessage" to messageMap,
                "lastMessageTime" to FirestoreConverters.fromLocalDateTime(message.timestamp),
                "updatedAt" to FirestoreConverters.fromLocalDateTime(LocalDateTime.now())
            )

            val batch = firestore.batch()
            val messageRef = messagesCollection.document()
            val chatRef = chatsCollection.document(chatId)

            batch.set(messageRef, messageMap)
            batch.update(chatRef, chatUpdates)
            batch.commit().await()

            messageRef.id
        } catch (e: Exception) {
            Log.e(TAG, "Error sending message: ${e.message}", e)
            throw e
        }
    }

    override suspend fun deleteMessage(chatId: String, messageId: String): Boolean {
        return try {
            messagesCollection.document(messageId).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun markMessageAsRead(chatId: String, messageId: String, userId: String): Boolean {
        return try {
            messagesCollection.document(messageId)
                .update("isRead", true)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun addReaction(chatId: String, messageId: String, reaction: ChatReaction): Boolean {
        return try {
            val reactionMap = mapOf<String, Any>(
                "messageId" to reaction.messageId,
                "userId" to reaction.userId,
                "emoji" to reaction.emoji,
                "timestamp" to FirestoreConverters.fromLocalDateTime(reaction.timestamp)
            )
            messagesCollection.document(messageId)
                .update("reactions.${reaction.userId}", reactionMap)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun removeReaction(chatId: String, messageId: String, userId: String): Boolean {
        return try {
            messagesCollection.document(messageId)
                .update("reactions.$userId", com.google.firebase.firestore.FieldValue.delete())
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getUnreadCount(chatId: String, userId: String): Int {
        return try {
            messagesCollection
                .whereEqualTo("chatId", chatId)
                .whereEqualTo("isRead", false)
                .whereNotEqualTo("senderId", userId)
                .get()
                .await()
                .size()
        } catch (e: Exception) {
            0
        }
    }

    override suspend fun archiveChat(chatId: String, userId: String): Boolean {
        return try {
            val updates = mutableMapOf<String, Any>(
                "isArchived" to true,
                "archivedBy" to userId,
                "archivedAt" to FirestoreConverters.fromLocalDateTime(LocalDateTime.now()),
                "updatedAt" to FirestoreConverters.fromLocalDateTime(LocalDateTime.now())
            )

            chatsCollection.document(chatId)
                .set(updates, SetOptions.merge())
                .await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error archiving chat: ${e.message}", e)
            false
        }
    }

    override suspend fun unarchiveChat(chatId: String, userId: String): Boolean {
        return try {
            val updates = mutableMapOf<String, Any>(
                "isArchived" to false,
                "archivedBy" to userId,
                "archivedAt" to FirestoreConverters.fromLocalDateTime(LocalDateTime.now()),
                "updatedAt" to FirestoreConverters.fromLocalDateTime(LocalDateTime.now())
            )

            chatsCollection.document(chatId)
                .set(updates, SetOptions.merge())
                .await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error unarchiving chat: ${e.message}", e)
            false
        }
    }

    override suspend fun getArchivedChats(userId: String): List<Chat> {
        return try {
            chatsCollection
                .whereEqualTo("isArchived", true)
                .whereEqualTo("archivedBy", userId)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObjectWithDates<Chat>() }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting archived chats: ${e.message}", e)
            emptyList()
        }
    }

    override suspend fun searchMessages(chatId: String, query: String): List<Message> {
        return try {
            messagesCollection
                .whereEqualTo("chatId", chatId)
                .whereGreaterThanOrEqualTo("content", query)
                .whereLessThanOrEqualTo("content", query + "\uf8ff")
                .get()
                .await()
                .documents
                .mapNotNull { it.toObjectWithDates<Message>() }
        } catch (e: Exception) {
            Log.e(TAG, "Error searching messages: ${e.message}", e)
            emptyList()
        }
    }

    override suspend fun getMessagesByDate(chatId: String, date: LocalDateTime): List<Message> {
        return try {
            val startOfDay = date.withHour(0).withMinute(0).withSecond(0)
            val endOfDay = date.withHour(23).withMinute(59).withSecond(59)

            messagesCollection
                .whereEqualTo("chatId", chatId)
                .whereGreaterThanOrEqualTo("timestamp", FirestoreConverters.fromLocalDateTime(startOfDay))
                .whereLessThanOrEqualTo("timestamp", FirestoreConverters.fromLocalDateTime(endOfDay))
                .get()
                .await()
                .documents
                .mapNotNull { it.toObjectWithDates<Message>() }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting messages by date: ${e.message}", e)
            emptyList()
        }
    }

    override suspend fun clearChat(chatId: String, userId: String): Boolean {
        return try {
            val updates = mutableMapOf<String, Any>(
                "lastMessage" to "",
                "lastMessageTime" to null,
                "clearedBy" to userId,
                "clearedAt" to FirestoreConverters.fromLocalDateTime(LocalDateTime.now()),
                "updatedAt" to FirestoreConverters.fromLocalDateTime(LocalDateTime.now())
            )

            val batch = firestore.batch()
            val chatRef = chatsCollection.document(chatId)
            
            // Delete all messages in the chat
            val messages = messagesCollection
                .whereEqualTo("chatId", chatId)
                .get()
                .await()

            messages.documents.forEach { doc ->
                batch.delete(doc.reference)
            }

            // Update chat document
            batch.update(chatRef, updates)
            batch.commit().await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing chat: ${e.message}", e)
            false
        }
    }

    override suspend fun getChats(): List<Chat> {
        return try {
            val currentUserId = auth.currentUser?.uid ?: return emptyList()
            chatsCollection
                .whereArrayContains("participants", currentUserId)
                .whereEqualTo("isArchived", false)
                .orderBy("lastMessageTime", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObjectWithDates<Chat>() }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting chats: ${e.message}", e)
            emptyList()
        }
    }

    override suspend fun createChat(chat: Chat): String {
        return try {
            val docRef = chatsCollection.document()
            val newChat = chat.copy(
                id = docRef.id,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
            docRef.set(newChat).await()
            docRef.id
        } catch (e: Exception) {
            Log.e(TAG, "Error creating chat", e)
            throw e
        }
    }

    override suspend fun updateChatReadStatus(chatId: String, userId: String, isRead: Boolean): Boolean {
        return try {
            val chat = get(chatId) ?: return false
            val updatedUnreadCount = if (isRead) 0 else chat.unreadCount + 1
            
            chatsCollection.document(chatId)
                .update("unreadCount", updatedUnreadCount)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }
} 