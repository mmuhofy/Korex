package com.muhofy.korex.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muhofy.korex.ui.components.ExtraKeyBar
import com.muhofy.korex.ui.components.LeftBar
import com.muhofy.korex.ui.components.TerminalPane
import com.muhofy.korex.ui.components.TopBar

@Composable
fun KorexScreen(viewModel: MainViewModel = hiltViewModel()) {
    val sessions        by viewModel.sessions.collectAsStateWithLifecycle()
    val activeSessionId by viewModel.activeSessionId.collectAsStateWithLifecycle()
    val activeBridge    = activeSessionId?.let { viewModel.getBridge(it) }
    val activeSession   = sessions.firstOrNull { it.id == activeSessionId }

    var isPanelOpen          by remember { mutableStateOf(false) }
    var showNewSessionDialog  by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.restoreOnStart() }
    LaunchedEffect(sessions) {
        if (sessions.isEmpty()) viewModel.createSession("Main")
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
        ) {
            TopBar(
                activeSessionName = activeSession?.name,
                sessions          = sessions,
                activeSessionId   = activeSessionId,
                onHamburgerClick  = { isPanelOpen = true },
                onSessionClick    = { viewModel.switchTo(it) },
                onSessionClose    = { viewModel.closeSession(it) },
                onNewSession      = { showNewSessionDialog = true },
                modifier          = Modifier.fillMaxWidth(),
            )

            TerminalPane(
                modifier        = Modifier.weight(1f),
                activeSessionId = activeSessionId,
                getBridge       = { viewModel.getBridge(it) },
                onSwipeLeft     = { viewModel.switchToNext() },
                onSwipeRight    = { viewModel.switchToPrevious() },
            )

            ExtraKeyBar(
                bridge   = activeBridge,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        LeftBar(
            isPanelOpen      = isPanelOpen,
            onHamburgerClick = { isPanelOpen = true },
            onClose          = { isPanelOpen = false },
            onNewSession     = { showNewSessionDialog = true },
            onSettings       = { },
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