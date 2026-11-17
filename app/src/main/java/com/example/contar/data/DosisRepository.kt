package com.example.contar.data

import android.content.Context
import org.json.JSONArray
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Repositorio para persistir y gestionar las dosis y configuraciones en SharedPreferences.
 */
class DosisRepository(context: Context) {
    private val prefs = context.getSharedPreferences("dosis_app_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_DOSIS_ITEMS = "key_dosis_items"
        private const val KEY_DARK_THEME = "key_dark_theme"
    }

    /**
     * Obtiene la lista actual de medicamentos programados, ordenados por hora.
     */
    fun getDosisList(): List<DosisItem> {
        val jsonString = prefs.getString(KEY_DOSIS_ITEMS, null)
        if (jsonString.isNullOrEmpty()) {
            val defaults = getDefaultDosisList()
            saveDosisList(defaults)
            return defaults
        }

        return try {
            val array = JSONArray(jsonString)
            val list = mutableListOf<DosisItem>()
            for (i in 0 until array.length()) {
                list.add(DosisItem.fromJson(array.getJSONObject(i)))
            }
            list.sortedWith(compareBy({ it.hora }, { it.nombre }))
        } catch (e: Exception) {
            val defaults = getDefaultDosisList()
            saveDosisList(defaults)
            defaults
        }
    }

    /**
     * Guarda la lista completa de medicamentos.
     */
    fun saveDosisList(list: List<DosisItem>) {
        val array = JSONArray()
        list.forEach { array.put(it.toJson()) }
        prefs.edit().putString(KEY_DOSIS_ITEMS, array.toString()).apply()
    }

    /**
     * Agrega una nueva dosis y devuelve la lista actualizada.
     */
    fun addDosis(dosis: DosisItem): List<DosisItem> {
        val current = getDosisList().toMutableList()
        current.add(dosis)
        val sorted = current.sortedWith(compareBy({ it.hora }, { it.nombre }))
        saveDosisList(sorted)
        return sorted
    }

    /**
     * Actualiza una dosis existente.
     */
    fun updateDosis(updated: DosisItem): List<DosisItem> {
        val current = getDosisList().toMutableList()
        val index = current.indexOfFirst { it.id == updated.id }
        if (index != -1) {
            current[index] = updated
            val sorted = current.sortedWith(compareBy({ it.hora }, { it.nombre }))
            saveDosisList(sorted)
            return sorted
        }
        return current
    }

    /**
     * Elimina una dosis por su ID.
     */
    fun deleteDosis(id: String): List<DosisItem> {
        val current = getDosisList().filterNot { it.id == id }
        saveDosisList(current)
        return current
    }

    /**
     * Registra una dosis como tomada a una hora determinada.
     */
    fun markAsTaken(id: String, time: String): List<DosisItem> {
        val current = getDosisList().toMutableList()
        val index = current.indexOfFirst { it.id == id }
        if (index != -1) {
            current[index] = current[index].copy(
                tomada = true,
                horaTomada = time
            )
            saveDosisList(current)
        }
        return current
    }

    /**
     * Desmarca una dosis para volverla a poner pendiente.
     */
    fun markAsUntaken(id: String): List<DosisItem> {
        val current = getDosisList().toMutableList()
        val index = current.indexOfFirst { it.id == id }
        if (index != -1) {
            current[index] = current[index].copy(
                tomada = false,
                horaTomada = null
            )
            saveDosisList(current)
        }
        return current
    }

    /**
     * Pospone la dosis agregándole N minutos a su hora programada.
     */
    fun snoozeDosis(id: String, minutes: Int = 10): List<DosisItem> {
        val current = getDosisList().toMutableList()
        val index = current.indexOfFirst { it.id == id }
        if (index != -1) {
            val item = current[index]
            val formatter = DateTimeFormatter.ofPattern("HH:mm")
            val newTime = try {
                val parsed = LocalTime.parse(item.hora, formatter)
                parsed.plusMinutes(minutes.toLong()).format(formatter)
            } catch (e: Exception) {
                item.hora
            }
            current[index] = item.copy(hora = newTime)
            val sorted = current.sortedWith(compareBy({ it.hora }, { it.nombre }))
            saveDosisList(sorted)
            return sorted
        }
        return current
    }

    /**
     * Reinicia el estado de las tomas del día (todas pendientes).
     */
    fun resetDay(): List<DosisItem> {
        val current = getDosisList().map { it.copy(tomada = false, horaTomada = null) }
        saveDosisList(current)
        return current
    }

    /**
     * Restaura la lista por defecto con 3 medicamentos de ejemplo.
     */
    fun resetToDefaults(): List<DosisItem> {
        val defaults = getDefaultDosisList()
        saveDosisList(defaults)
        return defaults
    }

    fun isDarkTheme(): Boolean {
        return prefs.getBoolean(KEY_DARK_THEME, true)
    }

    fun setDarkTheme(dark: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_THEME, dark).apply()
    }

    private fun getDefaultDosisList(): List<DosisItem> {
        return listOf(
            DosisItem(
                id = "1",
                nombre = "Vitamina D",
                dosis = "1 cápsula",
                instruccion = "Con el desayuno",
                hora = "07:00",
                repeticion = "Cada día",
                tomada = true,
                horaTomada = "07:02"
            ),
            DosisItem(
                id = "2",
                nombre = "Losartán",
                dosis = "50 mg · 1 comprimido",
                instruccion = "Con un vaso de agua",
                hora = "08:00",
                repeticion = "Cada día",
                tomada = false,
                horaTomada = null
            ),
            DosisItem(
                id = "3",
                nombre = "Metformina",
                dosis = "850 mg",
                instruccion = "Con la cena",
                hora = "21:00",
                repeticion = "Cada día",
                tomada = false,
                horaTomada = null
            )
        )
    }
}
