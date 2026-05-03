package com.muhofy.korex.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
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
import com.muhofy.korex.ui.components.SessionBar
import com.muhofy.korex.ui.components.TerminalPane

@Composable
fun KorexScreen(viewModel: MainViewModel = hiltViewModel()) {
    val sessions        by viewModel.sessions.collectAsStateWithLifecycle()
    val activeSessionId by viewModel.activeSessionId.collectAsStateWithLifecycle()

    var isPanelOpen         by remember { mutableStateOf(false) }
    var showNewSessionDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.restoreOnStart() }
    LaunchedEffect(sessions) {
        if (sessions.isEmpty()) viewModel.createSession("Main")
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

        Column(modifier = Modifier.fillMaxSize()) {

            // Top session bar — always visible
            SessionBar(
                sessions        = sessions,
                activeSessionId = activeSessionId,
                onSessionClick  = { viewModel.switchTo(it) },
                onSessionClose  = { viewModel.closeSession(it) },
                modifier        = Modifier.fillMaxWidth().wrapContentHeight(),
            )

            // Terminal fills remaining space
            TerminalPane(
                modifier        = Modifier.weight(1f),
                activeSessionId = activeSessionId,
                getBridge       = { viewModel.getBridge(it) },
                onSwipeLeft     = { viewModel.switchToNext() },
                onSwipeRight    = { viewModel.switchToPrevious() },
            )

            // Extra key bar — always visible at bottom
            ExtraKeyBar(
                bridge   = activeSessionId?.let { viewModel.getBridge(it) },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        // LeftBar — overlays top-left, hamburger always visible
        LeftBar(
            isPanelOpen      = isPanelOpen,
            onHamburgerClick = { isPanelOpen = true },
            onClose          = { isPanelOpen = false },
            onNewSession     = { showNewSessionDialog = true },
            onSettings       = { /* later phase */ },
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