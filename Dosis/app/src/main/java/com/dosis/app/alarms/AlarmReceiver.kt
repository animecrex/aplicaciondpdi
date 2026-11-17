package com.dosis.app.alarms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dosis.app.notifications.NotificationHelper

/**
 * BroadcastReceiver que recibe la señal de AlarmManager cuando llega la hora
 * programada y lanza la notificación de recordatorio.
 */
class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val medicationId = intent.getIntExtra(AlarmScheduler.EXTRA_MEDICATION_ID, -1)
        val medicationName = intent.getStringExtra(AlarmScheduler.EXTRA_MEDICATION_NAME) ?: return
        val dosage = intent.getStringExtra(AlarmScheduler.EXTRA_MEDICATION_DOSAGE) ?: ""
        val scheduledTime = intent.getLongExtra(AlarmScheduler.EXTRA_SCHEDULED_TIME, System.currentTimeMillis())
        val requestCode = intent.getIntExtra(AlarmScheduler.EXTRA_ALARM_REQUEST_CODE, medicationId)

        if (medicationId == -1) return

        NotificationHelper.showMedicationReminder(
            context = context,
            medicationId = medicationId,
            medicationName = medicationName,
            dosage = dosage,
            scheduledTime = scheduledTime,
            notificationId = requestCode
        )
    }
}
