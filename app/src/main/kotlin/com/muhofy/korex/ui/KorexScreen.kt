package com.muhofy.korex.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
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

@Composable
fun KorexScreen(viewModel: MainViewModel = hiltViewModel()) {
    val sessions by viewModel.sessions.collectAsStateWithLifecycle()
    val activeSessionId by viewModel.activeSessionId.collectAsStateWithLifecycle()

    var isPanelOpen by remember { mutableStateOf(false) }
    var showNewSessionDialog by remember { mutableStateOf(false) }

    // Auto-create first session if DB is empty on start
    LaunchedEffect(Unit) {
        viewModel.restoreOnStart()
    }
    LaunchedEffect(sessions) {
        if (sessions.isEmpty()) {
            viewModel.createSession("Main")
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(modifier = Modifier.fillMaxSize()) {

            // Left icon rail — always visible, only hamburger when panel closed
            LeftBar(
                isPanelOpen      = isPanelOpen,
                onHamburgerClick = { isPanelOpen = !isPanelOpen },
            )

            // Animated session panel
            AnimatedVisibility(
                visible = isPanelOpen,
                enter   = slideInHorizontally(animationSpec = tween(200)) { -it },
                exit    = slideOutHorizontally(animationSpec = tween(200)) { -it },
            ) {
                Box(
                    modifier = Modifier
                        .width(PANEL_WIDTH)
                        .fillMaxHeight()
                ) {
                    SessionPanel(
                        sessions        = sessions,
                        activeId        = activeSessionId,
                        onSessionClick  = {
                            viewModel.switchTo(it)
                            isPanelOpen = false
                        },
                        onNewSession    = { showNewSessionDialog = true },
                        onRename        = { id, name -> viewModel.renameSession(id, name) },
                        onPin           = { id, pinned -> viewModel.pinSession(id, pinned) },
                        onClose         = { viewModel.closeSession(it) },
                    )
                }
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
                isPanelOpen = false
            },
            onDismiss = { showNewSessionDialog = false },
        )
    }
}