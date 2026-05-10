package com.termux.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import com.termux.ui.components.CommandHistorySheet
import com.termux.ui.components.ExtraKeyBar
import com.termux.ui.components.LeftBar
import com.termux.ui.components.SnippetSheet
import com.termux.ui.components.SplitTerminalPane
import com.termux.ui.components.TopBar

@Composable
fun KorexScreen(
    viewModel: MainViewModel = hiltViewModel(),
    snippetViewModel: SnippetViewModel = hiltViewModel(),
    historyViewModel: CommandHistoryViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
) {
    val sessions        by viewModel.sessions.collectAsStateWithLifecycle()
    val activeSessionId by viewModel.activeSessionId.collectAsStateWithLifecycle()
    val splitState      by viewModel.splitState.collectAsStateWithLifecycle()
    val activeBridge    = activeSessionId?.let { viewModel.getBridge(it) }
    val activeSession   = sessions.firstOrNull { it.id == activeSessionId }
    val snippets        by snippetViewModel.snippets.collectAsStateWithLifecycle()
    val history         by historyViewModel.history.collectAsStateWithLifecycle()
    val searchQuery     by historyViewModel.searchQuery.collectAsStateWithLifecycle()
    val settings        by settingsViewModel.settings.collectAsStateWithLifecycle()

    val fontSize = settings.fontSize.toInt()

    var isPanelOpen          by remember { mutableStateOf(false) }
    var showNewSessionDialog by remember { mutableStateOf(false) }
    var showSnippetSheet     by remember { mutableStateOf(false) }
    var showHistorySheet     by remember { mutableStateOf(false) }
    var showSettings         by remember { mutableStateOf(false) }
    var showThemes           by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.restoreOnStart() }

    LaunchedEffect(sessions) {
        if (sessions.isEmpty()) viewModel.createSession("Main")
        viewModel.onSessionsUpdated(sessions.map { it.id })
    }

    // Apply font size from settings to all active bridges whenever it changes.
    // TerminalBridge.setFontSize() keeps the value; TerminalView picks it up
    // on next render via mRenderer update in KorexTerminalViewClient.onScale.
    LaunchedEffect(fontSize) {
        viewModel.applyFontSizeToAllBridges(fontSize)
    }

    AnimatedContent(
        targetState = when {
            showThemes   -> 2
            showSettings -> 1
            else         -> 0
        },
        transitionSpec = {
            if (targetState > initialState) slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
            else                            slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
        },
        label = "screen_transition",
    ) { screen ->
        when (screen) {
            2 -> ThemeMarketplaceScreen(onBack = { showThemes = false })
            1 -> SettingsScreen(onBack = { showSettings = false })
            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .imePadding(),
                    ) {
                        TopBar(
                            activeSessionName = activeSession?.name,
                            sessions          = sessions,
                            activeSessionId   = activeSessionId,
                            isSplit           = splitState?.isSplit == true,
                            onHamburgerClick  = { isPanelOpen = true },
                            onSessionClick    = { viewModel.switchTo(it) },
                            onSessionClose    = { viewModel.closeSession(it) },
                            onSessionRename   = { id, name -> viewModel.renameSession(id, name) },
                            onSessionPin      = { id, pinned -> viewModel.pinSession(id, pinned) },
                            onNewSession      = { showNewSessionDialog = true },
                            onToggleSplit     = {
                                if (splitState?.isSplit == true) viewModel.exitSplit()
                                else viewModel.enterSplit()
                            },
                            modifier = Modifier.fillMaxWidth(),
                        )

                        SplitTerminalPane(
                            splitState      = splitState,
                            getBridge       = { viewModel.getBridge(it) },
                            activeSessionId = activeSessionId,
                            onSwipeLeft     = { viewModel.switchToNext() },
                            onSwipeRight    = { viewModel.switchToPrevious() },
                            onSwipeUp       = { showHistorySheet = true },
                            onRatioChange   = { viewModel.updateSplitRatio(it) },
                            onEnterSplit    = { viewModel.enterSplit() },
                            onExitSplit     = { viewModel.exitSplit() },
                            modifier        = Modifier.weight(1f),
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
                        onSnippets       = { isPanelOpen = false; showSnippetSheet = true },
                        onThemes         = { isPanelOpen = false; showThemes = true },
                        onSettings       = { isPanelOpen = false; showSettings = true },
                    )
                }
            }
        }
    }

    if (showNewSessionDialog) {
        NewSessionDialog(
            onConfirm = { name -> viewModel.createSession(name); showNewSessionDialog = false },
            onDismiss = { showNewSessionDialog = false },
        )
    }

    if (showSnippetSheet) {
        SnippetSheet(
            snippets  = snippets,
            onDismiss = { showSnippetSheet = false },
            onExecute = { command ->
                activeBridge?.write("$command\n")
                activeSessionId?.let { historyViewModel.recordCommand(it, command) }
            },
            onAdd    = { t, c -> snippetViewModel.addSnippet(t, c) },
            onEdit   = { snippet, t, c -> snippetViewModel.updateSnippet(snippet, t, c) },
            onDelete = { snippetViewModel.deleteSnippet(it) },
        )
    }

    if (showHistorySheet) {
        CommandHistorySheet(
            history        = history,
            searchQuery    = searchQuery,
            onSearchChange = { historyViewModel.searchQuery.value = it },
            onDismiss      = { showHistorySheet = false },
            onExecute      = { command ->
                activeBridge?.write("$command\n")
                activeSessionId?.let { historyViewModel.recordCommand(it, command) }
            },
            onDelete   = { historyViewModel.delete(it) },
            onClearAll = { historyViewModel.clearAll() },
        )
    }
}