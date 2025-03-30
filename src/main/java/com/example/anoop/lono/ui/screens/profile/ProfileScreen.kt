package com.example.anoop.lono.ui.screens.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.anoop.lono.ui.viewmodel.AuthViewModel
import com.example.anoop.lono.data.model.User
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.anoop.lono.ui.viewmodel.AuthState

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel = viewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    when (authState) {
        is AuthState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is AuthState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = (authState as AuthState.Error).message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { authViewModel.signOut() }) {
                        Text("Sign Out")
                    }
                }
            }
        }
        else -> {
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
                        text = "Profile",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    IconButton(
                        onClick = { /* TODO: Show edit profile dialog */ }
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit profile")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Profile card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // TODO: Add profile picture
                        Text(
                            text = currentUser?.name ?: "User",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = currentUser?.email ?: "",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // Settings section
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        ListItem(
                            headlineContent = { Text("Notifications") },
                            leadingContent = { Icon(Icons.Default.Notifications, contentDescription = null) },
                            trailingContent = { Icon(Icons.Default.ArrowForward, contentDescription = null) },
                            modifier = Modifier.clickable { /* TODO: Navigate to notifications settings */ }
                        )
                        ListItem(
                            headlineContent = { Text("Privacy") },
                            leadingContent = { Icon(Icons.Default.Lock, contentDescription = null) },
                            trailingContent = { Icon(Icons.Default.ArrowForward, contentDescription = null) },
                            modifier = Modifier.clickable { /* TODO: Navigate to privacy settings */ }
                        )
                        ListItem(
                            headlineContent = { Text("Help & Support") },
                            leadingContent = { Icon(Icons.Default.HelpOutline, contentDescription = null) },
                            trailingContent = { Icon(Icons.Default.ArrowForward, contentDescription = null) },
                            modifier = Modifier.clickable { /* TODO: Navigate to help & support */ }
                        )
                        ListItem(
                            headlineContent = { Text("About") },
                            leadingContent = { Icon(Icons.Default.Info, contentDescription = null) },
                            trailingContent = { Icon(Icons.Default.ArrowForward, contentDescription = null) },
                            modifier = Modifier.clickable { /* TODO: Navigate to about */ }
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Sign out button
                Button(
                    onClick = { authViewModel.signOut() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sign Out")
                }
            }
        }
    }
} 