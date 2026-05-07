package com.termux.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val PANEL_WIDTH = 90.dp
private val ICON_SIZE   = 22.dp

@Composable
fun LeftBar(
    isPanelOpen: Boolean,
    onHamburgerClick: () -> Unit,
    onClose: () -> Unit,
    onSnippets: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxHeight()) {
        AnimatedVisibility(
            visible = isPanelOpen,
            enter   = slideInHorizontally(animationSpec = tween(220)) { -it },
            exit    = slideOutHorizontally(animationSpec = tween(220)) { -it },
        ) {
            Column(
                modifier = Modifier
                    .width(PANEL_WIDTH)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surface)
                    .statusBarsPadding(), // start below status bar, same as TopBar
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
            ) {
                // Close button — top, aligned with TopBar height
                PanelItem(
                    icon       = Icons.Rounded.Close,
                    label      = "Hide",
                    tint       = MaterialTheme.colorScheme.error,
                    onClick    = onClose,
                    topPadding = 14.dp,
                )

                PanelItem(
                    icon    = Icons.Rounded.Code,
                    label   = "Snippets",
                    tint    = MaterialTheme.colorScheme.primary,
                    onClick = onSnippets,
                )

                Spacer(modifier = Modifier.weight(1f))

                PanelItem(
                    icon          = Icons.Rounded.Settings,
                    label         = "Settings",
                    tint          = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                    onClick       = onSettings,
                    bottomPadding = 20.dp,
                )
            }
        }
    }
}

@Composable
private fun PanelItem(
    icon: ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit,
    topPadding: Dp = 8.dp,
    bottomPadding: Dp = 0.dp,
) {
    Column(
        modifier = Modifier
            .padding(top = topPadding, bottom = bottomPadding)
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = label,
            tint               = tint,
            modifier           = Modifier.size(ICON_SIZE),
        )
        Text(
            text     = label,
            color    = tint,
            fontSize = 10.sp,
            modifier = Modifier.padding(top = 3.dp),
            style    = MaterialTheme.typography.labelSmall,
        )
    }
}