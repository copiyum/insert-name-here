package com.grove.app.feature.home

import android.content.Context
import android.provider.Settings
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.grove.app.designsystem.format.Money
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.min

internal const val SpendMotionImpactMillis = 420
internal const val SpendMotionFinishBufferMillis = 140
private const val SpendMotionImpactLocalT = 0.9f

internal data class SpendMotionEvent(
    val id: Long,
    val amountMinor: Long,
    val currency: String,
    val categoryColor: Color,
    val origin: Offset?,
    val targetBounds: Rect?,
    val spec: SpendMotionSpec = spendMotionSpec(amountMinor, currency),
) {
    val durationMillis: Int get() = spec.durationMillis
    val particleCount: Int get() = spec.particles.size
}

internal data class SpendMotionSpec(
    val durationMillis: Int,
    val particles: List<SpendParticle>,
)

internal data class SpendParticle(
    val launch: Float,
    val travel: Float,
    val lane: Float,
    val radiusDp: Float,
    val drift: Float,
)

private data class ParticlePx(
    val launch: Float,
    val travel: Float,
    val lane: Float,
    val radiusPx: Float,
    val drift: Float,
)

@Composable
internal fun SpendMotionOverlay(
    event: SpendMotionEvent,
    targetBounds: Rect?,
    rootSize: IntSize,
    travelProgress: Float,
    impactProgress: Float,
) {
    if (targetBounds == null || rootSize.width == 0 || rootSize.height == 0) return

    val density = LocalDensity.current
    val particlesPx =
        remember(event.id, event.spec, density) {
            with(density) {
                event.spec.particles.map {
                    ParticlePx(
                        launch = it.launch,
                        travel = it.travel,
                        lane = it.lane,
                        radiusPx = it.radiusDp.dp.toPx(),
                        drift = it.drift,
                    )
                }
            }
        }
    val start =
        event.origin
            ?: with(density) {
                Offset(
                    x = rootSize.width / 2f,
                    y = rootSize.height - 156.dp.toPx(),
                )
            }
    val target = targetBounds.center

    Canvas(Modifier.fillMaxSize()) {
        val travel = travelProgress.coerceIn(0f, 1f)
        val laneSpreadPx = 52.dp.toPx()
        val driftSpreadPx = 16.dp.toPx()
        particlesPx.forEachIndexed { index, particle ->
            val localT = ((travel - particle.launch) / particle.travel).coerceIn(0f, 1f)
            if (travel > particle.launch && localT < 1f) {
                val eased = smoothStep(localT)
                val wave = sin((localT + index * 0.17f) * PI).toFloat()
                val spread = laneSpreadPx * particle.lane + driftSpreadPx * particle.drift * wave
                val point = flightPoint(eased, start, target, spread)
                val fadeIn = (localT / 0.12f).coerceIn(0f, 1f)
                val fadeOut = ((1f - localT) / 0.3f).coerceIn(0f, 1f)
                drawCircle(
                    color = event.categoryColor.copy(alpha = 0.78f * fadeIn * fadeOut),
                    radius = particle.radiusPx * (0.82f + 0.18f * wave),
                    center = point,
                )
            }
        }

        val pulse = impactProgress.coerceIn(0f, 1f)
        if (pulse > 0f) {
            val radius = targetBounds.maxDimension / 2f + 18.dp.toPx() * pulse
            drawCircle(
                color = event.categoryColor.copy(alpha = 0.22f * (1f - pulse)),
                radius = radius,
                center = target,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
            )
            drawCircle(
                color = event.categoryColor.copy(alpha = 0.1f * (1f - pulse)),
                radius = radius * 0.72f,
                center = target,
            )
        }
    }
}

private val Rect.maxDimension: Float
    get() = maxOf(width, height)

private fun flightPoint(
    t: Float,
    start: Offset,
    end: Offset,
    lateralOffset: Float = 0f,
): Offset {
    val distanceY = abs(end.y - start.y)
    val lift = min(280f, distanceY * 0.52f + 96f)
    val direction = if (end.x >= start.x) 1f else -1f
    val c1 =
        Offset(
            x = start.x + (end.x - start.x) * 0.14f - direction * lateralOffset * 0.32f,
            y = start.y - lift,
        )
    val c2 =
        Offset(
            x = start.x + (end.x - start.x) * 0.82f + lateralOffset,
            y = end.y + lift * 0.26f,
        )
    return cubic(start, c1, c2, end, t)
}

private fun cubic(
    p0: Offset,
    p1: Offset,
    p2: Offset,
    p3: Offset,
    t: Float,
): Offset {
    val u = 1f - t
    val tt = t * t
    val uu = u * u
    val uuu = uu * u
    val ttt = tt * t
    return Offset(
        x = uuu * p0.x + 3f * uu * t * p1.x + 3f * u * tt * p2.x + ttt * p3.x,
        y = uuu * p0.y + 3f * uu * t * p1.y + 3f * u * tt * p2.y + ttt * p3.y,
    )
}

private fun spendMotionSpec(
    amountMinor: Long,
    currency: String,
): SpendMotionSpec {
    val weight = spendMotionWeight(amountMinor, currency)
    val durationMillis = (2600f + weight * 720f).roundToInt().coerceIn(2800, 6800)
    val particleCount = (130f + weight * 58f).roundToInt().coerceIn(150, 380)
    return SpendMotionSpec(
        durationMillis = durationMillis,
        particles = List(particleCount) { index -> spendParticle(index, particleCount) },
    )
}

private fun spendMotionWeight(
    amountMinor: Long,
    currency: String,
): Float {
    val majorAmount = Money.fromMinor(amountMinor.coerceAtLeast(0L), currency).coerceAtLeast(0.0)
    return log10(majorAmount + 1.0).toFloat()
}

private fun spendParticle(
    index: Int,
    count: Int,
): SpendParticle {
    val fraction = if (count <= 1) 0f else index / (count - 1f)
    val jitter = (((index * 37) % 100) / 100f) - 0.5f
    val travel = 0.54f + (((index * 29) % 100) / 100f) * 0.2f
    val launchMax = (1f - travel - 0.025f).coerceIn(0.2f, 0.42f)
    return SpendParticle(
        launch = (fraction * launchMax + jitter * 0.014f).coerceIn(0f, launchMax),
        travel = travel,
        lane = ((index % 11) - 5) / 5f,
        radiusDp = 0.55f + (index % 7) * 0.13f,
        drift = if (index % 2 == 0) 1f else -1f,
    )
}

internal fun spendMotionSettlementProgress(
    travelProgress: Float,
    spec: SpendMotionSpec,
): Float {
    if (spec.particles.isEmpty()) return 1f
    val travel = travelProgress.coerceIn(0f, 1f)
    val settled = spec.particles.sumOf { particle ->
        val impactStart = particle.launch + particle.travel * SpendMotionImpactLocalT
        val impactEnd = particle.launch + particle.travel
        val localSettlement = ((travel - impactStart) / (impactEnd - impactStart)).coerceIn(0f, 1f)
        smoothStep(localSettlement).toDouble()
    }
    return (settled / spec.particles.size).toFloat().coerceIn(0f, 1f)
}

private fun smoothStep(t: Float): Float {
    val clamped = t.coerceIn(0f, 1f)
    return clamped * clamped * (3f - 2f * clamped)
}

internal fun systemAnimationsEnabled(context: Context): Boolean =
    runCatching {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) != 0f
    }.getOrDefault(true)
