package com.termux.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
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
    onEnterSplit: () -> Unit,
    onExitSplit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var totalWidth by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { totalWidth = it.width.toFloat() },
    ) {
        if (splitState != null && splitState.isSplit) {
            // ── Split mode — two panes side by side ──────────────────────
            Row(modifier = Modifier.fillMaxSize()) {

                // Primary pane — has session swipe and swipe-up (history)
                // Pinch IN on primary → exit split (handled inside SwipeAwareTerminalView
                // via onScale callback — see note below)
                PaneContainer(
                    bridge = getBridge(splitState.primarySessionId),
                    onSwipeLeft = onSwipeLeft,
                    onSwipeRight = onSwipeRight,
                    onSwipeUp = onSwipeUp,
                    onPinchIn = onExitSplit,
                    modifier = Modifier
                        .weight(splitState.splitRatio)
                        .fillMaxHeight(),
                )

                // Draggable divider — Compose gesture here is fine, it's not a terminal surface
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

                // Secondary pane — no session swipe, pinch IN → exit split
                PaneContainer(
                    bridge = getBridge(splitState.secondarySessionId!!),
                    onSwipeLeft = null,
                    onSwipeRight = null,
                    onSwipeUp = null,
                    onPinchIn = onExitSplit,
                    modifier = Modifier
                        .weight(1f - splitState.splitRatio)
                        .fillMaxHeight(),
                )
            }
        } else {
            // ── Single pane ──────────────────────────────────────────────
            // All gestures handled inside SwipeAwareTerminalView natively.
            // Pinch OUT to enter split is handled via KorexTerminalViewClient.onScale.
            // (See note at bottom of file.)
            PaneContainer(
                bridge = activeSessionId?.let { getBridge(it) },
                onSwipeLeft = onSwipeLeft,
                onSwipeRight = onSwipeRight,
                onSwipeUp = onSwipeUp,
                onPinchIn = null,
                onPinchOut = onEnterSplit,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

/**
 * Single terminal pane wrapper.
 *
 * Swipe callbacks are passed through to [TerminalViewCompose] → [SwipeAwareTerminalView].
 * Pinch callbacks are wired via [KorexTerminalViewClient.onScale]:
 *   - scale > threshold → [onPinchOut] (enter split)
 *   - scale < threshold → [onPinchIn]  (exit split)
 *
 * All callbacks are nullable — pass null to disable that gesture for this pane.
 */
@Composable
private fun PaneContainer(
    bridge: TerminalBridge?,
    onSwipeLeft: (() -> Unit)?,
    onSwipeRight: (() -> Unit)?,
    onSwipeUp: (() -> Unit)?,
    onPinchIn: (() -> Unit)? = null,
    onPinchOut: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.background(MaterialTheme.colorScheme.background)) {
        if (bridge != null) {
            val viewClient = rememberTerminalViewClient(
                bridge = bridge,
                onPinchIn = onPinchIn,
                onPinchOut = onPinchOut,
            )
            TerminalViewCompose(
                bridge = bridge,
                viewClient = viewClient,
                onSwipeLeft = onSwipeLeft,
                onSwipeRight = onSwipeRight,
                onSwipeUp = onSwipeUp,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

/*
 * NOTE — Pinch / split gesture strategy:
 *
 * TerminalView's native pinch handler calls TerminalViewClient.onScale(scaleFactor).
 * KorexTerminalViewClient.onScale already handles font size scaling.
 * We extend it to also fire onPinchOut / onPinchIn callbacks when the cumulative
 * scale crosses a threshold (PINCH_SPLIT_THRESHOLD from Constants).
 *
 * This means:
 *   - We don't block TerminalView's pinch handling
 *   - Split/exit-split is triggered by the same pinch gesture
 *   - No Compose gesture layer needed on top of TerminalView
 *
 * KorexTerminalViewClient needs onPinchIn / onPinchOut callbacks added — see
 * RememberTerminalViewClient.kt and KorexTerminalViewClient.kt updates.
 */