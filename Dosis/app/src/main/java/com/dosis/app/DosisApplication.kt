package com.dosis.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.dosis.app.data.database.DosisDatabase
import com.dosis.app.data.repository.MedicationRepository

/**
 * Clase Application de Dosis.
 * Inicializa la base de datos, el repositorio y los canales de notificación.
 */
class DosisApplication : Application() {

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "dosis_medication_channel"
        const val NOTIFICATION_CHANNEL_NAME = "Recordatorios de medicamentos"
        const val NOTIFICATION_CHANNEL_DESC =
            "Notificaciones para recordar cuándo tomar tus medicamentos"
    }

    // Base de datos y repositorio disponibles globalmente (sin Hilt por simplicidad)
    val database: DosisDatabase by lazy { DosisDatabase.getInstance(this) }
    val repository: MedicationRepository by lazy {
        MedicationRepository(
            database.medicationDao(),
            database.medicationLogDao()
        )
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    /**
     * Crea el canal de notificaciones requerido para Android 8.0+.
     * Debe llamarse antes de publicar cualquier notificación.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = NOTIFICATION_CHANNEL_DESC
                enableVibration(true)
                enableLights(true)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}
