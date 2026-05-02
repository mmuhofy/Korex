package com.muhofy.korex.terminal

import android.content.Context
import com.termux.terminal.TerminalSession
import com.termux.terminal.TerminalSessionClient
import com.muhofy.korex.util.TERMINAL_TRANSCRIPT_ROWS

// UNTESTED — verify before use
/**
 * Creates and owns a single TerminalSession (pty process).
 * One TerminalBridge per Korex session.
 */
class TerminalBridge(
    context: Context,
    private val sessionClient: TerminalSessionClient,
) {

    val session: TerminalSession = TerminalSession(
        /* shellPath       */ resolveShell(),
        /* cwd             */ context.filesDir.absolutePath,
        /* args            */ emptyArray(),
        /* env             */ buildEnv(),
        /* transcriptRows  */ TERMINAL_TRANSCRIPT_ROWS,
        /* client          */ sessionClient,
    )

    /** Write user input into the pty. */
    fun write(data: String) {
        session.write(data)
    }

    /** Terminate the pty process. */
    fun destroy() {
        session.finishIfRunning()
    }

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

    companion object {
        // Intentionally empty — HOME is resolved dynamically from context.filesDir
    }
}