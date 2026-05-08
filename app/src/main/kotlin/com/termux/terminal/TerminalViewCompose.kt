package com.termux.terminal

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Typeface
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.termux.view.TerminalRenderer
import com.termux.view.TerminalView
import com.termux.view.TerminalViewClient
import java.lang.ref.WeakReference

/** Global weak reference to the active TerminalView — used by MainActivity to restore keyboard. */
var terminalViewRef: WeakReference<TerminalView> = WeakReference(null)

@Composable
fun TerminalViewCompose(
    bridge: TerminalBridge,
    viewClient: TerminalViewClient,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var isCopyMode by remember { mutableStateOf(false) }

    // Wire up copy mode callback
    DisposableEffect(viewClient) {
        (viewClient as? KorexTerminalViewClient)?.onCopyModeChanged = { active ->
            isCopyMode = active
        }
        onDispose {
            (viewClient as? KorexTerminalViewClient)?.onCopyModeChanged = null
        }
    }

    val terminalView = remember(context) {
        TerminalView(context, null).apply {
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

    Box(modifier = modifier) {
        AndroidView(
            factory = { terminalView },
            update  = { view ->
                terminalViewRef = WeakReference(view)
                view.requestFocus()
                val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
            },
            modifier = Modifier.fillMaxSize(),
        )

        // Copy mode toolbar — appears at top when user is selecting text
        AnimatedVisibility(
            visible = isCopyMode,
            enter   = fadeIn(),
            exit    = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                // Copy button
                CopyActionButton(
                    label = "Copy",
                    onClick = {
                        val text = terminalView.storedSelectedText
                        if (!text.isNullOrBlank()) {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Korex", text))
                            Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                        }
                        terminalView.stopTextSelectionMode()
                        isCopyMode = false
                    },
                )

                // Cancel button
                CopyActionButton(
                    label = "Cancel",
                    onClick = {
                        terminalView.stopTextSelectionMode()
                        isCopyMode = false
                    },
                )
            }
        }
    }
}

@Composable
private fun CopyActionButton(
    label: String,
    onClick: () -> Unit,
) {
    Text(
        text     = label,
        style    = MaterialTheme.typography.labelMedium,
        color    = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

/**
 * Rebuilds TerminalRenderer with updated font size and invalidates the view.
 */
fun TerminalView.applyFontScale(bridge: TerminalBridge, scaleFactor: Float) {
    if (bridge.scaleFontSize(scaleFactor)) {
        mRenderer = TerminalRenderer(bridge.fontSize, Typeface.MONOSPACE)
        invalidate()
    }
}