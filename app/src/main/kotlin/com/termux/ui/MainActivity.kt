package com.termux.ui

import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.termux.terminal.terminalViewRef
import com.termux.ui.theme.KorexTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val bootstrapViewModel: BootstrapViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KorexTheme {
                val bootstrapState by bootstrapViewModel.state.collectAsStateWithLifecycle()

                when (val state = bootstrapState) {
                    is BootstrapState.Checking    -> BootstrapScreen("Checking…", 0)
                    is BootstrapState.Installing  -> BootstrapScreen(state.message, state.percent)
                    is BootstrapState.Done        -> KorexScreen()
                    is BootstrapState.Error       -> BootstrapErrorScreen(
                        message = state.message,
                        onRetry = { bootstrapViewModel.retry() },
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        showKeyboardWithDelay()
    }

    /**
     * Request focus on the terminal view and show the soft keyboard.
     * Matches Termux's setSoftKeyboardState() behavior — always show on resume
     * with a short delay to ensure the view is ready.
     */
    private fun showKeyboardWithDelay() {
        val view = terminalViewRef.get() ?: return
        view.requestFocus()
        view.postDelayed({
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }, 300)
    }
}