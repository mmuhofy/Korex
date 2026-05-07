package com.termux.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.termux.data.snippet.SnippetEntity
import com.termux.domain.SnippetRepository
import com.termux.terminal.SnippetSyncManager
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
            syncManager.sync(repository.getAll())
        }
    }

    fun updateSnippet(snippet: SnippetEntity, title: String, command: String) {
        viewModelScope.launch {
            repository.update(snippet.copy(title = title.trim(), command = command.trim()))
            syncManager.sync(repository.getAll())
        }
    }

    fun deleteSnippet(id: String) {
        viewModelScope.launch {
            repository.delete(id)
            syncManager.sync(repository.getAll())
        }
    }
}