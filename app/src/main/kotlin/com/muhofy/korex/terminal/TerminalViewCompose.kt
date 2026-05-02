package com.muhofy.korex.terminal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.termux.view.TerminalView
import com.termux.view.TerminalViewClient

// UNTESTED — verify before use
/**
 * Compose wrapper for Termux TerminalView.
 *
 * Session is attached AFTER the view has been laid out (inside update lambda),
 * preventing the NullPointerException on TerminalRenderer.mFontWidth.
 */
@Composable
fun TerminalViewCompose(
    bridge: TerminalBridge,
    viewClient: TerminalViewClient,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    val terminalView = remember(context) {
        TerminalView(context, null)
    }

    AndroidView(
        factory = { terminalView },
        update  = { view ->
            // Set client and attach session here — view is guaranteed to have a valid size
            view.setTerminalViewClient(viewClient)
            view.attachSession(bridge.session)
        },
        modifier = modifier,
    )
}