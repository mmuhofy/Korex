package com.termux.terminal

import android.content.Context
import android.util.Log
import com.termux.util.TERMINAL_FONT_SIZE_DEFAULT
import com.termux.util.TERMINAL_FONT_SIZE_MAX
import com.termux.util.TERMINAL_FONT_SIZE_MIN
import com.termux.util.TERMINAL_TRANSCRIPT_ROWS
import java.io.File

private const val TAG = "TerminalBridge"

class TerminalBridge(
    private val context: Context,
    val sessionClient: KorexTerminalSessionClient = KorexTerminalSessionClient(context),
    shellOverride: String? = null,                // set by SessionManager from DataStore
) {

    val session: TerminalSession = TerminalSession(
        /* shellPath      */ shellOverride ?: resolveShellPath(context),
        /* cwd            */ homeDir(context).also { it.mkdirs() }.absolutePath,
        /* args           */ resolveShellArgs(context),
        /* env            */ buildEnv(context),
        /* transcriptRows */ TERMINAL_TRANSCRIPT_ROWS,
        /* client         */ sessionClient,
    )

    var fontSize: Int = TERMINAL_FONT_SIZE_DEFAULT
        private set

    /**
     * Sticky modifier flags set by ExtraKeyBar.
     * Read and consumed (reset to false) by KorexTerminalViewClient.readControlKey()
     * and readAltKey() on the next keyboard event processed by TerminalView.
     * Volatile so reads/writes are visible across the Compose + View threads.
     */
    @Volatile var ctrlDown: Boolean = false
    @Volatile var altDown: Boolean  = false

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
            val zsh    = File(binDir, "zsh")
            val bash   = File(binDir, "bash")
            return when {
                zsh.exists()  -> zsh.absolutePath.also  { Log.i(TAG, "Shell: zsh ($it)") }
                bash.exists() -> bash.absolutePath.also { Log.i(TAG, "Shell: bash ($it)") }
                else          -> SHELL_FALLBACK.also    { Log.w(TAG, "Shell: system sh fallback") }
            }
        }

        fun resolveShellArgs(context: Context): Array<String> = emptyArray()

        fun prefixDir(context: Context): File = File(context.filesDir, "usr")
        fun homeDir(context: Context): File   = File(context.filesDir, "home")

        fun buildEnv(context: Context): Array<String> {
            val filesDir  = context.filesDir.absolutePath
            val prefix    = "$filesDir/usr"
            val nativeDir = context.applicationInfo.nativeLibraryDir
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
                "TERMUX_APP_DATA_DIR=$filesDir",
                "TERMUX_ROOTFS=$filesDir",
                "TERMUX_PREFIX=$prefix",
            )
        }
    }
}