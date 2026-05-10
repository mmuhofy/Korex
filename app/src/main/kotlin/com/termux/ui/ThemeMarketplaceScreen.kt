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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.termux.data.theme.ThemeEntity

// ── Discover catalogue — not in DB, shown in Discover tab ────────────────────
// Install → ThemeViewModel.install() → saved to Room → appears in Installed tab
private val DISCOVER_CATALOGUE = listOf(
    ThemeEntity(id="dracula",      name="Dracula",          author="dracula-theme",  background="#FF282A36", surface="#FF343746", accent="#FFBD93F9", text="#FFF8F8F2"),
    ThemeEntity(id="nord",         name="Nord",             author="arcticicestudio", background="#FF2E3440", surface="#FF3B4252", accent="#FF88C0D0", text="#FFECEFF4"),
    ThemeEntity(id="tokyo-night",  name="Tokyo Night",      author="enkia",           background="#FF1A1B2E", surface="#FF24283B", accent="#FF7AA2F7", text="#FFC0CAF5"),
    ThemeEntity(id="solarized",    name="Solarized Dark",   author="altercation",     background="#FF002B36", surface="#FF073642", accent="#FF268BD2", text="#FF839496"),
    ThemeEntity(id="one-dark",     name="One Dark",         author="atom",            background="#FF282C34", surface="#FF31353F", accent="#FF61AFEF", text="#FFABB2BF"),
    ThemeEntity(id="monokai",      name="Monokai",          author="monokai",         background="#FF272822", surface="#FF383830", accent="#FFA6E22E", text="#FFF8F8F2"),
    ThemeEntity(id="gruvbox",      name="Gruvbox Dark",     author="morhetz",         background="#FF282828", surface="#FF3C3836", accent="#FFD79921", text="#FFEBDBB2"),
    ThemeEntity(id="catppuccin",   name="Catppuccin Mocha", author="catppuccin",      background="#FF1E1E2E", surface="#FF313244", accent="#FFCBA6F7", text="#FFCDD6F4"),
)

@Composable
fun ThemeMarketplaceScreen(
    onBack: () -> Unit,
    viewModel: ThemeViewModel = hiltViewModel(),
) {
    val installed   by viewModel.installedThemes.collectAsStateWithLifecycle()
    val activeTheme by viewModel.activeTheme.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }

    val installedIds = installed.map { it.id }.toSet()

    // Discover tab hides already-installed themes
    val discoverList = DISCOVER_CATALOGUE.filter { it.id !in installedIds }

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
        val displayList = if (selectedTab == 0) installed else discoverList

        if (displayList.isEmpty()) {
            Box(
                modifier         = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text  = if (selectedTab == 0) "No themes installed." else "All themes installed!",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        } else {
            LazyColumn(
                contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(displayList, key = { it.id }) { theme ->
                    ThemeCard(
                        theme     = theme,
                        isActive  = theme.id == activeTheme?.id,
                        onApply   = { viewModel.setActive(theme.id) },
                        onInstall = { viewModel.install(theme) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeCard(
    theme: ThemeEntity,
    isActive: Boolean,
    onApply: () -> Unit,
    onInstall: () -> Unit,
) {
    val bg     = theme.background.toComposeColor()
    val accent = theme.accent.toComposeColor()
    val text   = theme.text.toComposeColor()

    val borderColor by animateColorAsState(
        targetValue   = if (isActive) accent else Color.Transparent,
        animationSpec = tween(220),
        label         = "border",
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable { if (theme.isInstalled) onApply() else onInstall() }
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
                    text     = theme.author,
                    style    = MaterialTheme.typography.bodySmall,
                    color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                    fontSize = 11.sp,
                )
            }

            when {
                isActive -> {
                    Box(
                        modifier         = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(accent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector        = Icons.Rounded.Check,
                            contentDescription = "Active",
                            tint               = accent,
                            modifier           = Modifier.size(15.dp),
                        )
                    }
                }
                theme.isInstalled -> {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            .clickable { onApply() }
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
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { onInstall() }
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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(bg)
                .padding(10.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row {
                    Text("~ ",  color = accent,              fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    Text("❯ ",  color = accent,              fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    Text("ls",  color = text,                fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("src",       color = accent,                    fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    Text("README.md", color = text,                      fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    Text(".env",      color = text.copy(alpha = 0.5f),   fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
                Row {
                    Text("~ ", color = accent, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    Text("❯ ", color = accent, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    Box(
                        modifier = Modifier
                            .size(width = 7.dp, height = 13.dp)
                            .background(accent.copy(alpha = 0.8f)),
                    )
                }
            }
        }
    }
}

/** Parses "#AARRGGBB" or "#RRGGBB" hex string to Compose Color. */
private fun String.toComposeColor(): Color =
    try { Color(android.graphics.Color.parseColor(this)) }
    catch (_: Exception) { Color.White }