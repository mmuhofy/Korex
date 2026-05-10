package com.termux.terminal

import android.content.Context
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import com.termux.util.PINCH_IN_SCALE_THRESHOLD
import com.termux.util.PINCH_OUT_SCALE_THRESHOLD
import com.termux.view.TerminalView
import com.termux.view.TerminalViewClient

// UNTESTED — verify before use
class KorexTerminalViewClient(
    private val bridge: TerminalBridge,
    private val context: Context,
    // Pinch IN: fingers coming together → exit split (null = disabled for this pane)
    private val onPinchIn: (() -> Unit)? = null,
    // Pinch OUT: fingers spreading → enter split (null = disabled for this pane)
    private val onPinchOut: (() -> Unit)? = null,
) : TerminalViewClient {

    var terminalView: TerminalView? = null

    var onCopyModeChanged: ((Boolean) -> Unit)? = null

    // Tracks cumulative scale across gesture frames so we can detect
    // a meaningful pinch without blocking font size changes.
    private var cumulativeScale = 1.0f
    private var pinchFired = false

    override fun onScale(scale: Float): Float {
        // Always apply font size scaling — this is TerminalView's primary use of onScale
        terminalView?.applyFontScale(bridge, scale)

        // Track cumulative scale for split gesture detection
        if (onPinchIn != null || onPinchOut != null) {
            cumulativeScale *= scale

            if (!pinchFired) {
                when {
                    cumulativeScale > PINCH_OUT_SCALE_THRESHOLD -> {
                        onPinchOut?.invoke()
                        pinchFired = true
                    }
                    cumulativeScale < PINCH_IN_SCALE_THRESHOLD -> {
                        onPinchIn?.invoke()
                        pinchFired = true
                    }
                }
            }
        }

        return scale
    }

    override fun onLongPress(event: MotionEvent?): Boolean {
        // Reset pinch tracking when touch ends (long press counts as a new gesture cycle)
        cumulativeScale = 1.0f
        pinchFired = false
        return false
    }

    override fun copyModeChanged(copyMode: Boolean) {
        // Reset pinch state when entering/exiting copy mode
        if (!copyMode) {
            cumulativeScale = 1.0f
            pinchFired = false
        }
        onCopyModeChanged?.invoke(copyMode)
    }

    override fun onSingleTapUp(e: MotionEvent?) {
        // Reset pinch tracking on tap
        cumulativeScale = 1.0f
        pinchFired = false

        val view = terminalView ?: return
        view.requestFocus()
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

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