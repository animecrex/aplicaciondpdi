package com.dosis.app.screens.wearable

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Pantalla de estado del dispositivo wearable (Wear OS).
 * No simula ninguna conexión real.
 * Deja la estructura preparada para integración futura.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WearableScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dispositivo conectado", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Estado de conexión ──────────────────────────────────────────
            ConnectionStatusCard()

            // ── Información Wear OS ─────────────────────────────────────────
            WearOsInfoCard()

            // ── Funciones futuras ───────────────────────────────────────────
            FutureFeaturesCard()
        }
    }
}

@Composable
private fun ConnectionStatusCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Watch,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(12.dp))
                Text("Estado del dispositivo",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(16.dp))

            // Indicador visual de estado
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.WatchOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(44.dp))
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("Sin dispositivo conectado",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Ningún reloj Wear OS detectado",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.7f))
                }
            }

            Divider()
            Spacer(Modifier.height(12.dp))

            // Detalles de estado
            StatusRow(label = "Nombre del dispositivo", value = "—")
            StatusRow(label = "Estado de sincronización", value = "No sincronizado")
            StatusRow(label = "Última conexión", value = "Nunca")
        }
    }
}

@Composable
private fun StatusRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun WearOsInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Icon(Icons.Filled.Info, null,
                tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Integración Wear OS",
                    style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Esta sección está preparada para la futura integración con tu reloj " +
                    "inteligente compatible con Wear OS. Un compañero de equipo desarrollará " +
                    "la aplicación del reloj que se sincronizará con Dosis.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun FutureFeaturesCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Funciones futuras con Wear OS",
                style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(12.dp))

            val features = listOf(
                Icons.Filled.Notifications to "Recibir recordatorios en el reloj",
                Icons.Filled.CheckCircle to "Marcar medicamentos como tomados desde el reloj",
                Icons.Filled.Sync to "Sincronizar medicamentos automáticamente",
                Icons.Filled.Vibration to "Vibración personalizada en el reloj",
                Icons.Filled.Watch to "Visualizar próximo medicamento en el reloj"
            )
            features.forEach { (icon, text) ->
                Row(
                    modifier = Modifier.padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(icon, null, tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(text, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
