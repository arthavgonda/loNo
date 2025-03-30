package com.example.anoop.lono

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.anoop.lono.ui.screens.auth.LoginScreen
import com.example.anoop.lono.ui.screens.auth.SignUpScreen
import com.example.anoop.lono.ui.screens.chat.ChatScreen
import com.example.anoop.lono.ui.screens.memory.MemoryScreen
import com.example.anoop.lono.ui.screens.challenge.ChallengeScreen
import com.example.anoop.lono.ui.screens.profile.ProfileScreen
import com.example.anoop.lono.ui.theme.LoNoTheme
import com.example.anoop.lono.ui.viewmodel.AuthViewModel
import com.example.anoop.lono.ui.components.BottomNavigation
import dagger.hilt.android.AndroidEntryPoint
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Logout

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LoNoTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen(
    authViewModel: AuthViewModel = viewModel()
) {
    val navController = rememberNavController()
    
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "login",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("login") {
                LoginScreen(
                    onSignUpClick = { navController.navigate("signup") },
                    onLoginSuccess = { navController.navigate("main") }
                )
            }
            
            composable("signup") {
                SignUpScreen(
                    onBackToLogin = { navController.navigateUp() },
                    onSignUpSuccess = { navController.navigate("main") }
                )
            }
            
            composable("main") {
                MainNavigation(navController = navController)
            }
        }
    }
}

@Composable
fun MainNavigation(navController: NavHostController) {
    Scaffold(
        bottomBar = { BottomNavigation(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "chat",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("chat") {
                ChatScreen()
            }
            
            composable("memories") {
                MemoryScreen()
            }
            
            composable("challenges") {
                ChallengeScreen()
            }
            
            composable("profile") {
                ProfileScreen()
            }
        }
    }
}