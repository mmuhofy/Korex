package com.muhofy.korex.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muhofy.korex.ui.components.LeftBar
import com.muhofy.korex.ui.components.SessionPanel
import com.muhofy.korex.ui.components.TerminalPane

private val PANEL_WIDTH = 260.dp

// UNTESTED — verify before use
@Composable
fun KorexScreen(
    viewModel: MainViewModel = hiltViewModel(),
) {
    val sessions by viewModel.sessions.collectAsStateWithLifecycle()
    val activeSessionId by viewModel.activeSessionId.collectAsStateWithLifecycle()

    var isPanelOpen by remember { mutableStateOf(false) }
    var showNewSessionDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.restoreOnStart()
    }

    val panelWidth by animateDpAsState(
        targetValue = if (isPanelOpen) PANEL_WIDTH else 0.dp,
        animationSpec = tween(durationMillis = 200),
        label = "panelWidth",
    )

    Surface(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxSize()) {

            // Left icon rail — always visible
            LeftBar(
                isPanelOpen = isPanelOpen,
                onHamburgerClick = { isPanelOpen = !isPanelOpen },
            )

            // Slide-in session panel
            if (panelWidth > 0.dp) {
                SessionPanel(
                    modifier      = Modifier.width(panelWidth),
                    sessions      = sessions,
                    activeId      = activeSessionId,
                    onSessionClick  = { viewModel.switchTo(it) },
                    onNewSession    = { showNewSessionDialog = true },
                    onRename        = { id, name -> viewModel.renameSession(id, name) },
                    onPin           = { id, pinned -> viewModel.pinSession(id, pinned) },
                    onClose         = { viewModel.closeSession(it) },
                )
            }

            // Terminal — fills remaining space
            TerminalPane(
                modifier        = Modifier.weight(1f),
                activeSessionId = activeSessionId,
                getBridge       = { viewModel.getBridge(it) },
                onSwipeLeft     = { viewModel.switchToNext() },
                onSwipeRight    = { viewModel.switchToPrevious() },
            )
        }
    }

    if (showNewSessionDialog) {
        NewSessionDialog(
            onConfirm = { name ->
                viewModel.createSession(name)
                showNewSessionDialog = false
            },
            onDismiss = { showNewSessionDialog = false },
        )
    }
}