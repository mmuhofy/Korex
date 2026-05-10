package com.termux.data.theme

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Persisted theme record.
 *
 * Colors stored as ARGB hex strings (e.g. "#FF0D1117") so they survive
 * serialization without a TypeConverter and can be parsed directly into
 * Compose Color via Color(android.graphics.Color.parseColor(hex)).
 *
 * [isBuiltIn]   — true for bundled themes (Korex Dark, Light). These are
 *                 never fetched from the network and cannot be deleted.
 * [isInstalled] — true once the user has explicitly installed a remote theme.
 *                 Built-in themes start as installed = true.
 * [isActive]    — only one theme can be active at a time. Enforced in repo.
 */
@Entity(tableName = "themes")
data class ThemeEntity(
    @PrimaryKey val id: String,
    val name: String,
    val author: String,
    val background: String,   // ARGB hex e.g. "#FF0D1117"
    val surface: String,
    val accent: String,
    val text: String,
    val isBuiltIn: Boolean   = false,
    val isInstalled: Boolean = false,
    val isActive: Boolean    = false,
    val installedAt: Long    = 0L,
)