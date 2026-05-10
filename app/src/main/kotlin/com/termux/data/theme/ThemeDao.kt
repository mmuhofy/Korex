package com.termux.data.theme

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ThemeDao {

    @Query("SELECT * FROM themes ORDER BY isBuiltIn DESC, installedAt ASC")
    fun observeAll(): Flow<List<ThemeEntity>>

    @Query("SELECT * FROM themes WHERE isInstalled = 1 ORDER BY isBuiltIn DESC, installedAt ASC")
    fun observeInstalled(): Flow<List<ThemeEntity>>

    @Query("SELECT * FROM themes WHERE isActive = 1 LIMIT 1")
    fun observeActive(): Flow<ThemeEntity?>

    @Query("SELECT * FROM themes WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): ThemeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(theme: ThemeEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfAbsent(theme: ThemeEntity)

    /** Sets one theme as active, clears all others. */
    @Query("UPDATE themes SET isActive = (id = :id)")
    suspend fun setActive(id: String)

    @Query("UPDATE themes SET isInstalled = 1, installedAt = :at WHERE id = :id")
    suspend fun markInstalled(id: String, at: Long)

    @Query("DELETE FROM themes WHERE id = :id AND isBuiltIn = 0")
    suspend fun delete(id: String)
}