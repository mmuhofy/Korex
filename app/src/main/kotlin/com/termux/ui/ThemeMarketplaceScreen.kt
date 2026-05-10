package com.termux.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Placeholder model — replaced by ThemeEntity in next step ─────────────────
data class ThemePreview(
    val id: String,
    val name: String,
    val author: String,
    val background: Color,
    val surface: Color,
    val accent: Color,
    val text: Color,
    val isInstalled: Boolean = false,
    val isActive: Boolean    = false,
)

// Built-in themes — hardcoded until ThemeRepository is wired
private val BUILT_IN_THEMES = listOf(
    ThemePreview(
        id         = "korex-dark",
        name       = "Korex Dark",
        author     = "Korex",
        background = Color(0xFF0D1117),
        surface    = Color(0xFF161B22),
        accent     = Color(0xFF58A6FF),
        text       = Color(0xFFE6EDF3),
        isInstalled = true,
        isActive    = true,
    ),
    ThemePreview(
        id         = "korex-light",
        name       = "Korex Light",
        author     = "Korex",
        background = Color(0xFFFFFFFF),
        surface    = Color(0xFFF6F8FA),
        accent     = Color(0xFF0969DA),
        text       = Color(0xFF1F2328),
        isInstalled = true,
    ),
)

private val DISCOVER_THEMES = listOf(
    ThemePreview(
        id         = "dracula",
        name       = "Dracula",
        author     = "dracula-theme",
        background = Color(0xFF282A36),
        surface    = Color(0xFF343746),
        accent     = Color(0xFFBD93F9),
        text       = Color(0xFFF8F8F2),
    ),
    ThemePreview(
        id         = "nord",
        name       = "Nord",
        author     = "arcticicestudio",
        background = Color(0xFF2E3440),
        surface    = Color(0xFF3B4252),
        accent     = Color(0xFF88C0D0),
        text       = Color(0xFFECEFF4),
    ),
    ThemePreview(
        id         = "tokyo-night",
        name       = "Tokyo Night",
        author     = "enkia",
        background = Color(0xFF1A1B2E),
        surface    = Color(0xFF24283B),
        accent     = Color(0xFF7AA2F7),
        text       = Color(0xFFC0CAF5),
    ),
    ThemePreview(
        id         = "solarized-dark",
        name       = "Solarized Dark",
        author     = "altercation",
        background = Color(0xFF002B36),
        surface    = Color(0xFF073642),
        accent     = Color(0xFF268BD2),
        text       = Color(0xFF839496),
    ),
    ThemePreview(
        id         = "one-dark",
        name       = "One Dark",
        author     = "atom",
        background = Color(0xFF282C34),
        surface    = Color(0xFF31353F),
        accent     = Color(0xFF61AFEF),
        text       = Color(0xFFABB2BF),
    ),
    ThemePreview(
        id         = "monokai",
        name       = "Monokai",
        author     = "monokai",
        background = Color(0xFF272822),
        surface    = Color(0xFF383830),
        accent     = Color(0xFFA6E22E),
        text       = Color(0xFFF8F8F2),
    ),
    ThemePreview(
        id         = "gruvbox",
        name       = "Gruvbox Dark",
        author     = "morhetz",
        background = Color(0xFF282828),
        surface    = Color(0xFF3C3836),
        accent     = Color(0xFFD79921),
        text       = Color(0xFFEBDBB2),
    ),
    ThemePreview(
        id         = "catppuccin",
        name       = "Catppuccin Mocha",
        author     = "catppuccin",
        background = Color(0xFF1E1E2E),
        surface    = Color(0xFF313244),
        accent     = Color(0xFFCBA6F7),
        text       = Color(0xFFCDD6F4),
    ),
)

@Composable
fun ThemeMarketplaceScreen(
    onBack: () -> Unit,
) {
    var selectedTab  by remember { mutableIntStateOf(0) }
    // Active theme id — will come from ThemeViewModel in next step
    var activeThemeId by remember { mutableStateOf("korex-dark") }
    // Installed set — will come from Room in next step
    var installed by remember { mutableStateOf(setOf("korex-dark", "korex-light")) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // ── Top bar ───────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector        = Icons.Rounded.ArrowBackIosNew,
                contentDescription = "Back",
                tint               = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier           = Modifier
                    .size(18.dp)
                    .clickable { onBack() },
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text       = "Themes",
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                fontSize   = 17.sp,
            )
        }

        // ── Tabs ──────────────────────────────────────────────────────────
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor   = MaterialTheme.colorScheme.background,
            contentColor     = MaterialTheme.colorScheme.primary,
            indicator        = { tabPositions ->
                Box(
                    modifier = Modifier
                        .tabIndicatorOffset(tabPositions[selectedTab])
                        .height(2.dp)
                        .padding(horizontal = 32.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp),
                        ),
                )
            },
        ) {
            listOf("Installed", "Discover").forEachIndexed { idx, label ->
                Tab(
                    selected = selectedTab == idx,
                    onClick  = { selectedTab = idx },
                    text     = {
                        Text(
                            text  = label,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selectedTab == idx)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        )
                    },
                )
            }
        }

        // ── Content ───────────────────────────────────────────────────────
        val displayList = if (selectedTab == 0) {
            BUILT_IN_THEMES.map { t ->
                t.copy(
                    isInstalled = t.id in installed,
                    isActive    = t.id == activeThemeId,
                )
            }
        } else {
            DISCOVER_THEMES.map { t ->
                t.copy(isInstalled = t.id in installed)
            }
        }

        LazyColumn(
            contentPadding  = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(displayList, key = { it.id }) { theme ->
                ThemeCard(
                    theme     = theme,
                    onApply   = { activeThemeId = it },
                    onInstall = { installed = installed + it },
                )
            }
        }
    }
}

@Composable
private fun ThemeCard(
    theme: ThemePreview,
    onApply: (String) -> Unit,
    onInstall: (String) -> Unit,
) {
    val borderColor by animateColorAsState(
        targetValue = if (theme.isActive) theme.accent else Color.Transparent,
        animationSpec = tween(200),
        label = "border",
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.5.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp),
            )
            .clickable {
                if (theme.isInstalled) onApply(theme.id)
                else onInstall(theme.id)
            }
            .padding(14.dp),
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text       = theme.name,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color      = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text  = theme.author,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                    fontSize = 11.sp,
                )
            }

            // Action button
            when {
                theme.isActive -> {
                    // Active checkmark
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(theme.accent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector        = Icons.Rounded.Check,
                            contentDescription = "Active",
                            tint               = theme.accent,
                            modifier           = Modifier.size(15.dp),
                        )
                    }
                }
                theme.isInstalled -> {
                    // Apply button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            .clickable { onApply(theme.id) }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    ) {
                        Text(
                            text  = "Apply",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                else -> {
                    // Install button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { onInstall(theme.id) }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    ) {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            Icon(
                                imageVector        = Icons.Rounded.Download,
                                contentDescription = "Install",
                                tint               = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier           = Modifier.size(13.dp),
                            )
                            Text(
                                text  = "Install",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── Terminal preview ──────────────────────────────────────────────
        TerminalPreview(theme = theme)
    }
}

/**
 * Mini terminal preview — shows what the theme looks like in a real terminal.
 * Static text, color-coded with the theme's actual palette.
 */
@Composable
private fun TerminalPreview(theme: ThemePreview) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(theme.background)
            .padding(10.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            // Prompt line
            Row {
                Text("~ ", color = theme.accent,     fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                Text("❯ ", color = theme.accent,     fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                Text("ls", color = theme.text,       fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            }
            // Output line
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("src",      color = theme.accent,              fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                Text("README.md",color = theme.text,                fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                Text(".env",     color = theme.text.copy(alpha=0.5f),fontSize= 11.sp, fontFamily = FontFamily.Monospace)
            }
            // Next prompt
            Row {
                Text("~ ", color = theme.accent, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                Text("❯ ", color = theme.accent, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                // Blinking cursor simulation
                Box(
                    modifier = Modifier
                        .size(width = 7.dp, height = 13.dp)
                        .background(theme.accent.copy(alpha = 0.8f)),
                )
            }
        }
    }
}