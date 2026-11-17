package com.dosis.app.screens.wearable

import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dosis.app.wearable.WearableDataManagerImpl
import kotlinx.coroutines.launch
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WearableScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val wearableManager = remember { WearableDataManagerImpl(context.applicationContext) }
    val snackbarHostState = remember { SnackbarHostState() }

    var isPaired by remember { mutableStateOf(wearableManager.isDevicePaired()) }
    var deviceName by remember { mutableStateOf(wearableManager.getPairedDeviceName()) }
    var lastSyncTime by remember { mutableStateOf(wearableManager.getLastSyncTime()) }
    var isSyncing by remember { mutableStateOf(false) }
    var showScannerDialog by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Smartwatch / Wear OS", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = { showScannerDialog = true }) {
                        Icon(
                            imageVector = Icons.Filled.QrCodeScanner,
                            contentDescription = "Escanear QR",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── Estado de conexión ──────────────────────────────────────────
                ConnectionStatusCard(
                    isPaired = isPaired,
                    deviceName = deviceName,
                    lastSyncTime = lastSyncTime,
                    isSyncing = isSyncing,
                    onOpenScanner = { showScannerDialog = true },
                    onSyncNow = {
                        scope.launch {
                            isSyncing = true
                            val success = wearableManager.syncMedicationsToWatch("")
                            isSyncing = false
                            if (success) {
                                lastSyncTime = System.currentTimeMillis()
                                snackbarHostState.showSnackbar("Datos sincronizados con el reloj")
                            } else {
                                snackbarHostState.showSnackbar("No se pudo enviar la sincronización")
                            }
                        }
                    },
                    onUnpair = {
                        scope.launch {
                            wearableManager.unpairDevice()
                            isPaired = false
                            deviceName = null
                            lastSyncTime = null
                            snackbarHostState.showSnackbar("Smartwatch desvinculado")
                        }
                    }
                )

                // ── Información Wear OS ─────────────────────────────────────────
                WearOsInfoCard(
                    isPaired = isPaired
                )

                // ── Funciones de Wear OS ─────────────────────────────────────────
                FutureFeaturesCard()
            }

            // Diálogo de Escáner QR
            if (showScannerDialog) {
                QRScannerDialog(
                    onQrCodeScanned = { qrText ->
                        showScannerDialog = false
                        scope.launch {
                            val result = wearableManager.pairFromQrCode(qrText)
                            result.onSuccess { pairedName ->
                                isPaired = true
                                deviceName = pairedName
                                lastSyncTime = System.currentTimeMillis()
                                snackbarHostState.showSnackbar("¡Smartwatch '$pairedName' vinculado con éxito!")
                            }.onFailure {
                                snackbarHostState.showSnackbar("Error al leer el código QR: ${it.message}")
                            }
                        }
                    },
                    onDismiss = { showScannerDialog = false }
                )
            }
        }
    }
}

@Composable
private fun ConnectionStatusCard(
    isPaired: Boolean,
    deviceName: String?,
    lastSyncTime: Long?,
    isSyncing: Boolean,
    onOpenScanner: () -> Unit,
    onSyncNow: () -> Unit,
    onUnpair: () -> Unit
) {
    val formattedLastSync = remember(lastSyncTime) {
        if (lastSyncTime != null) {
            DateFormat.format("dd/MM/yyyy HH:mm", Date(lastSyncTime)).toString()
        } else {
            "Nunca"
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Watch,
                    contentDescription = null,
                    tint = if (isPaired) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Estado de la Vinculación",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(16.dp))

            // Indicador visual de estado
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(
                                if (isPaired)
                                    Color(0xFF2E7D32).copy(alpha = 0.12f)
                                else
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPaired) Icons.Filled.CheckCircle else Icons.Filled.WatchOff,
                            contentDescription = null,
                            tint = if (isPaired) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(42.dp)
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = if (isPaired) "Smartwatch Vinculado" else "Sin dispositivo vinculado",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isPaired) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (isPaired) "Conectado vía código QR y Data Layer" else "Escanea el código QR de tu reloj para vincular",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }

            Divider()
            Spacer(Modifier.height(12.dp))

            // Detalles de estado
            StatusRow(label = "Dispositivo", value = deviceName ?: "—")
            StatusRow(label = "Estado", value = if (isPaired) "Vinculado" else "No vinculado")
            StatusRow(label = "Última sincronización", value = formattedLastSync)

            Spacer(Modifier.height(16.dp))

            // Botones de acción
            if (!isPaired) {
                Button(
                    onClick = onOpenScanner,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = Icons.Filled.QrCodeScanner,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Escanear Código QR para Vincular", fontWeight = FontWeight.Bold)
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onSyncNow,
                        enabled = !isSyncing,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Filled.Sync, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Sincronizar")
                        }
                    }

                    OutlinedButton(
                        onClick = onUnpair,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Filled.LinkOff, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Desvincular")
                    }
                }
            }
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
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun WearOsInfoCard(
    isPaired: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = "Vinculación por Código QR",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (isPaired)
                        "Tu reloj está vinculado. Los medicamentos que agregues o modifiques en esta app se sincronizarán con tu smartwatch."
                    else
                        "Para vincular tu reloj, abre la aplicación Dosis en tu Smartwatch, toca el botón de 'Dosis' para generar el código QR y escanéalo con este celular.",
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
            Text(
                text = "Funciones habilitadas con Wear OS",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))

            val features = listOf(
                Icons.Filled.QrCode to "Vinculación rápida e instantánea con QR",
                Icons.Filled.Notifications to "Recibir recordatorios de dosis en el reloj",
                Icons.Filled.CheckCircle to "Marcar medicamentos como tomados desde el reloj",
                Icons.Filled.Sync to "Sincronización automática mediante Wearable Data Layer",
                Icons.Filled.Watch to "Visualizar próxima dosis en tiempo real"
            )
            features.forEach { (icon, text) ->
                Row(
                    modifier = Modifier.padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(text, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
