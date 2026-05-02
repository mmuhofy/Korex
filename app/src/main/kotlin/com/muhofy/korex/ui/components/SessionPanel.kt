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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muhofy.korex.R
import com.muhofy.korex.data.session.SessionEntity
import com.muhofy.korex.data.session.SessionStatus
import com.muhofy.korex.ui.theme.KorexStatusActive
import com.muhofy.korex.ui.theme.KorexStatusBackground
import com.muhofy.korex.ui.theme.KorexStatusCrashed
import com.muhofy.korex.ui.theme.KorexStatusWarning

// UNTESTED — verify before use
@Composable
fun SessionPanel(
    sessions: List<SessionEntity>,
    activeId: String?,
    onSessionClick: (String) -> Unit,
    onNewSession: () -> Unit,
    onRename: (String, String) -> Unit,
    onPin: (String, Boolean) -> Unit,
    onClose: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text  = "SESSIONS",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onNewSession, modifier = Modifier.size(24.dp)) {
                Icon(
                    painter            = painterResource(R.drawable.ic_add),
                    contentDescription = "New session",
                    tint               = MaterialTheme.colorScheme.primary,
                    modifier           = Modifier.size(18.dp),
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

        LazyColumn {
            items(sessions, key = { it.id }) { session ->
                SessionCard(
                    session   = session,
                    isActive  = session.id == activeId,
                    onClick   = { onSessionClick(session.id) },
                    onRename  = { onRename(session.id, it) },
                    onPin     = { onPin(session.id, !session.isPinned) },
                    onClose   = { onClose(session.id) },
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
    onClick: () -> Unit,
    onRename: (String) -> Unit,
    onPin: () -> Unit,
    onClose: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isActive) MaterialTheme.colorScheme.surface
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .combinedClickable(
                onClick      = onClick,
                onLongClick  = { menuExpanded = true },
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Status dot
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(session.status.toColor()),
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text     = session.name,
                    style    = MaterialTheme.typography.titleMedium,
                    color    = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text     = session.cwd,
                    style    = MaterialTheme.typography.bodySmall,
                    color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 10.sp,
                )
            }

            if (session.isPinned) {
                Icon(
                    painter            = painterResource(R.drawable.ic_pin),
                    contentDescription = "Pinned",
                    tint               = MaterialTheme.colorScheme.primary,
                    modifier           = Modifier.size(12.dp),
                )
            }
        }

        DropdownMenu(
            expanded         = menuExpanded,
            onDismissRequest = { menuExpanded = false },
        ) {
            DropdownMenuItem(
                text    = { Text("Rename") },
                onClick = {
                    menuExpanded = false
                    onRename(session.name)
                },
            )
            DropdownMenuItem(
                text    = { Text(if (session.isPinned) "Unpin" else "Pin") },
                onClick = {
                    menuExpanded = false
                    onPin()
                },
            )
            DropdownMenuItem(
                text    = { Text("Close", color = MaterialTheme.colorScheme.error) },
                onClick = {
                    menuExpanded = false
                    onClose()
                },
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