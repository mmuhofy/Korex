package com.muhofy.korex.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muhofy.korex.ui.components.LeftBar
import com.muhofy.korex.ui.components.TerminalPane

@Composable
fun KorexScreen(viewModel: MainViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val homeDir = remember { context.filesDir.absolutePath }

    val sessions by viewModel.sessions.collectAsStateWithLifecycle()
    val activeSessionId by viewModel.activeSessionId.collectAsStateWithLifecycle()

    var isPanelOpen by remember { mutableStateOf(false) }
    var showNewSessionDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.restoreOnStart() }
    LaunchedEffect(sessions) {
        if (sessions.isEmpty()) viewModel.createSession("Main")
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

        // Terminal always full size
        TerminalPane(
            modifier        = Modifier.fillMaxSize(),
            activeSessionId = activeSessionId,
            getBridge       = { viewModel.getBridge(it) },
            onSwipeLeft     = { viewModel.switchToNext() },
            onSwipeRight    = { viewModel.switchToPrevious() },
        )

        // Left bar overlays terminal
        LeftBar(
            isPanelOpen     = isPanelOpen,
            sessions        = sessions,
            activeSessionId = activeSessionId,
            onHamburgerClick = { isPanelOpen = true },
            onClose          = { isPanelOpen = false },
            onSessionClick   = {
                viewModel.switchTo(it)
                isPanelOpen = false
            },
            onNewSession     = { showNewSessionDialog = true },
            onSettings       = { /* settings — later phase */ },
        )
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