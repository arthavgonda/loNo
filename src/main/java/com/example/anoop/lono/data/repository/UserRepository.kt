package com.example.anoop.lono.data.repository

import com.example.anoop.lono.data.model.User
import com.example.anoop.lono.data.model.SpecialDate
import com.example.anoop.lono.data.model.RelationshipMilestone

interface UserRepository : BaseRepository<User> {
    suspend fun getUserByEmail(email: String): User?
    suspend fun updateProfilePicture(userId: String, pictureUrl: String): Boolean
    suspend fun updateFcmToken(userId: String, token: String): Boolean
    suspend fun updateLastActive(userId: String): Boolean
    suspend fun addSpecialDate(userId: String, specialDate: SpecialDate): Boolean
    suspend fun removeSpecialDate(userId: String, specialDateId: String): Boolean
    suspend fun addRelationshipMilestone(userId: String, milestone: RelationshipMilestone): Boolean
    suspend fun removeRelationshipMilestone(userId: String, milestoneId: String): Boolean
    suspend fun updateLoveLanguage(userId: String, loveLanguage: String): Boolean
    suspend fun updatePartnerId(userId: String, partnerId: String?): Boolean
    suspend fun getPartner(userId: String): User?
    suspend fun searchUsers(query: String): List<User>
    suspend fun sendPartnerRequest(userId: String, partnerId: String): Boolean
    suspend fun acceptPartnerRequest(userId: String, partnerId: String): Boolean
    suspend fun rejectPartnerRequest(userId: String, partnerId: String): Boolean
} 