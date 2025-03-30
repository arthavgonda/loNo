package com.example.anoop.lono.ui.screens.memory

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.anoop.lono.ui.viewmodel.MemoryViewModel
import com.example.anoop.lono.data.model.Memory
import com.example.anoop.lono.data.model.Album
import androidx.lifecycle.viewmodel.compose.viewModel
import org.threeten.bp.format.DateTimeFormatter

@Composable
fun MemoryScreen(
    memoryViewModel: MemoryViewModel = viewModel()
) {
    val memoryState by memoryViewModel.memoryState.collectAsState()
    val memories by memoryViewModel.memories.collectAsState()
    val userAlbums by memoryViewModel.userAlbums.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Memories",
                style = MaterialTheme.typography.headlineMedium
            )
            IconButton(
                onClick = { /* TODO: Show create memory dialog */ }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add memory")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Albums section
            item {
                Text(
                    text = "Albums",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(userAlbums) { album ->
                AlbumCard(album)
            }

            // Memories section
            item {
                Text(
                    text = "Recent Memories",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(memories) { memory ->
                MemoryCard(memory)
            }
        }
    }
}

@Composable
fun AlbumCard(album: Album) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = album.name,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = album.description,
                style = MaterialTheme.typography.bodyMedium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${album.memoryCount} memories",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Created: ${album.createdAt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun MemoryCard(memory: Memory) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = memory.title,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = memory.description,
                style = MaterialTheme.typography.bodyMedium
            )
            if (memory.imageUrls.isNotEmpty()) {
                // TODO: Add image loading
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Date: ${memory.date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))}",
                    style = MaterialTheme.typography.bodySmall
                )
                memory.location?.let { location ->
                    Text(
                        text = "Location: ${location.name ?: ""}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
} 