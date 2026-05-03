package com.muhofy.korex.domain

import com.muhofy.korex.data.history.CommandHistoryEntity
import kotlinx.coroutines.flow.Flow

interface CommandHistoryRepository {
    fun observeAll(): Flow<List<CommandHistoryEntity>>
    fun observeBySession(sessionId: String): Flow<List<CommandHistoryEntity>>
    fun search(query: String): Flow<List<CommandHistoryEntity>>
    suspend fun insert(entry: CommandHistoryEntity)
    suspend fun deleteById(id: String)
    suspend fun clearAll()
    suspend fun clearBySession(sessionId: String)
}