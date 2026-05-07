package com.termux.data.session

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class SessionStatus {
    ACTIVE,
    BACKGROUND,
    UNEXPECTED_EXIT,
    CRASHED,
}

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey val id: String,           // UUID
    val name: String,
    val cwd: String,
    val env: String,                      // JSON serialized key=value pairs
    val status: SessionStatus,
    val isPinned: Boolean,
    val sortOrder: Int,                   // manual reorder index
    val createdAt: Long,                  // epoch ms
    val lastActiveAt: Long,               // epoch ms
)