package com.dosis.app.screens.home

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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dosis.app.components.MedicationCard
import com.dosis.app.data.model.Medication
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddMedication: () -> Unit,
    onMedicationClick: (Int) -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val todayMedications by viewModel.todayMedications.collectAsState()

    val dateFormat = remember { SimpleDateFormat("EEEE, d 'de' MMMM 'de' yyyy", Locale("es", "MX")) }
    val today = remember { dateFormat.format(Date()).replaceFirstChar { it.uppercase() } }

    // Próximo medicamento (el que tiene el horario más cercano en el futuro)
    val nextMedication = remember(todayMedications) {
        val now = Calendar.getInstance()
        val nowMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        todayMedications
            .flatMap { med ->
                med.scheduleTimes.map { time ->
                    val parts = time.split(":")
                    val h = parts.getOrNull(0)?.toIntOrNull() ?: 0
                    val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
                    Triple(med, time, h * 60 + m)
                }
            }
            .filter { (_, _, totalMin) -> totalMin >= nowMinutes }
            .minByOrNull { (_, _, totalMin) -> totalMin }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Dosis",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = today,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                actions = {
                    IconButton(onClick = onAddMedication) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = "Agregar medicamento",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddMedication,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Agregar medicamento")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Saludo ──────────────────────────────────────────────────────
            item {
                GreetingCard()
            }

            // ── Próximo medicamento ─────────────────────────────────────────
            item {
                if (nextMedication != null) {
                    NextMedicationCard(
                        medication = nextMedication.first,
                        time = nextMedication.second
                    )
                }
            }

            // ── Encabezado lista ────────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.MedicalServices,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Medicamentos de hoy (${todayMedications.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // ── Lista de medicamentos ───────────────────────────────────────
            if (todayMedications.isEmpty()) {
                item {
                    EmptyMedicationsCard(onAddMedication)
                }
            } else {
                items(todayMedications, key = { it.id }) { medication ->
                    MedicationCard(
                        medication = medication,
                        onClick = { onMedicationClick(medication.id) }
                    )
                }
            }

            // Espacio para el FAB
            item { Spacer(Modifier.height(72.dp)) }
        }
    }
}

@Composable
private fun GreetingCard() {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when (hour) {
        in 0..11 -> "Buenos días 🌅"
        in 12..17 -> "Buenas tardes ☀️"
        else -> "Buenas noches 🌙"
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.HealthAndSafety,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Tu salud es nuestra prioridad",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun NextMedicationCard(medication: Medication, time: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Alarm,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Próximo recordatorio",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = medication.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Text(
                text = medication.dosage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = time,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
private fun EmptyMedicationsCard(onAddMedication: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Filled.MedicationLiquid,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Sin medicamentos para hoy",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Agrega tu primer medicamento para comenzar",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onAddMedication,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Agregar medicamento")
            }
        }
    }
}
