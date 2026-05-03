package com.muhofy.korex.terminal

import android.graphics.Typeface
import android.view.inputmethod.InputMethodManager
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.termux.view.TerminalRenderer
import com.termux.view.TerminalView
import com.termux.view.TerminalViewClient

// UNTESTED — verify before use
@Composable
fun TerminalViewCompose(
    bridge: TerminalBridge,
    viewClient: TerminalViewClient,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    val terminalView = remember(context) {
        TerminalView(context, null).apply {
            setTerminalViewClient(viewClient)
            mRenderer = TerminalRenderer(bridge.fontSize, Typeface.MONOSPACE)
            attachSession(bridge.session)
            isFocusable = true
            isFocusableInTouchMode = true
            requestFocus()
            bridge.sessionClient.terminalView = this
            // Wire viewClient so onScale can call applyFontScale
            (viewClient as? KorexTerminalViewClient)?.terminalView = this
        }
    }

    AndroidView(
        factory  = { terminalView },
        update   = { view ->
            view.requestFocus()
            val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        },
        modifier = modifier,
    )
}

/**
 * Rebuilds TerminalRenderer with updated font size and invalidates the view.
 */
fun TerminalView.applyFontScale(bridge: TerminalBridge, scaleFactor: Float) {
    if (bridge.scaleFontSize(scaleFactor)) {
        mRenderer = TerminalRenderer(bridge.fontSize, Typeface.MONOSPACE)
        invalidate()
    }
}