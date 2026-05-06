package com.korexx.terminal

import android.content.Context
import android.util.Log
import com.termux.terminal.TerminalSession
import com.korexx.util.TERMINAL_FONT_SIZE_DEFAULT
import com.korexx.util.TERMINAL_FONT_SIZE_MAX
import com.korexx.util.TERMINAL_FONT_SIZE_MIN
import com.korexx.util.TERMINAL_TRANSCRIPT_ROWS
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
        /* args           */ resolveShellArgs(context),
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

        fun resolveShellPath(context: Context): String {
            val binDir = File(context.filesDir, "usr/bin")
            val proot  = File(binDir, "proot")
            return if (proot.exists()) {
                proot.absolutePath.also { Log.i(TAG, "Shell: proot ($it)") }
            } else {
                val zsh  = File(binDir, "zsh")
                val bash = File(binDir, "bash")
                when {
                    zsh.exists()  -> zsh.absolutePath.also  { Log.i(TAG, "Shell: zsh ($it)") }
                    bash.exists() -> bash.absolutePath.also { Log.i(TAG, "Shell: bash ($it)") }
                    else          -> SHELL_FALLBACK.also    { Log.w(TAG, "Shell: system sh fallback") }
                }
            }
        }

        fun resolveShellArgs(context: Context): Array<String> {
            val filesDir = context.filesDir.absolutePath
            val prefix   = "$filesDir/usr"
            val proot    = File("$prefix/bin/proot")
            if (!proot.exists()) return emptyArray()

            val shell = File("$prefix/bin/zsh").let {
                if (it.exists()) it.absolutePath else "$prefix/bin/bash"
            }

            return arrayOf(
                // Bind com.termux paths → com.korexx so dpkg/pkg syscalls work
                "-b", "/data/data/com.korexx:/data/data/com.termux",
                "-b", "/data/user/0/com.korexx:/data/user/0/com.termux",
                // Essential system bindings
                "-b", "/proc",
                "-b", "/dev",
                "-b", "/sys",
                "-b", "/system",
                // Working dir
                "--cwd", "$filesDir/home",
                // Shell
                shell,
            )
        }

        fun prefixDir(context: Context): File = File(context.filesDir, "usr")
        fun homeDir(context: Context): File   = File(context.filesDir, "home")

        fun buildEnv(context: Context): Array<String> {
            val filesDir  = context.filesDir.absolutePath
            val prefix    = "$filesDir/usr"
            val nativeDir = context.applicationInfo.nativeLibraryDir

            // libtermux.so = termux-exec, intercepts execve() for child processes
            val termuxExec = "$nativeDir/libtermux.so"

            return arrayOf(
                "TERM=xterm-256color",
                "HOME=$filesDir/home",
                "PREFIX=$prefix",
                "PATH=$prefix/bin:$prefix/bin/applets:/system/bin:/system/xbin",
                "LD_LIBRARY_PATH=$prefix/lib:$nativeDir",
                "LD_PRELOAD=$termuxExec",
                "LANG=en_US.UTF-8",
                "TMPDIR=$prefix/tmp",
                "SHELL=$prefix/bin/zsh",
                // Required by termux-exec to intercept and rewrite com.termux paths
                "TERMUX_APP_DATA_DIR=$filesDir",
                "TERMUX_ROOTFS=$filesDir",
                "TERMUX_PREFIX=$prefix",
            )
        }
    }
}