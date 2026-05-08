package com.termux.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "korex_settings")

data class KorexSettings(
    val darkTheme:    Boolean = true,
    val fontSize:     Float   = 14f,
    val defaultShell: String  = "zsh",
)

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val DARK_THEME    = booleanPreferencesKey("dark_theme")
        val FONT_SIZE     = floatPreferencesKey("font_size")
        val DEFAULT_SHELL = stringPreferencesKey("default_shell")
    }

    val settings: Flow<KorexSettings> = context.dataStore.data.map { prefs ->
        KorexSettings(
            darkTheme    = prefs[Keys.DARK_THEME]    ?: true,
            fontSize     = prefs[Keys.FONT_SIZE]     ?: 14f,
            defaultShell = prefs[Keys.DEFAULT_SHELL] ?: "zsh",
        )
    }

    suspend fun setDarkTheme(value: Boolean) {
        context.dataStore.edit { it[Keys.DARK_THEME] = value }
    }

    suspend fun setFontSize(value: Float) {
        context.dataStore.edit { it[Keys.FONT_SIZE] = value }
    }

    suspend fun setDefaultShell(value: String) {
        context.dataStore.edit { it[Keys.DEFAULT_SHELL] = value }
    }
}