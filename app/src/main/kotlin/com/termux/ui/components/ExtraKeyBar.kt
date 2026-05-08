package com.termux.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.termux.terminal.TerminalBridge

// ANSI escape sequences
private const val ESC   = "\u001B"
private const val TAB   = "\u0009"
private const val UP    = "\u001B[A"
private const val DOWN  = "\u001B[B"
private const val LEFT  = "\u001B[D"
private const val RIGHT = "\u001B[C"
private const val PGUP  = "\u001B[5~"
private const val PGDN  = "\u001B[6~"
private const val HOME  = "\u001B[H"
private const val END   = "\u001B[4~"

private sealed class Key {
    data class Text(val label: String, val code: String) : Key()
    data class Special(val label: String) : Key()  // CTRL, ALT, SHIFT
    data class Arrow(val icon: ImageVector, val label: String, val code: String) : Key()
}

// Row 1 — modifiers + symbols
private val ROW1 = listOf(
    Key.Special("CTRL"),
    Key.Special("ALT"),
    Key.Text("ESC",  ESC),
    Key.Text("TAB",  TAB),
    Key.Text("/",    "/"),
    Key.Text("-",    "-"),
    Key.Text("|",    "|"),
    Key.Text("HOME", HOME),
    Key.Text("END",  END),
    Key.Text("PGUP", PGUP),
    Key.Text("PGDN", PGDN),
)

// Row 2 — arrows + common symbols
private val ROW2 = listOf(
    Key.Arrow(Icons.AutoMirrored.Rounded.KeyboardArrowLeft,  "←", LEFT),
    Key.Arrow(Icons.Rounded.KeyboardArrowDown,               "↓", DOWN),
    Key.Arrow(Icons.Rounded.KeyboardArrowUp,                 "↑", UP),
    Key.Arrow(Icons.AutoMirrored.Rounded.KeyboardArrowRight, "→", RIGHT),
    Key.Text("~",  "~"),
    Key.Text("'",  "'"),
    Key.Text("\"", "\""),
    Key.Text("`",  "`"),
    Key.Text("\\", "\\"),
    Key.Text("_",  "_"),
    Key.Text(";",  ";"),
)

@Composable
fun ExtraKeyBar(
    bridge: TerminalBridge?,
    modifier: Modifier = Modifier,
) {
    var ctrlActive  by remember { mutableStateOf(false) }
    var altActive   by remember { mutableStateOf(false) }
    var shiftActive by remember { mutableStateOf(false) }

    fun send(code: String) {
        var input = code
        if (ctrlActive && input.length == 1) {
            input = (input[0].code and 0x1f).toChar().toString()
            ctrlActive = false
        }
        if (altActive) {
            input = "$ESC$input"
            altActive = false
        }
        if (shiftActive) {
            input = input.uppercase()
            shiftActive = false
        }
        bridge?.write(input)
    }

    fun handleKey(key: Key) {
        when (key) {
            is Key.Special -> when (key.label) {
                "CTRL"  -> ctrlActive  = !ctrlActive
                "ALT"   -> altActive   = !altActive
                "SHIFT" -> shiftActive = !shiftActive
            }
            is Key.Text  -> send(key.code)
            is Key.Arrow -> send(key.code)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        KeyRow(
            keys         = ROW1,
            ctrlActive   = ctrlActive,
            altActive    = altActive,
            shiftActive  = shiftActive,
            onKey        = { handleKey(it) },
        )
        KeyRow(
            keys         = ROW2,
            ctrlActive   = ctrlActive,
            altActive    = altActive,
            shiftActive  = shiftActive,
            onKey        = { handleKey(it) },
        )
    }
}

@Composable
private fun KeyRow(
    keys: List<Key>,
    ctrlActive: Boolean,
    altActive: Boolean,
    shiftActive: Boolean,
    onKey: (Key) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        keys.forEach { key ->
            KeyButton(
                key         = key,
                ctrlActive  = ctrlActive,
                altActive   = altActive,
                shiftActive = shiftActive,
                onClick     = { onKey(key) },
                modifier    = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun KeyButton(
    key: Key,
    ctrlActive: Boolean,
    altActive: Boolean,
    shiftActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isActive = when {
        key is Key.Special && key.label == "CTRL"  -> ctrlActive
        key is Key.Special && key.label == "ALT"   -> altActive
        key is Key.Special && key.label == "SHIFT" -> shiftActive
        else -> false
    }

    val bg     = if (isActive) MaterialTheme.colorScheme.primary
                 else MaterialTheme.colorScheme.surfaceVariant
    val fg     = if (isActive) MaterialTheme.colorScheme.onPrimary
                 else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
    val shape  = RoundedCornerShape(5.dp)

    Box(
        modifier = modifier
            .height(32.dp)
            .clip(shape)
            .background(bg)
            .clickable { onClick() }
            .padding(horizontal = 2.dp),
        contentAlignment = Alignment.Center,
    ) {
        when (key) {
            is Key.Arrow -> Icon(
                imageVector        = key.icon,
                contentDescription = key.label,
                tint               = fg,
                modifier           = Modifier.size(16.dp),
            )
            is Key.Text, is Key.Special -> {
                val label = when (key) {
                    is Key.Text    -> key.label
                    is Key.Special -> key.label
                    else           -> ""
                }
                Text(
                    text     = label,
                    color    = fg,
                    fontSize = 11.sp,
                    style    = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}