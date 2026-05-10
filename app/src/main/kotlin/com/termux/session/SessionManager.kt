package com.termux.session

import android.content.Context
import com.termux.data.SettingsDataStore
import com.termux.data.session.SessionEntity
import com.termux.data.session.SessionStatus
import com.termux.domain.SessionRepository
import com.termux.terminal.KorexTerminalSessionClient
import com.termux.terminal.TerminalBridge
import com.termux.util.SESSION_NAME_MAX_LENGTH
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: SessionRepository,
    private val settingsDataStore: SettingsDataStore,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val bridges = mutableMapOf<String, TerminalBridge>()

    private val _activeSessions  = MutableStateFlow<List<SessionEntity>>(emptyList())
    val activeSessions: StateFlow<List<SessionEntity>> = _activeSessions.asStateFlow()

    private val _activeSessionId = MutableStateFlow<String?>(null)
    val activeSessionId: StateFlow<String?> = _activeSessionId.asStateFlow()

    init {
        scope.launch {
            repository.observeAll().collect { sessions ->
                _activeSessions.value = sessions
            }
        }
    }

    // ------------------------------------------------------------------ //
    // Public API
    // ------------------------------------------------------------------ //

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
            val toRestore = sessions.firstOrNull { it.isPinned } ?: sessions.firstOrNull()
            toRestore?.let { switchTo(it.id) }
        }
    }

    fun createSession(name: String) {
        val id  = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val sanitizedName = name.trim().take(SESSION_NAME_MAX_LENGTH).ifEmpty { "Session" }
        val entity = SessionEntity(
            id           = id,
            name         = sanitizedName,
            cwd          = homeDir,
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

    fun switchToNext() {
        val sessions = _activeSessions.value
        if (sessions.size < 2) return
        val idx = sessions.indexOfFirst { it.id == _activeSessionId.value }
        switchTo(sessions[(idx + 1) % sessions.size].id)
    }

    fun switchToPrevious() {
        val sessions = _activeSessions.value
        if (sessions.size < 2) return
        val idx = sessions.indexOfFirst { it.id == _activeSessionId.value }
        switchTo(sessions[(idx - 1 + sessions.size) % sessions.size].id)
    }

    fun closeSession(id: String) {
        scope.launch {
            bridges[id]?.destroy()
            bridges.remove(id)
            repository.delete(id)
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

    fun getBridge(id: String): TerminalBridge? = bridges[id]

    /** Applies font size to all active terminal bridges. Called when settings change. */
    fun applyFontSizeToAll(size: Int) {
        bridges.values.forEach { it.setFontSize(size) }
    }

    // ------------------------------------------------------------------ //
    // Internal
    // ------------------------------------------------------------------ //

    private fun createBridge(id: String) {
        if (bridges.containsKey(id)) return
        scope.launch {
            val preferredShell = settingsDataStore.settings.first().defaultShell
            val client = KorexTerminalSessionClient(
                context           = context,
                onSessionFinished = { handleSessionFinished(id) },
            )
            bridges[id] = TerminalBridge(
                context       = context,
                sessionClient = client,
                shellOverride = resolveShell(preferredShell),
            )
        }
    }

    private fun handleSessionFinished(id: String) {
        scope.launch {
            repository.updateStatus(id, SessionStatus.CRASHED)
            bridges.remove(id)
        }
    }

    private fun resolveShell(preferred: String): String {
        val binDir = File(context.filesDir, "usr/bin")
        val order  = if (preferred == "bash") listOf("bash", "zsh") else listOf("zsh", "bash")
        return order
            .map { File(binDir, it) }
            .firstOrNull { it.exists() }
            ?.absolutePath
            ?: "/system/bin/sh"
    }

    private val homeDir: String get() = File(context.filesDir, "home").absolutePath
}