package com.muhofy.korex.terminal

import android.content.Context
import java.io.File
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

    private val prefix = File(context.filesDir, "usr")

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

    private fun resolveShell(): String {
        val bootstrapBash = File(context.filesDir, "usr/bin/bash")
        return when {
            bootstrapBash.exists() -> bootstrapBash.absolutePath
            else -> "/system/bin/sh"
        }
    }

    private fun buildEnv(): Array<String> {
        val filesDir  = context.filesDir.absolutePath
        val prefixDir = "$filesDir/usr"
        return arrayOf(
            "TERM=xterm-256color",
            "HOME=$filesDir/home",
            "PREFIX=$prefixDir",
            "PATH=$prefixDir/bin:$prefixDir/bin/applets:/system/bin:/system/xbin",
            "LD_LIBRARY_PATH=$prefixDir/lib",
            "LANG=en_US.UTF-8",
            "TMPDIR=$prefixDir/tmp",
        )
    }
}