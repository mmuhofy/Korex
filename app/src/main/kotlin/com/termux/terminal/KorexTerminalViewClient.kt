package com.termux.terminal

import android.view.KeyEvent
import android.view.MotionEvent
import com.termux.view.TerminalView
import com.termux.view.TerminalViewClient

class KorexTerminalViewClient(
    private val bridge: TerminalBridge,
) : TerminalViewClient {

    var terminalView: TerminalView? = null

    /**
     * Called when copy mode starts (user long pressed and is selecting text)
     * or ends (user lifted finger). We notify the Compose layer via callback.
     */
    var onCopyModeChanged: ((Boolean) -> Unit)? = null

    override fun onScale(scale: Float): Float {
        terminalView?.applyFontScale(bridge, scale)
        return scale
    }

    override fun onLongPress(event: MotionEvent?): Boolean = false

    /**
     * TerminalView calls this when copy mode starts/ends.
     * true  = user is selecting text
     * false = selection ended
     */
    override fun copyModeChanged(copyMode: Boolean) {
        onCopyModeChanged?.invoke(copyMode)
    }

    override fun onSingleTapUp(e: MotionEvent?) {}
    override fun shouldBackButtonBeMappedToEscape(): Boolean = false
    override fun shouldEnforceCharBasedInput(): Boolean = true
    override fun shouldUseCtrlSpaceWorkaround(): Boolean = false
    override fun isTerminalViewSelected(): Boolean = true
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