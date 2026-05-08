package com.termux.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.termux.terminal.KorexTerminalViewClient
import com.termux.terminal.TerminalBridge

@Composable
fun rememberTerminalViewClient(bridge: TerminalBridge): KorexTerminalViewClient {
    val context = LocalContext.current
    return remember(bridge) { KorexTerminalViewClient(bridge, context) }
}