package com.termux.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import com.termux.terminal.TerminalBridge
import com.termux.ui.theme.KorexBackground

// ── Escape sequences ──────────────────────────────────────────────────────────
private const val ESC   = "\u001B"
private const val TAB   = "\u0009"
private const val UP    = "\u001B[A"
private const val DOWN  = "\u001B[B"
private const val LEFT  = "\u001B[D"
private const val RIGHT = "\u001B[C"
private const val PGUP  = "\u001B[5~"
private const val PGDN  = "\u001B[6~"
private const val HOME  = "\u001B[H"
private const val END   = "\u001B[F"

private sealed class Key {
    /** A key that writes a fixed byte sequence to the pty. */
    data class Seq(val label: String, val seq: String) : Key()
    /** A sticky modifier toggle (CTRL / ALT). */
    data class Modifier(val label: String) : Key()
}

private val ROW1 = listOf<Key>(
    Key.Seq("ESC",  ESC),
    Key.Seq("/",    "/"),
    Key.Seq("-",    "-"),
    Key.Seq("HOME", HOME),
    Key.Seq("↑",    UP),
    Key.Seq("END",  END),
    Key.Seq("PGUP", PGUP),
)

private val ROW2 = listOf<Key>(
    Key.Seq("TAB",  TAB),
    Key.Modifier("CTRL"),
    Key.Modifier("ALT"),
    Key.Seq("←",    LEFT),
    Key.Seq("↓",    DOWN),
    Key.Seq("→",    RIGHT),
    Key.Seq("PGDN", PGDN),
)

/**
 * Two-row extra key bar.
 *
 * CTRL / ALT are sticky toggles. When active they write to
 * [TerminalBridge.ctrlDown] / [TerminalBridge.altDown] which are read by
 * [KorexTerminalViewClient.readControlKey()] / [readAltKey()] on the NEXT
 * keyboard event. TerminalView then applies the modifier internally and the
 * flags are auto-consumed (one-shot).
 *
 * Sequence keys (arrows, ESC, etc.) bypass the keyboard pipeline and are
 * written directly to the pty. When a modifier is active it is applied to
 * the sequence (e.g. CTRL+↑ → CSI 1;5A) and consumed immediately.
 */
@Composable
fun ExtraKeyBar(
    bridge: TerminalBridge?,
    modifier: Modifier = Modifier,
) {
    // Mirror of viewClient flags so Compose recomposes on toggle
    var ctrlActive by remember { mutableStateOf(false) }
    var altActive  by remember { mutableStateOf(false) }

    fun setCtrl(v: Boolean) {
        ctrlActive = v
        bridge?.ctrlDown = v
    }

    fun setAlt(v: Boolean) {
        altActive = v
        bridge?.altDown = v
    }

    fun sendSeq(seq: String) {
        var out = seq
        // Apply CTRL modifier to escape sequences (arrows etc.)
        if (ctrlActive && seq.length > 1 && seq.startsWith(ESC + "[")) {
            // Convert "\u001B[X" → "\u001B[1;5X"  (CTRL modifier = param 5)
            val letter = seq.last()
            out = "\u001B[1;5$letter"
            setCtrl(false)
        } else if (ctrlActive) {
            // Single-char seq with CTRL — convert to control character
            if (seq.length == 1 && seq[0].code in 0x40..0x7E) {
                out = (seq[0].code and 0x1F).toChar().toString()
            }
            setCtrl(false)
        }
        // Apply ALT modifier: prefix with ESC
        if (altActive) {
            out = "$ESC$out"
            setAlt(false)
        }
        bridge?.write(out)
    }

    fun handleKey(key: Key) {
        when (key) {
            is Key.Modifier -> when (key.label) {
                "CTRL" -> setCtrl(!ctrlActive)
                "ALT"  -> setAlt(!altActive)
            }
            is Key.Seq -> sendSeq(key.seq)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(KorexBackground),   // same as terminal — no visual break
    ) {
        KeyRow(ROW1, ctrlActive, altActive) { handleKey(it) }
        KeyRow(ROW2, ctrlActive, altActive) { handleKey(it) }
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
            val isActive = (key is Key.Modifier && key.label == "CTRL" && ctrlActive) ||
                           (key is Key.Modifier && key.label == "ALT"  && altActive)

            val label = when (key) {
                is Key.Seq      -> key.label
                is Key.Modifier -> key.label
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
                    .clickable { onKey(key) }
                    .padding(horizontal = 6.dp, vertical = 8.dp),
            )
        }
    }
}