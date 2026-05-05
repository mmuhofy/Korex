package com.korexx.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import com.korexx.session.SplitScreenState
import com.korexx.terminal.TerminalBridge
import com.korexx.terminal.TerminalViewCompose
import com.korexx.util.SWIPE_THRESHOLD_PX

private val DIVIDER_WIDTH = 4.dp

// UNTESTED — verify before use
@Composable
fun SplitTerminalPane(
    splitState: SplitScreenState?,
    getBridge: (String) -> TerminalBridge?,
    activeSessionId: String?,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    onSwipeUp: () -> Unit,
    onRatioChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    var totalWidth by remember { mutableFloatStateOf(0f) }
    var hDrag      by remember { mutableFloatStateOf(0f) }
    var vDrag      by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { totalWidth = it.width.toFloat() },
    ) {
        if (splitState != null && splitState.isSplit) {
            // Split mode — two panes side by side
            Row(modifier = Modifier.fillMaxSize()) {

                val primaryBridge = getBridge(splitState.primarySessionId)
                PaneContainer(
                    bridge   = primaryBridge,
                    modifier = Modifier
                        .weight(splitState.splitRatio)
                        .fillMaxHeight(),
                )

                // Draggable divider
                Box(
                    modifier = Modifier
                        .width(DIVIDER_WIDTH)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                        .pointerInput(Unit) {
                            detectDragGestures { _, dragAmount ->
                                if (totalWidth > 0f) {
                                    onRatioChange(dragAmount.x / totalWidth)
                                }
                            }
                        },
                )

                val secondaryBridge = getBridge(splitState.secondarySessionId!!)
                PaneContainer(
                    bridge   = secondaryBridge,
                    modifier = Modifier
                        .weight(1f - splitState.splitRatio)
                        .fillMaxHeight(),
                )
            }
        } else {
            // Single pane with swipe gestures
            Box(
                modifier = Modifier
                    .fillMaxSize()
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
                        modifier   = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}

@Composable
private fun PaneContainer(
    bridge: TerminalBridge?,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.background(MaterialTheme.colorScheme.background)) {
        if (bridge != null) {
            val viewClient = rememberTerminalViewClient(bridge)
            TerminalViewCompose(
                bridge     = bridge,
                viewClient = viewClient,
                modifier   = Modifier.fillMaxSize(),
            )
        }
    }
}