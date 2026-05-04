package com.muhofy.korex.terminal

import android.content.Context
import android.system.ErrnoException
import android.util.Log
import com.termux.terminal.TerminalSession
import com.muhofy.korex.util.TERMINAL_FONT_SIZE_DEFAULT
import com.muhofy.korex.util.TERMINAL_FONT_SIZE_MAX
import com.muhofy.korex.util.TERMINAL_FONT_SIZE_MIN
import com.muhofy.korex.util.TERMINAL_TRANSCRIPT_ROWS
import java.io.File

private const val TAG = "TerminalBridge"

/**
 * Creates and owns a single TerminalSession (pty process).
 * Also owns the current font size for this session's TerminalView.
 * One TerminalBridge per Korex session.
 *
 * NOEXEC WORKAROUND:
 * filesDir is mounted noexec on Android API 29+. We resolve the shell
 * binary path via /proc/self/fd/<N> using BootstrapInstaller.openFdPath(),
 * which bypasses the noexec mount flag check in the kernel.
 * See BootstrapInstaller.openFdPath() for full explanation.
 */
class TerminalBridge(
    private val context: Context,
    val sessionClient: KorexTerminalSessionClient,
) {

    val session: TerminalSession = TerminalSession(
        /* shellPath      */ resolveShellPath(),
        /* cwd            */ cwdDir(context).also { it.mkdirs() }.absolutePath,
        /* args           */ emptyArray(),
        /* env            */ buildEnv(context),
        /* transcriptRows */ TERMINAL_TRANSCRIPT_ROWS,
        /* client         */ sessionClient,
    )

    /** Current font size in sp — mutated by pinch zoom and settings slider. */
    var fontSize: Int = TERMINAL_FONT_SIZE_DEFAULT
        private set

    /**
     * Scale font size by [factor], clamped to allowed range.
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
    // Private helpers
    // ------------------------------------------------------------------ //

    /**
     * Resolves the shell path to use for TerminalSession.
     *
     * Priority:
     * 1. Bootstrap bash via /proc/self/fd/<N> (noexec bypass) — preferred
     * 2. System /system/bin/sh — fallback (always exec-able, limited)
     *
     * The /proc/self/fd path is only valid while the fd remains open.
     * BootstrapInstaller.openFdPath() intentionally does NOT close the fd.
     */
    private fun resolveShellPath(): String {
        val bootstrapBash = File(context.filesDir, "usr/bin/bash")

        if (bootstrapBash.exists()) {
            return try {
                val fdPath = BootstrapInstaller.openFdPath(bootstrapBash)
                Log.i(TAG, "Using bootstrap bash via fd path: $fdPath")
                fdPath
            } catch (e: ErrnoException) {
                Log.e(TAG, "Failed to open bash fd, falling back to /system/bin/sh", e)
                SHELL_FALLBACK
            }
        }

        Log.w(TAG, "Bootstrap bash not found, using system sh fallback")
        return SHELL_FALLBACK
    }

    companion object {
        private const val SHELL_FALLBACK = "/system/bin/sh"

        fun prefixDir(context: Context): File = File(context.filesDir, "usr")
        fun cwdDir(context: Context): File    = File(context.filesDir, "home")

        /**
         * Builds the environment array for the pty process.
         * All paths are relative to filesDir — no hardcoded /data paths.
         */
        fun buildEnv(context: Context): Array<String> {
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
}