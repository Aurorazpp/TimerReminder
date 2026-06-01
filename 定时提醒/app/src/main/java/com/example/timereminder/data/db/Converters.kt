package com.example.timereminder.data.db

import androidx.room.TypeConverter

/**
 * Room 类型转换器
 */
class Converters {

    @TypeConverter
    fun fromTimestamp(value: Long?): Long? = value

    @TypeConverter
    fun toTimestamp(value: Long?): Long? = value

    @TypeConverter
    fun fromBoolean(value: Boolean): Int = if (value) 1 else 0

    @TypeConverter
    fun toBoolean(value: Int): Boolean = value == 1
}
