package com.termux.domain

import com.termux.data.snippet.SnippetEntity
import kotlinx.coroutines.flow.Flow

interface SnippetRepository {
    fun observeAll(): Flow<List<SnippetEntity>>
    suspend fun getAll(): List<SnippetEntity>
    suspend fun insert(snippet: SnippetEntity)
    suspend fun update(snippet: SnippetEntity)
    suspend fun delete(id: String)
}