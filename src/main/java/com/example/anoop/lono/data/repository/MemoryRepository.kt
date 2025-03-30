package com.example.anoop.lono.data.repository

import com.example.anoop.lono.data.model.Memory
import com.example.anoop.lono.data.model.Album
import com.example.anoop.lono.data.model.Comment
import org.threeten.bp.LocalDateTime
import android.location.Location

interface MemoryRepository : BaseRepository<Memory> {
    suspend fun createAlbum(album: Album): String
    suspend fun getAlbum(id: String): Album?
    suspend fun updateAlbum(album: Album): Boolean
    suspend fun deleteAlbum(id: String): Boolean
    suspend fun getUserAlbums(userId: String): List<Album>
    suspend fun addMemoryToAlbum(albumId: String, memory: Memory): String
    suspend fun removeMemoryFromAlbum(albumId: String, memoryId: String): Boolean
    
    // Comment related methods
    suspend fun addComment(memoryId: String, comment: Comment): String
    suspend fun updateComment(memoryId: String, comment: Comment): Boolean
    suspend fun deleteComment(memoryId: String, commentId: String): Boolean
    
    // Like related methods
    suspend fun likeMemory(memoryId: String, userId: String): Boolean
    suspend fun unlikeMemory(memoryId: String, userId: String): Boolean
    suspend fun likeComment(commentId: String, userId: String): Boolean
    suspend fun unlikeComment(commentId: String, userId: String): Boolean
    
    // Reply related methods
    suspend fun addReply(commentId: String, reply: Comment): String
    suspend fun updateReply(commentId: String, reply: Comment): Boolean
    suspend fun deleteReply(commentId: String, replyId: String): Boolean
    
    // Memory retrieval methods
    suspend fun getMemoriesByDate(albumId: String, date: LocalDateTime): List<Memory>
    suspend fun getMemoriesByTag(albumId: String, tag: String): List<Memory>
    suspend fun searchMemories(albumId: String, query: String): List<Memory>
    suspend fun getUnlockedMemories(userId: String): List<Memory>
    suspend fun getMemoriesByLocation(albumId: String, latitude: Double, longitude: Double, radius: Double): List<Memory>
    
    // Additional methods for filtering memories
    suspend fun getMemoriesByUserId(userId: String): List<Memory>
    suspend fun getMemoriesByAlbumId(albumId: String): List<Memory>
    suspend fun getMemoriesByTags(tags: List<String>): List<Memory>
    suspend fun getMemoriesByDate(startDate: LocalDateTime, endDate: LocalDateTime): List<Memory>
    suspend fun getMemoriesByLocation(location: Location, radiusInKm: Double): List<Memory>
} 