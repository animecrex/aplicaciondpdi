package com.dosis.app.alarms

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.dosis.app.data.model.FrequencyType
import com.dosis.app.data.model.Medication
import java.util.Calendar

/**
 * Programa y cancela alarmas con AlarmManager para los medicamentos.
 * Usa alarmas exactas cuando el sistema lo permite.
 */
class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    companion object {
        const val EXTRA_MEDICATION_ID = "medication_id"
        const val EXTRA_MEDICATION_NAME = "medication_name"
        const val EXTRA_MEDICATION_DOSAGE = "medication_dosage"
        const val EXTRA_SCHEDULED_TIME = "scheduled_time"
        const val EXTRA_ALARM_REQUEST_CODE = "alarm_request_code"
    }

    /**
     * Programa todas las alarmas para los próximos 7 días del medicamento.
     */
    fun scheduleForMedication(medication: Medication) {
        if (!medication.isActive) return

        val now = System.currentTimeMillis()
        val endMillis = medication.endDate ?: (now + 7L * 24 * 60 * 60 * 1000)

        // Programar para los próximos 7 días como máximo
        val limitMillis = minOf(endMillis, now + 7L * 24 * 60 * 60 * 1000)

        val calendar = Calendar.getInstance()

        // Iterar día a día durante 7 días
        for (dayOffset in 0..6) {
            val checkCalendar = Calendar.getInstance().apply {
                timeInMillis = now
                add(Calendar.DAY_OF_YEAR, dayOffset)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            if (checkCalendar.timeInMillis > limitMillis) break
            if (checkCalendar.timeInMillis < medication.startDate - 86400000L) continue

            // Verificar si este día está en la frecuencia
            val shouldSchedule = when {
                FrequencyType.isDaily(medication.frequency) -> true
                medication.frequency.startsWith(FrequencyType.WEEKLY_PREFIX) -> {
                    val dow = checkCalendar.get(Calendar.DAY_OF_WEEK)
                    val normalizedDow = if (dow == Calendar.SUNDAY) 7 else dow - 1
                    normalizedDow in FrequencyType.getWeeklyDays(medication.frequency)
                }
                else -> false
            }

            if (!shouldSchedule) continue

            // Programar cada horario de ese día
            for (time in medication.scheduleTimes) {
                val parts = time.split(":")
                val hour = parts.getOrNull(0)?.toIntOrNull() ?: continue
                val minute = parts.getOrNull(1)?.toIntOrNull() ?: continue

                val alarmTime = Calendar.getInstance().apply {
                    timeInMillis = checkCalendar.timeInMillis
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis

                // No programar alarmas en el pasado
                if (alarmTime <= now) continue

                val requestCode = buildRequestCode(medication.id, alarmTime)
                scheduleExactAlarm(medication, alarmTime, requestCode)
            }
        }
    }

    /**
     * Programa una sola alarma exacta.
     */
    private fun scheduleExactAlarm(medication: Medication, triggerAtMillis: Long, requestCode: Int) {
        val intent = buildAlarmIntent(medication, triggerAtMillis, requestCode)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                // Fallback: alarma inexacta si no hay permiso
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    /**
     * Programa una alarma de posponer exactamente 10 minutos.
     * Evita duplicados usando el mismo requestCode con FLAG_UPDATE_CURRENT.
     */
    fun scheduleSnooze(medication: Medication, originalScheduledTime: Long) {
        val snoozeTime = System.currentTimeMillis() + 10L * 60 * 1000
        val requestCode = buildSnoozeRequestCode(medication.id, originalScheduledTime)
        scheduleExactAlarm(medication, snoozeTime, requestCode)
    }

    /**
     * Cancela todas las alarmas de un medicamento (programadas en 7 días).
     */
    fun cancelForMedication(medicationId: Int) {
        // Cancelar por los posibles request codes generados
        // Estrategia: generar los mismos códigos y cancelarlos
        val now = System.currentTimeMillis()
        for (dayOffset in 0..6) {
            val checkCalendar = Calendar.getInstance().apply {
                timeInMillis = now
                add(Calendar.DAY_OF_YEAR, dayOffset)
            }
            // Cancelar para las 24 posibles horas del día
            for (hour in 0..23) {
                for (minute in listOf(0, 15, 30, 45)) {
                    val alarmTime = Calendar.getInstance().apply {
                        timeInMillis = checkCalendar.timeInMillis
                        set(Calendar.HOUR_OF_DAY, hour)
                        set(Calendar.MINUTE, minute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                    cancelAlarmByRequestCode(buildRequestCode(medicationId, alarmTime))
                }
            }
        }
        // Cancelar posibles snoozes
        for (i in 0..10) {
            cancelAlarmByRequestCode(buildSnoozeRequestCode(medicationId, now - i * 60000))
        }
    }

    /**
     * Cancela una única alarma por request code.
     */
    fun cancelAlarmByRequestCode(requestCode: Int) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let { alarmManager.cancel(it) }
    }

    private fun buildAlarmIntent(
        medication: Medication,
        triggerAtMillis: Long,
        requestCode: Int
    ): Intent {
        return Intent(context, AlarmReceiver::class.java).apply {
            action = "com.dosis.app.ACTION_ALARM"
            putExtra(EXTRA_MEDICATION_ID, medication.id)
            putExtra(EXTRA_MEDICATION_NAME, medication.name)
            putExtra(EXTRA_MEDICATION_DOSAGE, medication.dosage)
            putExtra(EXTRA_SCHEDULED_TIME, triggerAtMillis)
            putExtra(EXTRA_ALARM_REQUEST_CODE, requestCode)
        }
    }

    /**
     * Genera un código único para cada combinación medicamento+tiempo.
     * Usa los últimos 20 bits del tiempo para combinarlos con el ID.
     */
    private fun buildRequestCode(medicationId: Int, timeMillis: Long): Int {
        val timePart = ((timeMillis / 60000) % 10000).toInt()
        return (medicationId * 10000 + timePart) and 0x7FFFFFFF
    }

    private fun buildSnoozeRequestCode(medicationId: Int, originalTime: Long): Int {
        return buildRequestCode(medicationId, originalTime) xor 0x01000000
    }
}
