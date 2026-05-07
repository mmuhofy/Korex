package com.termux.util

// Gesture thresholds
const val SWIPE_THRESHOLD_PX        = 80
const val SWIPE_VELOCITY_THRESHOLD  = 100
const val PINCH_SPLIT_THRESHOLD     = 150f  // px span delta to trigger split enter/exit

// Session limits
const val SESSION_NAME_MAX_LENGTH   = 32

// Terminal
const val TERMINAL_TRANSCRIPT_ROWS   = 2000
const val TERMINAL_FONT_SIZE_DEFAULT = 14
const val TERMINAL_FONT_SIZE_MIN     = 8
const val TERMINAL_FONT_SIZE_MAX     = 32

// Split screen
const val SPLIT_RATIO_MIN = 0.2f
const val SPLIT_RATIO_MAX = 0.8f

/**
 * Shortens a full path for display in session cards.
 */
fun shortenPath(path: String, homeDir: String): String {
    return when {
        path == homeDir                          -> "~"
        path.startsWith("$homeDir/")             -> "~/" + path.removePrefix("$homeDir/")
        path.startsWith("/storage/emulated/0/")  -> path.removePrefix("/storage/emulated/0/")
        path == "/storage/emulated/0"            -> "0"
        path.startsWith("/sdcard/")              -> path.removePrefix("/sdcard/")
        path == "/sdcard"                        -> "~sd"
        else                                     -> path
    }
}