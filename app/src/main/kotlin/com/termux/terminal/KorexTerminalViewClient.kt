package com.termux.terminal

import android.content.Context
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import com.termux.view.TerminalView
import com.termux.view.TerminalViewClient

class KorexTerminalViewClient(
    private val bridge: TerminalBridge,
    private val context: Context,
) : TerminalViewClient {

    var terminalView: TerminalView? = null

    var onCopyModeChanged: ((Boolean) -> Unit)? = null

    /**
     * ExtraKeyBar sets these when CTRL / ALT toggle buttons are tapped.
     * readControlKey() / readAltKey() are called by TerminalView on every
     * key event, so returning true here causes the NEXT keyboard character
     * to be processed with the modifier applied (e.g. Ctrl+C → 0x03).
     *
     * These are also mirrored on TerminalBridge so that ExtraKeyBar can
     * access them without holding a direct reference to the ViewClient.
     * Both must be reset to false after consumption to avoid bleed-through.
     */
    var ctrlDown: Boolean
        get()  = bridge.ctrlDown
        set(v) { bridge.ctrlDown = v }

    var altDown: Boolean
        get()  = bridge.altDown
        set(v) { bridge.altDown = v }

    override fun readControlKey(): Boolean {
        val v = bridge.ctrlDown
        if (v) bridge.ctrlDown = false  // consume — one-shot modifier
        return v
    }

    override fun readAltKey(): Boolean {
        val v = bridge.altDown
        if (v) bridge.altDown = false   // consume — one-shot modifier
        return v
    }

    override fun onScale(scale: Float): Float {
        terminalView?.applyFontScale(bridge, scale)
        return scale
    }

    override fun onLongPress(event: MotionEvent?): Boolean = false

    override fun copyModeChanged(copyMode: Boolean) {
        onCopyModeChanged?.invoke(copyMode)
    }

    override fun onSingleTapUp(e: MotionEvent?) {
        val view = terminalView ?: return
        view.requestFocus()
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    override fun shouldBackButtonBeMappedToEscape(): Boolean = false
    override fun shouldEnforceCharBasedInput(): Boolean      = true
    override fun shouldUseCtrlSpaceWorkaround(): Boolean     = false
    override fun isTerminalViewSelected(): Boolean           = true

    override fun onKeyDown(keyCode: Int, e: KeyEvent?, session: TerminalSession?): Boolean = false
    override fun onKeyUp(keyCode: Int, e: KeyEvent?): Boolean = false

    override fun onCodePoint(codePoint: Int, ctrlDown: Boolean, session: TerminalSession?): Boolean = false

    override fun readShiftKey(): Boolean = false
    override fun readFnKey(): Boolean    = false
    override fun onEmulatorSet()         {}
    override fun getInputMode(): Int     = 0

    override fun logError(tag: String?, message: String?)   { android.util.Log.e(tag ?: "KorexTV", message ?: "") }
    override fun logWarn(tag: String?, message: String?)    { android.util.Log.w(tag ?: "KorexTV", message ?: "") }
    override fun logInfo(tag: String?, message: String?)    { android.util.Log.i(tag ?: "KorexTV", message ?: "") }
    override fun logDebug(tag: String?, message: String?)   { android.util.Log.d(tag ?: "KorexTV", message ?: "") }
    override fun logVerbose(tag: String?, message: String?) { android.util.Log.v(tag ?: "KorexTV", message ?: "") }
    override fun logStackTraceWithMessage(tag: String?, message: String?, e: Exception?) { android.util.Log.e(tag ?: "KorexTV", message ?: "", e) }
    override fun logStackTrace(tag: String?, e: Exception?) { android.util.Log.e(tag ?: "KorexTV", "", e) }
}