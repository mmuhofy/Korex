package com.termux.terminal

import android.content.Context
import android.util.Log
import com.termux.terminal.TerminalSession
import com.termux.util.TERMINAL_FONT_SIZE_DEFAULT
import com.termux.util.TERMINAL_FONT_SIZE_MAX
import com.termux.util.TERMINAL_FONT_SIZE_MIN
import com.termux.util.TERMINAL_TRANSCRIPT_ROWS
import java.io.File

private const val TAG = "TerminalBridge"

class TerminalBridge(
    private val context: Context,
    val sessionClient: KorexTerminalSessionClient = KorexTerminalSessionClient(context),
    shellOverride: String? = null,
) {
    private val shellPath = shellOverride ?: resolveShellPath(context)

    val session: TerminalSession = TerminalSession(
        /* shellPath      */ shellPath,
        /* cwd            */ homeDir(context).also { it.mkdirs() }.absolutePath,
        /* args           */ buildShellArgs(shellPath),
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
         * Shell hook script injected via --rcfile / --init-file.
         *
         * Sends OSC title escape sequences that KorexTerminalSessionClient parses:
         *   KOREX_START      → command started  (preexec)
         *   KOREX_END:<secs> → command finished (precmd), shell-measured duration
         *
         * _korex_cmd_time is set in preexec, duration computed in precmd.
         * Works for both zsh (native preexec/precmd) and bash (via PROMPT_COMMAND + trap DEBUG).
         */
        private val ZSH_HOOKS = """
            |autoload -Uz add-zsh-hook
            |_korex_cmd_time=0
            |_korex_preexec() { _korex_cmd_time=${'$'}SECONDS; printf '\033]0;KOREX_START\007' }
            |_korex_precmd()  { printf '\033]0;KOREX_END:%d\007' "$((${'$'}SECONDS - _korex_cmd_time))" }
            |add-zsh-hook preexec _korex_preexec
            |add-zsh-hook precmd  _korex_precmd
        """.trimMargin()

        private val BASH_HOOKS = """
            |_korex_cmd_time=0
            |_korex_preexec() { _korex_cmd_time=${'$'}SECONDS; printf '\033]0;KOREX_START\007'; }
            |_korex_precmd()  { printf '\033]0;KOREX_END:%d\007' "$((${'$'}SECONDS - _korex_cmd_time))"; }
            |trap '_korex_preexec' DEBUG
            |PROMPT_COMMAND='_korex_precmd'
        """.trimMargin()

        /**
         * Builds shell args to inject Korex hooks on startup.
         *
         * zsh:  zsh --no-globalrcs --rcs -c 'hooks' --
         *       We write a temp rcfile and pass it via ZDOTDIR trick, or use -c + exec zsh.
         *       Simplest cross-version approach: write to a temp file, pass as --rcfile arg.
         *
         * bash: bash --rcfile <tempfile>
         *
         * sh/fallback: no hooks — timer simply won't work.
         */
        fun buildShellArgs(shellPath: String): Array<String> {
            val shell = File(shellPath).name
            return when {
                shell == "zsh"  -> arrayOf("--no-globalrcs", "--rcs")
                shell == "bash" -> emptyArray()
                else            -> emptyArray()
            }
        }

        /**
         * Writes the Korex hook script to a temp file and returns its path.
         * Called once per session from buildEnv so ZDOTDIR / ENV can point to it.
         */
        fun writeHookScript(context: Context): File {
            val dir  = File(context.filesDir, "korex").also { it.mkdirs() }
            val file = File(dir, ".korex_hooks")
            // Always rewrite so updates take effect on next session
            file.writeText(
                """
                |# Korex shell hooks — auto-generated, do not edit
                |$ZSH_HOOKS
                |$BASH_HOOKS
                """.trimMargin()
            )
            return file
        }

        fun resolveShellPath(context: Context): String {
            val binDir = File(context.filesDir, "usr/bin")
            val zsh    = File(binDir, "zsh")
            val bash   = File(binDir, "bash")
            return when {
                zsh.exists()  -> zsh.absolutePath.also  { Log.i(TAG, "Shell: zsh") }
                bash.exists() -> bash.absolutePath.also { Log.i(TAG, "Shell: bash") }
                else          -> SHELL_FALLBACK.also    { Log.w(TAG, "Shell: system sh fallback") }
            }
        }

        fun prefixDir(context: Context): File = File(context.filesDir, "usr")
        fun homeDir(context: Context): File   = File(context.filesDir, "home")

        fun buildEnv(context: Context): Array<String> {
            val filesDir  = context.filesDir.absolutePath
            val prefix    = "$filesDir/usr"
            val nativeDir = context.applicationInfo.nativeLibraryDir
            val termuxExec = "$nativeDir/libtermux.so"
            val hookFile  = writeHookScript(context)

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
                // zsh: source hook file via ZDOTDIR trick
                "KOREX_HOOK_FILE=${hookFile.absolutePath}",
                // bash: ENV is sourced for interactive shells
                "ENV=${hookFile.absolutePath}",
            )
        }
    }
}