package com.korexx.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DriveFileRenameOutline
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.SpaceDashboard
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.korexx.data.session.SessionEntity
import com.korexx.data.session.SessionStatus
import com.korexx.ui.theme.KorexStatusActive
import com.korexx.ui.theme.KorexStatusBackground
import com.korexx.ui.theme.KorexStatusCrashed
import com.korexx.ui.theme.KorexStatusWarning
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    activeSessionName: String?,
    sessions: List<SessionEntity>,
    activeSessionId: String?,
    isSplit: Boolean,
    onHamburgerClick: () -> Unit,
    onSessionClick: (String) -> Unit,
    onSessionClose: (String) -> Unit,
    onSessionRename: (String, String) -> Unit,
    onSessionPin: (String, Boolean) -> Unit,
    onNewSession: () -> Unit,
    onToggleSplit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState()
    val scope      = rememberCoroutineScope()
    var showSheet  by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Hamburger — opens left panel
        Icon(
            imageVector        = Icons.Rounded.Menu,
            contentDescription = "Open menu",
            tint               = MaterialTheme.colorScheme.onSurface,
            modifier           = Modifier
                .size(24.dp)
                .clickable { onHamburgerClick() },
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text       = "Korex",
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color      = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.weight(1f))

        // Active session chip — tap to open session sheet
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { showSheet = true }
                .padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment          = Alignment.CenterVertically,
            horizontalArrangement      = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector        = Icons.Rounded.Terminal,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.primary,
                modifier           = Modifier.size(14.dp),
            )
            Text(
                text     = activeSessionName ?: "Session",
                style    = MaterialTheme.typography.labelSmall,
                color    = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 12.sp,
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Split screen toggle button
        Icon(
            imageVector        = Icons.Rounded.SpaceDashboard,
            contentDescription = if (isSplit) "Exit split screen" else "Enter split screen",
            tint               = if (isSplit)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier           = Modifier
                .size(22.dp)
                .clickable { onToggleSplit() },
        )
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState       = sheetState,
            containerColor   = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
            ) {
                Text(
                    text     = "Sessions",
                    style    = MaterialTheme.typography.titleSmall,
                    color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )

                HorizontalDivider(
                    color     = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                    thickness = 0.5.dp,
                )

                sessions.forEach { session ->
                    SessionSheetItem(
                        session  = session,
                        isActive = session.id == activeSessionId,
                        onClick  = {
                            onSessionClick(session.id)
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                showSheet = false
                            }
                        },
                        onClose  = { onSessionClose(session.id) },
                        onRename = { newName -> onSessionRename(session.id, newName) },
                        onPin    = { onSessionPin(session.id, !session.isPinned) },
                    )
                }

                HorizontalDivider(
                    color     = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                    thickness = 0.5.dp,
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onNewSession()
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                showSheet = false
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        imageVector        = Icons.Rounded.Add,
                        contentDescription = "New session",
                        tint               = MaterialTheme.colorScheme.primary,
                        modifier           = Modifier.size(20.dp),
                    )
                    Text(
                        text  = "New Session",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun SessionSheetItem(
    session: SessionEntity,
    isActive: Boolean,
    onClick: () -> Unit,
    onClose: () -> Unit,
    onRename: (String) -> Unit,
    onPin: () -> Unit,
) {
    var menuExpanded     by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameText       by remember { mutableStateOf(session.name) }

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                    else MaterialTheme.colorScheme.surface
                )
                .combinedClickable(
                    onClick     = onClick,
                    onLongClick = { menuExpanded = true },
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(session.status.toColor()),
            )

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text       = session.name,
                        style      = MaterialTheme.typography.bodyMedium,
                        color      = if (isActive) MaterialTheme.colorScheme.primary
                                     else MaterialTheme.colorScheme.onSurface,
                        fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis,
                    )
                    if (session.isPinned) {
                        Icon(
                            imageVector        = Icons.Rounded.PushPin,
                            contentDescription = "Pinned",
                            tint               = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                            modifier           = Modifier.padding(start = 6.dp).size(12.dp),
                        )
                    }
                }
                Text(
                    text     = session.cwd,
                    style    = MaterialTheme.typography.bodySmall,
                    color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 11.sp,
                )
            }

            Icon(
                imageVector        = Icons.Rounded.Close,
                contentDescription = "Close",
                tint               = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                modifier           = Modifier
                    .size(16.dp)
                    .clickable { onClose() },
            )
        }

        DropdownMenu(
            expanded         = menuExpanded,
            onDismissRequest = { menuExpanded = false },
        ) {
            DropdownMenuItem(
                text        = { Text("Rename") },
                leadingIcon = { Icon(Icons.Rounded.DriveFileRenameOutline, null, modifier = Modifier.size(18.dp)) },
                onClick     = {
                    menuExpanded = false
                    renameText   = session.name
                    showRenameDialog = true
                },
            )
            DropdownMenuItem(
                text        = { Text(if (session.isPinned) "Unpin" else "Pin") },
                leadingIcon = { Icon(Icons.Rounded.PushPin, null, modifier = Modifier.size(18.dp)) },
                onClick     = { menuExpanded = false; onPin() },
            )
            DropdownMenuItem(
                text        = { Text("Close", color = MaterialTheme.colorScheme.error) },
                leadingIcon = {
                    Icon(Icons.Rounded.Close, null,
                        tint     = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp))
                },
                onClick     = { menuExpanded = false; onClose() },
            )
        }
    }

    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title            = { Text("Rename Session") },
            text             = {
                OutlinedTextField(
                    value         = renameText,
                    onValueChange = { renameText = it },
                    label         = { Text("Session name") },
                    singleLine    = true,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onRename(renameText)
                    showRenameDialog = false
                }) { Text("Rename") }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) { Text("Cancel") }
            },
        )
    }
}

private fun SessionStatus.toColor() = when (this) {
    SessionStatus.ACTIVE          -> KorexStatusActive
    SessionStatus.BACKGROUND      -> KorexStatusBackground
    SessionStatus.UNEXPECTED_EXIT -> KorexStatusWarning
    SessionStatus.CRASHED         -> KorexStatusCrashed
}