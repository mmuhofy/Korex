package com.muhofy.korex.ui.components

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.GearSix
import com.adamglin.phosphoricons.regular.List
import com.adamglin.phosphoricons.regular.Plus
import com.adamglin.phosphoricons.regular.Terminal
import com.adamglin.phosphoricons.regular.X
import com.muhofy.korex.data.session.SessionEntity

private val PANEL_WIDTH = 130.dp
private val ICON_SIZE = 26.dp

@Composable
fun LeftBar(
    isPanelOpen: Boolean,
    sessions: List<SessionEntity>,
    activeSessionId: String?,
    onHamburgerClick: () -> Unit,
    onClose: () -> Unit,
    onSessionClick: (String) -> Unit,
    onNewSession: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxHeight()) {

        // Hamburger — always visible, top left
        if (!isPanelOpen) {
            Icon(
                imageVector        = PhosphorIcons.Regular.List,
                contentDescription = "Open menu",
                tint               = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier           = Modifier
                    .padding(top = 14.dp, start = 12.dp)
                    .size(ICON_SIZE)
                    .clickable { onHamburgerClick() }
                    .align(Alignment.TopStart),
            )
        }

        // Slide-in panel
        AnimatedVisibility(
            visible = isPanelOpen,
            enter   = slideInHorizontally(animationSpec = tween(220)) { -it },
            exit    = slideOutHorizontally(animationSpec = tween(220)) { -it },
        ) {
            Column(
                modifier = Modifier
                    .width(PANEL_WIDTH)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surface),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
            ) {
                // X — close panel
                PanelItem(
                    icon        = PhosphorIcons.Regular.X,
                    label       = "Hide",
                    tint        = MaterialTheme.colorScheme.error,
                    onClick     = onClose,
                    topPadding  = 16.dp,
                )

                // Sessions
                sessions.forEach { session ->
                    PanelItem(
                        icon    = PhosphorIcons.Regular.Terminal,
                        label   = session.name,
                        tint    = if (session.id == activeSessionId)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        onClick = { onSessionClick(session.id) },
                    )
                }

                // New Session
                PanelItem(
                    icon    = PhosphorIcons.Regular.Plus,
                    label   = "New Session",
                    tint    = MaterialTheme.colorScheme.primary,
                    onClick = onNewSession,
                )

                Spacer(modifier = Modifier.weight(1f))

                // Settings — bottom
                PanelItem(
                    icon           = PhosphorIcons.Regular.GearSix,
                    label          = "Settings",
                    tint           = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    onClick        = onSettings,
                    bottomPadding  = 20.dp,
                )
            }
        }
    }
}

@Composable
private fun PanelItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    topPadding: Dp = 8.dp,
    bottomPadding: Dp = 0.dp,
) {
    Column(
        modifier = Modifier
            .padding(top = topPadding, bottom = bottomPadding)
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
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
            text      = label,
            color     = tint,
            fontSize  = 10.sp,
            maxLines  = 1,
            modifier  = Modifier.padding(top = 4.dp),
        )
    }
}