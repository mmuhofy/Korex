package com.termux.terminal

import android.content.Context
import android.graphics.Typeface
import android.view.inputmethod.InputMethodManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.termux.view.TerminalRenderer
import com.termux.view.TerminalView
import com.termux.view.TerminalViewClient
import java.lang.ref.WeakReference

/** Global weak reference to the active TerminalView — used by MainActivity to restore keyboard. */
var terminalViewRef: WeakReference<TerminalView> = WeakReference(null)

@Composable
fun TerminalViewCompose(
    bridge: TerminalBridge,
    viewClient: TerminalViewClient,
    /**
     * Font size in sp. Passed from SettingsViewModel so the terminal
     * hot-applies size changes without restarting the session.
     * Defaults to bridge.fontSize so callers that don't track settings
     * still work correctly.
     */
    fontSize: Int = bridge.fontSize,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    // Recreate the View only when the bridge (session) changes — NOT on fontSize.
    // Font size is applied live in the update block below.
    val terminalView = remember(bridge) {
        TerminalView(context, null).apply {
            setTerminalViewClient(viewClient)
            mRenderer = TerminalRenderer(bridge.fontSize, Typeface.MONOSPACE)
            attachSession(bridge.session)
            isFocusable = true
            isFocusableInTouchMode = true
            requestFocus()
            bridge.sessionClient.terminalView = this
            (viewClient as? KorexTerminalViewClient)?.terminalView = this
            terminalViewRef = WeakReference(this)
        }
    }

    AndroidView(
        factory = { terminalView },
        update  = { view ->
            // Hot-apply font size — runs whenever fontSize recompose key changes.
            // bridge.setFontSize() keeps the bridge state in sync so pinch-zoom
            // and settings slider don't fight each other.
            bridge.setFontSize(fontSize)
            view.mRenderer = TerminalRenderer(fontSize, Typeface.MONOSPACE)
            view.invalidate()

            terminalViewRef = WeakReference(view)
            view.requestFocus()
            val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        },
        modifier = modifier,
    )
}

fun TerminalView.applyFontScale(bridge: TerminalBridge, scaleFactor: Float) {
    if (bridge.scaleFontSize(scaleFactor)) {
        mRenderer = TerminalRenderer(bridge.fontSize, Typeface.MONOSPACE)
        invalidate()
    }
}