package com.example.contar.presentation

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.example.contar.data.DosisItem
import com.example.contar.presentation.theme.ContarTheme
import com.example.contar.presentation.theme.LocalDosisColors
import kotlinx.coroutines.delay
import java.util.Locale

enum class DosisScreen {
    RESUMEN,
    RECORDATORIO,
    CONFIRMACION,
    NUEVA_ALARMA
}

enum class PickerType {
    MEDICAMENTO,
    DOSIS,
    HORA
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            val viewModel = remember { DosisViewModel(context.applicationContext as Application) }
            DosisApp(viewModel = viewModel)
        }
    }
}

@Composable
fun DosisApp(viewModel: DosisViewModel? = null) {
    val context = LocalContext.current
    val vm = viewModel ?: remember { DosisViewModel(context.applicationContext as Application) }
    val haptic = LocalHapticFeedback.current

    // Actualizador en tiempo real del reloj del sistema (cada segundo)
    LaunchedEffect(Unit) {
        while (true) {
            vm.updateCurrentTime()
            delay(1000)
        }
    }

    ContarTheme(darkTheme = vm.isDarkTheme) {
        val colors = LocalDosisColors.current
        AppScaffold {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.background)
            ) {
                when (vm.currentScreen) {
                    DosisScreen.RESUMEN -> {
                        val nextDosis = vm.getNextPendingDosis()
                        val badgeText = if (nextDosis != null) vm.getBadgeText(nextDosis) else "TODO AL DÍA"

                        ResumenDelDiaScreen(
                            dosisList = vm.dosisList,
                            nextDosis = nextDosis,
                            badgeText = badgeText,
                            time = vm.currentTimeString,
                            onOpenReminder = { dosis ->
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                vm.openReminder(dosis)
                            },
                            onToggleTaken = { dosis ->
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                vm.toggleTaken(dosis)
                            },
                            onOpenNewAlarm = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                vm.openNewAlarm()
                            },
                            onToggleTheme = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                vm.toggleTheme()
                            },
                            onResetDay = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                vm.resetToday()
                            }
                        )
                    }

                    DosisScreen.RECORDATORIO -> {
                        RecordatorioDosisScreen(
                            dosis = vm.activeDosis ?: vm.getNextPendingDosis(),
                            time = vm.currentTimeString,
                            onTakeDose = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                vm.takeActiveDosis()
                            },
                            onSnooze = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                vm.snoozeActiveDosis(10)
                            },
                            onToggleTheme = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                vm.toggleTheme()
                            },
                            onBack = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                vm.navigateTo(DosisScreen.RESUMEN)
                            }
                        )
                    }

                    DosisScreen.CONFIRMACION -> {
                        ConfirmacionTomaScreen(
                            takenDosis = vm.lastTakenDosis,
                            nextDosis = vm.getNextPendingDosis(),
                            dosisList = vm.dosisList,
                            time = vm.currentTimeString,
                            onBackToHome = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                vm.navigateTo(DosisScreen.RESUMEN)
                            },
                            onToggleTheme = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                vm.toggleTheme()
                            }
                        )
                    }

                    DosisScreen.NUEVA_ALARMA -> {
                        NuevaAlarmaScreen(
                            time = vm.currentTimeString,
                            onBack = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                vm.navigateTo(DosisScreen.RESUMEN)
                            },
                            onSave = { nombre, dosis, instruccion, hora, repeticion ->
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                vm.addNewDosis(
                                    nombre = nombre,
                                    dosis = dosis,
                                    instruccion = instruccion,
                                    hora = hora,
                                    repeticion = repeticion
                                )
                            },
                            onToggleTheme = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                vm.toggleTheme()
                            }
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// PANTALLA 1: Resumen del día (Completamente funcional y reactiva)
// -------------------------------------------------------------
@Composable
fun ResumenDelDiaScreen(
    dosisList: List<DosisItem> = emptyList(),
    nextDosis: DosisItem? = null,
    badgeText: String = "PRÓXIMA · EN 2 MIN",
    time: String = "07:58",
    onOpenReminder: (DosisItem) -> Unit = {},
    onToggleTaken: (DosisItem) -> Unit = {},
    onOpenNewAlarm: () -> Unit = {},
    onToggleTheme: () -> Unit = {},
    onResetDay: () -> Unit = {}
) {
    val isRound = LocalConfiguration.current.isScreenRound
    val colors = LocalDosisColors.current
    val scrollState = rememberScrollState()

    val screenHorizontalPadding = if (isRound) 14.dp else 12.dp
    val headerHorizontalPadding = if (isRound) 14.dp else 4.dp
    val listHorizontalPadding = if (isRound) 10.dp else 4.dp
    val bottomSpacerHeight = if (isRound) 38.dp else 12.dp

    val totalDosis = dosisList.size
    val tomadasCount = dosisList.count { it.tomada }
    val allCompleted = totalDosis > 0 && tomadasCount == totalDosis

    ScreenScaffold(scrollState = scrollState) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = screenHorizontalPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DosisTopBar(time = time, onToggleTheme = onToggleTheme)

            Spacer(modifier = Modifier.height(if (isRound) 2.dp else 4.dp))

            // Encabezado "Hoy" + Contador de dosis reactivo
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = headerHorizontalPadding),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "Hoy",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Text(
                    text = if (totalDosis > 0) "$tomadasCount de $totalDosis tomadas" else "Sin dosis",
                    fontSize = 11.sp,
                    color = if (allCompleted) colors.brandGreen else colors.textSecondary,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tarjeta destacada: Próxima dosis o estado de completado
            if (nextDosis != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(if (isRound) 0.94f else 1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(colors.nextDoseCardBg)
                        .then(
                            if (colors.isDark) {
                                Modifier.border(
                                    BorderStroke(1.dp, colors.nextDoseCardBorder),
                                    RoundedCornerShape(18.dp)
                                )
                            } else Modifier
                        )
                        .clickable { onOpenReminder(nextDosis) }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = badgeText.uppercase(Locale.getDefault()),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.nextDoseBadgeText,
                                letterSpacing = 0.5.sp
                            )
                            ChevronRightIcon(
                                modifier = Modifier.size(9.dp),
                                color = colors.nextDoseBadgeText
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = nextDosis.hora,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.nextDoseTimeText
                        )

                        Spacer(modifier = Modifier.height(1.dp))

                        Text(
                            text = "${nextDosis.nombre} · ${nextDosis.dosis}",
                            fontSize = 13.sp,
                            color = colors.nextDoseSubText
                        )
                    }
                }
            } else if (allCompleted) {
                // Todas tomadas hoy
                Box(
                    modifier = Modifier
                        .fillMaxWidth(if (isRound) 0.94f else 1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(colors.nextDoseCardBg)
                        .clickable { onResetDay() }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CheckCircleIcon(
                            modifier = Modifier.size(24.dp),
                            circleColor = colors.chipBg,
                            checkColor = colors.chipText
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "¡TODO AL DÍA!",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.brandGreen
                            )
                            Text(
                                text = "Todas las tomas hechas",
                                fontSize = 12.sp,
                                color = colors.textPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Lista interactiva de todas las tomas del día
            if (dosisList.isEmpty()) {
                Text(
                    text = "No tienes alarmas programadas.\nToca abajo para agregar una.",
                    fontSize = 12.sp,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                dosisList.forEach { dosis ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                if (!dosis.tomada) {
                                    onOpenReminder(dosis)
                                } else {
                                    onToggleTaken(dosis)
                                }
                            }
                            .padding(horizontal = listHorizontalPadding, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (dosis.tomada) {
                            CheckCircleIcon(
                                modifier = Modifier.size(18.dp),
                                circleColor = colors.chipBg,
                                checkColor = colors.chipText
                            )
                        } else {
                            ClockIcon(
                                modifier = Modifier.size(18.dp),
                                color = colors.textSecondary
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = dosis.nombre,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.textPrimary
                            )
                            Text(
                                text = dosis.dosis,
                                fontSize = 11.sp,
                                color = colors.textSecondary
                            )
                        }
                        Text(
                            text = if (dosis.tomada) (dosis.horaTomada ?: dosis.hora) else dosis.hora,
                            fontSize = 12.sp,
                            color = if (dosis.tomada) colors.brandGreen else colors.textSecondary,
                            fontWeight = if (dosis.tomada) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Botón rápido para agregar nueva alarma ("+ Nueva alarma")
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(colors.chipBg)
                    .clickable { onOpenNewAlarm() }
                    .padding(horizontal = 12.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PlusIcon(
                    modifier = Modifier.size(10.dp),
                    color = colors.chipText
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "Nueva alarma",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.chipText
                )
            }

            // Si todas están completadas, permitir reiniciar el día con un toque
            if (allCompleted) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Reiniciar tomas de hoy",
                    fontSize = 10.sp,
                    color = colors.textSecondary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable { onResetDay() }
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }

            Spacer(modifier = Modifier.height(bottomSpacerHeight))
        }
    }
}

// -------------------------------------------------------------
// PANTALLA 2: Recordatorio de dosis dinámico (Toma / Posponer)
// -------------------------------------------------------------
@Composable
fun RecordatorioDosisScreen(
    dosis: DosisItem? = null,
    time: String = "08:00",
    onTakeDose: () -> Unit = {},
    onSnooze: () -> Unit = {},
    onToggleTheme: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val isRound = LocalConfiguration.current.isScreenRound
    val colors = LocalDosisColors.current
    val scrollState = rememberScrollState()

    val screenHorizontalPadding = if (isRound) 16.dp else 14.dp
    val primaryButtonWidthFraction = if (isRound) 0.90f else 1f
    val secondaryButtonWidthFraction = if (isRound) 0.86f else 1f
    val bottomSpacerHeight = if (isRound) 36.dp else 10.dp

    val nombre = dosis?.nombre ?: "Losartán"
    val detalleDosis = dosis?.dosis ?: "50 mg · 1 comprimido"
    val instruccion = dosis?.instruccion ?: "Con un vaso de agua"

    ScreenScaffold(scrollState = scrollState) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = screenHorizontalPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DosisTopBar(time = time, onToggleTheme = onToggleTheme)

            Spacer(modifier = Modifier.height(if (isRound) 4.dp else 6.dp))

            // Indicador de hora con campana
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                BellIcon(
                    modifier = Modifier.size(14.dp),
                    color = colors.brandGreen
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Hora de tu toma",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.brandGreen
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Nombre del medicamento
            Text(
                text = nombre,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Detalle de dosis
            Text(
                text = detalleDosis,
                fontSize = 13.sp,
                color = colors.textSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Chip instructivo dinámico
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(colors.chipBg)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WaterGlassIcon(
                        modifier = Modifier.size(12.dp),
                        color = colors.chipText
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = instruccion,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.chipText
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Botón principal: "✓ Tomar ahora"
            Box(
                modifier = Modifier
                    .fillMaxWidth(primaryButtonWidthFraction)
                    .height(40.dp)
                    .clip(RoundedCornerShape(50))
                    .background(colors.primaryButtonBg)
                    .clickable { onTakeDose() },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CheckIcon(
                        modifier = Modifier.size(14.dp),
                        color = colors.primaryButtonText,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Tomar ahora",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.primaryButtonText
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Botón secundario: "⏰ Posponer 10 min"
            Box(
                modifier = Modifier
                    .fillMaxWidth(secondaryButtonWidthFraction)
                    .height(36.dp)
                    .clip(RoundedCornerShape(50))
                    .background(colors.secondaryButtonBg)
                    .then(
                        if (!colors.isDark) {
                            Modifier.border(
                                BorderStroke(1.dp, Color(0xFFE2E8E4)),
                                RoundedCornerShape(50)
                            )
                        } else Modifier
                    )
                    .clickable { onSnooze() },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AlarmClockIcon(
                        modifier = Modifier.size(13.dp),
                        color = colors.secondaryButtonText
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Posponer 10 min",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.secondaryButtonText
                    )
                }
            }

            Spacer(modifier = Modifier.height(bottomSpacerHeight))
        }
    }
}

// -------------------------------------------------------------
// PANTALLA 3: Confirmación de toma (Datos calculados dinámicamente)
// -------------------------------------------------------------
@Composable
fun ConfirmacionTomaScreen(
    takenDosis: DosisItem? = null,
    nextDosis: DosisItem? = null,
    dosisList: List<DosisItem> = emptyList(),
    time: String = "08:01",
    onBackToHome: () -> Unit = {},
    onToggleTheme: () -> Unit = {}
) {
    val isRound = LocalConfiguration.current.isScreenRound
    val colors = LocalDosisColors.current
    val scrollState = rememberScrollState()

    val screenHorizontalPadding = if (isRound) 16.dp else 14.dp
    val nextDoseWidthFraction = if (isRound) 0.92f else 1f
    val buttonWidthFraction = if (isRound) 0.88f else 1f
    val bottomSpacerHeight = if (isRound) 36.dp else 10.dp

    val nombre = takenDosis?.nombre ?: "Medicamento"
    val dosisTexto = takenDosis?.dosis ?: "50 mg"
    val horaRegistrada = takenDosis?.horaTomada ?: time
    val tomadas = dosisList.count { it.tomada }
    val total = dosisList.size

    ScreenScaffold(scrollState = scrollState) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = screenHorizontalPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DosisTopBar(time = time, onToggleTheme = onToggleTheme)

            Spacer(modifier = Modifier.height(if (isRound) 2.dp else 4.dp))

            // Círculo grande con check verde
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(colors.successCircleBg),
                contentAlignment = Alignment.Center
            ) {
                CheckIcon(
                    modifier = Modifier.size(20.dp),
                    color = colors.successCheckColor,
                    strokeWidth = 2.4.dp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Título de confirmación
            Text(
                text = "¡Toma registrada!",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "$nombre · $dosisTexto",
                fontSize = 13.sp,
                color = colors.textSecondary
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "$horaRegistrada · $tomadas de $total tomadas",
                fontSize = 11.sp,
                color = colors.textSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Mini tarjeta de próxima toma calculada dinámicamente
            Box(
                modifier = Modifier
                    .fillMaxWidth(nextDoseWidthFraction)
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.nextDoseCardBg)
                    .then(
                        if (colors.isDark) {
                            Modifier.border(
                                BorderStroke(1.dp, colors.nextDoseCardBorder),
                                RoundedCornerShape(14.dp)
                            )
                        } else Modifier
                    )
                    .padding(horizontal = 12.dp, vertical = 7.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (nextDosis != null) {
                        ClockIcon(
                            modifier = Modifier.size(16.dp),
                            color = colors.nextDoseBadgeText
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "PRÓXIMA · ${nextDosis.hora}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.nextDoseBadgeText
                            )
                            Text(
                                text = "${nextDosis.nombre} · ${nextDosis.dosis}",
                                fontSize = 12.sp,
                                color = colors.nextDoseSubText
                            )
                        }
                    } else {
                        CheckIcon(
                            modifier = Modifier.size(16.dp),
                            color = colors.nextDoseBadgeText
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "¡TODO AL DÍA!",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.nextDoseBadgeText
                            )
                            Text(
                                text = "No quedan tomas hoy",
                                fontSize = 12.sp,
                                color = colors.nextDoseSubText
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Botón: "← Volver a hoy"
            Box(
                modifier = Modifier
                    .fillMaxWidth(buttonWidthFraction)
                    .height(38.dp)
                    .clip(RoundedCornerShape(50))
                    .background(colors.primaryButtonBg)
                    .clickable { onBackToHome() },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ArrowBackIcon(
                        modifier = Modifier.size(14.dp),
                        color = colors.primaryButtonText,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Volver a hoy",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.primaryButtonText
                    )
                }
            }

            Spacer(modifier = Modifier.height(bottomSpacerHeight))
        }
    }
}

// -------------------------------------------------------------
// PANTALLA 4: Nueva alarma (Interactiva: selección y creación real)
// -------------------------------------------------------------
@Composable
fun NuevaAlarmaScreen(
    time: String = "07:58",
    onBack: () -> Unit = {},
    onSave: (nombre: String, dosis: String, instruccion: String, hora: String, repeticion: String) -> Unit = { _, _, _, _, _ -> },
    onToggleTheme: () -> Unit = {}
) {
    val isRound = LocalConfiguration.current.isScreenRound
    val colors = LocalDosisColors.current
    val scrollState = rememberScrollState()

    val screenHorizontalPadding = if (isRound) 16.dp else 14.dp
    val headerHorizontalPadding = if (isRound) 14.dp else 4.dp
    val cardsWidthFraction = if (isRound) 0.94f else 1f
    val buttonWidthFraction = if (isRound) 0.88f else 1f
    val bottomSpacerHeight = if (isRound) 36.dp else 10.dp

    // Estados interactivos para crear la alarma
    var nombre by remember { mutableStateOf("Losartán") }
    var dosis by remember { mutableStateOf("50 mg · 1 comprimido") }
    var hour by remember { mutableIntStateOf(8) }
    var minute by remember { mutableIntStateOf(0) }
    var repeticion by remember { mutableStateOf("Cada día") }
    val instruccion by remember { mutableStateOf("Con un vaso de agua") }

    // Selector activo
    var activePicker by remember { mutableStateOf<PickerType?>(null) }

    val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)

    val commonMeds = listOf(
        "Losartán",
        "Metformina",
        "Vitamina D",
        "Paracetamol",
        "Ibuprofeno",
        "Omeprazol",
        "Aspirina",
        "Atorvastatina",
        "Enalapril",
        "Amoxicilina"
    )

    val commonDoses = listOf(
        "50 mg · 1 comprimido",
        "100 mg · 1 comprimido",
        "500 mg · 1 comprimido",
        "850 mg · 1 comprimido",
        "1 cápsula",
        "2 comprimidos",
        "10 ml jarabe",
        "5 ml jarabe"
    )

    val repeatOptions = listOf(
        "Cada día",
        "Cada 8 horas",
        "Cada 12 horas",
        "Días alternos",
        "Lunes a Viernes"
    )

    ScreenScaffold(scrollState = scrollState) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = screenHorizontalPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DosisTopBar(time = time, onToggleTheme = onToggleTheme)

            Spacer(modifier = Modifier.height(if (isRound) 2.dp else 4.dp))

            // Header: "< Nueva alarma" o título del selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = headerHorizontalPadding)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        if (activePicker != null) {
                            activePicker = null
                        } else {
                            onBack()
                        }
                    }
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ChevronLeftIcon(
                    modifier = Modifier.size(12.dp),
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = when (activePicker) {
                        PickerType.MEDICAMENTO -> "Elegir medicamento"
                        PickerType.DOSIS -> "Elegir dosis"
                        PickerType.HORA -> "Ajustar hora"
                        null -> "Nueva alarma"
                    },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Si hay un selector activo, mostrar su interfaz adaptada a Wear OS
            when (activePicker) {
                PickerType.MEDICAMENTO -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(cardsWidthFraction),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        commonMeds.forEach { med ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (nombre == med) colors.chipBg else colors.inputCardBg)
                                    .clickable {
                                        nombre = med
                                        activePicker = null
                                    }
                                    .padding(horizontal = 12.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    text = med,
                                    fontSize = 13.sp,
                                    fontWeight = if (nombre == med) FontWeight.Bold else FontWeight.Normal,
                                    color = if (nombre == med) colors.chipText else colors.textPrimary
                                )
                            }
                        }
                    }
                }

                PickerType.DOSIS -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(cardsWidthFraction),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        commonDoses.forEach { d ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (dosis == d) colors.chipBg else colors.inputCardBg)
                                    .clickable {
                                        dosis = d
                                        activePicker = null
                                    }
                                    .padding(horizontal = 12.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    text = d,
                                    fontSize = 12.sp,
                                    fontWeight = if (dosis == d) FontWeight.Bold else FontWeight.Normal,
                                    color = if (dosis == d) colors.chipText else colors.textPrimary
                                )
                            }
                        }
                    }
                }

                PickerType.HORA -> {
                    // Selector intuitivo de Hora y Minutos para Wear OS
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(cardsWidthFraction)
                            .clip(RoundedCornerShape(14.dp))
                            .background(colors.inputCardBg)
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = formattedTime,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Fila de control de Hora
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Hora", fontSize = 11.sp, color = colors.inputCardLabel)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(colors.chipBg)
                                        .clickable {
                                            hour = if (hour > 0) hour - 1 else 23
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    MinusIcon(modifier = Modifier.size(10.dp), color = colors.chipText)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = String.format(Locale.getDefault(), "%02d", hour),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(colors.chipBg)
                                        .clickable {
                                            hour = if (hour < 23) hour + 1 else 0
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    PlusIcon(modifier = Modifier.size(10.dp), color = colors.chipText)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Fila de control de Minutos (pasos de 5 min)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Minuto", fontSize = 11.sp, color = colors.inputCardLabel)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(colors.chipBg)
                                        .clickable {
                                            minute = if (minute >= 5) minute - 5 else 55
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    MinusIcon(modifier = Modifier.size(10.dp), color = colors.chipText)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = String.format(Locale.getDefault(), "%02d", minute),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(colors.chipBg)
                                        .clickable {
                                            minute = if (minute < 55) minute + 5 else 0
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    PlusIcon(modifier = Modifier.size(10.dp), color = colors.chipText)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Confirmar hora
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp)
                                .clip(RoundedCornerShape(50))
                                .background(colors.primaryButtonBg)
                                .clickable { activePicker = null },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Listo",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.primaryButtonText
                            )
                        }
                    }
                }

                null -> {
                    // Vista principal del formulario de creación

                    // Campo 1: Medicamento (Tocar para elegir)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(cardsWidthFraction)
                            .clip(RoundedCornerShape(14.dp))
                            .background(colors.inputCardBg)
                            .clickable { activePicker = PickerType.MEDICAMENTO }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Medicamento",
                                    fontSize = 10.sp,
                                    color = colors.inputCardLabel
                                )
                                Text(
                                    text = nombre,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.inputCardValue
                                )
                            }
                            PencilIcon(
                                modifier = Modifier.size(13.dp),
                                color = colors.inputCardIcon
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Campo 2: Dosis (Tocar para elegir)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(cardsWidthFraction)
                            .clip(RoundedCornerShape(14.dp))
                            .background(colors.inputCardBg)
                            .clickable { activePicker = PickerType.DOSIS }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Dosis",
                                    fontSize = 10.sp,
                                    color = colors.inputCardLabel
                                )
                                Text(
                                    text = dosis,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.inputCardValue
                                )
                            }
                            PencilIcon(
                                modifier = Modifier.size(13.dp),
                                color = colors.inputCardIcon
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // 2 Columnas: Hora y Repetir interactivos
                    Row(
                        modifier = Modifier.fillMaxWidth(cardsWidthFraction),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Columna Hora (Abre el editor de hora)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(colors.inputCardBg)
                                .clickable { activePicker = PickerType.HORA }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Column {
                                Text(
                                    text = "Hora",
                                    fontSize = 10.sp,
                                    color = colors.inputCardLabel
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = formattedTime,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.inputCardValue
                                )
                            }
                        }

                        // Columna Repetir (Cicla entre opciones con cada toque)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(colors.chipBg)
                                .clickable {
                                    val nextIndex = (repeatOptions.indexOf(repeticion) + 1) % repeatOptions.size
                                    repeticion = repeatOptions[nextIndex]
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Column {
                                Text(
                                    text = "Repetir",
                                    fontSize = 10.sp,
                                    color = colors.chipText
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = repeticion,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.chipText
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Botón: "✓ Guardar"
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(buttonWidthFraction)
                            .height(38.dp)
                            .clip(RoundedCornerShape(50))
                            .background(colors.primaryButtonBg)
                            .clickable {
                                onSave(nombre, dosis, instruccion, formattedTime, repeticion)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CheckIcon(
                                modifier = Modifier.size(14.dp),
                                color = colors.primaryButtonText,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Guardar",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.primaryButtonText
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(bottomSpacerHeight))
        }
    }
}

// =============================================================
// PREVIEWS - Pantallas circulares y cuadradas (Light y Dark)
// =============================================================

@Preview(
    name = "1. Resumen · Circular Dark",
    device = "id:wearos_small_round",
    showSystemUi = true
)
@Composable
fun ResumenDelDiaCircularDarkPreview() {
    val sampleList = listOf(
        DosisItem(id = "1", nombre = "Vitamina D", dosis = "1 cápsula", hora = "07:00", tomada = true, horaTomada = "07:02"),
        DosisItem(id = "2", nombre = "Losartán", dosis = "50 mg", hora = "08:00", tomada = false),
        DosisItem(id = "3", nombre = "Metformina", dosis = "850 mg", hora = "21:00", tomada = false)
    )
    ContarTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF09110E))
        ) {
            ResumenDelDiaScreen(
                dosisList = sampleList,
                nextDosis = sampleList[1],
                badgeText = "PRÓXIMA · EN 2 MIN"
            )
        }
    }
}

@Preview(
    name = "1. Resumen · Circular Light",
    device = "id:wearos_small_round",
    showSystemUi = true
)
@Composable
fun ResumenDelDiaCircularLightPreview() {
    val sampleList = listOf(
        DosisItem(id = "1", nombre = "Vitamina D", dosis = "1 cápsula", hora = "07:00", tomada = true, horaTomada = "07:02"),
        DosisItem(id = "2", nombre = "Losartán", dosis = "50 mg", hora = "08:00", tomada = false),
        DosisItem(id = "3", nombre = "Metformina", dosis = "850 mg", hora = "21:00", tomada = false)
    )
    ContarTheme(darkTheme = false) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            ResumenDelDiaScreen(
                dosisList = sampleList,
                nextDosis = sampleList[1],
                badgeText = "PRÓXIMA · EN 2 MIN"
            )
        }
    }
}

@Preview(
    name = "2. Recordatorio · Circular Dark",
    device = "id:wearos_small_round",
    showSystemUi = true
)
@Composable
fun RecordatorioDosisCircularDarkPreview() {
    val sample = DosisItem(id = "2", nombre = "Losartán", dosis = "50 mg · 1 comprimido", hora = "08:00", instruccion = "Con un vaso de agua")
    ContarTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF09110E))
        ) {
            RecordatorioDosisScreen(dosis = sample)
        }
    }
}

@Preview(
    name = "2. Recordatorio · Circular Light",
    device = "id:wearos_small_round",
    showSystemUi = true
)
@Composable
fun RecordatorioDosisCircularLightPreview() {
    val sample = DosisItem(id = "2", nombre = "Losartán", dosis = "50 mg · 1 comprimido", hora = "08:00", instruccion = "Con un vaso de agua")
    ContarTheme(darkTheme = false) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            RecordatorioDosisScreen(dosis = sample)
        }
    }
}

@Preview(
    name = "3. Confirmación · Circular Dark",
    device = "id:wearos_small_round",
    showSystemUi = true
)
@Composable
fun ConfirmacionTomaCircularDarkPreview() {
    val sampleTaken = DosisItem(id = "2", nombre = "Losartán", dosis = "50 mg", hora = "08:00", tomada = true, horaTomada = "08:01")
    val sampleNext = DosisItem(id = "3", nombre = "Metformina", dosis = "850 mg", hora = "21:00")
    val sampleList = listOf(
        DosisItem(id = "1", nombre = "Vitamina D", dosis = "1 cápsula", hora = "07:00", tomada = true, horaTomada = "07:02"),
        sampleTaken,
        sampleNext
    )
    ContarTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF09110E))
        ) {
            ConfirmacionTomaScreen(
                takenDosis = sampleTaken,
                nextDosis = sampleNext,
                dosisList = sampleList
            )
        }
    }
}

@Preview(
    name = "4. Nueva Alarma · Circular Dark",
    device = "id:wearos_small_round",
    showSystemUi = true
)
@Composable
fun NuevaAlarmaCircularDarkPreview() {
    ContarTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF09110E))
        ) {
            NuevaAlarmaScreen()
        }
    }
}

@Preview(
    name = "Resumen · Rectangular Light",
    device = "id:wearos_square",
    showSystemUi = true
)
@Composable
fun ResumenDelDiaSquarePreview() {
    val sampleList = listOf(
        DosisItem(id = "1", nombre = "Vitamina D", dosis = "1 cápsula", hora = "07:00", tomada = true, horaTomada = "07:02"),
        DosisItem(id = "2", nombre = "Losartán", dosis = "50 mg", hora = "08:00", tomada = false),
        DosisItem(id = "3", nombre = "Metformina", dosis = "850 mg", hora = "21:00", tomada = false)
    )
    ContarTheme(darkTheme = false) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            ResumenDelDiaScreen(
                dosisList = sampleList,
                nextDosis = sampleList[1],
                badgeText = "PRÓXIMA · EN 2 MIN"
            )
        }
    }
}
