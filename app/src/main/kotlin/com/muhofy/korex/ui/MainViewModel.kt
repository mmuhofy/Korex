package com.muhofy.korex.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muhofy.korex.data.session.SessionEntity
import com.muhofy.korex.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val sessionManager: SessionManager,
) : ViewModel() {

    val sessions: StateFlow<List<SessionEntity>> = sessionManager.activeSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeSessionId: StateFlow<String?> = sessionManager.activeSessionId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun createSession(name: String) = sessionManager.createSession(name)
    fun switchTo(id: String) = sessionManager.switchTo(id)
    fun switchToNext() = sessionManager.switchToNext()
    fun switchToPrevious() = sessionManager.switchToPrevious()
    fun closeSession(id: String) = sessionManager.closeSession(id)
    fun renameSession(id: String, name: String) = sessionManager.renameSession(id, name)
    fun pinSession(id: String, pinned: Boolean) = sessionManager.pinSession(id, pinned)
    fun getBridge(id: String) = sessionManager.getBridge(id)
    fun restoreOnStart() = sessionManager.restoreOnStart()
}