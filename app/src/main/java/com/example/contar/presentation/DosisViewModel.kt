package com.example.contar.presentation

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.example.contar.data.DosisItem
import com.example.contar.data.DosisRepository
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class DosisViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = DosisRepository(application)
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    var dosisList by mutableStateOf<List<DosisItem>>(emptyList())
        private set

    var currentScreen by mutableStateOf(DosisScreen.RESUMEN)
        private set

    var activeDosis by mutableStateOf<DosisItem?>(null)
        private set

    var lastTakenDosis by mutableStateOf<DosisItem?>(null)
        private set

    var isDarkTheme by mutableStateOf(true)
        private set

    var currentTimeString by mutableStateOf("08:00")
        private set

    init {
        loadData()
        updateCurrentTime()
    }

    fun loadData() {
        dosisList = repository.getDosisList()
        isDarkTheme = repository.isDarkTheme()
    }

    fun updateCurrentTime() {
        try {
            currentTimeString = LocalTime.now().format(timeFormatter)
        } catch (e: Exception) {
            currentTimeString = "08:00"
        }
    }

    fun navigateTo(screen: DosisScreen) {
        currentScreen = screen
    }

    fun openReminder(dosis: DosisItem? = null) {
        // Si no se especifica una dosis, abrir la siguiente dosis pendiente
        val target = dosis ?: getNextPendingDosis() ?: dosisList.firstOrNull()
        activeDosis = target
        currentScreen = DosisScreen.RECORDATORIO
    }

    fun openNewAlarm() {
        currentScreen = DosisScreen.NUEVA_ALARMA
    }

    fun takeActiveDosis() {
        val target = activeDosis ?: getNextPendingDosis() ?: return
        updateCurrentTime()
        val takenTime = currentTimeString
        dosisList = repository.markAsTaken(target.id, takenTime)
        lastTakenDosis = target.copy(tomada = true, horaTomada = takenTime)
        currentScreen = DosisScreen.CONFIRMACION
    }

    fun snoozeActiveDosis(minutes: Int = 10) {
        val target = activeDosis ?: return
        dosisList = repository.snoozeDosis(target.id, minutes)
        currentScreen = DosisScreen.RESUMEN
    }

    fun toggleTaken(dosis: DosisItem) {
        updateCurrentTime()
        dosisList = if (dosis.tomada) {
            repository.markAsUntaken(dosis.id)
        } else {
            repository.markAsTaken(dosis.id, currentTimeString)
        }
    }

    fun addNewDosis(
        nombre: String,
        dosis: String,
        instruccion: String = "Con un vaso de agua",
        hora: String,
        repeticion: String = "Cada día"
    ) {
        val newItem = DosisItem(
            nombre = nombre.ifBlank { "Medicamento" },
            dosis = dosis.ifBlank { "1 comprimido" },
            instruccion = instruccion,
            hora = hora,
            repeticion = repeticion,
            tomada = false,
            horaTomada = null
        )
        dosisList = repository.addDosis(newItem)
        currentScreen = DosisScreen.RESUMEN
    }

    fun deleteDosis(id: String) {
        dosisList = repository.deleteDosis(id)
        if (activeDosis?.id == id) {
            activeDosis = null
        }
    }

    fun resetToday() {
        dosisList = repository.resetDay()
    }

    fun resetToDefaults() {
        dosisList = repository.resetToDefaults()
    }

    fun toggleTheme() {
        isDarkTheme = !isDarkTheme
        repository.setDarkTheme(isDarkTheme)
    }

    /**
     * Encuentra la próxima dosis pendiente hoy.
     * Prioriza dosis a partir de la hora actual; si todas las pendientes son anteriores, toma la más antigua pendiente.
     */
    fun getNextPendingDosis(): DosisItem? {
        val pending = dosisList.filterNot { it.tomada }
        if (pending.isEmpty()) return null

        val now = try {
            LocalTime.now()
        } catch (e: Exception) {
            LocalTime.of(8, 0)
        }

        val upcoming = pending.filter {
            try {
                val itemTime = LocalTime.parse(it.hora, timeFormatter)
                !itemTime.isBefore(now)
            } catch (e: Exception) {
                true
            }
        }

        return upcoming.firstOrNull() ?: pending.firstOrNull()
    }

    /**
     * Calcula la etiqueta contextual para la próxima dosis:
     * Ej: "PRÓXIMA · EN 2 MIN", "PRÓXIMA · EN 12 H", "HORA DE TU TOMA", "HOY · 21:00"
     */
    fun getBadgeText(dosis: DosisItem): String {
        return try {
            val now = LocalTime.now()
            val itemTime = LocalTime.parse(dosis.hora, timeFormatter)
            val diffMinutes = (itemTime.toSecondOfDay() - now.toSecondOfDay()) / 60

            when {
                diffMinutes in 0..1 -> "HORA DE TU TOMA"
                diffMinutes in 2..59 -> "PRÓXIMA · EN $diffMinutes MIN"
                diffMinutes >= 60 -> {
                    val hours = diffMinutes / 60
                    "SIGUIENTE · EN $hours H"
                }
                diffMinutes in -30..-1 -> "PENDIENTE · HACE ${-diffMinutes} MIN"
                else -> "PENDIENTE · ${dosis.hora}"
            }
        } catch (e: Exception) {
            "PRÓXIMA · ${dosis.hora}"
        }
    }
}
