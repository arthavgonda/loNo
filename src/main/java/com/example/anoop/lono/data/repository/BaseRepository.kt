package com.example.anoop.lono.data.repository

interface BaseRepository<T> {
    suspend fun get(id: String): T?
    suspend fun getAll(): List<T>
    suspend fun create(item: T): String
    suspend fun update(item: T): Boolean
    suspend fun delete(id: String): Boolean
    suspend fun exists(id: String): Boolean
} 