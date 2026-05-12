package com.termux.terminal

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.termux.terminal.TerminalSession
import com.termux.terminal.TerminalSessionClient

// OSC title signals injected by shell hooks
private const val SIGNAL_START  = "KOREX_START"
private const val SIGNAL_END    = "KOREX_END:"

/**
 * TerminalSessionClient implementation.
 *
 * Shell duration tracking via OSC title signals:
 *   - preexec sets title to "KOREX_START"
 *   - precmd  sets title to "KOREX_END:<seconds>"
 *
 * onTitleChanged fires when the terminal receives an OSC title escape sequence.
 * We parse these signals here and drive CommandTimer accordingly.
 */
class KorexTerminalSessionClient(
    private val context: Context,
    val timer: CommandTimer = CommandTimer(),
    private val onSessionFinished: (TerminalSession) -> Unit = {},
    private val onBell: (TerminalSession) -> Unit = {},
    private val onColorsChanged: (TerminalSession) -> Unit = {},
) : TerminalSessionClient {

    var terminalView: com.termux.view.TerminalView? = null

    override fun onTextChanged(changedSession: TerminalSession) {
        terminalView?.invalidate()
    }

    override fun onTitleChanged(changedSession: TerminalSession) {
        val title = changedSession.title ?: return
        when {
            title == SIGNAL_START -> timer.onCommandStarted()
            title.startsWith(SIGNAL_END) -> {
                val secs = title.removePrefix(SIGNAL_END).toIntOrNull() ?: return
                timer.onCommandFinished(secs)
            }
        }
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

    override fun onBell(session: TerminalSession)            { onBell.invoke(session) }
    override fun onColorsChanged(session: TerminalSession)   { onColorsChanged.invoke(session) }
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