package com.termux.ui

import android.graphics.Rect
import android.os.Bundle
import android.view.View
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

    private var isKeyboardVisible = false
    private var wasKeyboardOpen   = false

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

    override fun onPause() {
        super.onPause()
        wasKeyboardOpen = isKeyboardVisible
    }

    override fun onResume() {
        super.onResume()
        val rootView = findViewById<View>(android.R.id.content)
        rootView.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect()
            rootView.getWindowVisibleDisplayFrame(rect)
            val screenHeight = rootView.rootView.height
            isKeyboardVisible = (screenHeight - rect.bottom) > screenHeight * 0.15
        }
        if (wasKeyboardOpen && !isKeyboardVisible) {
            terminalViewRef.get()?.let { view ->
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
            }
        }
    }
}