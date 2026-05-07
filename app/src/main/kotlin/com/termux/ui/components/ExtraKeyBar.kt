package com.termux.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.KeyboardArrowRight
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

private const val CODE_ESC   = "\u001b"
private const val CODE_TAB   = "\t"
private const val CODE_UP    = "\u001b[A"
private const val CODE_DOWN  = "\u001b[B"
private const val CODE_LEFT  = "\u001b[D"
private const val CODE_RIGHT = "\u001b[C"

private sealed class ExtraKey {
    data class TextKey(
        val label: String,
        val code: String,
        val isToggle: Boolean = false,
    ) : ExtraKey()

    data class IconKey(
        val icon: ImageVector,
        val label: String,
        val code: String,
    ) : ExtraKey()
}

private val KEYS = listOf(
    ExtraKey.TextKey("ESC",  CODE_ESC),
    ExtraKey.TextKey("TAB",  CODE_TAB),
    ExtraKey.TextKey("CTRL", "", isToggle = true),
    ExtraKey.TextKey("ALT",  CODE_ESC, isToggle = true),
    ExtraKey.TextKey("/",    "/"),
    ExtraKey.TextKey("-",    "-"),
    ExtraKey.TextKey("|",    "|"),
    ExtraKey.IconKey(Icons.Rounded.KeyboardArrowLeft,  "←", CODE_LEFT),
    ExtraKey.IconKey(Icons.Rounded.KeyboardArrowDown,  "↓", CODE_DOWN),
    ExtraKey.IconKey(Icons.Rounded.KeyboardArrowUp,    "↑", CODE_UP),
    ExtraKey.IconKey(Icons.Rounded.KeyboardArrowRight, "→", CODE_RIGHT),
)

@Composable
fun ExtraKeyBar(
    bridge: TerminalBridge?,
    modifier: Modifier = Modifier,
) {
    var ctrlActive by remember { mutableStateOf(false) }
    var altActive  by remember { mutableStateOf(false) }

    fun handleKey(code: String, label: String, isToggle: Boolean) {
        when {
            label == "CTRL" -> ctrlActive = !ctrlActive
            label == "ALT"  -> altActive  = !altActive
            else -> {
                var input = code
                if (ctrlActive && input.length == 1) {
                    input = (input[0].code and 0x1f).toChar().toString()
                }
                if (altActive) input = "$CODE_ESC$input"
                bridge?.write(input)
                ctrlActive = false
                altActive  = false
            }
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 6.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        KEYS.forEach { key ->
            when (key) {
                is ExtraKey.TextKey -> {
                    val isActive = (key.label == "CTRL" && ctrlActive) ||
                                   (key.label == "ALT"  && altActive)
                    TextKeyButton(
                        label    = key.label,
                        isActive = isActive,
                        onClick  = { handleKey(key.code, key.label, key.isToggle) },
                    )
                }
                is ExtraKey.IconKey -> {
                    IconKeyButton(
                        icon    = key.icon,
                        label   = key.label,
                        onClick = { handleKey(key.code, key.label, false) },
                    )
                }
            }
        }
    }
}

@Composable
private fun TextKeyButton(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
) {
    Text(
        text  = label,
        color = if (isActive)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        fontSize = 13.sp,
        style    = MaterialTheme.typography.labelMedium,
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
    )
}

@Composable
private fun IconKeyButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    // Arrow keys — rectangular chip
    Icon(
        imageVector        = icon,
        contentDescription = label,
        tint               = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        modifier           = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .size(18.dp),
    )
}