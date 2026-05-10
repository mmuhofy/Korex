package com.termux.util

// ── Gesture — swipe (fling-based, used in SwipeAwareTerminalView) ────────────
// Minimum fling velocity (px/sec) to trigger session swipe or panel open
const val SWIPE_VELOCITY_THRESHOLD = 1200f
// Minimum horizontal travel (px) to count as a horizontal session fling
const val SWIPE_THRESHOLD_PX = 80f
// Minimum vertical travel (px) to count as a swipe-up fling
const val SWIPE_THRESHOLD_Y_PX = 80f

// ── Gesture — pinch (scale-based, used in KorexTerminalViewClient) ───────────
// Cumulative scale factor above which pinch-out → enter split is fired
const val PINCH_OUT_SCALE_THRESHOLD = 1.4f
// Cumulative scale factor below which pinch-in → exit split is fired
const val PINCH_IN_SCALE_THRESHOLD  = 0.7f

// ── Session ───────────────────────────────────────────────────────────────────
const val SESSION_NAME_MAX_LENGTH = 32

// ── Terminal ──────────────────────────────────────────────────────────────────
const val TERMINAL_TRANSCRIPT_ROWS   = 2000
const val TERMINAL_FONT_SIZE_DEFAULT = 14
const val TERMINAL_FONT_SIZE_MIN     = 8
const val TERMINAL_FONT_SIZE_MAX     = 32

// ── Split screen ──────────────────────────────────────────────────────────────
const val SPLIT_RATIO_MIN = 0.2f
const val SPLIT_RATIO_MAX = 0.8f

/**
 * Shortens a full path for display in session cards.
 */
fun shortenPath(path: String, homeDir: String): String {
    return when {
        path == homeDir                         -> "~"
        path.startsWith("$homeDir/")            -> "~/" + path.removePrefix("$homeDir/")
        path.startsWith("/storage/emulated/0/") -> path.removePrefix("/storage/emulated/0/")
        path == "/storage/emulated/0"           -> "0"
        path.startsWith("/sdcard/")             -> path.removePrefix("/sdcard/")
        path == "/sdcard"                       -> "~sd"
        else                                    -> path
    }
}