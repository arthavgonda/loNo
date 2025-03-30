package com.example.anoop.lono.data.repository

import com.example.anoop.lono.data.model.Chat
import com.example.anoop.lono.data.model.Message
import com.example.anoop.lono.data.model.ChatReaction
import org.threeten.bp.LocalDateTime

interface ChatRepository : BaseRepository<Chat> {
    suspend fun getChats(): List<Chat>
    suspend fun createChat(chat: Chat): String
    suspend fun updateChatReadStatus(chatId: String, userId: String, isRead: Boolean): Boolean
    suspend fun getChatByParticipants(participantIds: List<String>): Chat?
    suspend fun getMessages(chatId: String, limit: Int = 50, before: LocalDateTime? = null): List<Message>
    suspend fun sendMessage(chatId: String, message: Message): String
    suspend fun deleteMessage(chatId: String, messageId: String): Boolean
    suspend fun markMessageAsRead(chatId: String, messageId: String, userId: String): Boolean
    suspend fun addReaction(chatId: String, messageId: String, reaction: ChatReaction): Boolean
    suspend fun removeReaction(chatId: String, messageId: String, userId: String): Boolean
    suspend fun getUnreadCount(chatId: String, userId: String): Int
    suspend fun archiveChat(chatId: String, userId: String): Boolean
    suspend fun unarchiveChat(chatId: String, userId: String): Boolean
    suspend fun getArchivedChats(userId: String): List<Chat>
    suspend fun searchMessages(chatId: String, query: String): List<Message>
    suspend fun getMessagesByDate(chatId: String, date: LocalDateTime): List<Message>
    suspend fun clearChat(chatId: String, userId: String): Boolean
} 