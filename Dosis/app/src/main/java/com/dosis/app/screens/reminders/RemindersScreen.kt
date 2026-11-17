package com.dosis.app.screens.reminders

import androidx.compose.foundation.background
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
import com.dosis.app.data.model.Medication

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(viewModel: RemindersViewModel = viewModel()) {
    val medications by viewModel.allMedications.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recordatorios", fontWeight = FontWeight.Bold) },
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
                // Tarjeta informativa sobre notificaciones
                NotificationInfoCard()
            }

            item {
                Text(
                    "Todos los medicamentos (${medications.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
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
                            Icon(Icons.Filled.NotificationsOff, null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.4f),
                                modifier = Modifier.size(56.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("No hay medicamentos registrados",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                items(medications, key = { it.id }) { med ->
                    ReminderMedicationCard(medication = med)
                }
            }
        }
    }
}

@Composable
private fun NotificationInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Icon(Icons.Filled.Notifications, null,
                tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Notificaciones activas",
                    style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Recibirás una notificación a la hora programada. " +
                    "Puedes marcarla como tomada o posponerla 10 minutos desde la notificación.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ReminderMedicationCard(medication: Medication) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
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
                    Text(medication.name,
                        style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text(medication.dosage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                // Badge activo
                Badge(containerColor = MaterialTheme.colorScheme.primary) {
                    Text("Activo", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary)
                }
            }
            Spacer(Modifier.height(12.dp))
            Divider()
            Spacer(Modifier.height(8.dp))
            // Lista de horarios con chip
            Text("Horarios:", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            medication.scheduleTimes.forEach { time ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Icon(Icons.Filled.Alarm, null,
                        tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(time, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.width(8.dp))
                    Text("• alarma programada",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
