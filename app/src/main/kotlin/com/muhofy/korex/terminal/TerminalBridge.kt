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
        /* cwd             */ HOME_DIR,
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
        listOf("/data/data/com.termux/files/usr/bin/bash", "/system/bin/sh")
            .firstOrNull { java.io.File(it).exists() }
            ?: "/system/bin/sh"

    private fun buildEnv(): Array<String> = arrayOf(
        "TERM=xterm-256color",
        "HOME=$HOME_DIR",
        "PATH=/data/data/com.termux/files/usr/bin:/system/bin:/system/xbin",
        "LANG=en_US.UTF-8",
    )

    companion object {
        private const val HOME_DIR = "/data/data/com.termux/files/home"
    }
}