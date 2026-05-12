package com.termux.terminal

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.termux.terminal.TerminalSession
import com.termux.terminal.TerminalSessionClient

// Prompt patterns that indicate a command has finished.
// Matches common bash/zsh/sh prompts ending with $ # or %
private val PROMPT_REGEX = Regex(""".*[\$#%]\s*$""")

/**
 * TerminalSessionClient implementation.
 * onTextChanged must call invalidate on the TerminalView to trigger a redraw.
 * Also detects shell prompt lines to stop the CommandTimer.
 */
class KorexTerminalSessionClient(
    private val context: Context,
    val timer: CommandTimer = CommandTimer(),
    private val onSessionFinished: (TerminalSession) -> Unit = {},
    private val onTitleChanged: (TerminalSession) -> Unit = {},
    private val onBell: (TerminalSession) -> Unit = {},
    private val onColorsChanged: (TerminalSession) -> Unit = {},
) : TerminalSessionClient {

    var terminalView: com.termux.view.TerminalView? = null

    override fun onTextChanged(changedSession: TerminalSession) {
        terminalView?.invalidate()
        detectPrompt(changedSession)
    }

    /**
     * Reads the last non-empty line from the terminal buffer.
     * If it matches a prompt pattern, stops the command timer.
     */
    private fun detectPrompt(session: TerminalSession) {
        if (!timer.isRunning) return
        val emulator = session.emulator ?: return
        val screen   = emulator.screen
        val rows     = emulator.mRows

        // Scan from bottom up for the last non-empty line
        for (row in rows - 1 downTo 0) {
            val line = screen.getSelectedText(0, row, emulator.mColumns, row).trimEnd()
            if (line.isNotEmpty()) {
                if (PROMPT_REGEX.matches(line)) {
                    timer.onPromptDetected()
                }
                break
            }
        }
    }

    override fun onTitleChanged(changedSession: TerminalSession) {
        onTitleChanged.invoke(changedSession)
    }

    override fun onSessionFinished(finishedSession: TerminalSession) {
        timer.reset()
        onSessionFinished.invoke(finishedSession)
    }

    override fun onCopyTextToClipboard(session: TerminalSession, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Korex", text))
    }

    override fun onPasteTextFromClipboard(session: TerminalSession?) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val text = clipboard.primaryClip?.getItemAt(0)?.text?.toString() ?: return
        session?.getEmulator()?.paste(text)
    }

    override fun onBell(session: TerminalSession)           { onBell.invoke(session) }
    override fun onColorsChanged(session: TerminalSession)  { onColorsChanged.invoke(session) }
    override fun onTerminalCursorStateChange(state: Boolean) {}
    override fun setTerminalShellPid(session: TerminalSession, pid: Int) {}
    override fun getTerminalCursorStyle(): Int = 0

    override fun logError(tag: String?, message: String?)   { android.util.Log.e(tag ?: "KorexTerminal", message ?: "") }
    override fun logWarn(tag: String?, message: String?)    { android.util.Log.w(tag ?: "KorexTerminal", message ?: "") }
    override fun logInfo(tag: String?, message: String?)    { android.util.Log.i(tag ?: "KorexTerminal", message ?: "") }
    override fun logDebug(tag: String?, message: String?)   { android.util.Log.d(tag ?: "KorexTerminal", message ?: "") }
    override fun logVerbose(tag: String?, message: String?) { android.util.Log.v(tag ?: "KorexTerminal", message ?: "") }
    override fun logStackTraceWithMessage(tag: String?, message: String?, e: Exception?) { android.util.Log.e(tag ?: "KorexTerminal", message ?: "", e) }
    override fun logStackTrace(tag: String?, e: Exception?) { android.util.Log.e(tag ?: "KorexTerminal", "", e) }
}