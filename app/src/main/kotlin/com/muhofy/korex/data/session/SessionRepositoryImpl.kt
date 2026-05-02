package com.muhofy.korex.data.session

import com.muhofy.korex.domain.SessionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SessionRepositoryImpl @Inject constructor(
    private val dao: SessionDao,
) : SessionRepository {

    override fun observeAll(): Flow<List<SessionEntity>> =
        dao.observeAll()

    override suspend fun getById(id: String): SessionEntity? =
        dao.getById(id)

    override suspend fun insert(session: SessionEntity) =
        dao.insert(session)

    override suspend fun updateStatus(id: String, status: SessionStatus) =
        dao.updateStatus(id, status, System.currentTimeMillis())

    override suspend fun updateName(id: String, name: String) =
        dao.updateName(id, name)

    override suspend fun updateCwd(id: String, cwd: String) =
        dao.updateCwd(id, cwd, System.currentTimeMillis())

    override suspend fun updatePinned(id: String, isPinned: Boolean) =
        dao.updatePinned(id, isPinned)

    override suspend fun updateSortOrder(id: String, sortOrder: Int) =
        dao.updateSortOrder(id, sortOrder)

    override suspend fun delete(id: String) =
        dao.deleteById(id)
}