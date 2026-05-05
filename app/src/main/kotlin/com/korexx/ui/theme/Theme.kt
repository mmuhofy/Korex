package com.korexx.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    background        = KorexBackground,
    surface           = KorexSurface,
    surfaceVariant    = KorexSurfaceVariant,
    primary           = KorexAccent,
    onPrimary         = KorexBackground,
    onBackground      = KorexText,
    onSurface         = KorexText,
    onSurfaceVariant  = KorexText,
    outline           = KorexBorder,
)

private val LightColorScheme = lightColorScheme(
    background        = KorexLightBackground,
    surface           = KorexLightSurface,
    surfaceVariant    = KorexLightSurface,
    primary           = KorexLightAccent,
    onPrimary         = KorexLightBackground,
    onBackground      = KorexLightText,
    onSurface         = KorexLightText,
    onSurfaceVariant  = KorexLightText,
    outline           = KorexLightBorder,
)

@Composable
fun KorexTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = KorexTypography,
        content     = content,
    )
}