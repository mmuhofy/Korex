package com.muhofy.korex.terminal

import android.content.Context
import com.termux.terminal.TerminalSession
import com.termux.terminal.TerminalSessionClient
import com.muhofy.korex.util.TERMINAL_TRANSCRIPT_ROWS
import com.muhofy.korex.util.TERMINAL_FONT_SIZE_DEFAULT
import com.muhofy.korex.util.TERMINAL_FONT_SIZE_MIN
import com.muhofy.korex.util.TERMINAL_FONT_SIZE_MAX

// UNTESTED — verify before use
/**
 * Creates and owns a single TerminalSession (pty process).
 * Also owns the current font size for this session's TerminalView.
 * One TerminalBridge per Korex session.
 */
class TerminalBridge(
    private val context: Context,
    val sessionClient: KorexTerminalSessionClient,
) {

    val session: TerminalSession = TerminalSession(
        /* shellPath       */ resolveShell(),
        /* cwd             */ context.filesDir.absolutePath,
        /* args            */ emptyArray(),
        /* env             */ buildEnv(),
        /* transcriptRows  */ TERMINAL_TRANSCRIPT_ROWS,
        /* client          */ sessionClient,
    )

    /** Current font size in sp — mutated by pinch zoom and settings slider. */
    var fontSize: Int = TERMINAL_FONT_SIZE_DEFAULT
        private set

    /**
     * Scale font size by [factor], clamped to [TERMINAL_FONT_SIZE_MIN]..[TERMINAL_FONT_SIZE_MAX].
     * Returns true if the size actually changed.
     */
    fun scaleFontSize(factor: Float): Boolean {
        val next = (fontSize * factor).toInt()
            .coerceIn(TERMINAL_FONT_SIZE_MIN, TERMINAL_FONT_SIZE_MAX)
        if (next == fontSize) return false
        fontSize = next
        return true
    }

    fun setFontSize(size: Int) {
        fontSize = size.coerceIn(TERMINAL_FONT_SIZE_MIN, TERMINAL_FONT_SIZE_MAX)
    }

    fun write(data: String) = session.write(data)

    fun destroy() = session.finishIfRunning()

    // ------------------------------------------------------------------ //

    private fun resolveShell(): String =
        listOf("/system/bin/sh", "/system/bin/bash")
            .firstOrNull { java.io.File(it).exists() }
            ?: "/system/bin/sh"

    private fun buildEnv(): Array<String> = arrayOf(
        "TERM=xterm-256color",
        "HOME=${context.filesDir.absolutePath}",
        "PATH=/system/bin:/system/xbin",
        "LANG=en_US.UTF-8",
    )
}