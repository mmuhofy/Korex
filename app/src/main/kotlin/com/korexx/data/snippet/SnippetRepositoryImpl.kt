package com.korexx.data.snippet

import com.korexx.domain.SnippetRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SnippetRepositoryImpl @Inject constructor(
    private val dao: SnippetDao,
) : SnippetRepository {

    override fun observeAll(): Flow<List<SnippetEntity>> = dao.observeAll()

    override suspend fun getAll(): List<SnippetEntity> = dao.getAll()

    override suspend fun insert(snippet: SnippetEntity) = dao.insert(snippet)

    override suspend fun update(snippet: SnippetEntity) = dao.update(snippet)

    override suspend fun delete(id: String) = dao.deleteById(id)
}