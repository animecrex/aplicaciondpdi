package com.dosis.app.notifications

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.dosis.app.DosisApplication
import com.dosis.app.MainActivity

/**
 * Helper para construir y mostrar notificaciones de medicamentos.
 * Incluye las acciones "Tomar" y "Posponer 10 min".
 */
object NotificationHelper {

    fun showMedicationReminder(
        context: Context,
        medicationId: Int,
        medicationName: String,
        dosage: String,
        scheduledTime: Long,
        notificationId: Int
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Intent para abrir la app al tocar la notificación
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context, notificationId, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // ── Acción: Marcar como tomado ──────────────────────────────────────
        val takenIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_MARK_TAKEN
            putExtra(NotificationActionReceiver.EXTRA_MEDICATION_ID, medicationId)
            putExtra(NotificationActionReceiver.EXTRA_MEDICATION_NAME, medicationName)
            putExtra(NotificationActionReceiver.EXTRA_MEDICATION_DOSAGE, dosage)
            putExtra(NotificationActionReceiver.EXTRA_SCHEDULED_TIME, scheduledTime)
            putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
        }
        val takenPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 1,
            takenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // ── Acción: Posponer 10 minutos ─────────────────────────────────────
        val snoozeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_SNOOZE
            putExtra(NotificationActionReceiver.EXTRA_MEDICATION_ID, medicationId)
            putExtra(NotificationActionReceiver.EXTRA_MEDICATION_NAME, medicationName)
            putExtra(NotificationActionReceiver.EXTRA_MEDICATION_DOSAGE, dosage)
            putExtra(NotificationActionReceiver.EXTRA_SCHEDULED_TIME, scheduledTime)
            putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(NotificationActionReceiver.EXTRA_ORIGINAL_REQUEST_CODE, notificationId)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 2,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // ── Construir notificación ──────────────────────────────────────────
        val notification = NotificationCompat.Builder(context, DosisApplication.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("💊 $medicationName")
            .setContentText("Es hora de tomar tu medicamento: $dosage")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Es hora de tomar:\n$medicationName — $dosage")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(false)
            .setOngoing(false)
            .setContentIntent(openAppPendingIntent)
            .addAction(
                android.R.drawable.checkbox_on_background,
                "✓ Tomado",
                takenPendingIntent
            )
            .addAction(
                android.R.drawable.ic_lock_idle_alarm,
                "⏱ Posponer 10 min",
                snoozePendingIntent
            )
            .build()

        notificationManager.notify(notificationId, notification)
    }

    fun cancelNotification(context: Context, notificationId: Int) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationId)
    }
}
