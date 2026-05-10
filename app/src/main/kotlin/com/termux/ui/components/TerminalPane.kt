package com.termux.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.termux.terminal.TerminalBridge
import com.termux.terminal.TerminalViewCompose

// UNTESTED — verify before use
@Composable
fun TerminalPane(
    activeSessionId: String?,
    getBridge: (String) -> TerminalBridge?,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    onSwipeUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        val bridge = activeSessionId?.let { getBridge(it) }

        if (bridge != null) {
            val viewClient = rememberTerminalViewClient(bridge)
            TerminalViewCompose(
                bridge = bridge,
                viewClient = viewClient,
                onSwipeLeft = onSwipeLeft,
                onSwipeRight = onSwipeRight,
                onSwipeUp = onSwipeUp,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Text(
                text = "No active session",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}