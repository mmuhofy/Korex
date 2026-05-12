package com.termux.terminal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Tracks how long the current command has been running.
 *
 * Usage:
 *   - Call [onCommandStarted] when Enter is sent to terminal
 *   - Call [onPromptDetected] when a shell prompt appears in output
 *   - Observe [elapsedSeconds] for live UI updates
 *   - Observe [lastDuration] for the finished command's duration (null = no previous command)
 */
class CommandTimer {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var tickJob: Job? = null
    private var startTime: Long = 0L

    // Live elapsed seconds while command is running (null = idle)
    private val _elapsedSeconds = MutableStateFlow<Int?>(null)
    val elapsedSeconds: StateFlow<Int?> = _elapsedSeconds.asStateFlow()

    // Duration of the last completed command in seconds (null = no command yet)
    private val _lastDuration = MutableStateFlow<Int?>(null)
    val lastDuration: StateFlow<Int?> = _lastDuration.asStateFlow()

    val isRunning: Boolean get() = tickJob?.isActive == true

    /** Call this when the user sends a command (Enter pressed). */
    fun onCommandStarted() {
        tickJob?.cancel()
        startTime = System.currentTimeMillis()
        _elapsedSeconds.value = 0
        tickJob = scope.launch {
            while (true) {
                delay(1_000)
                _elapsedSeconds.value = ((System.currentTimeMillis() - startTime) / 1000).toInt()
            }
        }
    }

    /**
     * Call this when a shell prompt is detected in terminal output.
     * Stops the timer and records the final duration.
     */
    fun onPromptDetected() {
        if (!isRunning) return
        tickJob?.cancel()
        tickJob = null
        val duration = ((System.currentTimeMillis() - startTime) / 1000).toInt()
        _lastDuration.value = duration
        _elapsedSeconds.value = null
    }

    fun reset() {
        tickJob?.cancel()
        tickJob = null
        _elapsedSeconds.value = null
        _lastDuration.value = null
    }
}