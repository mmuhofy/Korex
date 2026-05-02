package com.muhofy.korex.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.muhofy.korex.data.session.SessionConverters
import com.muhofy.korex.data.session.SessionDao
import com.muhofy.korex.data.session.SessionEntity

@Database(
    entities = [SessionEntity::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(SessionConverters::class)
abstract class KorexDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
}