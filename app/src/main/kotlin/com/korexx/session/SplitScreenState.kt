package com.korexx.session

/**
 * Holds the state for split screen mode.
 *
 * @param primarySessionId   Always present — left pane.
 * @param secondarySessionId Present when split is active — right pane.
 * @param splitRatio         Width ratio of primary pane (0.2..0.8), default 0.5.
 */
data class SplitScreenState(
    val primarySessionId: String,
    val secondarySessionId: String? = null,
    val splitRatio: Float = 0.5f,
) {
    val isSplit: Boolean get() = secondarySessionId != null
}