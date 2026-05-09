package com.termux.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import com.termux.terminal.TerminalBridge
import com.termux.terminal.TerminalViewCompose
import com.termux.util.SWIPE_THRESHOLD_PX

@Composable
fun TerminalPane(
    activeSessionId: String?,
    getBridge: (String) -> TerminalBridge?,
    fontSize: Int,                           // hot-applied, no session restart
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    onSwipeUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var hDrag by remember { mutableFloatStateOf(0f) }
    var vDrag by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .pointerInput(activeSessionId) {
                detectHorizontalDragGestures(
                    onDragStart      = { hDrag = 0f },
                    onDragEnd        = {
                        when {
                            hDrag < -SWIPE_THRESHOLD_PX -> onSwipeLeft()
                            hDrag > SWIPE_THRESHOLD_PX  -> onSwipeRight()
                        }
                        hDrag = 0f
                    },
                    onDragCancel     = { hDrag = 0f },
                    onHorizontalDrag = { _, d -> hDrag += d },
                )
            }
            .pointerInput(activeSessionId) {
                detectVerticalDragGestures(
                    onDragStart    = { vDrag = 0f },
                    onDragEnd      = {
                        if (vDrag < -SWIPE_THRESHOLD_PX) onSwipeUp()
                        vDrag = 0f
                    },
                    onDragCancel   = { vDrag = 0f },
                    onVerticalDrag = { _, d -> vDrag += d },
                )
            },
    ) {
        val bridge = activeSessionId?.let { getBridge(it) }

        if (bridge != null) {
            val viewClient = rememberTerminalViewClient(bridge)
            TerminalViewCompose(
                bridge     = bridge,
                viewClient = viewClient,
                fontSize   = fontSize,
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