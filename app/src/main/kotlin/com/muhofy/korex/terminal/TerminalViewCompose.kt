package com.muhofy.korex.terminal

import android.graphics.Typeface
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
 * Order matters:
 * 1. setTerminalViewClient — must be set before attachSession
 * 2. setTypeface — creates TerminalRenderer, must happen before onSizeChanged fires
 * 3. attachSession — starts the pty process
 */
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
            // TerminalRenderer is created here — must happen before layout/onSizeChanged
            setTypeface(Typeface.MONOSPACE)
        }
    }

    AndroidView(
        factory = { terminalView },
        update  = { view ->
            // attachSession after view is initialized and has a valid typeface/renderer
            if (view.currentSession == null) {
                view.attachSession(bridge.session)
            }
        },
        modifier = modifier,
    )
}