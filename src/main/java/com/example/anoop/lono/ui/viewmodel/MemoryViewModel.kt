package com.example.anoop.lono.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.anoop.lono.data.model.Memory
import com.example.anoop.lono.data.model.Album
import com.example.anoop.lono.data.model.Comment
import com.example.anoop.lono.data.repository.MemoryRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDateTime
import javax.inject.Inject
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Logout
import android.util.Log

data class MemoryUiState(
    val isLoading: Boolean = false,
    val memories: List<Memory> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class MemoryViewModel @Inject constructor(
    private val memoryRepository: MemoryRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    companion object {
        private const val TAG = "MemoryViewModel"
    }

    private val _currentAlbum = MutableStateFlow<Album?>(null)
    val currentAlbum: StateFlow<Album?> = _currentAlbum.asStateFlow()

    private val _memories = MutableStateFlow<List<Memory>>(emptyList())
    val memories: StateFlow<List<Memory>> = _memories.asStateFlow()

    private val _userAlbums = MutableStateFlow<List<Album>>(emptyList())
    val userAlbums: StateFlow<List<Album>> = _userAlbums.asStateFlow()

    private val _memoryState = MutableStateFlow<MemoryState>(MemoryState.Initial)
    val memoryState: StateFlow<MemoryState> = _memoryState.asStateFlow()

    private val _uiState = MutableStateFlow<MemoryUiState>(MemoryUiState(isLoading = true))
    val uiState: StateFlow<MemoryUiState> = _uiState.asStateFlow()

    fun createAlbum(album: Album) {
        viewModelScope.launch {
            try {
                _memoryState.value = MemoryState.Loading
                val albumId = memoryRepository.createAlbum(album)
                _currentAlbum.value = album.copy(id = albumId)
                _memoryState.value = MemoryState.Success
            } catch (e: Exception) {
                _memoryState.value = MemoryState.Error(e.message ?: "Failed to create album")
            }
        }
    }

    fun loadAlbum(albumId: String) {
        viewModelScope.launch {
            try {
                _memoryState.value = MemoryState.Loading
                val album = memoryRepository.getAlbum(albumId)
                if (album != null) {
                    _currentAlbum.value = album
                    loadMemories(albumId)
                    _memoryState.value = MemoryState.Success
                }
            } catch (e: Exception) {
                _memoryState.value = MemoryState.Error(e.message ?: "Failed to load album")
            }
        }
    }

    fun loadUserAlbums(userId: String) {
        viewModelScope.launch {
            try {
                _memoryState.value = MemoryState.Loading
                val albums = memoryRepository.getUserAlbums(userId)
                _userAlbums.value = albums
                _memoryState.value = MemoryState.Success
            } catch (e: Exception) {
                _memoryState.value = MemoryState.Error(e.message ?: "Failed to load user albums")
            }
        }
    }

    fun addMemory(memory: Memory) {
        viewModelScope.launch {
            try {
                _memoryState.value = MemoryState.Loading
                _currentAlbum.value?.let { album ->
                    memoryRepository.addMemoryToAlbum(album.id, memory)
                    loadMemories(album.id)
                    _memoryState.value = MemoryState.Success
                }
            } catch (e: Exception) {
                _memoryState.value = MemoryState.Error(e.message ?: "Failed to add memory")
            }
        }
    }

    fun deleteMemory(memoryId: String) {
        viewModelScope.launch {
            try {
                _memoryState.value = MemoryState.Loading
                _currentAlbum.value?.let { album ->
                    memoryRepository.removeMemoryFromAlbum(album.id, memoryId)
                    loadMemories(album.id)
                    _memoryState.value = MemoryState.Success
                }
            } catch (e: Exception) {
                _memoryState.value = MemoryState.Error(e.message ?: "Failed to delete memory")
            }
        }
    }

    fun addComment(memoryId: String, comment: Comment) {
        viewModelScope.launch {
            try {
                _memoryState.value = MemoryState.Loading
                memoryRepository.addComment(memoryId, comment)
                _currentAlbum.value?.let { album ->
                    loadMemories(album.id)
                }
                _memoryState.value = MemoryState.Success
            } catch (e: Exception) {
                _memoryState.value = MemoryState.Error(e.message ?: "Failed to add comment")
            }
        }
    }

    fun deleteComment(memoryId: String, commentId: String) {
        viewModelScope.launch {
            try {
                _memoryState.value = MemoryState.Loading
                memoryRepository.deleteComment(memoryId, commentId)
                _currentAlbum.value?.let { album ->
                    loadMemories(album.id)
                }
                _memoryState.value = MemoryState.Success
            } catch (e: Exception) {
                _memoryState.value = MemoryState.Error(e.message ?: "Failed to delete comment")
            }
        }
    }

    fun addReply(memoryId: String, commentId: String, content: String) {
        viewModelScope.launch {
            try {
                _memoryState.value = MemoryState.Loading
                val reply = Comment(
                    id = "",
                    userId = auth.currentUser?.uid ?: return@launch,
                    content = content,
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                )
                val replyId = memoryRepository.addReply(commentId, reply)
                _currentAlbum.value?.let { album ->
                    loadMemories(album.id)
                }
                _memoryState.value = MemoryState.Success
            } catch (e: Exception) {
                _memoryState.value = MemoryState.Error(e.message ?: "Failed to add reply")
            }
        }
    }

    fun likeComment(memoryId: String, commentId: String) {
        viewModelScope.launch {
            try {
                _memoryState.value = MemoryState.Loading
                val userId = auth.currentUser?.uid ?: return@launch
                val success = memoryRepository.likeComment(commentId, userId)
                if (success) {
                    _currentAlbum.value?.let { album ->
                        loadMemories(album.id)
                    }
                    _memoryState.value = MemoryState.Success
                }
            } catch (e: Exception) {
                _memoryState.value = MemoryState.Error(e.message ?: "Failed to like comment")
            }
        }
    }

    fun unlikeComment(memoryId: String, commentId: String) {
        viewModelScope.launch {
            try {
                _memoryState.value = MemoryState.Loading
                val userId = auth.currentUser?.uid ?: return@launch
                val success = memoryRepository.unlikeComment(commentId, userId)
                if (success) {
                    _currentAlbum.value?.let { album ->
                        loadMemories(album.id)
                    }
                    _memoryState.value = MemoryState.Success
                }
            } catch (e: Exception) {
                _memoryState.value = MemoryState.Error(e.message ?: "Failed to unlike comment")
            }
        }
    }

    fun likeMemory(memoryId: String, userId: String) {
        viewModelScope.launch {
            try {
                _memoryState.value = MemoryState.Loading
                memoryRepository.likeMemory(memoryId, userId)
                _currentAlbum.value?.let { album ->
                    loadMemories(album.id)
                }
                _memoryState.value = MemoryState.Success
            } catch (e: Exception) {
                _memoryState.value = MemoryState.Error(e.message ?: "Failed to like memory")
            }
        }
    }

    fun unlikeMemory(memoryId: String, userId: String) {
        viewModelScope.launch {
            try {
                _memoryState.value = MemoryState.Loading
                memoryRepository.unlikeMemory(memoryId, userId)
                _currentAlbum.value?.let { album ->
                    loadMemories(album.id)
                }
                _memoryState.value = MemoryState.Success
            } catch (e: Exception) {
                _memoryState.value = MemoryState.Error(e.message ?: "Failed to unlike memory")
            }
        }
    }

    fun getMemoriesByDate(date: LocalDateTime) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                _currentAlbum.value?.let { album ->
                    val memories = memoryRepository.getMemoriesByDate(album.id, date)
                    _uiState.update { it.copy(
                        isLoading = false,
                        memories = memories,
                        error = null
                    ) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = "Failed to load memories: ${e.message}"
                ) }
            }
        }
    }

    fun getMemoriesByTag(tag: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                _currentAlbum.value?.let { album ->
                    val memories = memoryRepository.getMemoriesByTag(album.id, tag)
                    _uiState.update { it.copy(
                        isLoading = false,
                        memories = memories,
                        error = null
                    ) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = "Failed to load memories by tag: ${e.message}"
                ) }
            }
        }
    }

    fun searchMemories(albumId: String, query: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                val memories = memoryRepository.searchMemories(albumId, query)
                _uiState.update { it.copy(
                    memories = memories,
                    isLoading = false,
                    error = null
                )}
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = e.message ?: "Error searching memories"
                )}
            }
        }
    }

    fun getUnlockedMemories(userId: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                val memories = memoryRepository.getUnlockedMemories(userId)
                _uiState.update { it.copy(
                    memories = memories,
                    isLoading = false,
                    error = null
                )}
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = e.message ?: "Error fetching unlocked memories"
                )}
            }
        }
    }

    fun getMemoriesByLocation(latitude: Double, longitude: Double, radiusInKm: Double) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                _currentAlbum.value?.let { album ->
                    val memories = memoryRepository.getMemoriesByLocation(album.id, latitude, longitude, radiusInKm)
                    _uiState.update { it.copy(
                        isLoading = false,
                        memories = memories,
                        error = null
                    ) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = "Failed to load memories by location: ${e.message}"
                ) }
            }
        }
    }

    fun getMemoriesByTags(tags: List<String>) {
        viewModelScope.launch {
            try {
                _uiState.value = MemoryUiState(isLoading = true)
                val memories = mutableListOf<Memory>()
                _currentAlbum.value?.let { album ->
                    tags.forEach { tag ->
                        val memoryList = memoryRepository.getMemoriesByTag(album.id, tag)
                        memories.addAll(memoryList)
                    }
                    _uiState.value = MemoryUiState(
                        isLoading = false,
                        memories = memories.distinct(),
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = MemoryUiState(
                    isLoading = false,
                    error = e.message ?: "Error fetching memories by tags"
                )
            }
        }
    }

    private suspend fun loadMemories(albumId: String) {
        try {
            _memoryState.value = MemoryState.Loading
            val memories = memoryRepository.getAll().filter { it.albumId == albumId }
            _memories.value = memories
            _memoryState.value = MemoryState.Success
        } catch (e: Exception) {
            _memoryState.value = MemoryState.Error(e.message ?: "Failed to load memories")
        }
    }
}

sealed class MemoryState {
    object Initial : MemoryState()
    object Loading : MemoryState()
    object Success : MemoryState()
    data class Error(val message: String) : MemoryState()
} 