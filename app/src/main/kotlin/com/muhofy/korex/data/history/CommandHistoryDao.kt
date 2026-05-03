package com.muhofy.korex.data.history

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CommandHistoryDao {

    @Query("SELECT * FROM command_history ORDER BY executedAt DESC LIMIT 200")
    fun observeAll(): Flow<List<CommandHistoryEntity>>

    @Query("SELECT * FROM command_history WHERE sessionId = :sessionId ORDER BY executedAt DESC LIMIT 200")
    fun observeBySession(sessionId: String): Flow<List<CommandHistoryEntity>>

    @Query("""
        SELECT * FROM command_history 
        WHERE command LIKE '%' || :query || '%'
        ORDER BY executedAt DESC LIMIT 100
    """)
    fun search(query: String): Flow<List<CommandHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: CommandHistoryEntity)

    @Query("DELETE FROM command_history WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM command_history")
    suspend fun clearAll()

    @Query("DELETE FROM command_history WHERE sessionId = :sessionId")
    suspend fun clearBySession(sessionId: String)
}