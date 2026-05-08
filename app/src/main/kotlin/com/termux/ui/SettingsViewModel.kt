package com.termux.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.termux.data.SettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val store: SettingsDataStore,
) : ViewModel() {

    val settings = store.settings.stateIn(
        scope         = viewModelScope,
        started       = SharingStarted.WhileSubscribed(5000),
        initialValue  = com.termux.data.KorexSettings(),
    )

    fun setDarkTheme(value: Boolean) {
        viewModelScope.launch { store.setDarkTheme(value) }
    }

    fun setFontSize(value: Float) {
        viewModelScope.launch { store.setFontSize(value) }
    }

    fun setDefaultShell(value: String) {
        viewModelScope.launch { store.setDefaultShell(value) }
    }
}