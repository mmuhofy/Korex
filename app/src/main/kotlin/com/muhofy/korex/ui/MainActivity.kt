package com.muhofy.korex.ui

import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.muhofy.korex.terminal.terminalViewRef
import com.muhofy.korex.ui.theme.KorexTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var isKeyboardVisible = false
    private var wasKeyboardOpen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Edge to edge — status and nav bars become transparent, no fullscreen flag needed
        enableEdgeToEdge()
        setContent {
            KorexTheme {
                KorexScreen()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        wasKeyboardOpen = isKeyboardVisible
    }

    override fun onResume() {
        super.onResume()

        // Track keyboard visibility via viewTreeObserver
        val rootView = findViewById<View>(android.R.id.content)
        rootView.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect()
            rootView.getWindowVisibleDisplayFrame(rect)
            val screenHeight = rootView.rootView.height
            val keypadHeight = screenHeight - rect.bottom
            isKeyboardVisible = keypadHeight > screenHeight * 0.15
        }

        // Restore keyboard if it was open before leaving the app
        if (wasKeyboardOpen && !isKeyboardVisible) {
            terminalViewRef.get()?.let { view ->
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
            }
        }
    }
}