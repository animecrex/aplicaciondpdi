package com.dosis.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dosis.app.DosisApplication
import com.dosis.app.alarms.AlarmScheduler
import com.dosis.app.data.model.Medication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver que procesa las acciones de la notificación:
 *   - ACTION_MARK_TAKEN: marca el medicamento como tomado y cancela la notificación.
 *   - ACTION_SNOOZE: pospone exactamente 10 minutos y cancela la notificación actual.
 *
 * Reglas estrictas:
 *  1. El posponer programa una nueva alarma exactamente 10 min después.
 *  2. No se permiten otros intervalos.
 *  3. Se evitan notificaciones duplicadas cancelando la actual antes de programar la nueva.
 *  4. El medicamento queda pendiente hasta marcarse como tomado.
 */
class NotificationActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_MARK_TAKEN = "com.dosis.app.ACTION_MARK_TAKEN"
        const val ACTION_SNOOZE = "com.dosis.app.ACTION_SNOOZE"

        const val EXTRA_MEDICATION_ID = "medication_id"
        const val EXTRA_MEDICATION_NAME = "medication_name"
        const val EXTRA_MEDICATION_DOSAGE = "medication_dosage"
        const val EXTRA_SCHEDULED_TIME = "scheduled_time"
        const val EXTRA_NOTIFICATION_ID = "notification_id"
        const val EXTRA_ORIGINAL_REQUEST_CODE = "original_request_code"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val medicationId = intent.getIntExtra(EXTRA_MEDICATION_ID, -1)
        val medicationName = intent.getStringExtra(EXTRA_MEDICATION_NAME) ?: return
        val dosage = intent.getStringExtra(EXTRA_MEDICATION_DOSAGE) ?: ""
        val scheduledTime = intent.getLongExtra(EXTRA_SCHEDULED_TIME, System.currentTimeMillis())
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, medicationId)

        if (medicationId == -1) return

        // Cancelar la notificación actual en todos los casos
        NotificationHelper.cancelNotification(context, notificationId)

        when (intent.action) {
            ACTION_MARK_TAKEN -> handleMarkTaken(context, medicationId, scheduledTime)
            ACTION_SNOOZE -> handleSnooze(context, medicationId, medicationName, dosage, scheduledTime, notificationId)
        }
    }

    /**
     * Marca el medicamento como tomado y registra en la base de datos.
     */
    private fun handleMarkTaken(context: Context, medicationId: Int, scheduledTime: Long) {
        val app = context.applicationContext as DosisApplication
        CoroutineScope(Dispatchers.IO).launch {
            val existing = app.repository.getLogForScheduledTime(medicationId, scheduledTime)
            if (existing != null) {
                app.repository.updateLog(
                    existing.copy(taken = true, takenAt = System.currentTimeMillis())
                )
            } else {
                app.repository.insertLog(
                    com.dosis.app.data.model.MedicationLog(
                        medicationId = medicationId,
                        scheduledTime = scheduledTime,
                        taken = true,
                        takenAt = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    /**
     * Pospone el recordatorio exactamente 10 minutos.
     * No permite otros intervalos.
     * Evita duplicados: la notificación actual ya fue cancelada arriba.
     */
    private fun handleSnooze(
        context: Context,
        medicationId: Int,
        medicationName: String,
        dosage: String,
        scheduledTime: Long,
        notificationId: Int
    ) {
        val app = context.applicationContext as DosisApplication
        val alarmScheduler = AlarmScheduler(context)

        // Crear un objeto Medication mínimo para pasar al scheduler
        val medication = Medication(
            id = medicationId,
            name = medicationName,
            dosage = dosage,
            startDate = scheduledTime,
            scheduleTimes = emptyList() // No se usa en snooze directo
        )

        // Programar nueva alarma en exactamente 10 minutos
        alarmScheduler.scheduleSnooze(medication, scheduledTime)

        // Registrar el snooze en la base de datos
        CoroutineScope(Dispatchers.IO).launch {
            val existing = app.repository.getLogForScheduledTime(medicationId, scheduledTime)
            if (existing != null) {
                app.repository.updateLog(
                    existing.copy(snoozeCount = existing.snoozeCount + 1)
                )
            } else {
                app.repository.insertLog(
                    com.dosis.app.data.model.MedicationLog(
                        medicationId = medicationId,
                        scheduledTime = scheduledTime,
                        taken = false,
                        snoozeCount = 1
                    )
                )
            }
        }
    }
}
