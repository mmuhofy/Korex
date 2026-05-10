package com.termux.data.theme

import com.termux.domain.ThemeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Built-in themes seeded once on first launch. */
private val BUILT_INS = listOf(
    ThemeEntity(
        id          = "korex-dark",
        name        = "Korex Dark",
        author      = "Korex",
        background  = "#FF0D1117",
        surface     = "#FF161B22",
        accent      = "#FF58A6FF",
        text        = "#FFE6EDF3",
        isBuiltIn   = true,
        isInstalled = true,
        isActive    = true,           // default active theme
        installedAt = 0L,
    ),
    ThemeEntity(
        id          = "korex-light",
        name        = "Korex Light",
        author      = "Korex",
        background  = "#FFFFFFFF",
        surface     = "#FFF6F8FA",
        accent      = "#FF0969DA",
        text        = "#FF1F2328",
        isBuiltIn   = true,
        isInstalled = true,
        installedAt = 1L,
    ),
)

class ThemeRepositoryImpl @Inject constructor(
    private val dao: ThemeDao,
) : ThemeRepository {

    override fun observeInstalled(): Flow<List<ThemeEntity>> = dao.observeInstalled()

    override fun observeActive(): Flow<ThemeEntity?> = dao.observeActive()

    override suspend fun install(theme: ThemeEntity) {
        // Upsert full record then mark as installed
        dao.upsert(theme.copy(isInstalled = true, installedAt = System.currentTimeMillis()))
    }

    override suspend fun setActive(id: String) {
        dao.setActive(id)
    }

    override suspend fun delete(id: String) {
        dao.delete(id)
    }

    override suspend fun seedBuiltIns() {
        // insertIfAbsent — skips rows that already exist (IGNORE conflict strategy)
        BUILT_INS.forEach { dao.insertIfAbsent(it) }
    }
}