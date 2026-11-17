package com.dosis.app.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dosis.app.data.model.Medication
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(viewModel: CalendarViewModel = viewModel()) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val medications by viewModel.medicationsForDay.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendario", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                MonthCalendarView(
                    selectedDate = selectedDate,
                    onDateSelected = viewModel::onDateSelected
                )
            }

            item {
                val dateFormat = SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "MX"))
                val dateStr = dateFormat.format(Date(selectedDate)).replaceFirstChar { it.uppercase() }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.MedicalServices, null,
                        tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Medicamentos — $dateStr",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (medications.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            Modifier.fillMaxWidth().padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Filled.EventAvailable, null,
                                tint = MaterialTheme.colorScheme.primary.copy(0.4f),
                                modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("Sin medicamentos para este día",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                items(medications) { med ->
                    CalendarMedicationItem(med)
                }
            }
        }
    }
}

@Composable
private fun CalendarMedicationItem(medication: Medication) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(40.dp).clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Medication, null,
                    tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(medication.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(medication.dosage, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Schedule, null,
                        tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = medication.scheduleTimes.joinToString(" · "),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthCalendarView(
    selectedDate: Long,
    onDateSelected: (Long) -> Unit
) {
    val today = remember { Calendar.getInstance() }
    var displayedMonth by remember {
        mutableStateOf(Calendar.getInstance().apply {
            timeInMillis = selectedDate
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        })
    }

    val monthFormat = remember { SimpleDateFormat("MMMM yyyy", Locale("es", "MX")) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            // Encabezado mes
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    displayedMonth = (displayedMonth.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
                }) { Icon(Icons.Filled.ChevronLeft, "Mes anterior") }

                Text(
                    text = monthFormat.format(displayedMonth.time).replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = {
                    displayedMonth = (displayedMonth.clone() as Calendar).apply { add(Calendar.MONTH, 1) }
                }) { Icon(Icons.Filled.ChevronRight, "Mes siguiente") }
            }

            Spacer(Modifier.height(8.dp))

            // Días de la semana
            val dayHeaders = listOf("L", "M", "X", "J", "V", "S", "D")
            Row(Modifier.fillMaxWidth()) {
                dayHeaders.forEach { d ->
                    Text(d, modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(4.dp))

            // Días del mes
            val firstDayOfMonth = (displayedMonth.clone() as Calendar).apply {
                set(Calendar.DAY_OF_MONTH, 1)
            }
            val daysInMonth = displayedMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
            // Ajuste: lunes=0, domingo=6
            var firstDow = firstDayOfMonth.get(Calendar.DAY_OF_WEEK) - 2
            if (firstDow < 0) firstDow = 6

            val cells = firstDow + daysInMonth
            val rows = (cells + 6) / 7

            val selectedCal = Calendar.getInstance().apply { timeInMillis = selectedDate }
            val selYear = selectedCal.get(Calendar.YEAR)
            val selMonth = selectedCal.get(Calendar.MONTH)
            val selDay = selectedCal.get(Calendar.DAY_OF_MONTH)

            for (row in 0 until rows) {
                Row(Modifier.fillMaxWidth()) {
                    for (col in 0 until 7) {
                        val cellIndex = row * 7 + col
                        val dayNum = cellIndex - firstDow + 1
                        val isValid = dayNum in 1..daysInMonth

                        val isSelected = isValid &&
                                displayedMonth.get(Calendar.YEAR) == selYear &&
                                displayedMonth.get(Calendar.MONTH) == selMonth &&
                                dayNum == selDay

                        val isToday = isValid &&
                                displayedMonth.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                                displayedMonth.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                                dayNum == today.get(Calendar.DAY_OF_MONTH)

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isSelected -> MaterialTheme.colorScheme.primary
                                        isToday -> MaterialTheme.colorScheme.primaryContainer
                                        else -> androidx.compose.ui.graphics.Color.Transparent
                                    }
                                )
                                .then(
                                    if (isValid) Modifier.clickable {
                                        val cal = (displayedMonth.clone() as Calendar).apply {
                                            set(Calendar.DAY_OF_MONTH, dayNum)
                                        }
                                        onDateSelected(cal.timeInMillis)
                                    } else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isValid) {
                                Text(
                                    text = dayNum.toString(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = when {
                                        isSelected -> MaterialTheme.colorScheme.onPrimary
                                        isToday -> MaterialTheme.colorScheme.primary
                                        else -> MaterialTheme.colorScheme.onSurface
                                    },
                                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
