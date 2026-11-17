package com.dosis.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Registro histórico: cada vez que el usuario marca un medicamento como tomado
 * o lo pospone, se almacena aquí para seguimiento.
 */
@Entity(
    tableName = "medication_logs",
    foreignKeys = [
        ForeignKey(
            entity = Medication::class,
            parentColumns = ["id"],
            childColumns = ["medicationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("medicationId")]
)
data class MedicationLog(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val medicationId: Int,

    /** Hora programada en milisegundos epoch */
    val scheduledTime: Long,

    /** Hora real en que se tomó; null = pospuesto, no tomado */
    val takenAt: Long? = null,

    /** true = marcado como tomado; false = pospuesto / pendiente */
    val taken: Boolean = false,

    /** Número de veces pospuesto */
    val snoozeCount: Int = 0,

    val createdAt: Long = System.currentTimeMillis()
)
