package com.muhofy.korex.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Plus
import com.adamglin.phosphoricons.regular.PushPin
import com.adamglin.phosphoricons.regular.Terminal
import com.muhofy.korex.data.session.SessionEntity
import com.muhofy.korex.data.session.SessionStatus
import com.muhofy.korex.util.shortenPath
import com.muhofy.korex.ui.theme.KorexStatusActive
import com.muhofy.korex.ui.theme.KorexStatusBackground
import com.muhofy.korex.ui.theme.KorexStatusCrashed
import com.muhofy.korex.ui.theme.KorexStatusWarning

@Composable
fun SessionPanel(
    sessions: List<SessionEntity>,
    activeId: String?,
    onSessionClick: (String) -> Unit,
    onNewSession: () -> Unit,
    onRename: (String, String) -> Unit,
    onPin: (String, Boolean) -> Unit,
    onClose: (String) -> Unit,
    homeDir: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text     = "SESSIONS",
                style    = MaterialTheme.typography.labelSmall,
                color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.weight(1f),
                letterSpacing = 1.5.sp,
            )
            IconButton(onClick = onNewSession, modifier = Modifier.size(28.dp)) {
                Icon(
                    imageVector        = PhosphorIcons.Regular.Plus,
                    contentDescription = "New session",
                    tint               = MaterialTheme.colorScheme.primary,
                    modifier           = Modifier.size(18.dp),
                )
            }
        }

        HorizontalDivider(
            color     = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
            thickness = 0.5.dp,
        )

        LazyColumn {
            items(sessions, key = { it.id }) { session ->
                SessionCard(
                    session  = session,
                    isActive = session.id == activeId,
                    homeDir  = homeDir,
                    onClick  = { onSessionClick(session.id) },
                    onRename = { onRename(session.id, it) },
                    onPin    = { onPin(session.id, !session.isPinned) },
                    onClose  = { onClose(session.id) },
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SessionCard(
    session: SessionEntity,
    isActive: Boolean,
    homeDir: String,
    onClick: () -> Unit,
    onRename: (String) -> Unit,
    onPin: () -> Unit,
    onClose: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                else MaterialTheme.colorScheme.surface
            )
            .combinedClickable(
                onClick     = onClick,
                onLongClick = { menuExpanded = true },
            )
            .padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Status dot
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(session.status.toColor()),
            )

            Spacer(modifier = Modifier.width(10.dp))

            Icon(
                imageVector        = PhosphorIcons.Regular.Terminal,
                contentDescription = null,
                tint               = if (isActive)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                modifier           = Modifier.size(14.dp),
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text     = session.name,
                    style    = MaterialTheme.typography.bodyMedium,
                    color    = if (isActive)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text     = shortenPath(session.cwd, homeDir),
                    style    = MaterialTheme.typography.bodySmall,
                    color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 10.sp,
                )
            }

            if (session.isPinned) {
                Icon(
                    imageVector        = PhosphorIcons.Regular.PushPin,
                    contentDescription = "Pinned",
                    tint               = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    modifier           = Modifier.size(11.dp),
                )
            }
        }

        DropdownMenu(
            expanded         = menuExpanded,
            onDismissRequest = { menuExpanded = false },
        ) {
            DropdownMenuItem(
                text    = { Text("Rename") },
                onClick = { menuExpanded = false; onRename(session.name) },
            )
            DropdownMenuItem(
                text    = { Text(if (session.isPinned) "Unpin" else "Pin") },
                onClick = { menuExpanded = false; onPin() },
            )
            DropdownMenuItem(
                text    = { Text("Close", color = MaterialTheme.colorScheme.error) },
                onClick = { menuExpanded = false; onClose() },
            )
        }
    }
}

private fun SessionStatus.toColor() = when (this) {
    SessionStatus.ACTIVE          -> KorexStatusActive
    SessionStatus.BACKGROUND      -> KorexStatusBackground
    SessionStatus.UNEXPECTED_EXIT -> KorexStatusWarning
    SessionStatus.CRASHED         -> KorexStatusCrashed
}