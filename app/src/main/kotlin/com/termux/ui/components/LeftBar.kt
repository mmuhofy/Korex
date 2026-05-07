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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val PANEL_WIDTH = 72.dp
private val ICON_SIZE   = 20.dp

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
            enter   = slideInHorizontally(animationSpec = tween(200)) { -it },
            exit    = slideOutHorizontally(animationSpec = tween(200)) { -it },
        ) {
            Column(
                modifier = Modifier
                    .width(PANEL_WIDTH)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surface)
                    .statusBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
            ) {
                // Hide button
                PanelItem(
                    icon       = Icons.Rounded.Close,
                    label      = "Hide",
                    tint       = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    onClick    = onClose,
                    topPadding = 16.dp,
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
                    tint          = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    onClick       = onSettings,
                    bottomPadding = 24.dp,
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
    topPadding: androidx.compose.ui.unit.Dp = 8.dp,
    bottomPadding: androidx.compose.ui.unit.Dp = 0.dp,
) {
    Column(
        modifier = Modifier
            .padding(top = topPadding, bottom = bottomPadding)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 10.dp),
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
            modifier = Modifier.padding(top = 4.dp),
            style    = MaterialTheme.typography.labelSmall,
        )
    }
}