package com.dosis.app.screens.add

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicationScreen(
    onNavigateBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: AddMedicationViewModel = viewModel()
) {
    val state by viewModel.formState.collectAsState()

    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) onSaved()
    }

    MedicationFormScaffold(
        title = "Agregar medicamento",
        state = state,
        onNavigateBack = onNavigateBack,
        onNameChange = viewModel::onNameChange,
        onDosageChange = viewModel::onDosageChange,
        onStartDateChange = viewModel::onStartDateChange,
        onEndDateChange = viewModel::onEndDateChange,
        onFrequencyChange = viewModel::onFrequencyChange,
        onDayToggle = viewModel::onDayToggle,
        onAddTime = viewModel::onAddTime,
        onRemoveTime = viewModel::onRemoveTime,
        onNotesChange = viewModel::onNotesChange,
        onSave = viewModel::saveMedication,
        showDelete = false,
        onDelete = {}
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMedicationScreen(
    medicationId: Int,
    onNavigateBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: AddMedicationViewModel = viewModel()
) {
    val state by viewModel.formState.collectAsState()

    LaunchedEffect(medicationId) {
        viewModel.loadMedication(medicationId)
    }
    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) onSaved()
    }

    MedicationFormScaffold(
        title = "Editar medicamento",
        state = state,
        onNavigateBack = onNavigateBack,
        onNameChange = viewModel::onNameChange,
        onDosageChange = viewModel::onDosageChange,
        onStartDateChange = viewModel::onStartDateChange,
        onEndDateChange = viewModel::onEndDateChange,
        onFrequencyChange = viewModel::onFrequencyChange,
        onDayToggle = viewModel::onDayToggle,
        onAddTime = viewModel::onAddTime,
        onRemoveTime = viewModel::onRemoveTime,
        onNotesChange = viewModel::onNotesChange,
        onSave = viewModel::updateMedication,
        showDelete = true,
        onDelete = { viewModel.deleteMedication(onSaved) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MedicationFormScaffold(
    title: String,
    state: MedicationFormState,
    onNavigateBack: () -> Unit,
    onNameChange: (String) -> Unit,
    onDosageChange: (String) -> Unit,
    onStartDateChange: (Long) -> Unit,
    onEndDateChange: (Long?) -> Unit,
    onFrequencyChange: (Boolean) -> Unit,
    onDayToggle: (Int) -> Unit,
    onAddTime: (String) -> Unit,
    onRemoveTime: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onSave: () -> Unit,
    showDelete: Boolean,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar medicamento") },
            text = { Text("¿Estás seguro de que deseas eliminar este medicamento? Se cancelarán todas sus alarmas.") },
            confirmButton = {
                TextButton(onClick = { showDeleteDialog = false; onDelete() }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, "Regresar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    if (showDelete) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Filled.Delete, "Eliminar", tint = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Nombre ────────────────────────────────────────────────────
            item {
                SectionCard(title = "Información del medicamento") {
                    OutlinedTextField(
                        value = state.name,
                        onValueChange = onNameChange,
                        label = { Text("Nombre del medicamento *") },
                        leadingIcon = { Icon(Icons.Filled.Medication, null) },
                        isError = state.nameError != null,
                        supportingText = state.nameError?.let { { Text(it) } },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.dosage,
                        onValueChange = onDosageChange,
                        label = { Text("Dosis *  (ej: 1 tableta, 5ml)") },
                        leadingIcon = { Icon(Icons.Filled.LocalPharmacy, null) },
                        isError = state.dosageError != null,
                        supportingText = state.dosageError?.let { { Text(it) } },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }

            // ── Fechas ────────────────────────────────────────────────────
            item {
                SectionCard(title = "Período de tratamiento") {
                    // Fecha de inicio
                    DatePickerField(
                        label = "Fecha de inicio *",
                        dateMillis = state.startDate,
                        onDateSelected = onStartDateChange,
                        dateFormat = dateFormat
                    )
                    Spacer(Modifier.height(8.dp))
                    // Fecha de finalización (opcional)
                    var hasEndDate by remember { mutableStateOf(state.endDate != null) }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = hasEndDate,
                            onCheckedChange = {
                                hasEndDate = it
                                if (!it) onEndDateChange(null)
                            }
                        )
                        Text("Tiene fecha de finalización", style = MaterialTheme.typography.bodyMedium)
                    }
                    if (hasEndDate) {
                        DatePickerField(
                            label = "Fecha de finalización",
                            dateMillis = state.endDate ?: state.startDate,
                            onDateSelected = { onEndDateChange(it) },
                            dateFormat = dateFormat
                        )
                    }
                }
            }

            // ── Frecuencia ─────────────────────────────────────────────────
            item {
                SectionCard(title = "Frecuencia") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = state.isDaily,
                            onClick = { onFrequencyChange(true) },
                            label = { Text("Todos los días") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = !state.isDaily,
                            onClick = { onFrequencyChange(false) },
                            label = { Text("Días específicos") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (!state.isDaily) {
                        Spacer(Modifier.height(8.dp))
                        DaySelector(
                            selectedDays = state.selectedDays,
                            onDayToggle = onDayToggle
                        )
                    }
                }
            }

            // ── Horarios ───────────────────────────────────────────────────
            item {
                SectionCard(title = "Horarios de administración") {
                    state.timesError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(4.dp))
                    }
                    state.scheduleTimes.forEach { time ->
                        TimeChip(
                            time = time,
                            onRemove = { onRemoveTime(time) }
                        )
                        Spacer(Modifier.height(4.dp))
                    }
                    Spacer(Modifier.height(8.dp))
                    AddTimeButton(onAddTime = onAddTime)
                }
            }

            // ── Notas ──────────────────────────────────────────────────────
            item {
                SectionCard(title = "Notas adicionales") {
                    OutlinedTextField(
                        value = state.notes,
                        onValueChange = onNotesChange,
                        label = { Text("Notas (opcional)") },
                        leadingIcon = { Icon(Icons.Filled.Notes, null) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4
                    )
                }
            }

            // ── Botón guardar ──────────────────────────────────────────────
            item {
                Button(
                    onClick = onSave,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !state.isSaving
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Filled.Save, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Guardar medicamento", style = MaterialTheme.typography.labelLarge)
                    }
                }
                Spacer(Modifier.height(72.dp))
            }
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun DatePickerField(
    label: String,
    dateMillis: Long,
    onDateSelected: (Long) -> Unit,
    dateFormat: SimpleDateFormat
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance().apply { timeInMillis = dateMillis }

    OutlinedTextField(
        value = dateFormat.format(Date(dateMillis)),
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        leadingIcon = { Icon(Icons.Filled.CalendarMonth, null) },
        trailingIcon = {
            IconButton(onClick = {
                DatePickerDialog(
                    context,
                    { _, year, month, day ->
                        val cal = Calendar.getInstance()
                        cal.set(year, month, day, 0, 0, 0)
                        cal.set(Calendar.MILLISECOND, 0)
                        onDateSelected(cal.timeInMillis)
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }) {
                Icon(Icons.Filled.EditCalendar, null)
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun DaySelector(selectedDays: Set<Int>, onDayToggle: (Int) -> Unit) {
    val dayLabels = listOf(1 to "L", 2 to "M", 3 to "X", 4 to "J", 5 to "V", 6 to "S", 7 to "D")
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        dayLabels.forEach { (day, label) ->
            FilterChip(
                selected = day in selectedDays,
                onClick = { onDayToggle(day) },
                label = { Text(label) }
            )
        }
    }
}

@Composable
private fun TimeChip(time: String, onRemove: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AssistChip(
            onClick = {},
            label = { Text(time, style = MaterialTheme.typography.bodyMedium) },
            leadingIcon = { Icon(Icons.Filled.Schedule, null, modifier = Modifier.size(16.dp)) },
            trailingIcon = {
                IconButton(onClick = onRemove, modifier = Modifier.size(20.dp)) {
                    Icon(Icons.Filled.Close, "Eliminar horario", modifier = Modifier.size(16.dp))
                }
            }
        )
    }
}

@Composable
private fun AddTimeButton(onAddTime: (String) -> Unit) {
    val context = LocalContext.current
    val cal = Calendar.getInstance()
    OutlinedButton(
        onClick = {
            TimePickerDialog(
                context,
                { _, hour, minute ->
                    onAddTime(String.format("%02d:%02d", hour, minute))
                },
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                true
            ).show()
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(Icons.Filled.AddAlarm, null)
        Spacer(Modifier.width(8.dp))
        Text("Agregar horario")
    }
}
