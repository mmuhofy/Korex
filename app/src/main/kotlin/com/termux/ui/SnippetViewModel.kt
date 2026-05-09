package com.termux.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.termux.data.snippet.SnippetEntity
import com.termux.domain.SnippetRepository
import com.termux.session.SessionManager
import com.termux.terminal.SnippetSyncManager
import com.termux.terminal.TerminalBridge
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class SnippetViewModel @Inject constructor(
    private val repository: SnippetRepository,
    private val syncManager: SnippetSyncManager,
    private val sessionManager: SessionManager,
) : ViewModel() {

    val snippets: StateFlow<List<SnippetEntity>> = repository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addSnippet(title: String, command: String) {
        viewModelScope.launch {
            repository.insert(
                SnippetEntity(
                    id        = UUID.randomUUID().toString(),
                    title     = title.trim(),
                    command   = command.trim(),
                    createdAt = System.currentTimeMillis(),
                )
            )
            doSync()
        }
    }

    fun updateSnippet(snippet: SnippetEntity, title: String, command: String) {
        viewModelScope.launch {
            repository.update(snippet.copy(title = title.trim(), command = command.trim()))
            doSync()
        }
    }

    fun deleteSnippet(id: String) {
        viewModelScope.launch {
            repository.delete(id)
            doSync()
        }
    }

    private suspend fun doSync() {
        syncManager.sync(
            snippets   = repository.getAll(),
            getBridges = { liveBridges() },
        )
    }

    /** Returns all currently alive TerminalBridge instances. */
    private fun liveBridges(): Collection<TerminalBridge> =
        sessionManager.activeSessions.value
            .mapNotNull { sessionManager.getBridge(it.id) }
}