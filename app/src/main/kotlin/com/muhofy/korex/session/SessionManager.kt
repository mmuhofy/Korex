package com.muhofy.korex.session

import android.content.Context
import com.muhofy.korex.data.session.SessionEntity
import com.muhofy.korex.data.session.SessionStatus
import com.muhofy.korex.domain.SessionRepository
import com.muhofy.korex.terminal.KorexTerminalSessionClient
import com.muhofy.korex.terminal.TerminalBridge
import com.muhofy.korex.util.SESSION_NAME_MAX_LENGTH
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

// UNTESTED — verify before use
@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: SessionRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // Active pty bridges — keyed by session id
    private val bridges = mutableMapOf<String, TerminalBridge>()

    private val _activeSessions = MutableStateFlow<List<SessionEntity>>(emptyList())
    val activeSessions: StateFlow<List<SessionEntity>> = _activeSessions.asStateFlow()

    private val _activeSessionId = MutableStateFlow<String?>(null)
    val activeSessionId: StateFlow<String?> = _activeSessionId.asStateFlow()

    init {
        // Observe DB and keep state in sync
        scope.launch {
            repository.observeAll().collect { sessions ->
                _activeSessions.value = sessions
            }
        }
    }

    // ------------------------------------------------------------------ //
    // Public API
    // ------------------------------------------------------------------ //

    /** Restore sessions from DB on app start. Re-creates pty bridges for surviving sessions. */
    fun restoreOnStart() {
        scope.launch {
            val sessions = _activeSessions.value
            sessions.forEach { entity ->
                if (entity.status == SessionStatus.ACTIVE ||
                    entity.status == SessionStatus.BACKGROUND
                ) {
                    createBridge(entity.id)
                    repository.updateStatus(entity.id, SessionStatus.BACKGROUND)
                }
            }
            // Set active to first pinned, or first in list
            val toRestore = sessions.firstOrNull { it.isPinned } ?: sessions.firstOrNull()
            toRestore?.let { switchTo(it.id) }
        }
    }

    /** Create a new session with the given name and make it active. */
    fun createSession(name: String) {
        val id = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val sanitizedName = name.trim().take(SESSION_NAME_MAX_LENGTH).ifEmpty { "Session" }
        val entity = SessionEntity(
            id           = id,
            name         = sanitizedName,
            cwd          = DEFAULT_CWD,
            env          = "",
            status       = SessionStatus.ACTIVE,
            isPinned     = false,
            sortOrder    = _activeSessions.value.size,
            createdAt    = now,
            lastActiveAt = now,
        )
        scope.launch {
            repository.insert(entity)
            createBridge(id)
            switchTo(id)
        }
    }

    /** Switch the active terminal to the session with the given id. */
    fun switchTo(id: String) {
        scope.launch {
            val prev = _activeSessionId.value
            if (prev != null && prev != id) {
                repository.updateStatus(prev, SessionStatus.BACKGROUND)
            }
            _activeSessionId.value = id
            repository.updateStatus(id, SessionStatus.ACTIVE)
        }
    }

    /** Switch to next session in list (wraps around). */
    fun switchToNext() {
        val sessions = _activeSessions.value
        if (sessions.size < 2) return
        val currentIndex = sessions.indexOfFirst { it.id == _activeSessionId.value }
        val nextIndex = (currentIndex + 1) % sessions.size
        switchTo(sessions[nextIndex].id)
    }

    /** Switch to previous session in list (wraps around). */
    fun switchToPrevious() {
        val sessions = _activeSessions.value
        if (sessions.size < 2) return
        val currentIndex = sessions.indexOfFirst { it.id == _activeSessionId.value }
        val prevIndex = (currentIndex - 1 + sessions.size) % sessions.size
        switchTo(sessions[prevIndex].id)
    }

    /** Close and destroy a session. */
    fun closeSession(id: String) {
        scope.launch {
            bridges[id]?.destroy()
            bridges.remove(id)
            repository.delete(id)
            // If closed session was active, switch to first available
            if (_activeSessionId.value == id) {
                val remaining = _activeSessions.value.filter { it.id != id }
                val next = remaining.firstOrNull { it.isPinned } ?: remaining.firstOrNull()
                _activeSessionId.value = next?.id
                next?.let { repository.updateStatus(it.id, SessionStatus.ACTIVE) }
            }
        }
    }

    fun renameSession(id: String, newName: String) {
        val sanitized = newName.trim().take(SESSION_NAME_MAX_LENGTH).ifEmpty { "Session" }
        scope.launch { repository.updateName(id, sanitized) }
    }

    fun pinSession(id: String, pinned: Boolean) {
        scope.launch { repository.updatePinned(id, pinned) }
    }

    fun updateCwd(id: String, cwd: String) {
        scope.launch { repository.updateCwd(id, cwd) }
    }

    /** Get the TerminalBridge for the given session id, if alive. */
    fun getBridge(id: String): TerminalBridge? = bridges[id]

    // ------------------------------------------------------------------ //
    // Internal
    // ------------------------------------------------------------------ //

    private fun createBridge(id: String) {
        if (bridges.containsKey(id)) return
        val client = KorexTerminalSessionClient(
            onSessionFinished = { handleSessionFinished(id) },
        )
        bridges[id] = TerminalBridge(context, client)
    }

    private fun handleSessionFinished(id: String) {
        scope.launch {
            repository.updateStatus(id, SessionStatus.CRASHED)
            bridges.remove(id)
        }
    }

    companion object {
        private const val DEFAULT_CWD = "/data/data/com.termux/files/home"
    }
}