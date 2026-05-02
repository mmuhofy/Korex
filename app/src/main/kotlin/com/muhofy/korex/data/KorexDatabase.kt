package com.muhofy.korex.data

import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.RoomDatabase

// Placeholder entity — will be replaced with real entities in the session system phase
@Entity(tableName = "placeholder")
data class PlaceholderEntity(@PrimaryKey val id: Int = 0)

// Entities and DAOs will be added in the session system phase
@Database(entities = [PlaceholderEntity::class], version = 1, exportSchema = false)
abstract class KorexDatabase : RoomDatabase()