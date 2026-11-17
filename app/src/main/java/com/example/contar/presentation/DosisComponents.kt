package com.example.contar.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Text
import com.example.contar.presentation.theme.LocalDosisColors

@Composable
fun PillIcon(
    modifier: Modifier = Modifier,
    color: Color
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokePx = 1.6.dp.toPx()
        withTransform({
            rotate(degrees = -45f, pivot = center)
        }) {
            val pillWidth = w * 0.95f
            val pillHeight = h * 0.52f
            val left = (w - pillWidth) / 2f
            val top = (h - pillHeight) / 2f
            val radius = CornerRadius(pillHeight / 2f, pillHeight / 2f)
            drawRoundRect(
                color = color,
                topLeft = Offset(left, top),
                size = Size(pillWidth, pillHeight),
                cornerRadius = radius,
                style = Stroke(width = strokePx)
            )
            drawLine(
                color = color,
                start = Offset(center.x, top),
                end = Offset(center.x, top + pillHeight),
                strokeWidth = strokePx
            )
        }
    }
}

@Composable
fun ClockIcon(
    modifier: Modifier = Modifier,
    color: Color,
    strokeWidth: Dp = 1.4.dp
) {
    Canvas(modifier = modifier) {
        val strokePx = strokeWidth.toPx()
        val radius = (size.minDimension - strokePx) / 2f
        drawCircle(
            color = color,
            radius = radius,
            center = center,
            style = Stroke(width = strokePx)
        )
        // Minute hand (up)
        drawLine(
            color = color,
            start = center,
            end = Offset(center.x, center.y - radius * 0.6f),
            strokeWidth = strokePx,
            cap = StrokeCap.Round
        )
        // Hour hand (pointing ~3 o'clock)
        drawLine(
            color = color,
            start = center,
            end = Offset(center.x + radius * 0.45f, center.y),
            strokeWidth = strokePx,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun CheckIcon(
    modifier: Modifier = Modifier,
    color: Color,
    strokeWidth: Dp = 2.dp
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokePx = strokeWidth.toPx()
        val path = Path().apply {
            moveTo(w * 0.2f, h * 0.52f)
            lineTo(w * 0.42f, h * 0.76f)
            lineTo(w * 0.82f, h * 0.26f)
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = strokePx, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
fun CheckCircleIcon(
    modifier: Modifier = Modifier,
    circleColor: Color,
    checkColor: Color
) {
    Canvas(modifier = modifier) {
        val radius = size.minDimension / 2f
        drawCircle(
            color = circleColor,
            radius = radius,
            center = center
        )
        val w = size.width
        val h = size.height
        val strokePx = 1.5.dp.toPx()
        val path = Path().apply {
            moveTo(w * 0.28f, h * 0.52f)
            lineTo(w * 0.44f, h * 0.70f)
            lineTo(w * 0.74f, h * 0.34f)
        }
        drawPath(
            path = path,
            color = checkColor,
            style = Stroke(width = strokePx, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
fun BellIcon(
    modifier: Modifier = Modifier,
    color: Color
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokePx = 1.4.dp.toPx()
        val path = Path().apply {
            moveTo(w * 0.2f, h * 0.75f)
            lineTo(w * 0.8f, h * 0.75f)
            quadraticTo(w * 0.75f, h * 0.42f, w * 0.5f, h * 0.24f)
            quadraticTo(w * 0.25f, h * 0.42f, w * 0.2f, h * 0.75f)
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = strokePx, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        // Clapper at bottom
        drawCircle(
            color = color,
            radius = w * 0.08f,
            center = Offset(w * 0.5f, h * 0.86f)
        )
        // Top loop
        drawCircle(
            color = color,
            radius = w * 0.07f,
            center = Offset(w * 0.5f, h * 0.16f),
            style = Stroke(strokePx)
        )
    }
}

@Composable
fun WaterGlassIcon(
    modifier: Modifier = Modifier,
    color: Color
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokePx = 1.3.dp.toPx()
        val path = Path().apply {
            moveTo(w * 0.22f, h * 0.15f)
            lineTo(w * 0.78f, h * 0.15f)
            lineTo(w * 0.68f, h * 0.86f)
            quadraticTo(w * 0.5f, h * 0.95f, w * 0.32f, h * 0.86f)
            close()
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = strokePx, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        // Water surface line
        drawLine(
            color = color,
            start = Offset(w * 0.30f, h * 0.46f),
            end = Offset(w * 0.70f, h * 0.46f),
            strokeWidth = strokePx,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun AlarmClockIcon(
    modifier: Modifier = Modifier,
    color: Color
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokePx = 1.4.dp.toPx()
        val r = w * 0.33f
        val c = Offset(w * 0.5f, h * 0.55f)
        drawCircle(color, radius = r, center = c, style = Stroke(strokePx))
        // Hands
        drawLine(color, c, Offset(c.x, c.y - r * 0.55f), strokePx, StrokeCap.Round)
        drawLine(color, c, Offset(c.x + r * 0.45f, c.y), strokePx, StrokeCap.Round)
        // Top bells
        drawArc(
            color = color,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(w * 0.16f, h * 0.12f),
            size = Size(w * 0.26f, h * 0.26f),
            style = Stroke(strokePx)
        )
        drawArc(
            color = color,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(w * 0.58f, h * 0.12f),
            size = Size(w * 0.26f, h * 0.26f),
            style = Stroke(strokePx)
        )
    }
}

@Composable
fun PencilIcon(
    modifier: Modifier = Modifier,
    color: Color
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokePx = 1.4.dp.toPx()
        withTransform({
            rotate(degrees = 45f, pivot = center)
        }) {
            val pw = w * 0.3f
            val ph = h * 0.72f
            val left = (w - pw) / 2f
            val top = (h - ph) / 2f
            drawRoundRect(
                color = color,
                topLeft = Offset(left, top),
                size = Size(pw, ph * 0.72f),
                cornerRadius = CornerRadius(1.5.dp.toPx(), 1.5.dp.toPx()),
                style = Stroke(strokePx)
            )
            val tip = Path().apply {
                moveTo(left, top + ph * 0.72f)
                lineTo(left + pw, top + ph * 0.72f)
                lineTo(left + pw / 2f, top + ph)
                close()
            }
            drawPath(tip, color = color, style = Stroke(strokePx, join = StrokeJoin.Round))
        }
    }
}

@Composable
fun ChevronRightIcon(
    modifier: Modifier = Modifier,
    color: Color,
    strokeWidth: Dp = 1.8.dp
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokePx = strokeWidth.toPx()
        val path = Path().apply {
            moveTo(w * 0.25f, h * 0.15f)
            lineTo(w * 0.75f, h * 0.5f)
            lineTo(w * 0.25f, h * 0.85f)
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = strokePx, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
fun ChevronLeftIcon(
    modifier: Modifier = Modifier,
    color: Color,
    strokeWidth: Dp = 1.8.dp
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokePx = strokeWidth.toPx()
        val path = Path().apply {
            moveTo(w * 0.75f, h * 0.15f)
            lineTo(w * 0.25f, h * 0.5f)
            lineTo(w * 0.75f, h * 0.85f)
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = strokePx, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
fun ArrowBackIcon(
    modifier: Modifier = Modifier,
    color: Color,
    strokeWidth: Dp = 2.dp
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokePx = strokeWidth.toPx()
        // Horizontal shaft
        drawLine(
            color = color,
            start = Offset(w * 0.15f, h * 0.5f),
            end = Offset(w * 0.85f, h * 0.5f),
            strokeWidth = strokePx,
            cap = StrokeCap.Round
        )
        // Left arrow head
        val path = Path().apply {
            moveTo(w * 0.5f, h * 0.2f)
            lineTo(w * 0.15f, h * 0.5f)
            lineTo(w * 0.5f, h * 0.8f)
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = strokePx, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
fun DosisTopBar(
    time: String = "07:58",
    onToggleTheme: () -> Unit = {}
) {
    val isRound = LocalConfiguration.current.isScreenRound
    val colors = LocalDosisColors.current

    // Insets responsivos para pantallas circulares:
    // En pantallas redondas, las esquinas superiores no existen y el ancho útil en la parte alta
    // es menor, por lo que agregamos un margen horizontal y superior adecuado para que
    // quede perfectamente enmarcado dentro del arco de la esfera.
    val horizontalPadding = if (isRound) 24.dp else 6.dp
    val topPadding = if (isRound) 10.dp else 2.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding)
            .padding(top = topPadding, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .clickable { onToggleTheme() }
                .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            PillIcon(
                modifier = Modifier.size(13.dp),
                color = colors.brandGreen
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Dosis",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.brandGreen
            )
        }

        Text(
            text = time,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = colors.textPrimary,
            modifier = Modifier.padding(end = 2.dp)
        )
    }
}

@Composable
fun PlusIcon(
    modifier: Modifier = Modifier,
    color: Color,
    strokeWidth: Dp = 1.8.dp
) {
    Canvas(modifier = modifier) {
        val strokePx = strokeWidth.toPx()
        drawLine(
            color = color,
            start = Offset(size.width * 0.2f, size.height * 0.5f),
            end = Offset(size.width * 0.8f, size.height * 0.5f),
            strokeWidth = strokePx,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.5f, size.height * 0.2f),
            end = Offset(size.width * 0.5f, size.height * 0.8f),
            strokeWidth = strokePx,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun MinusIcon(
    modifier: Modifier = Modifier,
    color: Color,
    strokeWidth: Dp = 1.8.dp
) {
    Canvas(modifier = modifier) {
        val strokePx = strokeWidth.toPx()
        drawLine(
            color = color,
            start = Offset(size.width * 0.25f, size.height * 0.5f),
            end = Offset(size.width * 0.75f, size.height * 0.5f),
            strokeWidth = strokePx,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun TrashIcon(
    modifier: Modifier = Modifier,
    color: Color,
    strokeWidth: Dp = 1.4.dp
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokePx = strokeWidth.toPx()
        drawLine(
            color = color,
            start = Offset(w * 0.2f, h * 0.3f),
            end = Offset(w * 0.8f, h * 0.3f),
            strokeWidth = strokePx,
            cap = StrokeCap.Round
        )
        drawArc(
            color = color,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(w * 0.38f, h * 0.15f),
            size = Size(w * 0.24f, h * 0.24f),
            style = Stroke(strokePx, cap = StrokeCap.Round)
        )
        val path = Path().apply {
            moveTo(w * 0.28f, h * 0.3f)
            lineTo(w * 0.32f, h * 0.85f)
            quadraticTo(w * 0.33f, h * 0.9f, w * 0.4f, h * 0.9f)
            lineTo(w * 0.6f, h * 0.9f)
            quadraticTo(w * 0.67f, h * 0.9f, w * 0.68f, h * 0.85f)
            lineTo(w * 0.72f, h * 0.3f)
        }
        drawPath(path, color = color, style = Stroke(strokePx, join = StrokeJoin.Round))
    }
}
