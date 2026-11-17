package com.dosis.app.data.database

import androidx.room.TypeConverter
/**
 * TypeConverters para Room: convierte tipos no primitivos a/desde String.
 * Usamos una serialización JSON manual sin dependencia de Gson compleja;
 * para List<String> usamos separador "|".
 */
class Converters {

    @TypeConverter
    fun fromStringList(list: List<String>?): String {
        if (list == null) return ""
        return list.joinToString("|")
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        return value.split("|").filter { it.isNotBlank() }
    }
}
