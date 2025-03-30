package com.example.anoop.lono.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.anoop.lono.data.model.User
import com.example.anoop.lono.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Logout
import org.threeten.bp.LocalDateTime

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val messaging: FirebaseMessaging,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    init {
        auth.currentUser?.let { firebaseUser ->
            viewModelScope.launch {
                try {
                    _authState.value = AuthState.Loading
                    loadUser(firebaseUser.uid)
                } catch (e: Exception) {
                    _authState.value = AuthState.Error(e.message ?: "Failed to load user")
                    _currentUser.value = null
                }
            }
        } ?: run {
            _authState.value = AuthState.Initial
            _currentUser.value = null
        }
    }

    private suspend fun loadUser(userId: String) {
        try {
            val user = userRepository.get(userId)
            if (user != null) {
                _currentUser.value = user
                _authState.value = AuthState.Success
                updateFcmToken()
                userRepository.updateLastActive(userId)
            } else {
                _authState.value = AuthState.Error("User data not found")
                _currentUser.value = null
                auth.signOut()
            }
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "Failed to load user")
            _currentUser.value = null
            auth.signOut()
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                val result = auth.signInWithEmailAndPassword(email, password).await()
                result.user?.let { firebaseUser ->
                    loadUser(firebaseUser.uid)
                } ?: run {
                    _authState.value = AuthState.Error("Failed to get user data")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Authentication failed")
            }
        }
    }

    fun signUp(email: String, password: String, name: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                result.user?.let { firebaseUser ->
                    val user = User(
                        id = firebaseUser.uid,
                        email = email,
                        name = name,
                        createdAt = LocalDateTime.now(),
                        lastActive = LocalDateTime.now()
                    )
                    userRepository.create(user)
                    _currentUser.value = user
                    _authState.value = AuthState.Success
                    updateFcmToken()
                } ?: run {
                    _authState.value = AuthState.Error("Failed to create user")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Failed to create account")
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                auth.signOut()
                _currentUser.value = null
                _authState.value = AuthState.Initial
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Failed to sign out")
            }
        }
    }

    fun updateProfilePicture(pictureUrl: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                _currentUser.value?.let { user ->
                    userRepository.updateProfilePicture(user.id, pictureUrl)
                    _currentUser.value = user.copy(profilePictureUrl = pictureUrl)
                    _authState.value = AuthState.Success
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Failed to update profile picture")
            }
        }
    }

    private suspend fun updateFcmToken() {
        try {
            val token = messaging.token.await()
            _currentUser.value?.let { user ->
                userRepository.updateFcmToken(user.id, token)
            }
        } catch (e: Exception) {
            // Log but don't fail the auth process
            println("Failed to update FCM token: ${e.message}")
        }
    }
} 