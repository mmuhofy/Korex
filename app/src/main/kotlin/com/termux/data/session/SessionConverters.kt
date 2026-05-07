package com.termux.data.session

import androidx.room.TypeConverter

class SessionConverters {

    @TypeConverter
    fun fromSessionStatus(status: SessionStatus): String = status.name

    @TypeConverter
    fun toSessionStatus(value: String): SessionStatus =
        SessionStatus.valueOf(value)
}