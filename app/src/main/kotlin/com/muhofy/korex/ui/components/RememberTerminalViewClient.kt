package com.muhofy.korex.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.muhofy.korex.terminal.KorexTerminalViewClient
import com.muhofy.korex.terminal.TerminalBridge

@Composable
fun rememberTerminalViewClient(bridge: TerminalBridge): KorexTerminalViewClient {
    return remember(bridge) { KorexTerminalViewClient(bridge) }
}