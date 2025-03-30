package com.example.anoop.lono.data.repository.impl

import com.example.anoop.lono.data.model.Challenge
import com.example.anoop.lono.data.model.TodoList
import com.example.anoop.lono.data.model.TodoItem
import com.example.anoop.lono.data.model.ChallengeProgress
import com.example.anoop.lono.data.repository.ChallengeRepository
import com.example.anoop.lono.data.util.FirestoreConverters
import com.example.anoop.lono.data.util.FirestoreConverters.toObjectWithDates
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import org.threeten.bp.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log

@Singleton
class FirebaseChallengeRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ChallengeRepository {

    companion object {
        private const val TAG = "FirebaseChallengeRepo"
        private const val CHALLENGES_COLLECTION = "challenges"
        private const val PROGRESS_COLLECTION = "progress"
        private const val TODO_LISTS_COLLECTION = "todoLists"
        private const val TODO_ITEMS_COLLECTION = "todoItems"
    }

    private val challengesCollection = firestore.collection(CHALLENGES_COLLECTION)
    private val progressCollection = firestore.collection(PROGRESS_COLLECTION)
    private val todoListsCollection = firestore.collection(TODO_LISTS_COLLECTION)

    override suspend fun get(id: String): Challenge? {
        return try {
            val doc = challengesCollection.document(id).get().await()
            doc.toObjectWithDates<Challenge>()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting challenge: ${e.message}")
            null
        }
    }

    override suspend fun getAll(): List<Challenge> {
        return try {
            challengesCollection.get().await().documents
                .mapNotNull { it.toObjectWithDates<Challenge>() }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting all challenges: ${e.message}")
            emptyList()
        }
    }

    override suspend fun create(challenge: Challenge): Challenge {
        return try {
            val docRef = challengesCollection.document()
            val newChallenge = challenge.copy(
                id = docRef.id,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
            docRef.set(newChallenge).await()
            newChallenge
        } catch (e: Exception) {
            Log.e(TAG, "Error creating challenge: ${e.message}")
            throw e
        }
    }

    override suspend fun update(challenge: Challenge): Challenge {
        return try {
            val updatedChallenge = challenge.copy(updatedAt = LocalDateTime.now())
            challengesCollection.document(challenge.id).set(updatedChallenge).await()
            updatedChallenge
        } catch (e: Exception) {
            Log.e(TAG, "Error updating challenge: ${e.message}")
            throw e
        }
    }

    override suspend fun delete(id: String) {
        try {
            challengesCollection.document(id).delete().await()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting challenge: ${e.message}")
            throw e
        }
    }

    override suspend fun exists(id: String): Boolean {
        return try {
            val doc = challengesCollection.document(id).get().await()
            doc.exists()
        } catch (e: Exception) {
            Log.e(TAG, "Error checking if challenge exists: ${e.message}")
            false
        }
    }

    override suspend fun joinChallenge(challengeId: String): Boolean {
        return try {
            val userId = auth.currentUser?.uid ?: return false
            val progress = ChallengeProgress(
                id = "${challengeId}_${userId}",
                challengeId = challengeId,
                userId = userId,
                progress = 0,
                completed = false,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
            challengesCollection.document(challengeId)
                .collection(PROGRESS_COLLECTION)
                .document(userId)
                .set(progress)
                .await()
            challengesCollection.document(challengeId)
                .update("participants", FieldValue.arrayUnion(userId))
                .await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error joining challenge: ${e.message}")
            false
        }
    }

    override suspend fun leaveChallenge(challengeId: String): Boolean {
        return try {
            val userId = auth.currentUser?.uid ?: return false
            challengesCollection.document(challengeId)
                .collection(PROGRESS_COLLECTION)
                .document(userId)
                .delete()
                .await()
            challengesCollection.document(challengeId)
                .update("participants", FieldValue.arrayRemove(userId))
                .await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error leaving challenge: ${e.message}")
            false
        }
    }

    override suspend fun updateProgress(challengeId: String, progress: Int): Boolean {
        return try {
            val userId = auth.currentUser?.uid ?: return false
            val progressRef = challengesCollection
                .document(challengeId)
                .collection(PROGRESS_COLLECTION)
                .document(userId)
            
            val updates = hashMapOf<String, Any>(
                "progress" to progress,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            
            progressRef.set(updates, SetOptions.merge()).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating challenge progress: ${e.message}")
            false
        }
    }

    override suspend fun completeChallenge(challengeId: String): Boolean {
        return try {
            val challengeRef = challengesCollection.document(challengeId)
            val updates = hashMapOf<String, Any>(
                "completed" to true,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            challengeRef.update(updates).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error completing challenge: ${e.message}")
            false
        }
    }

    override suspend fun getActiveUserChallenges(): List<Challenge> {
        return try {
            val userId = auth.currentUser?.uid ?: return emptyList()
            val snapshots = challengesCollection
                .whereArrayContains("participants", userId)
                .whereEqualTo("completed", false)
                .get()
                .await()
            
            snapshots.documents.mapNotNull { getChallengeFromSnapshot(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting active user challenges: ${e.message}")
            emptyList()
        }
    }

    override suspend fun getCompletedUserChallenges(): List<Challenge> {
        return try {
            val userId = auth.currentUser?.uid ?: return emptyList()
            val snapshots = challengesCollection
                .whereArrayContains("participants", userId)
                .whereEqualTo("completed", true)
                .get()
                .await()
            
            snapshots.documents.mapNotNull { getChallengeFromSnapshot(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting completed user challenges: ${e.message}")
            emptyList()
        }
    }

    override suspend fun getProgress(challengeId: String): ChallengeProgress? {
        return try {
            val userId = auth.currentUser?.uid ?: return null
            val progressRef = challengesCollection
                .document(challengeId)
                .collection(PROGRESS_COLLECTION)
                .document(userId)
                .get()
                .await()
            
            progressRef.toObjectWithDates<ChallengeProgress>()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting challenge progress: ${e.message}")
            null
        }
    }

    override suspend fun createTodoList(todoList: TodoList): String {
        return try {
            val docRef = todoListsCollection.document()
            val newTodoList = todoList.copy(
                id = docRef.id,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
            docRef.set(newTodoList).await()
            docRef.id
        } catch (e: Exception) {
            Log.e(TAG, "Error creating todo list: ${e.message}")
            throw e
        }
    }

    override suspend fun getTodoList(id: String): TodoList? {
        return try {
            val doc = todoListsCollection.document(id).get().await()
            doc.toObjectWithDates<TodoList>()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting todo list: ${e.message}")
            null
        }
    }

    override suspend fun updateTodoList(todoList: TodoList): Boolean {
        return try {
            val updatedTodoList = todoList.copy(updatedAt = LocalDateTime.now())
            todoListsCollection.document(todoList.id).set(updatedTodoList).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating todo list: ${e.message}")
            false
        }
    }

    override suspend fun deleteTodoList(id: String): Boolean {
        return try {
            todoListsCollection.document(id).delete().await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting todo list: ${e.message}")
            false
        }
    }

    override suspend fun getUserTodoLists(userId: String): List<TodoList> {
        return try {
            todoListsCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObjectWithDates<TodoList>() }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user todo lists: ${e.message}")
            emptyList()
        }
    }

    override suspend fun addTodoItem(todoListId: String, item: TodoItem): String {
        return try {
            val docRef = todoListsCollection.document(todoListId)
                .collection(TODO_ITEMS_COLLECTION)
                .document()
            val newItem = item.copy(
                id = docRef.id,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
            docRef.set(newItem).await()
            docRef.id
        } catch (e: Exception) {
            Log.e(TAG, "Error adding todo item: ${e.message}")
            throw e
        }
    }

    override suspend fun updateTodoItem(todoListId: String, item: TodoItem): Boolean {
        return try {
            val updatedItem = item.copy(updatedAt = LocalDateTime.now())
            todoListsCollection.document(todoListId)
                .collection(TODO_ITEMS_COLLECTION)
                .document(item.id)
                .set(updatedItem)
                .await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating todo item: ${e.message}")
            false
        }
    }

    override suspend fun deleteTodoItem(todoListId: String, itemId: String): Boolean {
        return try {
            todoListsCollection.document(todoListId)
                .collection(TODO_ITEMS_COLLECTION)
                .document(itemId)
                .delete()
                .await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting todo item: ${e.message}")
            false
        }
    }

    override suspend fun completeTodoItem(todoListId: String, itemId: String, userId: String): Boolean {
        return try {
            todoListsCollection.document(todoListId)
                .collection(TODO_ITEMS_COLLECTION)
                .document(itemId)
                .update(
                    mapOf(
                        "completed" to true,
                        "completedBy" to userId,
                        "completedAt" to LocalDateTime.now(),
                        "updatedAt" to LocalDateTime.now()
                    )
                )
                .await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error completing todo item: ${e.message}")
            false
        }
    }

    override suspend fun uncompleteTodoItem(todoListId: String, itemId: String): Boolean {
        return try {
            todoListsCollection.document(todoListId)
                .collection(TODO_ITEMS_COLLECTION)
                .document(itemId)
                .update(
                    mapOf(
                        "completed" to false,
                        "completedBy" to null,
                        "completedAt" to null,
                        "updatedAt" to LocalDateTime.now()
                    )
                )
                .await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error uncompleting todo item: ${e.message}")
            false
        }
    }

    override suspend fun assignTodoItem(todoListId: String, itemId: String, userId: String): Boolean {
        return try {
            todoListsCollection.document(todoListId)
                .collection(TODO_ITEMS_COLLECTION)
                .document(itemId)
                .update(
                    mapOf(
                        "assignedTo" to userId,
                        "updatedAt" to LocalDateTime.now()
                    )
                )
                .await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error assigning todo item: ${e.message}")
            false
        }
    }

    private suspend fun getChallengeFromSnapshot(snapshot: DocumentSnapshot): Challenge? {
        return try {
            snapshot.toObjectWithDates<Challenge>()
        } catch (e: Exception) {
            Log.e(TAG, "Error converting snapshot to Challenge: ${e.message}")
            null
        }
    }
} 