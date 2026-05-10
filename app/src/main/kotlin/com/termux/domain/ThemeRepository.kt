package com.termux.domain

import com.termux.data.theme.ThemeEntity
import kotlinx.coroutines.flow.Flow

interface ThemeRepository {
    fun observeInstalled(): Flow<List<ThemeEntity>>
    fun observeActive(): Flow<ThemeEntity?>
    suspend fun install(theme: ThemeEntity)
    suspend fun setActive(id: String)
    suspend fun delete(id: String)
    /** Seeds built-in themes on first run — no-op if already present. */
    suspend fun seedBuiltIns()
}