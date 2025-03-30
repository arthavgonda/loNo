package com.example.anoop.lono.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.anoop.lono.data.model.Message
import com.example.anoop.lono.data.model.Chat
import com.example.anoop.lono.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import org.threeten.bp.LocalDateTime

sealed class ChatState {
    object Initial : ChatState()
    object Loading : ChatState()
    data class Success(val chats: List<Chat>, val messages: List<Message>) : ChatState()
    data class Error(val message: String) : ChatState()
}

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _chatState = MutableStateFlow<ChatState>(ChatState.Initial)
    val chatState: StateFlow<ChatState> = _chatState.asStateFlow()

    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
    val chats: StateFlow<List<Chat>> = _chats.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _currentChatId = MutableStateFlow<String?>(null)
    val currentChatId: StateFlow<String?> = _currentChatId.asStateFlow()

    init {
        loadChats()
    }

    private fun loadChats() {
        viewModelScope.launch {
            try {
                _chatState.value = ChatState.Loading
                val chats = chatRepository.getChats()
                _chats.value = chats
                _chatState.value = ChatState.Success(chats, _messages.value)
            } catch (e: Exception) {
                _chatState.value = ChatState.Error(e.message ?: "Failed to load chats")
            }
        }
    }

    fun loadMessages(chatId: String) {
        viewModelScope.launch {
            try {
                _currentChatId.value = chatId
                val messages = chatRepository.getMessages(chatId)
                _messages.value = messages
                _chatState.value = ChatState.Success(_chats.value, messages)
            } catch (e: Exception) {
                _chatState.value = ChatState.Error(e.message ?: "Failed to load messages")
            }
        }
    }

    fun sendMessage(chatId: String, content: String, senderId: String) {
        viewModelScope.launch {
            try {
                val message = Message(
                    id = "",
                    chatId = chatId,
                    senderId = senderId,
                    content = content,
                    timestamp = LocalDateTime.now()
                )
                chatRepository.sendMessage(chatId, message)
                loadMessages(chatId)
            } catch (e: Exception) {
                _chatState.value = ChatState.Error(e.message ?: "Failed to send message")
            }
        }
    }

    fun createChat(participants: List<String>) {
        viewModelScope.launch {
            try {
                val chat = Chat(
                    id = "",
                    participants = participants,
                    lastMessage = null,
                    lastMessageTime = null,
                    unreadCount = 0,
                    isArchived = false
                )
                chatRepository.createChat(chat)
                loadChats()
            } catch (e: Exception) {
                _chatState.value = ChatState.Error(e.message ?: "Failed to create chat")
            }
        }
    }

    fun archiveChat(chatId: String, userId: String) {
        viewModelScope.launch {
            try {
                chatRepository.archiveChat(chatId, userId)
                loadChats()
            } catch (e: Exception) {
                _chatState.value = ChatState.Error(e.message ?: "Failed to archive chat")
            }
        }
    }

    fun unarchiveChat(chatId: String, userId: String) {
        viewModelScope.launch {
            try {
                chatRepository.unarchiveChat(chatId, userId)
                loadChats()
            } catch (e: Exception) {
                _chatState.value = ChatState.Error(e.message ?: "Failed to unarchive chat")
            }
        }
    }

    fun markChatAsRead(chatId: String, userId: String) {
        viewModelScope.launch {
            try {
                chatRepository.updateChatReadStatus(chatId, userId, true)
                loadChats()
            } catch (e: Exception) {
                _chatState.value = ChatState.Error(e.message ?: "Failed to mark chat as read")
            }
        }
    }
} 