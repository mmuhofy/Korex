package com.termux.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.termux.data.history.CommandHistoryDao
import com.termux.data.history.CommandHistoryEntity
import com.termux.data.session.SessionConverters
import com.termux.data.session.SessionDao
import com.termux.data.session.SessionEntity
import com.termux.data.snippet.SnippetDao
import com.termux.data.snippet.SnippetEntity
import com.termux.data.theme.ThemeDao
import com.termux.data.theme.ThemeEntity

@Database(
    entities = [
        SessionEntity::class,
        SnippetEntity::class,
        CommandHistoryEntity::class,
        ThemeEntity::class,
    ],
    version      = 4,
    exportSchema = false,
)
@TypeConverters(SessionConverters::class)
abstract class KorexDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
    abstract fun snippetDao(): SnippetDao
    abstract fun commandHistoryDao(): CommandHistoryDao
    abstract fun themeDao(): ThemeDao
}