package com.termux.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.termux.data.history.CommandHistoryEntity
import com.termux.domain.CommandHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CommandHistoryViewModel @Inject constructor(
    private val repository: CommandHistoryRepository,
) : ViewModel() {

    val searchQuery = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val history: StateFlow<List<CommandHistoryEntity>> = searchQuery
        .debounce(200)
        .flatMapLatest { query ->
            if (query.isBlank()) repository.observeAll()
            else repository.search(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun recordCommand(sessionId: String, command: String) {
        if (command.isBlank()) return
        viewModelScope.launch {
            repository.insert(
                CommandHistoryEntity(
                    id          = UUID.randomUUID().toString(),
                    sessionId   = sessionId,
                    command     = command.trim(),
                    executedAt  = System.currentTimeMillis(),
                )
            )
        }
    }

    fun delete(id: String) {
        viewModelScope.launch { repository.deleteById(id) }
    }

    fun clearAll() {
        viewModelScope.launch { repository.clearAll() }
    }
}