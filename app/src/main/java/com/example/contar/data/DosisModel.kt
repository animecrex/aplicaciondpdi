package com.example.contar.data

import org.json.JSONObject
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID

/**
 * Modelo de datos para un recordatorio / alarma de dosis de medicamento.
 */
data class DosisItem(
    val id: String = UUID.randomUUID().toString(),
    val nombre: String,
    val dosis: String,
    val instruccion: String = "Con un vaso de agua",
    val hora: String, // Formato "HH:mm" (24h), ej. "08:00"
    val repeticion: String = "Cada día",
    val tomada: Boolean = false,
    val horaTomada: String? = null
) {
    /**
     * Convierte el modelo a un objeto JSON para persistencia local en SharedPreferences.
     */
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("nombre", nombre)
            put("dosis", dosis)
            put("instruccion", instruccion)
            put("hora", hora)
            put("repeticion", repeticion)
            put("tomada", tomada)
            put("horaTomada", horaTomada ?: "")
        }
    }

    /**
     * Calcula los minutos que faltan para la toma respecto a una hora dada.
     */
    fun minutosRestantes(desde: LocalTime = LocalTime.now()): Int {
        return try {
            val horaDosis = LocalTime.parse(hora, DateTimeFormatter.ofPattern("HH:mm"))
            val diffSeconds = horaDosis.toSecondOfDay() - desde.toSecondOfDay()
            diffSeconds / 60
        } catch (e: Exception) {
            0
        }
    }

    companion object {
        fun fromJson(json: JSONObject): DosisItem {
            val horaTomadaStr = json.optString("horaTomada", "")
            return DosisItem(
                id = json.optString("id", UUID.randomUUID().toString()),
                nombre = json.optString("nombre", "Medicamento"),
                dosis = json.optString("dosis", "1 dosis"),
                instruccion = json.optString("instruccion", "Con un vaso de agua"),
                hora = json.optString("hora", "08:00"),
                repeticion = json.optString("repeticion", "Cada día"),
                tomada = json.optBoolean("tomada", false),
                horaTomada = if (horaTomadaStr.isNotEmpty()) horaTomadaStr else null
            )
        }
    }
}
