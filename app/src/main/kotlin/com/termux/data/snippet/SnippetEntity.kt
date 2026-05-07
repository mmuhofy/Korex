package com.termux.data.snippet

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "snippets")
data class SnippetEntity(
    @PrimaryKey val id: String,   // UUID
    val title: String,
    val command: String,
    val createdAt: Long,          // epoch ms
)