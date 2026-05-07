package com.termux.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.termux.terminal.KorexTerminalViewClient
import com.termux.terminal.TerminalBridge

@Composable
fun rememberTerminalViewClient(bridge: TerminalBridge): KorexTerminalViewClient {
    return remember(bridge) { KorexTerminalViewClient(bridge) }
}