package com.example.anoop.lono.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.anoop.lono.data.model.Challenge
import com.example.anoop.lono.data.model.ChallengeProgress
import com.example.anoop.lono.data.model.TodoList
import com.example.anoop.lono.data.model.TodoItem
import com.example.anoop.lono.data.repository.ChallengeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChallengeViewModel @Inject constructor(
    private val repository: ChallengeRepository
) : ViewModel() {

    private val _challenges = MutableStateFlow<List<Challenge>>(emptyList())
    val challenges: StateFlow<List<Challenge>> = _challenges

    private val _userProgress = MutableStateFlow<Map<String, ChallengeProgress>>(emptyMap())
    val userProgress: StateFlow<Map<String, ChallengeProgress>> = _userProgress

    private val _activeChallenges = MutableStateFlow<List<Challenge>>(emptyList())
    val activeChallenges: StateFlow<List<Challenge>> = _activeChallenges

    private val _completedChallenges = MutableStateFlow<List<Challenge>>(emptyList())
    val completedChallenges: StateFlow<List<Challenge>> = _completedChallenges

    private val _userTodoLists = MutableStateFlow<List<TodoList>>(emptyList())
    val userTodoLists: StateFlow<List<TodoList>> = _userTodoLists

    init {
        loadChallenges()
    }

    private fun loadChallenges() {
        viewModelScope.launch {
            try {
                _challenges.value = repository.getAll()
                _activeChallenges.value = repository.getActiveUserChallenges()
                _completedChallenges.value = repository.getCompletedUserChallenges()
                loadUserProgress()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    private fun loadUserProgress() {
        viewModelScope.launch {
            try {
                val progress = mutableMapOf<String, ChallengeProgress>()
                _challenges.value.forEach { challenge ->
                    repository.getProgress(challenge.id)?.let { 
                        progress[challenge.id] = it
                    }
                }
                _userProgress.value = progress
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun joinChallenge(challengeId: String) {
        viewModelScope.launch {
            try {
                repository.joinChallenge(challengeId)
                loadUserProgress()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun updateProgress(challengeId: String, progress: Int) {
        viewModelScope.launch {
            try {
                repository.updateProgress(challengeId, progress)
                loadUserProgress()
                if (progress >= 100) {
                    repository.completeChallenge(challengeId)
                    loadChallenges()
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun updateTodoItem(listId: String, itemId: String, isCompleted: Boolean) {
        viewModelScope.launch {
            try {
                val currentLists = _userTodoLists.value.toMutableList()
                val listIndex = currentLists.indexOfFirst { it.id == listId }
                if (listIndex != -1) {
                    val list = currentLists[listIndex]
                    val updatedItems = list.items.map { item ->
                        if (item.id == itemId) item.copy(completed = isCompleted) else item
                    }
                    currentLists[listIndex] = list.copy(items = updatedItems)
                    _userTodoLists.value = currentLists
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}