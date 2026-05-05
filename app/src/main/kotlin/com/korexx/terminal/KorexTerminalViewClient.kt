package com.korexx.terminal

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.KeyEvent
import android.view.MotionEvent
import com.termux.terminal.TerminalSession
import com.termux.view.TerminalView
import com.termux.view.TerminalViewClient

class KorexTerminalViewClient(
    private val bridge: TerminalBridge,
    private val context: Context,
) : TerminalViewClient {

    var terminalView: TerminalView? = null

    // Pinch zoom — scales font size
    override fun onScale(scale: Float): Float {
        terminalView?.applyFontScale(bridge, scale)
        return scale
    }

    // Long press → copy selected text to clipboard
    override fun onLongPress(event: MotionEvent?): Boolean {
        val view = terminalView ?: return false
        val selectedText = view.mEmulator?.selectedText ?: return false
        if (selectedText.isBlank()) return false

        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Korex", selectedText))
        view.stopTextSelectionMode()
        return true
    }

    override fun onSingleTapUp(e: MotionEvent?) {}
    override fun shouldBackButtonBeMappedToEscape(): Boolean = false
    override fun shouldEnforceCharBasedInput(): Boolean = true
    override fun shouldUseCtrlSpaceWorkaround(): Boolean = false
    override fun isTerminalViewSelected(): Boolean = true
    override fun copyModeChanged(copyMode: Boolean) {}
    override fun onKeyDown(keyCode: Int, e: KeyEvent?, session: TerminalSession?): Boolean = false
    override fun onKeyUp(keyCode: Int, e: KeyEvent?): Boolean = false
    override fun readControlKey(): Boolean = false
    override fun readAltKey(): Boolean = false
    override fun readShiftKey(): Boolean = false
    override fun readFnKey(): Boolean = false
    override fun onCodePoint(codePoint: Int, ctrlDown: Boolean, session: TerminalSession?): Boolean = false
    override fun onEmulatorSet() {}
    override fun getInputMode(): Int = 0
    override fun logError(tag: String?, message: String?)   { android.util.Log.e(tag ?: "KorexTV", message ?: "") }
    override fun logWarn(tag: String?, message: String?)    { android.util.Log.w(tag ?: "KorexTV", message ?: "") }
    override fun logInfo(tag: String?, message: String?)    { android.util.Log.i(tag ?: "KorexTV", message ?: "") }
    override fun logDebug(tag: String?, message: String?)   { android.util.Log.d(tag ?: "KorexTV", message ?: "") }
    override fun logVerbose(tag: String?, message: String?) { android.util.Log.v(tag ?: "KorexTV", message ?: "") }
    override fun logStackTraceWithMessage(tag: String?, message: String?, e: Exception?) { android.util.Log.e(tag ?: "KorexTV", message ?: "", e) }
    override fun logStackTrace(tag: String?, e: Exception?) { android.util.Log.e(tag ?: "KorexTV", "", e) }
}