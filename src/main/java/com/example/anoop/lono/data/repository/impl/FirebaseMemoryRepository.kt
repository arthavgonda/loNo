package com.example.anoop.lono.data.repository.impl

import com.example.anoop.lono.data.model.Memory
import com.example.anoop.lono.data.model.Album
import com.example.anoop.lono.data.model.Comment
import com.example.anoop.lono.data.model.Location as ModelLocation
import com.example.anoop.lono.data.repository.MemoryRepository
import com.example.anoop.lono.data.util.FirestoreConverters
import com.example.anoop.lono.data.util.FirestoreConverters.toObjectWithDates
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.tasks.await
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import org.threeten.bp.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log
import kotlin.math.*
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.SetOptions
import java.util.Date
import com.google.firebase.Timestamp
import android.location.Location as AndroidLocation

@Singleton
class FirebaseMemoryRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage
) : MemoryRepository {

    companion object {
        private const val TAG = "FirebaseMemoryRepo"
        private const val MEMORIES_COLLECTION = "memories"
        private const val COMMENTS_COLLECTION = "comments"
        private const val REPLIES_COLLECTION = "replies"
        private const val ALBUMS_COLLECTION = "albums"
    }

    private val memoriesCollection = firestore.collection(MEMORIES_COLLECTION)
    private val albumsCollection = firestore.collection(ALBUMS_COLLECTION)
    private val commentsCollection = firestore.collection(COMMENTS_COLLECTION)

    override suspend fun get(id: String): Memory? {
        return try {
            memoriesCollection.document(id).get().await().toObjectWithDates<Memory>()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getAll(): List<Memory> {
        return try {
            memoriesCollection.get().await().documents.mapNotNull { it.toObjectWithDates<Memory>() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun create(item: Memory): String {
        return try {
            val memoryMap = mutableMapOf<String, Any>(
                "id" to item.id,
                "userId" to item.userId,
                "title" to item.title,
                "description" to item.description,
                "date" to FirestoreConverters.fromLocalDateTime(item.date),
                "createdAt" to FirestoreConverters.fromLocalDateTime(item.createdAt),
                "updatedAt" to FirestoreConverters.fromLocalDateTime(item.updatedAt),
                "tags" to (item.tags as List<String>),
                "likes" to (item.likes as List<String>),
                "comments" to (item.comments as List<Comment>),
                "imageUrls" to (item.imageUrls as List<String>)
            )

            item.location?.let {
                memoryMap["location"] = mutableMapOf<String, Any>(
                    "latitude" to it.latitude,
                    "longitude" to it.longitude
                )
            }

            item.albumId?.let {
                memoryMap["albumId"] = it
            }

            val docRef = memoriesCollection.document(item.id)
            docRef.set(memoryMap).await()
            item.id
        } catch (e: Exception) {
            Log.e(TAG, "Error creating memory: ${e.message}", e)
            throw e
        }
    }

    override suspend fun update(item: Memory): Boolean {
        return try {
            val updates = mutableMapOf<String, Any>(
                "title" to item.title,
                "description" to item.description,
                "date" to FirestoreConverters.fromLocalDateTime(item.date),
                "updatedAt" to FirestoreConverters.fromLocalDateTime(item.updatedAt),
                "tags" to (item.tags as List<String>),
                "likes" to (item.likes as List<String>),
                "comments" to (item.comments as List<Comment>),
                "imageUrls" to (item.imageUrls as List<String>)
            )

            item.location?.let {
                updates["location"] = mutableMapOf<String, Any>(
                    "latitude" to it.latitude,
                    "longitude" to it.longitude
                )
            }

            item.albumId?.let {
                updates["albumId"] = it
            }

            val docRef = memoriesCollection.document(item.id)
            docRef.set(updates, SetOptions.merge()).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating memory: ${e.message}", e)
            false
        }
    }

    override suspend fun delete(id: String): Boolean {
        return try {
            memoriesCollection.document(id).delete().await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting memory", e)
            false
        }
    }

    override suspend fun exists(id: String): Boolean {
        return try {
            memoriesCollection.document(id).get().await().exists()
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun createAlbum(album: Album): String {
        return try {
            val albumMap = mapOf(
                "id" to album.id,
                "name" to album.name,
                "description" to album.description,
                "coverImageUrl" to album.coverImageUrl,
                "memoryCount" to album.memoryCount,
                "memoryIds" to album.memoryIds,
                "createdAt" to FirestoreConverters.fromLocalDateTime(LocalDateTime.now()),
                "updatedAt" to FirestoreConverters.fromLocalDateTime(LocalDateTime.now())
            )
            albumsCollection.document(album.id).set(albumMap).await()
            album.id
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun getAlbum(id: String): Album? {
        return try {
            albumsCollection.document(id).get().await().toObjectWithDates<Album>()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun updateAlbum(album: Album): Boolean {
        return try {
            val updates = mutableMapOf<String, Any>(
                "name" to album.name,
                "description" to album.description,
                "memoryCount" to album.memoryCount,
                "memoryIds" to album.memoryIds,
                "updatedAt" to FirestoreConverters.fromLocalDateTime(LocalDateTime.now())
            )
            
            album.coverImageUrl?.let { updates["coverImageUrl"] = it }

            albumsCollection.document(album.id)
                .set(updates, SetOptions.merge())
                .await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating album: ${e.message}", e)
            false
        }
    }

    override suspend fun deleteAlbum(id: String): Boolean {
        return try {
            albumsCollection.document(id).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getUserAlbums(userId: String): List<Album> {
        return try {
            albumsCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObjectWithDates<Album>() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun addMemoryToAlbum(albumId: String, memory: Memory): String {
        return try {
            val memoryRef = memoriesCollection.document()
            val memoryWithId = memory.copy(
                id = memoryRef.id,
                albumId = albumId,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
            
            val memoryMap = mutableMapOf<String, Any>(
                "id" to memoryWithId.id,
                "userId" to memoryWithId.userId,
                "title" to memoryWithId.title,
                "description" to memoryWithId.description,
                "date" to FirestoreConverters.fromLocalDateTime(memoryWithId.date),
                "albumId" to memoryWithId.albumId,
                "imageUrls" to (memoryWithId.imageUrls as List<String>),
                "tags" to (memoryWithId.tags as List<String>),
                "likes" to (memoryWithId.likes as List<String>),
                "createdAt" to FirestoreConverters.fromLocalDateTime(memoryWithId.createdAt),
                "updatedAt" to FirestoreConverters.fromLocalDateTime(memoryWithId.updatedAt)
            )

            memoryWithId.location?.let { loc ->
                memoryMap["location"] = mutableMapOf<String, Any>(
                    "latitude" to loc.latitude,
                    "longitude" to loc.longitude
                )
            }

            memoryWithId.comments.let { commentsList ->
                memoryMap["comments"] = commentsList.map { comment ->
                    mutableMapOf<String, Any>(
                        "id" to comment.id,
                        "userId" to comment.userId,
                        "content" to comment.content,
                        "timestamp" to FirestoreConverters.fromLocalDateTime(comment.timestamp),
                        "likes" to (comment.likes as List<String>)
                    ).apply {
                        comment.replies?.let { replyList ->
                            put("replies", replyList.map { reply ->
                                mutableMapOf<String, Any>(
                                    "id" to reply.id,
                                    "userId" to reply.userId,
                                    "content" to reply.content,
                                    "timestamp" to FirestoreConverters.fromLocalDateTime(reply.timestamp),
                                    "likes" to (reply.likes as List<String>)
                                )
                            })
                        }
                    }
                }
            }

            val batch = firestore.batch()
            batch.set(memoryRef, memoryMap)
            batch.update(
                albumsCollection.document(albumId),
                mutableMapOf<String, Any>(
                    "memoryIds" to FieldValue.arrayUnion(memoryWithId.id),
                    "memoryCount" to FieldValue.increment(1),
                    "updatedAt" to FirestoreConverters.fromLocalDateTime(LocalDateTime.now())
                )
            )
            batch.commit().await()

            memoryRef.id
        } catch (e: Exception) {
            Log.e(TAG, "Error adding memory to album: ${e.message}", e)
            throw e
        }
    }

    override suspend fun removeMemoryFromAlbum(albumId: String, memoryId: String): Boolean {
        return try {
            memoriesCollection.document(memoryId).delete().await()

            // Remove from album's memoryIds list
            albumsCollection.document(albumId)
                .update(
                    "memoryIds" to FieldValue.arrayRemove(memoryId),
                    "memoryCount" to FieldValue.increment(-1)
                )
                .await()

            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun addComment(memoryId: String, comment: Comment): String {
        return try {
            val commentRef = commentsCollection.document()
            val commentWithId = comment.copy(id = commentRef.id)
            val commentMap = mapOf<String, Any>(
                "id" to commentWithId.id,
                "memoryId" to memoryId,
                "userId" to commentWithId.userId,
                "content" to commentWithId.content,
                "createdAt" to FirestoreConverters.fromLocalDateTime(commentWithId.createdAt),
                "updatedAt" to FirestoreConverters.fromLocalDateTime(commentWithId.updatedAt)
            )
            commentRef.set(commentMap).await()
            commentRef.id
        } catch (e: Exception) {
            Log.e(TAG, "Error adding comment", e)
            throw e
        }
    }

    override suspend fun updateComment(memoryId: String, comment: Comment): Boolean {
        return try {
            val updates = mapOf(
                "content" to comment.content,
                "updatedAt" to FirestoreConverters.fromLocalDateTime(LocalDateTime.now())
            ).filterValues { it != null }.mapValues { it.value!! }

            memoriesCollection.document(memoryId)
                .collection(COMMENTS_COLLECTION)
                .document(comment.id)
                .set(updates, SetOptions.merge())
                .await()

            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating comment: ${e.message}", e)
            false
        }
    }

    override suspend fun deleteComment(memoryId: String, commentId: String): Boolean {
        return try {
            commentsCollection.document(commentId).delete().await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting comment", e)
            false
        }
    }

    override suspend fun likeMemory(memoryId: String, userId: String): Boolean {
        return try {
            memoriesCollection.document(memoryId)
                .update("likes", FieldValue.arrayUnion(userId))
                .await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error liking memory", e)
            false
        }
    }

    override suspend fun unlikeMemory(memoryId: String, userId: String): Boolean {
        return try {
            memoriesCollection.document(memoryId)
                .update("likes", FieldValue.arrayRemove(userId))
                .await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error unliking memory", e)
            false
        }
    }

    override suspend fun likeComment(commentId: String, userId: String): Boolean {
        return try {
            commentsCollection.document(commentId)
                .update("likes", FieldValue.arrayUnion(userId))
                .await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error liking comment", e)
            false
        }
    }

    override suspend fun unlikeComment(commentId: String, userId: String): Boolean {
        return try {
            commentsCollection.document(commentId)
                .update("likes", FieldValue.arrayRemove(userId))
                .await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error unliking comment", e)
            false
        }
    }

    override suspend fun addReply(commentId: String, reply: Comment): String {
        return try {
            val replyRef = commentsCollection.document()
            val replyWithId = reply.copy(id = replyRef.id)
            val replyMap = mapOf<String, Any>(
                "id" to replyWithId.id,
                "commentId" to commentId,
                "userId" to replyWithId.userId,
                "content" to replyWithId.content,
                "createdAt" to FirestoreConverters.fromLocalDateTime(replyWithId.createdAt),
                "updatedAt" to FirestoreConverters.fromLocalDateTime(replyWithId.updatedAt)
            )
            replyRef.set(replyMap).await()
            replyRef.id
        } catch (e: Exception) {
            Log.e(TAG, "Error adding reply", e)
            throw e
        }
    }

    override suspend fun updateReply(commentId: String, reply: Comment): Boolean {
        return try {
            val updates = mapOf(
                "content" to reply.content,
                "updatedAt" to FirestoreConverters.fromLocalDateTime(LocalDateTime.now())
            ).filterValues { it != null }.mapValues { it.value!! }

            commentsCollection.document(commentId)
                .collection(REPLIES_COLLECTION)
                .document(reply.id)
                .set(updates, SetOptions.merge())
                .await()

            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating reply: ${e.message}", e)
            false
        }
    }

    override suspend fun deleteReply(commentId: String, replyId: String): Boolean {
        return try {
            commentsCollection.document(replyId).delete().await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting reply", e)
            false
        }
    }

    override suspend fun getMemoriesByDate(albumId: String, date: LocalDateTime): List<Memory> {
        return try {
            val startOfDay = date.withHour(0).withMinute(0).withSecond(0).withNano(0)
            val endOfDay = date.withHour(23).withMinute(59).withSecond(59).withNano(999999999)

            memoriesCollection
                .whereEqualTo("albumId", albumId)
                .whereGreaterThanOrEqualTo("date", FirestoreConverters.fromLocalDateTime(startOfDay))
                .whereLessThanOrEqualTo("date", FirestoreConverters.fromLocalDateTime(endOfDay))
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObjectWithDates<Memory>() }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting memories by date: ${e.message}", e)
            emptyList()
        }
    }

    override suspend fun getMemoriesByTag(albumId: String, tag: String): List<Memory> {
        return try {
            memoriesCollection
                .whereEqualTo("albumId", albumId)
                .whereArrayContains("tags", tag)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObjectWithDates<Memory>() }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting memories by tag: ${e.message}", e)
            emptyList()
        }
    }

    override suspend fun searchMemories(albumId: String, query: String): List<Memory> {
        return try {
            memoriesCollection
                .whereEqualTo("albumId", albumId)
                .whereGreaterThanOrEqualTo("title", query)
                .whereLessThanOrEqualTo("title", query + "\uf8ff")
                .get()
                .await()
                .documents
                .mapNotNull { it.toObjectWithDates<Memory>() }
        } catch (e: Exception) {
            Log.e(TAG, "Error searching memories: ${e.message}", e)
            emptyList()
        }
    }

    override suspend fun getUnlockedMemories(userId: String): List<Memory> {
        return try {
            memoriesCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("isLocked", false)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObjectWithDates<Memory>() }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting unlocked memories: ${e.message}", e)
            emptyList()
        }
    }

    override suspend fun getMemoriesByLocation(albumId: String, latitude: Double, longitude: Double, radius: Double): List<Memory> {
        return try {
            memoriesCollection
                .whereEqualTo("albumId", albumId)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObjectWithDates<Memory>() }
                .filter { memory ->
                    memory.location?.let { memoryLocation ->
                        calculateDistance(
                            latitude,
                            longitude,
                            memoryLocation.latitude,
                            memoryLocation.longitude
                        ) <= radius
                    } ?: false
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting memories by location: ${e.message}", e)
            emptyList()
        }
    }

    override suspend fun getMemoriesByLocation(location: AndroidLocation, radiusInKm: Double): List<Memory> {
        return try {
            val memories = memoriesCollection.get().await()
            memories.documents
                .mapNotNull { doc ->
                    doc.toObjectWithDates<Memory>()?.let { memory ->
                        memory.location?.let { memoryLocation ->
                            val distance = calculateDistance(
                                location.latitude, location.longitude,
                                memoryLocation.latitude, memoryLocation.longitude
                            )
                            if (distance <= radiusInKm) memory else null
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting memories by location: ${e.message}", e)
            emptyList()
        }
    }

    private fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val r = 6371.0 // Earth's radius in kilometers
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return r * c
    }

    private fun getMemoryFromSnapshot(snapshot: DocumentSnapshot): Memory {
        return try {
            val data = snapshot.data ?: throw Exception("No data in snapshot")
            val location = (data["location"] as? Map<String, Any>)?.let {
                ModelLocation(
                    latitude = it["latitude"] as Double,
                    longitude = it["longitude"] as Double
                )
            }
            
            Memory(
                id = snapshot.id,
                userId = data["userId"] as String,
                title = data["title"] as String,
                description = data["description"] as String,
                date = FirestoreConverters.toLocalDateTime(data["date"] as Timestamp),
                location = location,
                tags = (data["tags"] as? List<String>) ?: emptyList(),
                likes = (data["likes"] as? List<String>) ?: emptyList(),
                comments = (data["comments"] as? List<Comment>) ?: emptyList(),
                imageUrls = (data["imageUrls"] as? List<String>) ?: emptyList(),
                createdAt = FirestoreConverters.toLocalDateTime(data["createdAt"] as Timestamp),
                updatedAt = FirestoreConverters.toLocalDateTime(data["updatedAt"] as Timestamp)
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error converting memory snapshot: ${e.message}", e)
            throw e
        }
    }

    override suspend fun getMemoriesByUserId(userId: String): List<Memory> {
        return try {
            memoriesCollection
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObjectWithDates<Memory>() }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting memories by user ID: ${e.message}", e)
            emptyList()
        }
    }

    override suspend fun getMemoriesByAlbumId(albumId: String): List<Memory> {
        return try {
            memoriesCollection
                .whereEqualTo("albumId", albumId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObjectWithDates<Memory>() }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting memories by album ID: ${e.message}", e)
            emptyList()
        }
    }

    override suspend fun getMemoriesByTags(tags: List<String>): List<Memory> {
        return try {
            memoriesCollection
                .whereArrayContainsAny("tags", tags)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObjectWithDates<Memory>() }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting memories by tags: ${e.message}", e)
            emptyList()
        }
    }

    override suspend fun getMemoriesByDate(startDate: LocalDateTime, endDate: LocalDateTime): List<Memory> {
        return try {
            memoriesCollection
                .whereGreaterThanOrEqualTo("createdAt", FirestoreConverters.fromLocalDateTime(startDate))
                .whereLessThanOrEqualTo("createdAt", FirestoreConverters.fromLocalDateTime(endDate))
                .get()
                .await()
                .documents
                .mapNotNull { it.toObjectWithDates<Memory>() }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting memories by date: ${e.message}", e)
            emptyList()
        }
    }
} 