package com.muhofy.korex.terminal

import android.graphics.Bitmap
import android.graphics.Color
import com.termux.terminal.TerminalSession
import com.termux.terminal.TerminalSessionClient

// UNTESTED — verify before use
/**
 * Minimal TerminalSessionClient implementation required by TerminalSession.
 * Callbacks are forwarded to the UI layer via lambdas.
 */
class KorexTerminalSessionClient(
    private val onSessionFinished: (TerminalSession) -> Unit = {},
    private val onTitleChanged: (TerminalSession) -> Unit = {},
    private val onBell: (TerminalSession) -> Unit = {},
    private val onColorsChanged: (TerminalSession) -> Unit = {},
) : TerminalSessionClient {

    override fun onTextChanged(changedSession: TerminalSession) {
        // UI refresh is handled by TerminalView internally — no action needed here
    }

    override fun onTitleChanged(changedSession: TerminalSession) {
        onTitleChanged.invoke(changedSession)
    }

    override fun onSessionFinished(finishedSession: TerminalSession) {
        onSessionFinished.invoke(finishedSession)
    }

    override fun onCopyTextToClipboard(session: TerminalSession, text: String) {
        // Clipboard copy — will be wired to Android ClipboardManager in a later phase
    }

    override fun onPasteTextFromClipboard(session: TerminalSession?) {
        // Clipboard paste — will be wired in a later phase
    }

    override fun onBell(session: TerminalSession) {
        onBell.invoke(session)
    }

    override fun onColorsChanged(session: TerminalSession) {
        onColorsChanged.invoke(session)
    }

    override fun onTerminalCursorStateChange(state: Boolean) {
        // Cursor blink state — handled by TerminalView internally
    }

    override fun getTerminalCursorStyle(): Int =
        TerminalEmulatorCursorStyle.CURSOR_STYLE_BLOCK // default block cursor

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

/** Cursor style constants from TerminalEmulator — defined here to avoid direct dependency. */
private object TerminalEmulatorCursorStyle {
    const val CURSOR_STYLE_BLOCK = 0
}