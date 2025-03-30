package com.example.anoop.lono.data.repository.impl

import com.example.anoop.lono.data.model.User
import com.example.anoop.lono.data.model.SpecialDate
import com.example.anoop.lono.data.model.RelationshipMilestone
import com.example.anoop.lono.data.repository.UserRepository
import com.example.anoop.lono.data.util.FirestoreConverters
import com.example.anoop.lono.data.util.FirestoreConverters.toObjectWithDates
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log

@Singleton
class FirebaseUserRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val messaging: FirebaseMessaging
) : UserRepository {

    companion object {
        private const val TAG = "FirebaseUserRepo"
        private const val USERS_COLLECTION = "users"
    }

    private val usersCollection = firestore.collection(USERS_COLLECTION)

    override suspend fun get(id: String): User? {
        return try {
            usersCollection.document(id).get().await().toObjectWithDates<User>()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getAll(): List<User> {
        return try {
            usersCollection.get().await().documents.mapNotNull { it.toObjectWithDates<User>() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun create(item: User): String {
        return try {
            val docRef = usersCollection.document(item.id)
            val user = item.copy(
                createdAt = LocalDateTime.now(),
                lastActive = LocalDateTime.now()
            )
            docRef.set(user).await()
            docRef.id
        } catch (e: Exception) {
            Log.e(TAG, "Error creating user", e)
            throw e
        }
    }

    override suspend fun update(item: User): Boolean {
        return try {
            val user = item.copy(lastActive = LocalDateTime.now())
            usersCollection.document(item.id).set(user).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user", e)
            false
        }
    }

    override suspend fun delete(id: String): Boolean {
        return try {
            usersCollection.document(id).delete().await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting user", e)
            false
        }
    }

    override suspend fun exists(id: String): Boolean {
        return try {
            usersCollection.document(id).get().await().exists()
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getUserByEmail(email: String): User? {
        return try {
            usersCollection
                .whereEqualTo("email", email)
                .get()
                .await()
                .documents
                .firstOrNull()
                ?.toObjectWithDates<User>()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun updateProfilePicture(userId: String, pictureUrl: String): Boolean {
        return try {
            usersCollection.document(userId)
                .update("profilePictureUrl", pictureUrl)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun updateFcmToken(userId: String, token: String): Boolean {
        return try {
            usersCollection.document(userId)
                .update("fcmToken", token)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun updateLastActive(userId: String): Boolean {
        return try {
            usersCollection.document(userId)
                .update("lastActive", FirestoreConverters.fromLocalDateTime(LocalDateTime.now()))
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun addSpecialDate(userId: String, specialDate: SpecialDate): Boolean {
        return try {
            val specialDateMap = mapOf(
                "id" to specialDate.id,
                "title" to specialDate.title,
                "date" to FirestoreConverters.fromLocalDateTime(specialDate.date),
                "description" to specialDate.description,
                "reminderDays" to specialDate.reminderDays,
                "isRecurring" to specialDate.isRecurring
            )
            usersCollection.document(userId)
                .update("specialDates", com.google.firebase.firestore.FieldValue.arrayUnion(specialDateMap))
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun removeSpecialDate(userId: String, specialDateId: String): Boolean {
        return try {
            val user = get(userId) ?: return false
            val updatedDates = user.specialDates.filter { it.id != specialDateId }
            val updatedDatesMap = updatedDates.map { specialDate ->
                mapOf(
                    "id" to specialDate.id,
                    "title" to specialDate.title,
                    "date" to FirestoreConverters.fromLocalDateTime(specialDate.date),
                    "description" to specialDate.description,
                    "reminderDays" to specialDate.reminderDays,
                    "isRecurring" to specialDate.isRecurring
                )
            }
            usersCollection.document(userId)
                .update("specialDates", updatedDatesMap)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun addRelationshipMilestone(userId: String, milestone: RelationshipMilestone): Boolean {
        return try {
            val milestoneMap = mapOf(
                "id" to milestone.id,
                "title" to milestone.title,
                "date" to FirestoreConverters.fromLocalDateTime(milestone.date),
                "description" to milestone.description,
                "badge" to milestone.badge
            )
            usersCollection.document(userId)
                .update("relationshipMilestones", com.google.firebase.firestore.FieldValue.arrayUnion(milestoneMap))
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun removeRelationshipMilestone(userId: String, milestoneId: String): Boolean {
        return try {
            val user = get(userId) ?: return false
            val updatedMilestones = user.relationshipMilestones.filter { it.id != milestoneId }
            val updatedMilestonesMap = updatedMilestones.map { milestone ->
                mapOf(
                    "id" to milestone.id,
                    "title" to milestone.title,
                    "date" to FirestoreConverters.fromLocalDateTime(milestone.date),
                    "description" to milestone.description,
                    "badge" to milestone.badge
                )
            }
            usersCollection.document(userId)
                .update("relationshipMilestones", updatedMilestonesMap)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun updateLoveLanguage(userId: String, loveLanguage: String): Boolean {
        return try {
            usersCollection.document(userId)
                .update("loveLanguage", loveLanguage)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun updatePartnerId(userId: String, partnerId: String?): Boolean {
        return try {
            usersCollection.document(userId)
                .update("partnerId", partnerId)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getPartner(userId: String): User? {
        return try {
            val user = get(userId) ?: return null
            user.partnerId?.let { get(it) }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun searchUsers(query: String): List<User> {
        return try {
            usersCollection
                .orderBy("name")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .get()
                .await()
                .documents
                .mapNotNull { it.toObjectWithDates<User>() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun sendPartnerRequest(userId: String, partnerId: String): Boolean {
        return try {
            firestore.collection("partner_requests")
                .add(mapOf(
                    "fromUserId" to userId,
                    "toUserId" to partnerId,
                    "status" to "pending",
                    "timestamp" to FirestoreConverters.fromLocalDateTime(LocalDateTime.now())
                ))
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun acceptPartnerRequest(userId: String, partnerId: String): Boolean {
        return try {
            val batch = firestore.batch()

            // Update both users' partner IDs
            batch.update(usersCollection.document(userId), "partnerId", partnerId)
            batch.update(usersCollection.document(partnerId), "partnerId", userId)

            // Update request status
            val requestQuery = firestore.collection("partner_requests")
                .whereEqualTo("fromUserId", partnerId)
                .whereEqualTo("toUserId", userId)
                .whereEqualTo("status", "pending")
                .get()
                .await()

            requestQuery.documents.forEach { doc ->
                batch.update(doc.reference, "status", "accepted")
            }

            batch.commit().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun rejectPartnerRequest(userId: String, partnerId: String): Boolean {
        return try {
            val requestQuery = firestore.collection("partner_requests")
                .whereEqualTo("fromUserId", partnerId)
                .whereEqualTo("toUserId", userId)
                .whereEqualTo("status", "pending")
                .get()
                .await()

            val batch = firestore.batch()
            requestQuery.documents.forEach { doc ->
                batch.update(doc.reference, "status", "rejected")
            }

            batch.commit().await()
            true
        } catch (e: Exception) {
            false
        }
    }
}