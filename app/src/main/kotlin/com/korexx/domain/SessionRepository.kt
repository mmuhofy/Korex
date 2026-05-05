package com.korexx.domain

import com.korexx.data.session.SessionEntity
import com.korexx.data.session.SessionStatus
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    fun observeAll(): Flow<List<SessionEntity>>
    suspend fun getById(id: String): SessionEntity?
    suspend fun insert(session: SessionEntity)
    suspend fun updateStatus(id: String, status: SessionStatus)
    suspend fun updateName(id: String, name: String)
    suspend fun updateCwd(id: String, cwd: String)
    suspend fun updatePinned(id: String, isPinned: Boolean)
    suspend fun updateSortOrder(id: String, sortOrder: Int)
    suspend fun delete(id: String)
}