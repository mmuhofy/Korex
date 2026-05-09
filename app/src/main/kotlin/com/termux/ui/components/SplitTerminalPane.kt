package com.termux.ui.components

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
import com.termux.session.SplitScreenState
import com.termux.terminal.TerminalBridge
import com.termux.terminal.TerminalViewCompose
import com.termux.util.PINCH_SPLIT_THRESHOLD
import com.termux.util.SWIPE_THRESHOLD_PX

private val DIVIDER_WIDTH = 4.dp

@Composable
fun SplitTerminalPane(
    splitState: SplitScreenState?,
    getBridge: (String) -> TerminalBridge?,
    activeSessionId: String?,
    fontSize: Int,                           // hot-applied to all panes
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    onSwipeUp: () -> Unit,
    onRatioChange: (Float) -> Unit,
    onEnterSplit: () -> Unit,
    onExitSplit: () -> Unit,
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
            // ── Split mode — two panes side by side ──────────────────────
            Row(modifier = Modifier.fillMaxSize()) {

                PaneContainer(
                    bridge   = getBridge(splitState.primarySessionId),
                    fontSize = fontSize,
                    modifier = Modifier
                        .weight(splitState.splitRatio)
                        .fillMaxHeight()
                        .pointerInput(Unit) {
                            detectTransformGesturesForSplit(onPinchIn = onExitSplit)
                        },
                )

                // Draggable divider
                Box(
                    modifier = Modifier
                        .width(DIVIDER_WIDTH)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                        .pointerInput(Unit) {
                            detectDragGestures { _, dragAmount ->
                                if (totalWidth > 0f) onRatioChange(dragAmount.x / totalWidth)
                            }
                        },
                )

                PaneContainer(
                    bridge   = getBridge(splitState.secondarySessionId!!),
                    fontSize = fontSize,
                    modifier = Modifier
                        .weight(1f - splitState.splitRatio)
                        .fillMaxHeight()
                        .pointerInput(Unit) {
                            detectTransformGesturesForSplit(onPinchIn = onExitSplit)
                        },
                )
            }
        } else {
            // ── Single pane — swipe + pinch OUT to enter split ───────────
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
                    }
                    .pointerInput(Unit) {
                        detectTransformGesturesForSplit(onPinchOut = onEnterSplit)
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
                }
            }
        }
    }
}

private suspend fun androidx.compose.ui.input.pointer.PointerInputScope.detectTransformGesturesForSplit(
    onPinchOut: (() -> Unit)? = null,
    onPinchIn:  (() -> Unit)? = null,
) {
    var initialSpan = 0f
    var fired       = false

    awaitPointerEventScope {
        while (true) {
            val event    = awaitPointerEvent()
            val pointers = event.changes.filter { it.pressed }

            if (pointers.size < 2) {
                initialSpan = 0f
                fired       = false
                continue
            }

            val p1   = pointers[0].position
            val p2   = pointers[1].position
            val span = kotlin.math.sqrt(
                (p2.x - p1.x) * (p2.x - p1.x) + (p2.y - p1.y) * (p2.y - p1.y)
            )

            if (initialSpan == 0f) {
                initialSpan = span
                fired       = false
                continue
            }

            if (!fired) {
                val delta = span - initialSpan
                when {
                    delta >  PINCH_SPLIT_THRESHOLD -> { onPinchOut?.invoke(); fired = true }
                    delta < -PINCH_SPLIT_THRESHOLD -> { onPinchIn?.invoke();  fired = true }
                }
            }
        }
    }
}

@Composable
private fun PaneContainer(
    bridge: TerminalBridge?,
    fontSize: Int,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.background(MaterialTheme.colorScheme.background)) {
        if (bridge != null) {
            val viewClient = rememberTerminalViewClient(bridge)
            TerminalViewCompose(
                bridge     = bridge,
                viewClient = viewClient,
                fontSize   = fontSize,
                modifier   = Modifier.fillMaxSize(),
            )
        }
    }
}