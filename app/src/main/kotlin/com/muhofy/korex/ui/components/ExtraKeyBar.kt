package com.muhofy.korex.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muhofy.korex.terminal.TerminalBridge
import androidx.compose.foundation.clickable

// Key codes sent to terminal pty
private const val CODE_ESC      = "\u001b"
private const val CODE_TAB      = "\t"
private const val CODE_UP       = "\u001b[A"
private const val CODE_DOWN     = "\u001b[B"
private const val CODE_LEFT     = "\u001b[D"
private const val CODE_RIGHT    = "\u001b[C"
private const val CODE_HOME     = "\u001b[H"
private const val CODE_END      = "\u001b[F"
private const val CODE_PGUP     = "\u001b[5~"
private const val CODE_PGDN     = "\u001b[6~"

private data class ExtraKey(
    val label: String,
    val code: String,
    val isToggle: Boolean = false,
)

private val KEYS = listOf(
    ExtraKey("ESC",  CODE_ESC),
    ExtraKey("TAB",  CODE_TAB),
    ExtraKey("CTRL", "",       isToggle = true),
    ExtraKey("ALT",  CODE_ESC, isToggle = true),
    ExtraKey("↑",    CODE_UP),
    ExtraKey("↓",    CODE_DOWN),
    ExtraKey("←",    CODE_LEFT),
    ExtraKey("→",    CODE_RIGHT),
    ExtraKey("HOME", CODE_HOME),
    ExtraKey("END",  CODE_END),
    ExtraKey("PGUP", CODE_PGUP),
    ExtraKey("PGDN", CODE_PGDN),
)

@Composable
fun ExtraKeyBar(
    bridge: TerminalBridge?,
    modifier: Modifier = Modifier,
) {
    var ctrlActive by remember { mutableStateOf(false) }
    var altActive  by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 6.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        KEYS.forEach { key ->
            val isActive = when (key.label) {
                "CTRL" -> ctrlActive
                "ALT"  -> altActive
                else   -> false
            }

            KeyButton(
                label    = key.label,
                isActive = isActive,
                onClick  = {
                    when {
                        key.label == "CTRL" -> ctrlActive = !ctrlActive
                        key.label == "ALT"  -> altActive = !altActive
                        else -> {
                            val input = buildInput(key.code, ctrlActive, altActive)
                            bridge?.write(input)
                            // Reset toggles after use
                            ctrlActive = false
                            altActive  = false
                        }
                    }
                },
            )
        }
    }
}

/**
 * Builds the correct terminal input sequence based on active modifier keys.
 *
 * CTRL: converts letter to control character (e.g. CTRL+C = 0x03)
 * ALT:  prepends ESC to the sequence
 */
private fun buildInput(code: String, ctrl: Boolean, alt: Boolean): String {
    var result = code
    if (ctrl && result.length == 1) {
        val c = result[0].code
        result = (c and 0x1f).toChar().toString()
    }
    if (alt) result = "$CODE_ESC$result"
    return result
}

@Composable
private fun KeyButton(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
) {
    val bg = if (isActive)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.surfaceVariant

    val fg = if (isActive)
        MaterialTheme.colorScheme.onPrimary
    else
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)

    Text(
        text     = label,
        color    = fg,
        fontSize = 12.sp,
        style    = MaterialTheme.typography.labelSmall,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp),
    )
}