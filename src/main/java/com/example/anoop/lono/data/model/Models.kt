package com.example.anoop.lono.data.model

import org.threeten.bp.LocalDateTime

data class Message(
    val id: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val content: String = "",
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val type: MessageType = MessageType.TEXT,
    val mediaUrl: String? = null,
    val reactions: Map<String, String> = emptyMap(), // userId to emoji
    val isRead: Boolean = false,
    val isDeleted: Boolean = false
)

enum class MessageType {
    TEXT,
    IMAGE,
    VIDEO,
    AUDIO,
    LOCATION,
    STICKER,
    SYSTEM
}

data class Chat(
    val id: String = "",
    val participants: List<String> = emptyList(),
    val lastMessage: Message? = null,
    val lastMessageTime: LocalDateTime? = null,
    val unreadCount: Int = 0,
    val isArchived: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

data class ChatReaction(
    val messageId: String,
    val userId: String,
    val emoji: String,
    val timestamp: LocalDateTime = LocalDateTime.now()
)

data class Memory(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val date: LocalDateTime = LocalDateTime.now(),
    val albumId: String? = null,
    val imageUrls: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val location: Location? = null,
    val likes: List<String> = emptyList(),
    val comments: List<Comment> = emptyList(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

data class Album(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val description: String = "",
    val coverImageUrl: String = "",
    val memoryCount: Int = 0,
    val memoryIds: List<String> = emptyList(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

data class Challenge(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val type: String = "",
    val points: Int = 0,
    val startDate: LocalDateTime = LocalDateTime.now(),
    val endDate: LocalDateTime = LocalDateTime.now(),
    val requirements: List<String> = emptyList(),
    val rewards: List<String> = emptyList(),
    val participants: List<String> = emptyList(),
    val completedBy: List<String> = emptyList(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

data class ChallengeProgress(
    val id: String = "",
    val challengeId: String = "",
    val userId: String = "",
    val progress: Int = 0,
    val completed: Boolean = false,
    val lastUpdated: LocalDateTime = LocalDateTime.now(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

data class TodoList(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val description: String = "",
    val items: List<TodoItem> = emptyList(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

data class TodoItem(
    val id: String = "",
    val listId: String = "",
    val title: String = "",
    val description: String = "",
    val dueDate: LocalDateTime? = null,
    val priority: String = "",
    val completed: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

data class Location(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val name: String? = null
)

data class Comment(
    val id: String = "",
    val userId: String = "",
    val content: String = "",
    val userName: String = "",
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val likes: List<String> = emptyList(),
    val replies: List<Comment> = emptyList(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) 