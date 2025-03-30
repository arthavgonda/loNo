package com.example.anoop.lono.ui.screens.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.anoop.lono.ui.viewmodel.ChatViewModel
import com.example.anoop.lono.data.model.Message
import androidx.lifecycle.viewmodel.compose.viewModel
import org.threeten.bp.format.DateTimeFormatter

@Composable
fun ChatScreen(
    chatViewModel: ChatViewModel = viewModel()
) {
    val chatState by chatViewModel.chatState.collectAsState()
    val messages by chatViewModel.messages.collectAsState()
    val currentChatId by chatViewModel.currentChatId.collectAsState()
    var messageText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Chat",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Messages list
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { message ->
                ChatMessageItem(message)
            }
        }

        // Input row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message...") }
            )
            IconButton(
                onClick = {
                    if (messageText.isNotBlank() && currentChatId != null) {
                        chatViewModel.sendMessage(
                            chatId = currentChatId!!,
                            content = messageText,
                            senderId = "current_user_id" // TODO: Get actual user ID
                        )
                        messageText = ""
                    }
                }
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send message")
            }
        }
    }
}

@Composable
fun ChatMessageItem(message: Message) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = message.senderName,
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = message.content,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = message.timestamp.format(DateTimeFormatter.ofPattern("MMM dd, HH:mm")),
            style = MaterialTheme.typography.bodySmall
        )
    }
} 