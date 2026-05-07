package com.termux.data.history

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "command_history")
data class CommandHistoryEntity(
    @PrimaryKey val id: String,       // UUID
    val sessionId: String,            // which session this command belongs to
    val command: String,
    val executedAt: Long,             // epoch ms
)