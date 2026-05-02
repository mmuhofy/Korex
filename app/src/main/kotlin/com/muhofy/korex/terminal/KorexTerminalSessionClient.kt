package com.muhofy.korex.terminal

import com.termux.terminal.TerminalSession
import com.termux.terminal.TerminalSessionClient

// UNTESTED — verify before use
/**
 * TerminalSessionClient implementation.
 * onTextChanged must call invalidate on the TerminalView to trigger a redraw.
 * The view reference is set after construction via [setView].
 */
class KorexTerminalSessionClient(
    private val onSessionFinished: (TerminalSession) -> Unit = {},
    private val onTitleChanged: (TerminalSession) -> Unit = {},
    private val onBell: (TerminalSession) -> Unit = {},
    private val onColorsChanged: (TerminalSession) -> Unit = {},
) : TerminalSessionClient {

    // Set after TerminalView is created — used to trigger redraws on text change
    var terminalView: com.termux.view.TerminalView? = null

    override fun onTextChanged(changedSession: TerminalSession) {
        // Must invalidate the view so new output is rendered
        terminalView?.invalidate()
    }

    override fun onTitleChanged(changedSession: TerminalSession) {
        onTitleChanged.invoke(changedSession)
    }

    override fun onSessionFinished(finishedSession: TerminalSession) {
        onSessionFinished.invoke(finishedSession)
    }

    override fun onCopyTextToClipboard(session: TerminalSession, text: String) {}

    override fun onPasteTextFromClipboard(session: TerminalSession?) {}

    override fun onBell(session: TerminalSession) {
        onBell.invoke(session)
    }

    override fun onColorsChanged(session: TerminalSession) {
        onColorsChanged.invoke(session)
    }

    override fun onTerminalCursorStateChange(state: Boolean) {}

    override fun getTerminalCursorStyle(): Int = 0 // block cursor

    override fun logError(tag: String?, message: String?) {
        android.util.Log.e(tag ?: "KorexTerminal", message ?: "")
    }

    override fun logWarn(tag: String?, message: String?) {
        android.util.Log.w(tag ?: "KorexTerminal", message ?: "")
    }

    override fun logInfo(tag: String?, message: String?) {
        android.util.Log.i(tag ?: "KorexTerminal", message ?: "")
    }

    override fun logDebug(tag: String?, message: String?) {
        android.util.Log.d(tag ?: "KorexTerminal", message ?: "")
    }

    override fun logVerbose(tag: String?, message: String?) {
        android.util.Log.v(tag ?: "KorexTerminal", message ?: "")
    }

    override fun logStackTraceWithMessage(tag: String?, message: String?, e: Exception?) {
        android.util.Log.e(tag ?: "KorexTerminal", message ?: "", e)
    }

    override fun logStackTrace(tag: String?, e: Exception?) {
        android.util.Log.e(tag ?: "KorexTerminal", "", e)
    }
}