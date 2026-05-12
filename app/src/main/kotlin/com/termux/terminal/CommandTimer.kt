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
 * Tracks command duration via shell OSC title signals.
 *
 * Shell sends:
 *   KOREX_START        → preexec  (command started)
 *   KOREX_END:<secs>   → precmd   (command finished, shell-measured duration)
 *
 * We also run a local tick so the UI shows a live counter while waiting.
 */
class CommandTimer {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var tickJob: Job? = null
    private var startTime: Long = 0L

    // Live elapsed seconds while command is running (null = idle)
    private val _elapsedSeconds = MutableStateFlow<Int?>(null)
    val elapsedSeconds: StateFlow<Int?> = _elapsedSeconds.asStateFlow()

    // Duration of the last completed command in seconds (null = none yet)
    private val _lastDuration = MutableStateFlow<Int?>(null)
    val lastDuration: StateFlow<Int?> = _lastDuration.asStateFlow()

    val isRunning: Boolean get() = tickJob?.isActive == true

    /** Called when shell fires preexec (KOREX_START). */
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
     * Called when shell fires precmd (KOREX_END:<secs>).
     * Uses shell-measured duration for accuracy.
     */
    fun onCommandFinished(durationSeconds: Int) {
        tickJob?.cancel()
        tickJob = null
        _elapsedSeconds.value = null
        _lastDuration.value = durationSeconds

        // Clear the "finished" indicator after 5 seconds
        scope.launch {
            delay(5_000)
            if (_lastDuration.value == durationSeconds) {
                _lastDuration.value = null
            }
        }
    }

    fun reset() {
        tickJob?.cancel()
        tickJob = null
        _elapsedSeconds.value = null
        _lastDuration.value = null
    }
}