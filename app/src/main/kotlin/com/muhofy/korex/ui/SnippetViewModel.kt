package com.muhofy.korex.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muhofy.korex.data.snippet.SnippetEntity
import com.muhofy.korex.domain.SnippetRepository
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
        }
    }

    fun updateSnippet(snippet: SnippetEntity, title: String, command: String) {
        viewModelScope.launch {
            repository.update(snippet.copy(title = title.trim(), command = command.trim()))
        }
    }

    fun deleteSnippet(id: String) {
        viewModelScope.launch { repository.delete(id) }
    }
}