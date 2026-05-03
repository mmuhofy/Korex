package com.muhofy.korex.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muhofy.korex.terminal.TerminalBridge

private const val CODE_ESC   = "\u001b"
private const val CODE_TAB   = "\t"
private const val CODE_UP    = "\u001b[A"
private const val CODE_DOWN  = "\u001b[B"
private const val CODE_LEFT  = "\u001b[D"
private const val CODE_RIGHT = "\u001b[C"
private const val CODE_HOME  = "\u001b[H"
private const val CODE_END   = "\u001b[F"
private const val CODE_PGUP  = "\u001b[5~"
private const val CODE_PGDN  = "\u001b[6~"

private data class ExtraKey(
    val label: String,
    val code: String,
    val isToggle: Boolean = false,
)

// Split into 2 rows
private val ROW1 = listOf(
    ExtraKey("ESC",  CODE_ESC),
    ExtraKey("/",    "/"),
    ExtraKey("-",    "-"),
    ExtraKey("|",    "|"),
    ExtraKey("HOME", CODE_HOME),
    ExtraKey("END",  CODE_END),
    ExtraKey("PGUP", CODE_PGUP),
    ExtraKey("PGDN", CODE_PGDN),
)

private val ROW2 = listOf(
    ExtraKey("TAB",  CODE_TAB),
    ExtraKey("CTRL", "",       isToggle = true),
    ExtraKey("ALT",  CODE_ESC, isToggle = true),
    ExtraKey("←",    CODE_LEFT),
    ExtraKey("↑",    CODE_UP),
    ExtraKey("↓",    CODE_DOWN),
    ExtraKey("→",    CODE_RIGHT),
)

@Composable
fun ExtraKeyBar(
    bridge: TerminalBridge?,
    modifier: Modifier = Modifier,
) {
    var ctrlActive by remember { mutableStateOf(false) }
    var altActive  by remember { mutableStateOf(false) }

    fun handleKey(key: ExtraKey) {
        when (key.label) {
            "CTRL" -> ctrlActive = !ctrlActive
            "ALT"  -> altActive  = !altActive
            else   -> {
                val input = buildInput(key.code, ctrlActive, altActive)
                bridge?.write(input)
                ctrlActive = false
                altActive  = false
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        KeyRow(
            keys       = ROW1,
            ctrlActive = ctrlActive,
            altActive  = altActive,
            onKey      = { handleKey(it) },
        )
        KeyRow(
            keys       = ROW2,
            ctrlActive = ctrlActive,
            altActive  = altActive,
            onKey      = { handleKey(it) },
        )
    }
}

@Composable
private fun KeyRow(
    keys: List<ExtraKey>,
    ctrlActive: Boolean,
    altActive: Boolean,
    onKey: (ExtraKey) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        keys.forEach { key ->
            val isActive = (key.label == "CTRL" && ctrlActive) ||
                           (key.label == "ALT"  && altActive)
            KeyText(
                label    = key.label,
                isActive = isActive,
                onClick  = { onKey(key) },
            )
        }
    }
}

@Composable
private fun KeyText(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
) {
    Text(
        text     = label,
        fontSize = 13.sp,
        color    = if (isActive)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
        style    = MaterialTheme.typography.labelSmall,
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp),
    )
}

private fun buildInput(code: String, ctrl: Boolean, alt: Boolean): String {
    var result = code
    if (ctrl && result.length == 1) {
        result = (result[0].code and 0x1f).toChar().toString()
    }
    if (alt) result = "$CODE_ESC$result"
    return result
}