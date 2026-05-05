package com.korexx.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.korexx.terminal.KorexTerminalViewClient
import com.korexx.terminal.TerminalBridge

@Composable
fun rememberTerminalViewClient(bridge: TerminalBridge): KorexTerminalViewClient {
    return remember(bridge) { KorexTerminalViewClient(bridge) }
}