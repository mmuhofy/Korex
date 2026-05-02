package com.muhofy.korex.terminal

import android.graphics.Typeface
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.termux.view.TerminalRenderer
import com.termux.view.TerminalView
import com.termux.view.TerminalViewClient

private const val TERMINAL_TEXT_SIZE = 14

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
            mRenderer = TerminalRenderer(TERMINAL_TEXT_SIZE, Typeface.MONOSPACE)
            attachSession(bridge.session)
            // Request focus so keyboard opens on tap
            isFocusable = true
            isFocusableInTouchMode = true
            requestFocus()
        }
    }

    AndroidView(
        factory = { terminalView },
        update  = { view ->
            view.requestFocus()
        },
        modifier = modifier,
    )
}