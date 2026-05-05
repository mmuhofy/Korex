package com.korexx.data.history

import com.korexx.domain.CommandHistoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CommandHistoryRepositoryImpl @Inject constructor(
    private val dao: CommandHistoryDao,
) : CommandHistoryRepository {

    override fun observeAll(): Flow<List<CommandHistoryEntity>> = dao.observeAll()

    override fun observeBySession(sessionId: String): Flow<List<CommandHistoryEntity>> =
        dao.observeBySession(sessionId)

    override fun search(query: String): Flow<List<CommandHistoryEntity>> = dao.search(query)

    override suspend fun insert(entry: CommandHistoryEntity) = dao.insert(entry)

    override suspend fun deleteById(id: String) = dao.deleteById(id)

    override suspend fun clearAll() = dao.clearAll()

    override suspend fun clearBySession(sessionId: String) = dao.clearBySession(sessionId)
}