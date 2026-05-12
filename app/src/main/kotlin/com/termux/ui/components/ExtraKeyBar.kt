package com.termux.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.termux.terminal.TerminalBridge

private const val ESC   = "\u001B"
private const val TAB   = "\u0009"
private const val ENTER = "\r"
private const val UP    = "\u001B[A"
private const val DOWN  = "\u001B[B"
private const val LEFT  = "\u001B[D"
private const val RIGHT = "\u001B[C"
private const val PGUP  = "\u001B[5~"
private const val PGDN  = "\u001B[6~"
private const val HOME  = "\u001B[1~"
private const val END   = "\u001B[4~"

private sealed class Key {
    data class Char(val label: String, val code: String) : Key()
    data class Special(val label: String) : Key()
}

private val ROW1 = listOf(
    Key.Char("ESC",  ESC),
    Key.Char("/",    "/"),
    Key.Char("—",    "-"),
    Key.Char("HOME", HOME),
    Key.Char("↑",    UP),
    Key.Char("END",  END),
    Key.Char("PGUP", PGUP),
)

private val ROW2 = listOf(
    Key.Char("TAB",  TAB),
    Key.Special("CTRL"),
    Key.Special("ALT"),
    Key.Char("←",    LEFT),
    Key.Char("↓",    DOWN),
    Key.Char("→",    RIGHT),
    Key.Char("PGDN", PGDN),
)

@Composable
fun ExtraKeyBar(
    bridge: TerminalBridge?,
    modifier: Modifier = Modifier,
) {
    var ctrlActive by remember { mutableStateOf(false) }
    var altActive  by remember { mutableStateOf(false) }

    // Observe timer state from the active bridge's session client
    val timer = bridge?.sessionClient?.timer
    val elapsed by (timer?.elapsedSeconds ?: kotlinx.coroutines.flow.MutableStateFlow(null))
        .collectAsStateWithLifecycle()
    val lastDuration by (timer?.lastDuration ?: kotlinx.coroutines.flow.MutableStateFlow(null))
        .collectAsStateWithLifecycle()

    fun send(code: String) {
        var out = code
        if (ctrlActive) {
            if (out.length == 1 && out[0].code in 0x40..0x7E) {
                out = (out[0].code and 0x1f).toChar().toString()
            } else if (out.startsWith(ESC)) {
                out = "\u001B[1;5${out.last()}"
            }
            ctrlActive = false
        }
        if (altActive) {
            out = "$ESC$out"
            altActive = false
        }

        // Start timer when Enter is sent
        if (out == ENTER || out == "\n") {
            timer?.onCommandStarted()
        }

        bridge?.write(out)
    }

    fun handleKey(key: Key) {
        when (key) {
            is Key.Special -> when (key.label) {
                "CTRL" -> ctrlActive = !ctrlActive
                "ALT"  -> altActive  = !altActive
            }
            is Key.Char -> send(key.code)
        }
    }

    val bg = MaterialTheme.colorScheme.surface

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(bg),
    ) {
        // Timer indicator — shows above key rows when a command is running or just finished
        TimerRow(elapsed = elapsed, lastDuration = lastDuration)

        KeyRow(ROW1, ctrlActive, altActive) { handleKey(it) }
        KeyRow(ROW2, ctrlActive, altActive) { handleKey(it) }
    }
}

/**
 * Shows command duration above the key bar.
 *
 * - Running: accent color, live seconds  "[14s ▶]"
 * - Finished: muted color, last duration "[8s ✓]"  — fades after 5s (handled by timer reset)
 * - Idle: nothing shown
 */
@Composable
private fun TimerRow(
    elapsed: Int?,
    lastDuration: Int?,
) {
    val text = when {
        elapsed != null      -> "[${elapsed}s ▶]"
        lastDuration != null -> "[${lastDuration}s ✓]"
        else                 -> return  // idle — show nothing
    }

    val color = if (elapsed != null)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 1.dp),
        contentAlignment = Alignment.CenterEnd,
    ) {
        Text(
            text     = text,
            color    = color,
            fontSize = 11.sp,
            style    = MaterialTheme.typography.labelSmall,
        )
    }
}

@Composable
private fun KeyRow(
    keys: List<Key>,
    ctrlActive: Boolean,
    altActive: Boolean,
    onKey: (Key) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 1.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        keys.forEach { key ->
            val isActive = (key is Key.Special && key.label == "CTRL" && ctrlActive) ||
                           (key is Key.Special && key.label == "ALT"  && altActive)

            val label = when (key) {
                is Key.Char    -> key.label
                is Key.Special -> key.label
            }

            val color: Color = if (isActive)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)

            Text(
                text     = label,
                color    = color,
                fontSize = 13.sp,
                style    = MaterialTheme.typography.labelMedium,
                modifier = Modifier
                    .clickable { onKey(it) }
                    .padding(horizontal = 6.dp, vertical = 8.dp),
            )
        }
    }
}