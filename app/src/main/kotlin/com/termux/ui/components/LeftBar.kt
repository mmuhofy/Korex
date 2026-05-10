package com.termux.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

private val PANEL_WIDTH = 56.dp
private val ICON_SIZE   = 21.dp

@Composable
fun LeftBar(
    isPanelOpen: Boolean,
    onHamburgerClick: () -> Unit,
    onClose: () -> Unit,
    onSnippets: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        // Scrim — tap outside to close
        if (isPanelOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f))
                    .clickable { onClose() },
            )
        }

        AnimatedVisibility(
            visible = isPanelOpen,
            enter   = slideInHorizontally(animationSpec = tween(180)) { -it },
            exit    = slideOutHorizontally(animationSpec = tween(180)) { -it },
        ) {
            Column(
                modifier = Modifier
                    .width(PANEL_WIDTH)
                    .fillMaxHeight()
                    // Slightly lighter than terminal bg for subtle separation
                    .background(MaterialTheme.colorScheme.surface)
                    .statusBarsPadding()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Close
                BarIcon(
                    icon    = Icons.Rounded.Close,
                    tint    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    onClick = onClose,
                )

                Spacer(modifier = Modifier.weight(1f))

                // Snippets
                BarIcon(
                    icon    = Icons.Rounded.Code,
                    tint    = MaterialTheme.colorScheme.primary,
                    onClick = onSnippets,
                )

                Spacer(modifier = Modifier.weight(1f))

                // Settings
                BarIcon(
                    icon    = Icons.Rounded.Settings,
                    tint    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                    onClick = onSettings,
                )
            }
        }
    }
}

@Composable
private fun BarIcon(
    icon: ImageVector,
    tint: Color,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = tint,
            modifier           = Modifier.size(ICON_SIZE),
        )
    }
}