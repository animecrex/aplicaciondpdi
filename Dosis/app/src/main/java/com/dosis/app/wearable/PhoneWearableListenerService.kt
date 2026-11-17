package com.dosis.app.wearable

import android.util.Log
import com.dosis.app.DosisApplication
import com.dosis.app.alarms.AlarmScheduler
import com.dosis.app.data.model.MedicationLog
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * Escucha los mensajes enviados desde el smartwatch (ej. tomar o posponer medicamento).
 */
class PhoneWearableListenerService : WearableListenerService() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)
        Log.d(TAG, "Mensaje recibido del smartwatch: ${messageEvent.path}")

        when (messageEvent.path) {
            WearableConstants.PATH_MARK_TAKEN -> {
                val data = String(messageEvent.data, Charsets.UTF_8)
                handleMarkTaken(data)
            }
            WearableConstants.PATH_SNOOZE -> {
                val data = String(messageEvent.data, Charsets.UTF_8)
                handleSnooze(data)
            }
        }
    }

    private fun handleMarkTaken(dataJson: String) {
        scope.launch {
            try {
                val json = JSONObject(dataJson)
                val medId = json.optInt(WearableConstants.KEY_MEDICATION_ID, -1)
                val time = json.optLong(WearableConstants.KEY_SCHEDULED_TIME, System.currentTimeMillis())
                if (medId != -1) {
                    val app = applicationContext as DosisApplication
                    val existing = app.repository.getLogForScheduledTime(medId, time)
                    if (existing != null) {
                        app.repository.updateLog(
                            existing.copy(taken = true, takenAt = System.currentTimeMillis())
                        )
                    } else {
                        app.repository.insertLog(
                            MedicationLog(
                                medicationId = medId,
                                scheduledTime = time,
                                taken = true,
                                takenAt = System.currentTimeMillis()
                            )
                        )
                    }
                    Log.d(TAG, "Dosis $medId registrada como tomada desde el smartwatch")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al procesar mensaje de toma", e)
            }
        }
    }

    private fun handleSnooze(dataJson: String) {
        scope.launch {
            try {
                val json = JSONObject(dataJson)
                val medId = json.optInt(WearableConstants.KEY_MEDICATION_ID, -1)
                val time = json.optLong(WearableConstants.KEY_SCHEDULED_TIME, System.currentTimeMillis())
                if (medId != -1) {
                    val app = applicationContext as DosisApplication
                    val existing = app.repository.getLogForScheduledTime(medId, time)
                    if (existing != null) {
                        app.repository.updateLog(
                            existing.copy(snoozeCount = existing.snoozeCount + 1)
                        )
                    } else {
                        app.repository.insertLog(
                            MedicationLog(
                                medicationId = medId,
                                scheduledTime = time,
                                taken = false,
                                snoozeCount = 1
                            )
                        )
                    }
                    val medication = app.repository.getMedicationById(medId)
                    if (medication != null) {
                        val alarmScheduler = AlarmScheduler(applicationContext)
                        alarmScheduler.scheduleSnooze(medication, time)
                    }
                    Log.d(TAG, "Dosis $medId pospuesta desde el smartwatch")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al procesar mensaje de posponer", e)
            }
        }
    }

    companion object {
        private const val TAG = "PhoneWearableListener"
    }
}
