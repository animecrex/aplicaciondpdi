package com.dosis.app.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dosis.app.data.model.FrequencyType
import com.dosis.app.data.model.Medication

/**
 * Tarjeta reutilizable para mostrar un medicamento en listas.
 */
@Composable
fun MedicationCard(
    medication: Medication,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ícono circular
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Medication,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            // Información
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = medication.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = medication.dosage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                // Horarios
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = medication.scheduleTimes.joinToString("  •  "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                // Frecuencia
                val freqText = if (FrequencyType.isDaily(medication.frequency)) {
                    "Todos los días"
                } else {
                    val days = FrequencyType.getWeeklyDays(medication.frequency)
                    val dayNames = listOf("", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
                    days.mapNotNull { dayNames.getOrNull(it) }.joinToString(", ")
                }
                Text(
                    text = freqText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            // Flecha
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = "Ver detalle",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
