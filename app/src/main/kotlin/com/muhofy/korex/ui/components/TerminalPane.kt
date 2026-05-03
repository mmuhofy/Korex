package com.muhofy.korex.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import com.muhofy.korex.terminal.TerminalBridge
import com.muhofy.korex.terminal.TerminalViewCompose
import com.muhofy.korex.util.SWIPE_THRESHOLD_PX

// UNTESTED — verify before use
@Composable
fun TerminalPane(
    activeSessionId: String?,
    getBridge: (String) -> TerminalBridge?,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var dragTotal by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .pointerInput(activeSessionId) {
                detectHorizontalDragGestures(
                    onDragStart  = { dragTotal = 0f },
                    onDragEnd    = {
                        when {
                            dragTotal < -SWIPE_THRESHOLD_PX -> onSwipeLeft()
                            dragTotal > SWIPE_THRESHOLD_PX  -> onSwipeRight()
                        }
                        dragTotal = 0f
                    },
                    onDragCancel = { dragTotal = 0f },
                    onHorizontalDrag = { _, delta -> dragTotal += delta },
                )
            },
    ) {
        val bridge = activeSessionId?.let { getBridge(it) }

        if (bridge != null) {
            val viewClient = rememberTerminalViewClient(bridge)
            // Wire viewClient.terminalView after composition via AndroidView factory
            TerminalViewCompose(
                bridge     = bridge,
                viewClient = viewClient,
                modifier   = Modifier.fillMaxSize(),
            )
        } else {
            Text(
                text     = "No active session",
                color    = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}