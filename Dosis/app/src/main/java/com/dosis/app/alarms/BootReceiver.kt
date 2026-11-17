package com.dosis.app.alarms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dosis.app.DosisApplication
import com.dosis.app.data.repository.MedicationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver que se activa cuando el teléfono se reinicia.
 * Restaura todas las alarmas de medicamentos activos.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action != Intent.ACTION_BOOT_COMPLETED &&
            action != Intent.ACTION_MY_PACKAGE_REPLACED) return

        val app = context.applicationContext as DosisApplication
        val repository = app.repository
        val alarmScheduler = AlarmScheduler(context)

        // Reprogramar alarmas de todos los medicamentos activos
        CoroutineScope(Dispatchers.IO).launch {
            val medications = repository.getAllActiveMedicationsOnce()
            medications.forEach { medication ->
                alarmScheduler.scheduleForMedication(medication)
            }
        }
    }
}
