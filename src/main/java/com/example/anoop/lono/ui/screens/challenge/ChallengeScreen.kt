package com.example.anoop.lono.ui.screens.challenge

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
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.anoop.lono.data.model.Challenge
import com.example.anoop.lono.data.model.ChallengeProgress
import com.example.anoop.lono.data.model.TodoList
import com.example.anoop.lono.data.model.TodoItem
import com.example.anoop.lono.ui.viewmodel.ChallengeViewModel
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter

@Composable
fun ChallengeScreen(
    viewModel: ChallengeViewModel = hiltViewModel()
) {
    val challenges by viewModel.challenges.collectAsState()
    val userProgress by viewModel.userProgress.collectAsState()
    val activeChallenges by viewModel.activeChallenges.collectAsState()
    val completedChallenges by viewModel.completedChallenges.collectAsState()
    val userTodoLists by viewModel.userTodoLists.collectAsState()

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
                text = "Challenges",
                style = MaterialTheme.typography.headlineMedium
            )
            IconButton(
                onClick = { /* TODO: Show create challenge dialog */ }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add challenge")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(challenges) { challenge ->
                ChallengeCard(
                    challenge = challenge,
                    progress = userProgress[challenge.id],
                    onJoinClick = { viewModel.joinChallenge(challenge.id) },
                    onUpdateProgress = { progress -> viewModel.updateProgress(challenge.id, progress) }
                )
            }

            // Todo Lists section
            item {
                Text(
                    text = "Todo Lists",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(userTodoLists) { todoList ->
                TodoListCard(todoList, viewModel)
            }

            // Completed Challenges section
            item {
                Text(
                    text = "Completed Challenges",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(completedChallenges) { challenge ->
                ChallengeCard(
                    challenge = challenge,
                    progress = userProgress[challenge.id],
                    onJoinClick = { viewModel.joinChallenge(challenge.id) },
                    onUpdateProgress = { progress -> viewModel.updateProgress(challenge.id, progress) }
                )
            }
        }
    }
}

@Composable
fun ChallengeCard(
    challenge: Challenge,
    progress: ChallengeProgress?,
    onJoinClick: () -> Unit,
    onUpdateProgress: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                text = challenge.title,
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = challenge.description,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Points: ${challenge.points}",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "Due: ${challenge.endDate.format(DateTimeFormatter.ISO_LOCAL_DATE)}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (progress != null) {
                LinearProgressIndicator(
                    progress = progress.progress.toFloat() / 100,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${progress.progress}% Complete",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    if (!progress.completed) {
                        Button(
                            onClick = { onUpdateProgress(progress.progress + 10) },
                            enabled = progress.progress < 100
                        ) {
                            Text("Update Progress")
                        }
                    }
                }
            } else {
                Button(
                    onClick = onJoinClick,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Join Challenge")
                }
            }
        }
    }
}

@Composable
fun TodoListCard(todoList: TodoList, viewModel: ChallengeViewModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = todoList.name,
                style = MaterialTheme.typography.titleMedium
            )
            todoList.items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = item.completed,
                        onCheckedChange = { isChecked ->
                            viewModel.updateTodoItem(todoList.id, item.id, isChecked)
                        }
                    )
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
} 