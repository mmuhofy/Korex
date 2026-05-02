package com.muhofy.korex.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muhofy.korex.ui.components.LeftBar
import com.muhofy.korex.ui.components.SessionBar
import com.muhofy.korex.ui.components.TerminalPane

@Composable
fun KorexScreen(viewModel: MainViewModel = hiltViewModel()) {
    val sessions        by viewModel.sessions.collectAsStateWithLifecycle()
    val activeSessionId by viewModel.activeSessionId.collectAsStateWithLifecycle()

    var isBarVisible         by remember { mutableStateOf(false) }
    var showNewSessionDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.restoreOnStart() }
    LaunchedEffect(sessions) {
        if (sessions.isEmpty()) viewModel.createSession("Main")
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

        // Terminal + LeftBar side by side, terminal fills remaining space
        Row(modifier = Modifier.fillMaxSize()) {
            LeftBar(
                onHamburgerClick = { isBarVisible = !isBarVisible },
                modifier         = Modifier.fillMaxHeight(),
            )
            TerminalPane(
                modifier        = Modifier.weight(1f),
                activeSessionId = activeSessionId,
                getBridge       = { viewModel.getBridge(it) },
                onSwipeLeft     = { viewModel.switchToNext() },
                onSwipeRight    = { viewModel.switchToPrevious() },
            )
        }

        // Session bar — slides up from bottom, overlays everything
        SessionBar(
            visible         = isBarVisible,
            sessions        = sessions,
            activeSessionId = activeSessionId,
            onSessionClick  = {
                viewModel.switchTo(it)
                isBarVisible = false
            },
            onSessionClose  = { viewModel.closeSession(it) },
            onNewSession    = { showNewSessionDialog = true },
            modifier        = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .align(Alignment.BottomCenter),
        )
    }

    if (showNewSessionDialog) {
        NewSessionDialog(
            onConfirm = { name ->
                viewModel.createSession(name)
                showNewSessionDialog = false
                isBarVisible = false
            },
            onDismiss = { showNewSessionDialog = false },
        )
    }
}