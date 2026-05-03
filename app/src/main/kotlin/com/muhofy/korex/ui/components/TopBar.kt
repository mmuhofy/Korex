package com.muhofy.korex.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
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
import com.muhofy.korex.data.session.SessionEntity
import com.muhofy.korex.data.session.SessionStatus
import com.muhofy.korex.ui.theme.KorexStatusActive
import com.muhofy.korex.ui.theme.KorexStatusBackground
import com.muhofy.korex.ui.theme.KorexStatusCrashed
import com.muhofy.korex.ui.theme.KorexStatusWarning
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    activeSessionName: String?,
    sessions: List<SessionEntity>,
    activeSessionId: String?,
    onHamburgerClick: () -> Unit,
    onSessionClick: (String) -> Unit,
    onSessionClose: (String) -> Unit,
    onNewSession: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartialExpansion = true)
    val scope = rememberCoroutineScope()
    var showSheet by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
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

        // Session button
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { showSheet = true }
                .padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
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
                        onClose  = {
                            onSessionClose(session.id)
                        },
                    )
                }

                HorizontalDivider(
                    color     = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                    thickness = 0.5.dp,
                )

                // New session
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
                    verticalAlignment = Alignment.CenterVertically,
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

@Composable
private fun SessionSheetItem(
    session: SessionEntity,
    isActive: Boolean,
    onClick: () -> Unit,
    onClose: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                else MaterialTheme.colorScheme.surface
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Status dot
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(50))
                .background(session.status.toColor()),
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text     = session.name,
                style    = MaterialTheme.typography.bodyMedium,
                color    = if (isActive) MaterialTheme.colorScheme.primary
                           else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
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
            contentDescription = "Close session",
            tint               = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
            modifier           = Modifier
                .size(16.dp)
                .clickable { onClose() },
        )
    }
}

private fun SessionStatus.toColor() = when (this) {
    SessionStatus.ACTIVE          -> KorexStatusActive
    SessionStatus.BACKGROUND      -> KorexStatusBackground
    SessionStatus.UNEXPECTED_EXIT -> KorexStatusWarning
    SessionStatus.CRASHED         -> KorexStatusCrashed
}