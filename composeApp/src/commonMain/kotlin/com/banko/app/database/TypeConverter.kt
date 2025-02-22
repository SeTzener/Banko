package com.banko.app.database

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import androidx.room.TypeConverter

class TypeConverter {
    private val json = Json { ignoreUnknownKeys = true }
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return json.decodeFromString(value)
    }
}