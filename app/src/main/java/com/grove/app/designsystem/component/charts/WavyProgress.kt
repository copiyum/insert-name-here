package com.grove.app.designsystem.component.charts

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.grove.app.designsystem.component.animatedOnce
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun WavyProgress(
    progress: Float,
    color: Color,
    trackColor: Color,
    modifier: Modifier = Modifier,
    height: Dp = 14.dp,
    strokeWidth: Dp = 3.dp,
    wavelength: Dp = 24.dp,
    animateWave: Boolean = true,
) {
    val clamped = progress.coerceIn(0f, 1f)
    val shown = animatedOnce(clamped)
    val phase by if (animateWave && com.grove.app.designsystem.component.rememberMotionEnabled()) {
        rememberInfiniteTransition(label = "wavePhase").animateFloat(
            initialValue = 0f,
            targetValue = (2 * PI).toFloat(),
            animationSpec = infiniteRepeatable(tween(3600, easing = LinearEasing), RepeatMode.Restart),
            label = "wavePhase",
        )
    } else {
        androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    }

    Canvas(modifier = modifier.fillMaxWidth().height(height)) {
        val w = size.width
        val midY = size.height / 2f
        val stroke = strokeWidth.toPx()
        val waveLenPx = wavelength.toPx()
        val splitX = w * shown
        val amplitude = (size.height - stroke) / 2f * (1f - shown * 0.35f)

        if (shown > 0.005f) {
            val path = Path()
            var x = 0f
            path.moveTo(0f, midY + amplitude * sin(phase))
            while (x < splitX) {
                x = (x + 3f).coerceAtMost(splitX)
                val y = midY + amplitude * sin(phase + (x / waveLenPx) * 2f * PI.toFloat())
                path.lineTo(x, y)
            }
            drawPath(path, color, style = Stroke(width = stroke, cap = StrokeCap.Round))
        }
        if (splitX < w - stroke) {
            drawLine(
                color = trackColor,
                start = androidx.compose.ui.geometry.Offset(splitX + stroke, midY),
                end = androidx.compose.ui.geometry.Offset(w - stroke / 2f, midY),
                strokeWidth = stroke,
                cap = StrokeCap.Round,
            )
        }
    }
}
