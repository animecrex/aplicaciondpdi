package com.dosis.app.screens.calendar

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dosis.app.DosisApplication
import com.dosis.app.data.model.FrequencyType
import com.dosis.app.data.model.Medication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar

class CalendarViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as DosisApplication).repository

    private val _selectedDate = MutableStateFlow(
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    )
    val selectedDate: StateFlow<Long> = _selectedDate.asStateFlow()

    private val _medicationsForDay = MutableStateFlow<List<Medication>>(emptyList())
    val medicationsForDay: StateFlow<List<Medication>> = _medicationsForDay.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllActiveMedications().collectLatest { all ->
                filterForDate(all, _selectedDate.value)
            }
        }
    }

    fun onDateSelected(dateMillis: Long) {
        _selectedDate.value = dateMillis
        viewModelScope.launch {
            val all = repository.getAllActiveMedicationsOnce()
            filterForDate(all, dateMillis)
        }
    }

    private fun filterForDate(medications: List<Medication>, dateMillis: Long) {
        val calendar = Calendar.getInstance().apply { timeInMillis = dateMillis }
        val dow = calendar.get(Calendar.DAY_OF_WEEK)
        val normalizedDow = if (dow == Calendar.SUNDAY) 7 else dow - 1

        val filtered = medications.filter { med ->
            if (dateMillis < med.startDate) return@filter false
            if (med.endDate != null && dateMillis > med.endDate) return@filter false
            when {
                FrequencyType.isDaily(med.frequency) -> true
                med.frequency.startsWith(FrequencyType.WEEKLY_PREFIX) -> {
                    normalizedDow in FrequencyType.getWeeklyDays(med.frequency)
                }
                else -> false
            }
        }
        _medicationsForDay.value = filtered
    }
}
