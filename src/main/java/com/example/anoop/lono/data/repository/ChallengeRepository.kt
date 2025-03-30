package com.example.anoop.lono.data.repository

import com.example.anoop.lono.data.model.Challenge
import com.example.anoop.lono.data.model.TodoList
import com.example.anoop.lono.data.model.TodoItem
import com.example.anoop.lono.data.model.ChallengeProgress
import org.threeten.bp.LocalDateTime

interface ChallengeRepository {
    suspend fun get(id: String): Challenge?
    suspend fun getAll(): List<Challenge>
    suspend fun create(challenge: Challenge): Challenge
    suspend fun update(challenge: Challenge): Challenge
    suspend fun delete(id: String)
    suspend fun exists(id: String): Boolean
    
    // Challenge progress methods
    suspend fun joinChallenge(challengeId: String): Boolean
    suspend fun leaveChallenge(challengeId: String): Boolean
    suspend fun updateProgress(challengeId: String, progress: Int): Boolean
    suspend fun completeChallenge(challengeId: String): Boolean
    suspend fun getActiveUserChallenges(): List<Challenge>
    suspend fun getCompletedUserChallenges(): List<Challenge>
    suspend fun getProgress(challengeId: String): ChallengeProgress?
    
    // Todo list methods
    suspend fun createTodoList(todoList: TodoList): String
    suspend fun getTodoList(id: String): TodoList?
    suspend fun updateTodoList(todoList: TodoList): Boolean
    suspend fun deleteTodoList(id: String): Boolean
    suspend fun getUserTodoLists(userId: String): List<TodoList>
    suspend fun addTodoItem(todoListId: String, item: TodoItem): String
    suspend fun updateTodoItem(todoListId: String, item: TodoItem): Boolean
    suspend fun deleteTodoItem(todoListId: String, itemId: String): Boolean
    suspend fun completeTodoItem(todoListId: String, itemId: String, userId: String): Boolean
    suspend fun uncompleteTodoItem(todoListId: String, itemId: String): Boolean
    suspend fun assignTodoItem(todoListId: String, itemId: String, userId: String): Boolean
} 