package com.termux.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.termux.data.theme.ThemeEntity
import com.termux.domain.ThemeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val repository: ThemeRepository,
) : ViewModel() {

    val installedThemes: StateFlow<List<ThemeEntity>> = repository.observeInstalled()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeTheme: StateFlow<ThemeEntity?> = repository.observeActive()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        // Seed built-in themes once — no-op if already in DB
        viewModelScope.launch { repository.seedBuiltIns() }
    }

    fun install(theme: ThemeEntity) {
        viewModelScope.launch { repository.install(theme) }
    }

    fun setActive(id: String) {
        viewModelScope.launch { repository.setActive(id) }
    }

    fun delete(id: String) {
        viewModelScope.launch { repository.delete(id) }
    }
}