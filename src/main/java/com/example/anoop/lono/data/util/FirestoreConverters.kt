package com.example.anoop.lono.data.util

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset

object FirestoreConverters {
    fun toLocalDateTime(timestamp: Any?): LocalDateTime? {
        return when (timestamp) {
            is Long -> LocalDateTime.ofEpochSecond(timestamp, 0, ZoneOffset.UTC)
            is Timestamp -> LocalDateTime.ofEpochSecond(timestamp.seconds, timestamp.nanoseconds, ZoneOffset.UTC)
            else -> null
        }
    }

    fun fromLocalDateTime(dateTime: LocalDateTime?): Any? {
        return dateTime?.let { Timestamp(it.toEpochSecond(ZoneOffset.UTC), 0) }
    }

    inline fun <reified T> DocumentSnapshot.toObjectWithDates(): T? {
        val data = data ?: return null
        val convertedData = data.mapValues { (key, value) ->
            when {
                key.endsWith("At") || key == "date" || key == "timestamp" || key == "dueDate" || key == "lastMessageTime" -> {
                    toLocalDateTime(value) ?: value
                }
                else -> value
            }
        }
        return toObject(T::class.java)?.apply {
            // Update all LocalDateTime fields in the object
            this::class.java.declaredFields.forEach { field ->
                if (field.type == LocalDateTime::class.java) {
                    field.isAccessible = true
                    val timestamp = convertedData[field.name]
                    if (timestamp != null) {
                        field.set(this, toLocalDateTime(timestamp))
                    }
                }
            }
        }
    }
} 