package com.dosis.app.screens.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dosis.app.DosisApplication
import com.dosis.app.data.model.Medication
import com.dosis.app.data.model.FrequencyType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as DosisApplication).repository

    val allMedications: StateFlow<List<Medication>> = repository
        .getAllActiveMedications()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Medicamentos programados para hoy */
    val todayMedications: StateFlow<List<Medication>> = allMedications
        .map { medications -> medications.filter { isScheduledToday(it) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun isScheduledToday(medication: Medication): Boolean {
        val today = Calendar.getInstance()
        val now = today.timeInMillis

        // Verificar que esté dentro del rango de fechas
        if (now < medication.startDate) return false
        if (medication.endDate != null && now > medication.endDate) return false

        return when {
            FrequencyType.isDaily(medication.frequency) -> true
            medication.frequency.startsWith(FrequencyType.WEEKLY_PREFIX) -> {
                val todayDow = today.get(Calendar.DAY_OF_WEEK)
                // Android: 1=domingo, 2=lunes... convertimos a 1=lunes, 7=domingo
                val normalizedDow = if (todayDow == Calendar.SUNDAY) 7 else todayDow - 1
                val scheduledDays = FrequencyType.getWeeklyDays(medication.frequency)
                normalizedDow in scheduledDays
            }
            else -> false
        }
    }

    fun deleteMedication(medication: Medication, onAlarmCancel: (Int) -> Unit) {
        viewModelScope.launch {
            onAlarmCancel(medication.id)
            repository.deleteMedication(medication)
        }
    }
}
