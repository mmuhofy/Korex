package com.muhofy.korex.terminal

import android.content.Context
import android.util.Log
import com.termux.terminal.TerminalSession
import com.muhofy.korex.util.TERMINAL_FONT_SIZE_DEFAULT
import com.muhofy.korex.util.TERMINAL_FONT_SIZE_MIN
import com.muhofy.korex.util.TERMINAL_FONT_SIZE_MAX
import com.muhofy.korex.util.TERMINAL_TRANSCRIPT_ROWS
import java.io.File

private const val TAG = "TerminalBridge"

/**
 * Creates and owns a single TerminalSession (pty process).
 * One TerminalBridge per Korex session.
 *
 * SHELL RESOLUTION — noexec bypass:
 * Android API 29+ mounts filesDir as noexec — execve() on binaries there fails.
 * Shell binaries (bash, zsh) are shipped as libkorex-*.so inside jniLibs/.
 * Android extracts these to nativeLibraryDir at install time, which is always
 * exec-able. We resolve the shell from there — no /proc/self/fd tricks needed.
 *
 * Priority: zsh → bash → /system/bin/sh
 */
class TerminalBridge(
    private val context: Context,
    val sessionClient: KorexTerminalSessionClient,
) {

    val session: TerminalSession = TerminalSession(
        /* shellPath      */ resolveShellPath(context),
        /* cwd            */ homDir(context).also { it.mkdirs() }.absolutePath,
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
         * Resolves the shell binary path from nativeLibraryDir.
         * Binaries are shipped as libkorex-*.so in jniLibs/ and extracted
         * by Android to nativeLibraryDir at install time (always exec-able).
         *
         * Priority: zsh → bash → system sh
         */
        fun resolveShellPath(context: Context): String {
            val nativeDir = context.applicationInfo.nativeLibraryDir

            val zsh  = File(nativeDir, "libkorex-zsh.so")
            val bash = File(nativeDir, "libkorex-bash.so")

            return when {
                zsh.exists()  -> {
                    Log.i(TAG, "Shell: zsh (${zsh.absolutePath})")
                    zsh.absolutePath
                }
                bash.exists() -> {
                    Log.i(TAG, "Shell: bash (${bash.absolutePath})")
                    bash.absolutePath
                }
                else -> {
                    Log.w(TAG, "Shell: system sh fallback — zsh/bash not found in nativeLibraryDir")
                    SHELL_FALLBACK
                }
            }
        }

        fun prefixDir(context: Context): File = File(context.filesDir, "usr")
        fun homDir(context: Context): File    = File(context.filesDir, "home")

        /**
         * Environment for the pty process.
         * LD_LIBRARY_PATH is set so zsh/bash can find their shared libs
         * under filesDir/usr/lib at runtime.
         */
        fun buildEnv(context: Context): Array<String> {
            val filesDir  = context.filesDir.absolutePath
            val prefix    = "$filesDir/usr"
            val nativeDir = context.applicationInfo.nativeLibraryDir
            return arrayOf(
                "TERM=xterm-256color",
                "HOME=$filesDir/home",
                "PREFIX=$prefix",
                "PATH=$prefix/bin:$prefix/bin/applets:/system/bin:/system/xbin",
                "LD_LIBRARY_PATH=$prefix/lib:$nativeDir",
                "LANG=en_US.UTF-8",
                "TMPDIR=$prefix/tmp",
                "SHELL=${resolveShellPath(context.applicationContext)
                    .let { if (it == SHELL_FALLBACK) it else it }}",
            )
        }
    }
}