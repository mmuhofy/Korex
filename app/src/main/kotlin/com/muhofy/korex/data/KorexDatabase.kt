package com.muhofy.korex.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.muhofy.korex.data.history.CommandHistoryDao
import com.muhofy.korex.data.history.CommandHistoryEntity
import com.muhofy.korex.data.session.SessionConverters
import com.muhofy.korex.data.session.SessionDao
import com.muhofy.korex.data.session.SessionEntity
import com.muhofy.korex.data.snippet.SnippetDao
import com.muhofy.korex.data.snippet.SnippetEntity

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