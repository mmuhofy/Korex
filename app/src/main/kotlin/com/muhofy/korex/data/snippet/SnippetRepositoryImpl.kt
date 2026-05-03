package com.muhofy.korex.data.snippet

import com.muhofy.korex.domain.SnippetRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SnippetRepositoryImpl @Inject constructor(
    private val dao: SnippetDao,
) : SnippetRepository {

    override fun observeAll(): Flow<List<SnippetEntity>> = dao.observeAll()

    override suspend fun insert(snippet: SnippetEntity) = dao.insert(snippet)

    override suspend fun update(snippet: SnippetEntity) = dao.update(snippet)

    override suspend fun delete(id: String) = dao.deleteById(id)
}