package com.muhofy.korex.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muhofy.korex.data.session.SessionEntity

private val ICON_SIZE = 22.dp

@Composable
fun HamburgerButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Icon(
        imageVector        = Icons.Rounded.Menu,
        contentDescription = "Toggle session bar",
        tint               = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
        modifier           = modifier
            .size(ICON_SIZE)
            .clickable { onClick() },
    )
}

@Composable
fun SessionBar(
    visible: Boolean,
    sessions: List<SessionEntity>,
    activeSessionId: String?,
    onSessionClick: (String) -> Unit,
    onSessionClose: (String) -> Unit,
    onNewSession: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible,
        enter   = slideInVertically(animationSpec = tween(220)) { it },
        exit    = slideOutVertically(animationSpec = tween(220)) { it },
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(vertical = 8.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                sessions.forEach { session ->
                    SessionChip(
                        session   = session,
                        isActive  = session.id == activeSessionId,
                        onClick   = { onSessionClick(session.id) },
                        onClose   = { onSessionClose(session.id) },
                    )
                }

                // New session button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { onNewSession() }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector        = Icons.Rounded.Add,
                        contentDescription = "New session",
                        tint               = MaterialTheme.colorScheme.primary,
                        modifier           = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun SessionChip(
    session: SessionEntity,
    isActive: Boolean,
    onClick: () -> Unit,
    onClose: () -> Unit,
) {
    val bg   = if (isActive) MaterialTheme.colorScheme.primaryContainer
               else MaterialTheme.colorScheme.surfaceVariant
    val fg   = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer
               else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .clickable { onClick() }
            .padding(start = 10.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text      = session.name,
            color     = fg,
            fontSize  = 12.sp,
            maxLines  = 1,
            overflow  = TextOverflow.Ellipsis,
            style     = MaterialTheme.typography.labelSmall,
        )
        Spacer(modifier = Modifier.width(6.dp))
        Icon(
            imageVector        = Icons.Rounded.Close,
            contentDescription = "Close ${session.name}",
            tint               = fg.copy(alpha = 0.6f),
            modifier           = Modifier
                .size(14.dp)
                .clickable { onClose() },
        )
    }
}