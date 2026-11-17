package com.example.contar.presentation

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.example.contar.presentation.theme.ContarTheme
import com.example.contar.presentation.theme.LocalDosisColors

enum class DosisScreen {
    RESUMEN,
    RECORDATORIO,
    CONFIRMACION,
    NUEVA_ALARMA
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DosisApp()
        }
    }
}

@Composable
fun DosisApp() {
    var isDarkTheme by remember { mutableStateOf(true) }
    var currentScreen by remember { mutableStateOf(DosisScreen.RESUMEN) }
    var losartanTaken by remember { mutableStateOf(false) }

    val haptic = LocalHapticFeedback.current

    ContarTheme(darkTheme = isDarkTheme) {
        val colors = LocalDosisColors.current
        AppScaffold {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.background)
            ) {
                when (currentScreen) {
                    DosisScreen.RESUMEN -> {
                        ResumenDelDiaScreen(
                            losartanTaken = losartanTaken,
                            onOpenReminder = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                currentScreen = DosisScreen.RECORDATORIO
                            },
                            onOpenNewAlarm = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                currentScreen = DosisScreen.NUEVA_ALARMA
                            },
                            onToggleTheme = {
                                isDarkTheme = !isDarkTheme
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        )
                    }

                    DosisScreen.RECORDATORIO -> {
                        RecordatorioDosisScreen(
                            onTakeDose = {
                                losartanTaken = true
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                currentScreen = DosisScreen.CONFIRMACION
                            },
                            onSnooze = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                currentScreen = DosisScreen.RESUMEN
                            },
                            onToggleTheme = {
                                isDarkTheme = !isDarkTheme
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        )
                    }

                    DosisScreen.CONFIRMACION -> {
                        ConfirmacionTomaScreen(
                            onBackToHome = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                currentScreen = DosisScreen.RESUMEN
                            },
                            onToggleTheme = {
                                isDarkTheme = !isDarkTheme
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        )
                    }

                    DosisScreen.NUEVA_ALARMA -> {
                        NuevaAlarmaScreen(
                            onBack = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                currentScreen = DosisScreen.RESUMEN
                            },
                            onSave = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                currentScreen = DosisScreen.RESUMEN
                            },
                            onToggleTheme = {
                                isDarkTheme = !isDarkTheme
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// PANTALLA 1: Resumen del día (Responsivo para pantallas circulares)
// -------------------------------------------------------------
@Composable
fun ResumenDelDiaScreen(
    losartanTaken: Boolean = false,
    time: String = "07:58",
    onOpenReminder: () -> Unit = {},
    onOpenNewAlarm: () -> Unit = {},
    onToggleTheme: () -> Unit = {}
) {
    val isRound = LocalConfiguration.current.isScreenRound
    val colors = LocalDosisColors.current
    val scrollState = rememberScrollState()

    // Márgenes y anchos calculados responsivamente para relojes redondos
    val screenHorizontalPadding = if (isRound) 14.dp else 12.dp
    val headerHorizontalPadding = if (isRound) 14.dp else 4.dp
    val listHorizontalPadding = if (isRound) 10.dp else 4.dp
    val bottomSpacerHeight = if (isRound) 38.dp else 12.dp

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

            // Encabezado "Hoy" + Contador de dosis
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
                    text = if (losartanTaken) "2 de 3 tomadas" else "1 de 3 tomadas",
                    fontSize = 11.sp,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tarjeta destacada: PRÓXIMA · EN 2 MIN (ancho adaptable en círculos)
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
                    .clickable { onOpenReminder() }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (losartanTaken) "SIGUIENTE · EN 12 H" else "PRÓXIMA · EN 2 MIN",
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
                        text = if (losartanTaken) "21:00" else "08:00",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.nextDoseTimeText
                    )

                    Spacer(modifier = Modifier.height(1.dp))

                    Text(
                        text = if (losartanTaken) "Metformina · 850 mg" else "Losartán · 50 mg",
                        fontSize = 13.sp,
                        color = colors.nextDoseSubText
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Lista de tomas con padding de seguridad circular
            // 1. Vitamina D
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = listHorizontalPadding, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CheckCircleIcon(
                    modifier = Modifier.size(18.dp),
                    circleColor = colors.chipBg,
                    checkColor = colors.chipText
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Vitamina D",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "1 cápsula",
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                }
                Text(
                    text = "07:00",
                    fontSize = 12.sp,
                    color = colors.textSecondary
                )
            }

            // Si Losartán ya se tomó, mostrarlo en la lista también
            if (losartanTaken) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = listHorizontalPadding, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CheckCircleIcon(
                        modifier = Modifier.size(18.dp),
                        circleColor = colors.chipBg,
                        checkColor = colors.chipText
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Losartán",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "50 mg",
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                    }
                    Text(
                        text = "08:01",
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                }
            }

            // 2. Metformina
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = listHorizontalPadding, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ClockIcon(
                    modifier = Modifier.size(18.dp),
                    color = colors.textSecondary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Metformina",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "850 mg",
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                }
                Text(
                    text = "21:00",
                    fontSize = 12.sp,
                    color = colors.textSecondary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Indicador inferior (píldora como en la imagen) interactivo para ir a Nueva Alarma
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable { onOpenNewAlarm() }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(16.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(50))
                        .background(colors.brandGreen)
                )
            }

            // Espaciador inferior para no ser cortado por el bisel circular inferior
            Spacer(modifier = Modifier.height(bottomSpacerHeight))
        }
    }
}

// -------------------------------------------------------------
// PANTALLA 2: Recordatorio de dosis (Responsivo para pantallas circulares)
// -------------------------------------------------------------
@Composable
fun RecordatorioDosisScreen(
    time: String = "08:00",
    onTakeDose: () -> Unit = {},
    onSnooze: () -> Unit = {},
    onToggleTheme: () -> Unit = {}
) {
    val isRound = LocalConfiguration.current.isScreenRound
    val colors = LocalDosisColors.current
    val scrollState = rememberScrollState()

    val screenHorizontalPadding = if (isRound) 16.dp else 14.dp
    val primaryButtonWidthFraction = if (isRound) 0.90f else 1f
    val secondaryButtonWidthFraction = if (isRound) 0.86f else 1f
    val bottomSpacerHeight = if (isRound) 36.dp else 10.dp

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

            // Hora de tu toma con icono de campana
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
                text = "Losartán",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Dosis
            Text(
                text = "50 mg · 1 comprimido",
                fontSize = 13.sp,
                color = colors.textSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Chip instructivo "Con un vaso de agua"
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
                        text = "Con un vaso de agua",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.chipText
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Botón principal: "✓ Tomar ahora" (proporcional al arco del reloj)
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

            // Botón secundario: "⏰ Posponer 10 min" (un poco más estrecho para seguir la curvatura)
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
// PANTALLA 3: Confirmación de toma (Responsivo para pantallas circulares)
// -------------------------------------------------------------
@Composable
fun ConfirmacionTomaScreen(
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
                text = "Losartán · 50 mg",
                fontSize = 13.sp,
                color = colors.textSecondary
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "08:01 · 2 de 3 tomadas",
                fontSize = 11.sp,
                color = colors.textSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Mini tarjeta de próxima toma (Metformina)
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
                    ClockIcon(
                        modifier = Modifier.size(16.dp),
                        color = colors.nextDoseBadgeText
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "PRÓXIMA · 21:00",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.nextDoseBadgeText
                        )
                        Text(
                            text = "Metformina · 850 mg",
                            fontSize = 12.sp,
                            color = colors.nextDoseSubText
                        )
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
// PANTALLA 4: Nueva alarma · Reloj (Responsivo para pantallas circulares)
// -------------------------------------------------------------
@Composable
fun NuevaAlarmaScreen(
    time: String = "07:58",
    onBack: () -> Unit = {},
    onSave: () -> Unit = {},
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

            // Header: "< Nueva alarma" con margen de seguridad para no chocar con el bisel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = headerHorizontalPadding)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onBack() }
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ChevronLeftIcon(
                    modifier = Modifier.size(12.dp),
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Nueva alarma",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Campo 1: Medicamento · manual
            Box(
                modifier = Modifier
                    .fillMaxWidth(cardsWidthFraction)
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.inputCardBg)
                    .padding(horizontal = 12.dp, vertical = 7.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Medicamento · manual",
                            fontSize = 10.sp,
                            color = colors.inputCardLabel
                        )
                        Text(
                            text = "Losartán",
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

            // Campo 2: Dosis · manual
            Box(
                modifier = Modifier
                    .fillMaxWidth(cardsWidthFraction)
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.inputCardBg)
                    .padding(horizontal = 12.dp, vertical = 7.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Dosis · manual",
                            fontSize = 10.sp,
                            color = colors.inputCardLabel
                        )
                        Text(
                            text = "50 mg · 1 comprimido",
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

            // 2 Columnas: Hora y Repetir
            Row(
                modifier = Modifier.fillMaxWidth(cardsWidthFraction),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Columna Hora
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.inputCardBg)
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
                            text = "08:00",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.inputCardValue
                        )
                    }
                }

                // Columna Repetir
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.chipBg)
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
                            text = "Cada día",
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
                    .clickable { onSave() },
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

            Spacer(modifier = Modifier.height(bottomSpacerHeight))
        }
    }
}

// =============================================================
// PREVIEWS - Pantallas circulares y cuadradas (Light y Dark)
// =============================================================

// 1. Resumen del día · Circular
@Preview(
    name = "1. Resumen · Circular Dark",
    device = "id:wearos_small_round",
    showSystemUi = true
)
@Composable
fun ResumenDelDiaCircularDarkPreview() {
    ContarTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF09110E))
        ) {
            ResumenDelDiaScreen(losartanTaken = false)
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
    ContarTheme(darkTheme = false) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            ResumenDelDiaScreen(losartanTaken = false)
        }
    }
}

// 2. Recordatorio de dosis · Circular
@Preview(
    name = "2. Recordatorio · Circular Dark",
    device = "id:wearos_small_round",
    showSystemUi = true
)
@Composable
fun RecordatorioDosisCircularDarkPreview() {
    ContarTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF09110E))
        ) {
            RecordatorioDosisScreen()
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
    ContarTheme(darkTheme = false) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            RecordatorioDosisScreen()
        }
    }
}

// 3. Confirmación de toma · Circular
@Preview(
    name = "3. Confirmación · Circular Dark",
    device = "id:wearos_small_round",
    showSystemUi = true
)
@Composable
fun ConfirmacionTomaCircularDarkPreview() {
    ContarTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF09110E))
        ) {
            ConfirmacionTomaScreen()
        }
    }
}

// 4. Nueva alarma · Circular
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

// 5. Resumen del día · Rectangular / Cuadrado (como en la imagen original)
@Preview(
    name = "Resumen · Rectangular Light",
    device = "id:wearos_square",
    showSystemUi = true
)
@Composable
fun ResumenDelDiaSquarePreview() {
    ContarTheme(darkTheme = false) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            ResumenDelDiaScreen(losartanTaken = false)
        }
    }
}
