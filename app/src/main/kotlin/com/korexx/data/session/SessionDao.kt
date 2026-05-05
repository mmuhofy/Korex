package com.korexx.data.session

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {

    @Query("SELECT * FROM sessions ORDER BY isPinned DESC, sortOrder ASC")
    fun observeAll(): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE id = :id")
    suspend fun getById(id: String): SessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: SessionEntity)

    @Update
    suspend fun update(session: SessionEntity)

    @Query("DELETE FROM sessions WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE sessions SET status = :status, lastActiveAt = :lastActiveAt WHERE id = :id")
    suspend fun updateStatus(id: String, status: SessionStatus, lastActiveAt: Long)

    @Query("UPDATE sessions SET name = :name WHERE id = :id")
    suspend fun updateName(id: String, name: String)

    @Query("UPDATE sessions SET cwd = :cwd, lastActiveAt = :lastActiveAt WHERE id = :id")
    suspend fun updateCwd(id: String, cwd: String, lastActiveAt: Long)

    @Query("UPDATE sessions SET isPinned = :isPinned WHERE id = :id")
    suspend fun updatePinned(id: String, isPinned: Boolean)

    @Query("UPDATE sessions SET sortOrder = :sortOrder WHERE id = :id")
    suspend fun updateSortOrder(id: String, sortOrder: Int)
}