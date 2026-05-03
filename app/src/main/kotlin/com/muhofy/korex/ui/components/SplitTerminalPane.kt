package com.muhofy.korex.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import com.muhofy.korex.session.SplitScreenState
import com.muhofy.korex.terminal.TerminalBridge
import com.muhofy.korex.terminal.TerminalViewCompose
import com.muhofy.korex.util.SWIPE_THRESHOLD_PX

private val DIVIDER_WIDTH = 4.dp
private const val PINCH_OUT_THRESHOLD = 1.15f
private const val PINCH_IN_THRESHOLD  = 0.88f

// UNTESTED — verify before use
@Composable
fun SplitTerminalPane(
    splitState: SplitScreenState?,
    getBridge: (String) -> TerminalBridge?,
    activeSessionId: String?,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    onRatioChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    var totalWidth by remember { mutableFloatStateOf(0f) }
    var dragTotal  by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { totalWidth = it.width.toFloat() }
    ) {
        if (splitState != null && splitState.isSplit) {
            // Split mode — two panes side by side
            Row(modifier = Modifier.fillMaxSize()) {

                // Primary pane
                val primaryBridge = getBridge(splitState.primarySessionId)
                PaneContainer(
                    bridge     = primaryBridge,
                    isActive   = activeSessionId == splitState.primarySessionId,
                    modifier   = Modifier
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
                        }
                )

                // Secondary pane
                val secondaryBridge = getBridge(splitState.secondarySessionId!!)
                PaneContainer(
                    bridge   = secondaryBridge,
                    isActive = activeSessionId == splitState.secondarySessionId,
                    modifier = Modifier
                        .weight(1f - splitState.splitRatio)
                        .fillMaxHeight(),
                )
            }
        } else {
            // Single pane mode with swipe gesture
            Box(
                modifier = Modifier
                    .fillMaxSize()
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
                    }
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
    isActive: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.background)
    ) {
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