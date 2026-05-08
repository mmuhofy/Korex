package com.termux.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
) {
    var darkTheme   by remember { mutableStateOf(true) }
    var fontSize    by remember { mutableFloatStateOf(14f) }
    var defaultShell by remember { mutableStateOf("zsh") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
    ) {
        // Top bar
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector        = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Back",
                tint               = MaterialTheme.colorScheme.onSurface,
                modifier           = Modifier
                    .size(24.dp)
                    .clickable { onBack() },
            )
            Text(
                text     = "Settings",
                style    = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 16.dp),
                color    = MaterialTheme.colorScheme.onSurface,
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            // ── Appearance ───────────────────────────────────────────────
            SettingsGroupHeader("Appearance")

            SettingsToggleItem(
                icon    = Icons.Rounded.DarkMode,
                title   = "Dark theme",
                checked = darkTheme,
                onCheckedChange = { darkTheme = it },
            )

            SettingsDivider()

            // Font size slider
            SettingsSectionTitle(Icons.Rounded.TextFields, "Font size")
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .padding(start = 40.dp, end = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Slider(
                    value         = fontSize,
                    onValueChange = { fontSize = it },
                    valueRange    = 8f..32f,
                    steps         = 23,
                    modifier      = Modifier.weight(1f),
                )
                Text(
                    text     = "${fontSize.toInt()}sp",
                    style    = MaterialTheme.typography.bodySmall,
                    color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.padding(start = 12.dp),
                )
            }

            SettingsDivider()

            // ── Terminal ─────────────────────────────────────────────────
            SettingsGroupHeader("Terminal")

            SettingsSectionTitle(Icons.Rounded.Terminal, "Default shell")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 40.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                listOf("bash", "zsh").forEach { shell ->
                    val selected = defaultShell == shell
                    Text(
                        text     = shell,
                        style    = MaterialTheme.typography.bodyMedium,
                        color    = if (selected) MaterialTheme.colorScheme.primary
                                   else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        fontSize = 13.sp,
                        modifier = Modifier
                            .clickable { defaultShell = shell }
                            .padding(end = 20.dp, top = 4.dp, bottom = 4.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SettingsGroupHeader(title: String) {
    Text(
        text     = title,
        style    = MaterialTheme.typography.labelSmall,
        color    = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
    )
}

@Composable
private fun SettingsSectionTitle(icon: ImageVector, title: String) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            modifier           = Modifier.size(18.dp),
        )
        Text(
            text     = title,
            style    = MaterialTheme.typography.bodyMedium,
            color    = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 12.dp),
        )
    }
}

@Composable
private fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            modifier           = Modifier.size(18.dp),
        )
        Text(
            text     = title,
            style    = MaterialTheme.typography.bodyMedium,
            color    = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp),
        )
        Switch(
            checked         = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier  = Modifier.padding(vertical = 4.dp),
        color     = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f),
        thickness = 0.5.dp,
    )
}