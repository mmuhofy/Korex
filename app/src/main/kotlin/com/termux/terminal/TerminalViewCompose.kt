package com.termux.terminal

import android.content.Context
import android.graphics.Typeface
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.termux.util.SWIPE_THRESHOLD_PX
import com.termux.util.SWIPE_THRESHOLD_Y_PX
import com.termux.util.SWIPE_VELOCITY_THRESHOLD
import com.termux.view.TerminalRenderer
import com.termux.view.TerminalView
import com.termux.view.TerminalViewClient
import java.lang.ref.WeakReference
import kotlin.math.abs

/** Global weak reference to the active TerminalView — used by MainActivity to restore keyboard. */
var terminalViewRef: WeakReference<TerminalView> = WeakReference(null)

// UNTESTED — verify before use
@Composable
fun TerminalViewCompose(
    bridge: TerminalBridge,
    viewClient: TerminalViewClient,
    // All gesture callbacks are optional — split panes don't need session swipe
    onSwipeLeft: (() -> Unit)? = null,
    onSwipeRight: (() -> Unit)? = null,
    onSwipeUp: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    val terminalView = remember(context) {
        SwipeAwareTerminalView(
            context = context,
            onSwipeLeft = onSwipeLeft,
            onSwipeRight = onSwipeRight,
            onSwipeUp = onSwipeUp,
        ).apply {
            setTerminalViewClient(viewClient)
            mRenderer = TerminalRenderer(bridge.fontSize, Typeface.MONOSPACE)
            attachSession(bridge.session)
            isFocusable = true
            isFocusableInTouchMode = true
            requestFocus()
            bridge.sessionClient.terminalView = this
            (viewClient as? KorexTerminalViewClient)?.terminalView = this
            terminalViewRef = WeakReference(this)
        }
    }

    AndroidView(
        factory = { terminalView },
        update = { view ->
            terminalViewRef = WeakReference(view)
            view.requestFocus()
            val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        },
        modifier = modifier,
    )
}

fun TerminalView.applyFontScale(bridge: TerminalBridge, scaleFactor: Float) {
    if (bridge.scaleFontSize(scaleFactor)) {
        mRenderer = TerminalRenderer(bridge.fontSize, Typeface.MONOSPACE)
        invalidate()
    }
}

/**
 * TerminalView subclass that detects fast directional flings for session navigation
 * while letting all other touch events pass through to TerminalView's native handler.
 *
 * Strategy:
 * - Fast fling → session swipe (LEFT/RIGHT) or history panel (UP)
 * - Everything else → super.onTouchEvent() → TerminalView handles scroll, zoom, copy natively
 *
 * All callbacks are nullable — pass null for panes that don't need swipe navigation
 * (e.g. the secondary pane in split mode).
 */
class SwipeAwareTerminalView(
    context: Context,
    private val onSwipeLeft: (() -> Unit)?,
    private val onSwipeRight: (() -> Unit)?,
    private val onSwipeUp: (() -> Unit)?,
) : TerminalView(context, null) {

    // Only attach gesture detector if at least one swipe callback is provided
    private val gestureDetector: GestureDetector? =
        if (onSwipeLeft != null || onSwipeRight != null || onSwipeUp != null) {
            GestureDetector(
                context,
                object : GestureDetector.SimpleOnGestureListener() {
                    override fun onFling(
                        e1: MotionEvent?,
                        e2: MotionEvent,
                        velocityX: Float,
                        velocityY: Float,
                    ): Boolean {
                        val e1 = e1 ?: return false
                        val dX = e2.x - e1.x
                        val dY = e2.y - e1.y
                        val absVX = abs(velocityX)
                        val absVY = abs(velocityY)

                        // Swipe UP — vertical fling upward, vertical velocity dominates
                        if (onSwipeUp != null
                            && dY < -SWIPE_THRESHOLD_Y_PX
                            && absVY > SWIPE_VELOCITY_THRESHOLD
                            && absVY > absVX
                        ) {
                            onSwipeUp.invoke()
                            return true
                        }

                        // Swipe LEFT / RIGHT — horizontal fling, horizontal velocity dominates
                        if (abs(dX) > SWIPE_THRESHOLD_PX
                            && absVX > SWIPE_VELOCITY_THRESHOLD
                            && absVX > absVY
                        ) {
                            if (dX < 0) onSwipeLeft?.invoke()
                            else onSwipeRight?.invoke()
                            return true
                        }

                        return false
                    }
                }
            )
        } else null

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Feed event to gesture detector first.
        // If it consumes (fling matched), skip TerminalView so it doesn't
        // also react to the same touch sequence.
        if (gestureDetector?.onTouchEvent(event) == true) return true

        // All other events (scroll, tap, long-press, pinch-zoom) → TerminalView native handler
        return super.onTouchEvent(event)
    }
}