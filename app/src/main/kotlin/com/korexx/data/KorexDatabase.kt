package com.korexx.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.korexx.data.history.CommandHistoryDao
import com.korexx.data.history.CommandHistoryEntity
import com.korexx.data.session.SessionConverters
import com.korexx.data.session.SessionDao
import com.korexx.data.session.SessionEntity
import com.korexx.data.snippet.SnippetDao
import com.korexx.data.snippet.SnippetEntity

@Database(
    entities = [
        SessionEntity::class,
        SnippetEntity::class,
        CommandHistoryEntity::class,
    ],
    version  = 3,
    exportSchema = false,
)
@TypeConverters(SessionConverters::class)
abstract class KorexDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
    abstract fun snippetDao(): SnippetDao
    abstract fun commandHistoryDao(): CommandHistoryDao
}