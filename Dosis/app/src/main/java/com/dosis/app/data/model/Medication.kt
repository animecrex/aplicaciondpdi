package com.dosis.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.dosis.app.data.database.Converters

/**
 * Entidad principal: representa un medicamento registrado por el usuario.
 */
@Entity(tableName = "medications")
@TypeConverters(Converters::class)
data class Medication(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    /** Nombre del medicamento (ej: "Ibuprofeno 400mg") */
    val name: String,

    /** Dosis a tomar (ej: "1 tableta", "5ml") */
    val dosage: String,

    /** Fecha de inicio en milisegundos epoch */
    val startDate: Long,

    /** Fecha de finalización en milisegundos epoch; null = sin fin */
    val endDate: Long? = null,

    /**
     * Frecuencia:
     *   - "DAILY" = todos los días
     *   - "WEEKLY:1,3,5" = lunes, miércoles, viernes (1=lunes … 7=domingo)
     */
    val frequency: String = FrequencyType.DAILY,

    /**
     * Lista de horarios en formato "HH:mm", ej: ["08:00","14:00","20:00"]
     * Almacenada como JSON por el TypeConverter
     */
    val scheduleTimes: List<String>,

    /** Notas adicionales opcionales */
    val notes: String = "",

    /** Si el medicamento está activo */
    val isActive: Boolean = true,

    /** Timestamp de creación */
    val createdAt: Long = System.currentTimeMillis()
)

object FrequencyType {
    const val DAILY = "DAILY"
    const val WEEKLY_PREFIX = "WEEKLY:"

    fun isDaily(frequency: String) = frequency == DAILY

    fun getWeeklyDays(frequency: String): List<Int> {
        if (!frequency.startsWith(WEEKLY_PREFIX)) return emptyList()
        return frequency.removePrefix(WEEKLY_PREFIX)
            .split(",")
            .mapNotNull { it.trim().toIntOrNull() }
    }

    fun buildWeeklyFrequency(days: List<Int>): String {
        return "$WEEKLY_PREFIX${days.sorted().joinToString(",")}"
    }
}
