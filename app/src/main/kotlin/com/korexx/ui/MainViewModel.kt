package com.korexx.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.korexx.data.session.SessionEntity
import com.korexx.session.SessionManager
import com.korexx.session.SplitScreenState
import com.korexx.util.SPLIT_RATIO_MIN
import com.korexx.util.SPLIT_RATIO_MAX
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val sessionManager: SessionManager,
) : ViewModel() {

    val sessions: StateFlow<List<SessionEntity>> = sessionManager.activeSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeSessionId: StateFlow<String?> = sessionManager.activeSessionId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _splitState = MutableStateFlow<SplitScreenState?>(null)
    val splitState: StateFlow<SplitScreenState?> = _splitState.asStateFlow()

    // ------------------------------------------------------------------ //
    // Session actions
    // ------------------------------------------------------------------ //

    fun createSession(name: String) = sessionManager.createSession(name)
    fun switchTo(id: String) = sessionManager.switchTo(id)
    fun switchToNext() = sessionManager.switchToNext()
    fun switchToPrevious() = sessionManager.switchToPrevious()
    fun closeSession(id: String) {
        // If closed session is part of split, exit split first
        _splitState.value?.let { split ->
            if (split.primarySessionId == id || split.secondarySessionId == id) {
                exitSplit()
            }
        }
        sessionManager.closeSession(id)
    }
    fun renameSession(id: String, name: String) = sessionManager.renameSession(id, name)
    fun pinSession(id: String, pinned: Boolean) = sessionManager.pinSession(id, pinned)
    fun getBridge(id: String) = sessionManager.getBridge(id)
    fun restoreOnStart() = sessionManager.restoreOnStart()

    // ------------------------------------------------------------------ //
    // Split screen actions
    // ------------------------------------------------------------------ //

    /**
     * Enter split screen — primary is current active session,
     * secondary is the next available session or a newly created one.
     */
    fun enterSplit() {
        val primaryId = activeSessionId.value ?: return
        val sessions  = sessions.value
        val secondaryId = sessions.firstOrNull { it.id != primaryId }?.id
        if (secondaryId != null) {
            _splitState.value = SplitScreenState(
                primarySessionId   = primaryId,
                secondarySessionId = secondaryId,
            )
        } else {
            // No other session — create one automatically
            sessionManager.createSession("Split")
            // secondaryId will be set when sessions flow updates
            _splitState.value = SplitScreenState(primarySessionId = primaryId)
        }
    }

    fun exitSplit() {
        _splitState.value = null
    }

    fun updateSplitRatio(delta: Float) {
        _splitState.update { state ->
            state?.copy(
                splitRatio = (state.splitRatio + delta)
                    .coerceIn(SPLIT_RATIO_MIN, SPLIT_RATIO_MAX)
            )
        }
    }

    /** Called when sessions update — wires the new session into split if pending. */
    fun onSessionsUpdated(sessionIds: List<String>) {
        val state = _splitState.value ?: return
        if (state.secondarySessionId == null) {
            val secondaryId = sessionIds.firstOrNull { it != state.primarySessionId }
            if (secondaryId != null) {
                _splitState.value = state.copy(secondarySessionId = secondaryId)
            }
        }
    }
}