package com.dosis.app.screens.add

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dosis.app.DosisApplication
import com.dosis.app.alarms.AlarmScheduler
import com.dosis.app.data.model.FrequencyType
import com.dosis.app.data.model.Medication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

data class MedicationFormState(
    val name: String = "",
    val dosage: String = "",
    val startDate: Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis,
    val endDate: Long? = null,
    val isDaily: Boolean = true,
    val selectedDays: Set<Int> = emptySet(), // 1=lun ... 7=dom
    val scheduleTimes: List<String> = listOf("08:00"),
    val notes: String = "",
    // Errores de validación
    val nameError: String? = null,
    val dosageError: String? = null,
    val timesError: String? = null,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false
)

class AddMedicationViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as DosisApplication).repository
    private val alarmScheduler = AlarmScheduler(application)

    private val _formState = MutableStateFlow(MedicationFormState())
    val formState: StateFlow<MedicationFormState> = _formState.asStateFlow()

    fun onNameChange(value: String) {
        _formState.value = _formState.value.copy(name = value, nameError = null)
    }

    fun onDosageChange(value: String) {
        _formState.value = _formState.value.copy(dosage = value, dosageError = null)
    }

    fun onStartDateChange(value: Long) {
        _formState.value = _formState.value.copy(startDate = value)
    }

    fun onEndDateChange(value: Long?) {
        _formState.value = _formState.value.copy(endDate = value)
    }

    fun onFrequencyChange(isDaily: Boolean) {
        _formState.value = _formState.value.copy(isDaily = isDaily)
    }

    fun onDayToggle(day: Int) {
        val current = _formState.value.selectedDays.toMutableSet()
        if (current.contains(day)) current.remove(day) else current.add(day)
        _formState.value = _formState.value.copy(selectedDays = current)
    }

    fun onAddTime(time: String) {
        val times = _formState.value.scheduleTimes.toMutableList()
        if (!times.contains(time)) {
            times.add(time)
            times.sort()
        }
        _formState.value = _formState.value.copy(scheduleTimes = times, timesError = null)
    }

    fun onRemoveTime(time: String) {
        val times = _formState.value.scheduleTimes.toMutableList()
        times.remove(time)
        _formState.value = _formState.value.copy(scheduleTimes = times)
    }

    fun onNotesChange(value: String) {
        _formState.value = _formState.value.copy(notes = value)
    }

    private fun validate(): Boolean {
        val state = _formState.value
        var valid = true
        var newState = state

        if (state.name.isBlank()) {
            newState = newState.copy(nameError = "El nombre es obligatorio")
            valid = false
        }
        if (state.dosage.isBlank()) {
            newState = newState.copy(dosageError = "La dosis es obligatoria")
            valid = false
        }
        if (state.scheduleTimes.isEmpty()) {
            newState = newState.copy(timesError = "Agrega al menos un horario")
            valid = false
        }
        if (!state.isDaily && state.selectedDays.isEmpty()) {
            newState = newState.copy(timesError = "Selecciona al menos un día")
            valid = false
        }
        _formState.value = newState
        return valid
    }

    fun saveMedication() {
        if (!validate()) return
        val state = _formState.value
        _formState.value = state.copy(isSaving = true)

        viewModelScope.launch {
            val frequency = if (state.isDaily) {
                FrequencyType.DAILY
            } else {
                FrequencyType.buildWeeklyFrequency(state.selectedDays.toList())
            }

            val medication = Medication(
                name = state.name.trim(),
                dosage = state.dosage.trim(),
                startDate = state.startDate,
                endDate = state.endDate,
                frequency = frequency,
                scheduleTimes = state.scheduleTimes,
                notes = state.notes.trim()
            )

            val id = repository.insertMedication(medication)
            // Programar alarmas para el medicamento recién creado
            alarmScheduler.scheduleForMedication(medication.copy(id = id.toInt()))

            _formState.value = _formState.value.copy(isSaving = false, saveSuccess = true)
        }
    }

    /** Carga un medicamento existente para edición */
    fun loadMedication(medicationId: Int) {
        viewModelScope.launch {
            val medication = repository.getMedicationById(medicationId) ?: return@launch
            val isDaily = FrequencyType.isDaily(medication.frequency)
            val days = if (!isDaily) {
                FrequencyType.getWeeklyDays(medication.frequency).toSet()
            } else emptySet()

            _formState.value = MedicationFormState(
                name = medication.name,
                dosage = medication.dosage,
                startDate = medication.startDate,
                endDate = medication.endDate,
                isDaily = isDaily,
                selectedDays = days,
                scheduleTimes = medication.scheduleTimes,
                notes = medication.notes
            )
            _editingMedicationId = medicationId
        }
    }

    private var _editingMedicationId: Int? = null
    val editingMedicationId: Int? get() = _editingMedicationId

    fun updateMedication() {
        val id = _editingMedicationId ?: return
        if (!validate()) return
        val state = _formState.value
        _formState.value = state.copy(isSaving = true)

        viewModelScope.launch {
            val frequency = if (state.isDaily) FrequencyType.DAILY
            else FrequencyType.buildWeeklyFrequency(state.selectedDays.toList())

            val medication = Medication(
                id = id,
                name = state.name.trim(),
                dosage = state.dosage.trim(),
                startDate = state.startDate,
                endDate = state.endDate,
                frequency = frequency,
                scheduleTimes = state.scheduleTimes,
                notes = state.notes.trim()
            )
            // Cancelar alarmas anteriores y reprogramar
            alarmScheduler.cancelForMedication(id)
            repository.updateMedication(medication)
            alarmScheduler.scheduleForMedication(medication)

            _formState.value = _formState.value.copy(isSaving = false, saveSuccess = true)
        }
    }

    fun deleteMedication(onDone: () -> Unit) {
        val id = _editingMedicationId ?: return
        viewModelScope.launch {
            alarmScheduler.cancelForMedication(id)
            repository.deleteMedicationById(id)
            onDone()
        }
    }
}
