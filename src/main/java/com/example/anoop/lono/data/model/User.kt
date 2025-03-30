package com.example.anoop.lono.data.model

import org.threeten.bp.LocalDateTime

data class User(
    val id: String = "",
    val email: String = "",
    val name: String = "",
    val profilePictureUrl: String? = null,
    val partnerId: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val lastActive: LocalDateTime = LocalDateTime.now(),
    val fcmToken: String? = null,
    val loveLanguage: String? = null,
    val specialDates: List<SpecialDate> = emptyList(),
    val relationshipMilestones: List<RelationshipMilestone> = emptyList(),
    val settings: UserSettings = UserSettings()
)

data class UserSettings(
    val theme: String = "light",
    val notificationsEnabled: Boolean = true,
    val chatBubbleColor: String = "#FF69B4",
    val privacyMode: Boolean = true
)

data class SpecialDate(
    val id: String = "",
    val title: String = "",
    val date: LocalDateTime,
    val description: String? = null,
    val reminderDays: Int = 5,
    val isRecurring: Boolean = false
)

data class RelationshipMilestone(
    val id: String = "",
    val title: String = "",
    val date: LocalDateTime,
    val description: String? = null,
    val badge: String? = null
) 