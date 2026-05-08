package com.termux.terminal

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.termux.terminal.TerminalSession
import com.termux.terminal.TerminalSessionClient

/**
 * TerminalSessionClient implementation.
 * onTextChanged must call invalidate on the TerminalView to trigger a redraw.
 */
class KorexTerminalSessionClient(
    private val context: Context,
    private val onSessionFinished: (TerminalSession) -> Unit = {},
    private val onTitleChanged: (TerminalSession) -> Unit = {},
    private val onBell: (TerminalSession) -> Unit = {},
    private val onColorsChanged: (TerminalSession) -> Unit = {},
) : TerminalSessionClient {

    var terminalView: com.termux.view.TerminalView? = null

    override fun onTextChanged(changedSession: TerminalSession) {
        terminalView?.invalidate()
    }

    override fun onTitleChanged(changedSession: TerminalSession) {
        onTitleChanged.invoke(changedSession)
    }

    override fun onSessionFinished(finishedSession: TerminalSession) {
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

    override fun onBell(session: TerminalSession) { onBell.invoke(session) }
    override fun onColorsChanged(session: TerminalSession) { onColorsChanged.invoke(session) }
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