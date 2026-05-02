package com.muhofy.korex.terminal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.termux.view.TerminalView
import com.termux.view.TerminalViewClient

// UNTESTED — verify before use
/**
 * Compose wrapper for Termux TerminalView.
 * Embeds the native Android View into the Compose hierarchy via AndroidView.
 *
 * @param bridge          The TerminalBridge owning the pty session.
 * @param viewClient      TerminalViewClient for key/touch event callbacks.
 * @param modifier        Compose modifier (fill available space by default).
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
            mClient = viewClient
            attachSession(bridge.session)
        }
    }

    DisposableEffect(bridge) {
        onDispose {
            // Nothing to clean up on the view side — bridge.destroy() is called by SessionManager
        }
    }

    AndroidView(
        factory = { terminalView },
        modifier = modifier,
    )
}