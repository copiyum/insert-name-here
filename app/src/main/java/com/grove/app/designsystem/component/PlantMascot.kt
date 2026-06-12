package com.grove.app.designsystem.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.grove.app.data.SpendPace
import com.grove.app.designsystem.theme.GroveSprings
import com.grove.app.designsystem.theme.GroveTheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

/**
 * Grove's mascot: a procedurally drawn plant that reflects real budget health.
 * [growth] 0..1 controls how tall it stands (how far through the month you've
 * stayed funded); [pace] controls posture and color — a healthy plant sways
 * upright, a tight one dulls, an over-budget one wilts. Blooms at full growth
 * when healthy.
 */
@Composable
fun PlantMascot(
    growth: Float,
    pace: SpendPace,
    modifier: Modifier = Modifier,
    size: Dp = 96.dp,
) {
    val c = GroveTheme.colors
    val motionEnabled = rememberMotionEnabled()
    val sway by if (motionEnabled) {
        rememberInfiniteTransition(label = "plantSway").animateFloat(
            initialValue = 0f,
            targetValue = (2 * PI).toFloat(),
            animationSpec = infiniteRepeatable(tween(5200, easing = LinearEasing), RepeatMode.Restart),
            label = "plantSway",
        )
    } else {
        remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    }
    val shownGrowth by animateFloatAsState(
        targetValue = growth.coerceIn(0.08f, 1f),
        animationSpec = GroveSprings.gentle(),
        label = "plantGrowth",
    )
    val droop by animateFloatAsState(
        targetValue = when (pace) {
            SpendPace.Healthy -> 0f
            SpendPace.Tight -> 0.35f
            SpendPace.Over -> 1f
        },
        animationSpec = GroveSprings.gentle(),
        label = "plantDroop",
    )
    val healthyColor = c.accent
    val tiredColor = lerp(c.accent, c.clay, 0.55f)
    val stemColor = lerp(healthyColor, tiredColor, droop)
    val leafColor = lerp(c.accentSoft, c.clay, droop * 0.7f)
    val bloomColor = c.clay
    val stateLabel = when (pace) {
        SpendPace.Healthy -> "thriving"
        SpendPace.Tight -> "a little thirsty"
        SpendPace.Over -> "wilting"
    }

    Canvas(
        modifier = modifier
            .size(size)
            .semantics { contentDescription = "Your money plant is $stateLabel" },
    ) {
        val w = this.size.width
        val h = this.size.height
        val baseX = w / 2f
        val baseY = h * 0.94f
        val maxStem = h * 0.78f
        val stemLen = maxStem * shownGrowth
        val swayOffset = sin(sway) * w * 0.018f * (1f - droop * 0.5f)
        // Wilting bends the upper stem over; sway keeps it gently alive.
        val bend = droop * w * 0.16f + swayOffset

        val tip = Offset(baseX + bend, baseY - stemLen * (1f - droop * 0.18f))
        val control = Offset(baseX + bend * 0.25f, baseY - stemLen * 0.55f)

        // Soil mound.
        drawArc(
            color = stemColor.copy(alpha = 0.25f),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset(baseX - w * 0.18f, baseY - h * 0.035f),
            size = androidx.compose.ui.geometry.Size(w * 0.36f, h * 0.09f),
        )

        val stem = Path().apply {
            moveTo(baseX, baseY)
            quadraticTo(control.x, control.y, tip.x, tip.y)
        }
        drawPath(stem, stemColor, style = Stroke(width = w * 0.035f, cap = StrokeCap.Round))

        // Leaves sprout in alternating pairs as the plant grows.
        val leafCount = (2 + (shownGrowth * 4)).toInt()
        for (i in 0 until leafCount) {
            val f = (i + 1) / (leafCount + 1f)
            val pos = quadPoint(Offset(baseX, baseY), control, tip, f)
            val side = if (i % 2 == 0) -1f else 1f
            val leafSize = w * 0.16f * (1f - f * 0.35f)
            val angle = side * (52f + droop * 30f) + sin(sway + i) * 4f
            drawLeaf(pos, leafSize, angle, leafColor)
        }

        // Bloom only at full, healthy growth — the month's reward.
        val bloom = ((shownGrowth - 0.85f) / 0.15f).coerceIn(0f, 1f) * (1f - droop)
        if (bloom > 0.01f) {
            val petalR = w * 0.085f * bloom
            for (p in 0 until 5) {
                val a = p / 5f * 2f * PI.toFloat() + sway * 0.1f
                drawCircle(
                    color = bloomColor.copy(alpha = 0.92f),
                    radius = petalR * 0.62f,
                    center = tip + Offset(cos(a) * petalR, sin(a) * petalR),
                )
            }
            drawCircle(color = c.fg1.copy(alpha = 0.85f), radius = petalR * 0.4f, center = tip)
        }
    }
}

private fun quadPoint(p0: Offset, p1: Offset, p2: Offset, t: Float): Offset {
    val u = 1f - t
    return Offset(
        u.pow(2) * p0.x + 2f * u * t * p1.x + t.pow(2) * p2.x,
        u.pow(2) * p0.y + 2f * u * t * p1.y + t.pow(2) * p2.y,
    )
}

private fun DrawScope.drawLeaf(at: Offset, size: Float, angleDeg: Float, color: Color) {
    rotate(degrees = angleDeg, pivot = at) {
        val leaf = Path().apply {
            moveTo(at.x, at.y)
            quadraticTo(at.x + size * 0.5f, at.y - size * 0.7f, at.x, at.y - size * 1.4f)
            quadraticTo(at.x - size * 0.5f, at.y - size * 0.7f, at.x, at.y)
            close()
        }
        drawPath(leaf, color)
    }
}
