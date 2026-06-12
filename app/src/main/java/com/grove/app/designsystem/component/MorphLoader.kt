package com.grove.app.designsystem.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.grove.app.designsystem.theme.GroveTheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Morphing-shape loading indicator: a petaled blob whose petal count and depth
 * breathe through a loop while the whole shape rotates. The silhouette is the
 * polar curve r(t) = R * (1 - depth + depth * cos(petals * t)), interpolated
 * continuously so the shape flows flower -> scallop -> circle and back.
 */
@Composable
fun MorphLoader(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    color: Color = GroveTheme.colors.accent,
) {
    val transition = rememberInfiniteTransition(label = "morphLoader")
    val cycle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2800, easing = LinearEasing), RepeatMode.Restart),
        label = "morphCycle",
    )
    val spin by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(5200, easing = LinearEasing), RepeatMode.Restart),
        label = "morphSpin",
    )

    Canvas(modifier = modifier.size(size)) {
        val radius = this.size.minDimension / 2f
        val cx = this.size.width / 2f
        val cy = this.size.height / 2f
        // Petal count sweeps 4 -> 7 -> 4 by crossfading adjacent integer harmonics —
        // non-integer counts would leave a seam where the curve fails to close.
        val tri = 1f - kotlin.math.abs(2f * cycle - 1f)
        val kFloat = 4f + 3f * tri
        val k1 = kFloat.toInt()
        val blend = kFloat - k1
        val depth = 0.16f + 0.10f * (0.5f + 0.5f * sin(cycle * 4f * PI.toFloat()))

        val path = Path()
        val steps = 120
        for (i in 0..steps) {
            val t = i / steps.toFloat() * 2f * PI.toFloat()
            val harmonic = (1f - blend) * cos(k1 * t) + blend * cos((k1 + 1) * t)
            val r = radius * (1f - depth + depth * harmonic) * 0.92f
            val x = cx + r * cos(t + spin)
            val y = cy + r * sin(t + spin)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        drawPath(path, color)
    }
}
