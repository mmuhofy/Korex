package com.muhofy.korex.terminal

import android.content.Context
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
 * One TerminalBridge per Korex session.
 *
 * SHELL RESOLUTION:
 * targetSdkVersion = 28 allows execve() from filesDir on Android 9 compat mode.
 * Shell binaries live in filesDir/usr/bin/ after bootstrap installation.
 * Priority: zsh → bash → /system/bin/sh
 */
class TerminalBridge(
    private val context: Context,
    val sessionClient: KorexTerminalSessionClient,
) {

    val session: TerminalSession = TerminalSession(
        /* shellPath      */ resolveShellPath(context),
        /* cwd            */ homeDir(context).also { it.mkdirs() }.absolutePath,
        /* args           */ emptyArray(),
        /* env            */ buildEnv(context),
        /* transcriptRows */ TERMINAL_TRANSCRIPT_ROWS,
        /* client         */ sessionClient,
    )

    var fontSize: Int = TERMINAL_FONT_SIZE_DEFAULT
        private set

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

    companion object {

        private const val SHELL_FALLBACK = "/system/bin/sh"

        /**
         * Resolves shell from bootstrap prefix.
         * targetSdk 28 allows execve() from filesDir — no tricks needed.
         * Priority: zsh → bash → system sh
         */
        fun resolveShellPath(context: Context): String {
            val binDir = File(context.filesDir, "usr/bin")
            val zsh    = File(binDir, "zsh")
            val bash   = File(binDir, "bash")

            return when {
                zsh.exists()  -> zsh.absolutePath.also {
                    Log.i(TAG, "Shell: zsh ($it)")
                }
                bash.exists() -> bash.absolutePath.also {
                    Log.i(TAG, "Shell: bash ($it)")
                }
                else -> SHELL_FALLBACK.also {
                    Log.w(TAG, "Shell: system sh fallback — bootstrap not installed")
                }
            }
        }

        fun prefixDir(context: Context): File = File(context.filesDir, "usr")
        fun homeDir(context: Context): File   = File(context.filesDir, "home")

        /**
         * Environment for the pty process.
         * LD_LIBRARY_PATH set so bootstrap shared libs under usr/lib are found.
         */
        fun buildEnv(context: Context): Array<String> {
            val filesDir = context.filesDir.absolutePath
            val prefix   = "$filesDir/usr"
            return arrayOf(
                "TERM=xterm-256color",
                "HOME=$filesDir/home",
                "PREFIX=$prefix",
                "PATH=$prefix/bin:$prefix/bin/applets:/system/bin:/system/xbin",
                "LD_LIBRARY_PATH=$prefix/lib:${context.filesDir.absolutePath}/lib",
                "LANG=en_US.UTF-8",
                "TMPDIR=$prefix/tmp",
                "SHELL=$prefix/bin/zsh",
            )
        }
    }
}