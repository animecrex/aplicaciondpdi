package com.dosis.app.wearable

/**
 * Estructura preparada para la futura integración con Wear OS.
 *
 * Este archivo define los contratos de comunicación entre la app móvil
 * y la aplicación Wear OS que desarrollará el compañero de equipo.
 *
 * Implementación pendiente: Wearable Data Layer API
 * Referencia: https://developer.android.com/training/wearables/data/data-layer
 */

/** Constantes de la Data Layer para sincronización Wear OS */
object WearableConstants {
    // Rutas de mensajes (Message API)
    const val PATH_MEDICATION_LIST = "/medication/list"
    const val PATH_MEDICATION_ADDED = "/medication/added"
    const val PATH_MEDICATION_DELETED = "/medication/deleted"
    const val PATH_REMINDER_FIRED = "/reminder/fired"
    const val PATH_MARK_TAKEN = "/medication/taken"
    const val PATH_SNOOZE = "/medication/snooze"

    // Claves de DataMap
    const val KEY_MEDICATION_ID = "medication_id"
    const val KEY_MEDICATION_NAME = "medication_name"
    const val KEY_MEDICATION_DOSAGE = "medication_dosage"
    const val KEY_SCHEDULED_TIME = "scheduled_time"
    const val KEY_MEDICATIONS_JSON = "medications_json"
}

/**
 * Interfaz que deberá implementar el WearableManager cuando se integre la Data Layer.
 * El compañero de equipo (Wear OS) implementará el lado del reloj.
 */
interface WearableDataManager {
    /** Envía la lista de medicamentos al reloj */
    suspend fun syncMedicationsToWatch(medicationsJson: String)

    /** Notifica al reloj que se disparó un recordatorio */
    suspend fun sendReminderToWatch(medicationId: Int, name: String, dosage: String, scheduledTime: Long)

    /** Escucha mensajes del reloj (tomar/posponer) */
    fun startListeningForWatchMessages()

    /** Detiene la escucha */
    fun stopListeningForWatchMessages()
}

/**
 * Estado de conexión del dispositivo wearable.
 * Se actualizará con la Data Layer API real.
 */
data class WearableConnectionState(
    val isConnected: Boolean = false,
    val deviceName: String? = null,
    val lastSyncTime: Long? = null,
    val isSyncing: Boolean = false
)
