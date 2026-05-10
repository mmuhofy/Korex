package com.termux.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.termux.data.theme.ThemeEntity

private val DefaultDarkScheme = darkColorScheme(
    background       = KorexBackground,
    surface          = KorexSurface,
    surfaceVariant   = KorexSurfaceVariant,
    primary          = KorexAccent,
    onPrimary        = KorexBackground,
    onBackground     = KorexText,
    onSurface        = KorexText,
    onSurfaceVariant = KorexText,
    outline          = KorexBorder,
)

private val DefaultLightScheme = lightColorScheme(
    background       = KorexLightBackground,
    surface          = KorexLightSurface,
    surfaceVariant   = KorexLightSurface,
    primary          = KorexLightAccent,
    onPrimary        = KorexLightBackground,
    onBackground     = KorexLightText,
    onSurface        = KorexLightText,
    onSurfaceVariant = KorexLightText,
    outline          = KorexLightBorder,
)

/**
 * Builds a [darkColorScheme] from a [ThemeEntity].
 * Falls back to the default dark scheme if the entity is null.
 */
fun themeEntityToColorScheme(entity: ThemeEntity?) =
    if (entity == null) DefaultDarkScheme
    else {
        val bg     = entity.background.parseColor()
        val surf   = entity.surface.parseColor()
        val accent = entity.accent.parseColor()
        val text   = entity.text.parseColor()
        // Derive a slightly lighter surface variant
        val surfVariant = surf.copy(alpha = 0.85f)
        // Border = text at low alpha
        val border = text.copy(alpha = 0.12f)

        darkColorScheme(
            background       = bg,
            surface          = surf,
            surfaceVariant   = surfVariant,
            primary          = accent,
            onPrimary        = bg,
            onBackground     = text,
            onSurface        = text,
            onSurfaceVariant = text,
            outline          = border,
        )
    }

@Composable
fun KorexTheme(
    darkTheme: Boolean    = isSystemInDarkTheme(),
    activeTheme: ThemeEntity? = null,      // null → fall back to dark/light default
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        activeTheme != null -> themeEntityToColorScheme(activeTheme)
        darkTheme           -> DefaultDarkScheme
        else                -> DefaultLightScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = KorexTypography,
        content     = content,
    )
}

private fun String.parseColor(): Color =
    try { Color(android.graphics.Color.parseColor(this)) }
    catch (_: Exception) { Color.White }