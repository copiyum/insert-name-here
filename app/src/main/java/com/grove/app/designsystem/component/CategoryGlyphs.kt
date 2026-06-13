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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private const val TwoPi = (2.0 * PI).toFloat()

/**
 * Bespoke, hand-drawn category glyph with a subtle looping idle animation. Each
 * category has its own motion (steam rises, wheels roll, heart beats, dots
 * ripple). All motion freezes to a clean static pose when reduce-motion is on.
 */
@Composable
fun CategoryGlyph(
    iconKey: String,
    color: Color,
    sizeDp: Int,
    modifier: Modifier = Modifier,
) {
    val t = if (rememberMotionEnabled()) {
        val transition = rememberInfiniteTransition(label = "catGlyph")
        val phase by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(2600, easing = LinearEasing), RepeatMode.Restart),
            label = "catPhase",
        )
        phase
    } else {
        0f
    }
    Canvas(modifier = modifier.size(sizeDp.dp)) {
        when (iconKey) {
            "restaurant" -> glyphFood(color, t)
            "car" -> glyphCar(color, t)
            "receipt" -> glyphReceipt(color, t)
            "shopping_bag" -> glyphBag(color, t)
            "favorite" -> glyphHeart(color, t)
            "sports_esports" -> glyphPlay(color, t)
            "trending_up" -> glyphTrend(color, t)
            else -> glyphDots(color, t)
        }
    }
}

// --- glyphs -----------------------------------------------------------------

/** A bowl with two ribbons of steam rising and fading. */
private fun DrawScope.glyphFood(color: Color, t: Float) {
    val w = size.width
    val h = size.height
    val sw = size.minDimension * 0.09f
    val cap = StrokeCap.Round
    val bowlTop = h * 0.58f
    drawArc(
        color, 0f, 180f, false,
        topLeft = Offset(w * 0.18f, bowlTop - w * 0.30f),
        size = Size(w * 0.64f, w * 0.60f),
        style = Stroke(sw, cap = cap),
    )
    drawLine(color, Offset(w * 0.14f, bowlTop), Offset(w * 0.86f, bowlTop), sw, cap)
    for (i in 0..1) {
        val rise = (t + i * 0.5f) % 1f
        val lift = rise * w * 0.06f
        val baseY = bowlTop - w * 0.10f - lift
        val x = w * (0.40f + i * 0.20f)
        val span = w * 0.26f
        val a = (1f - rise).coerceIn(0f, 1f) * 0.75f
        val p = Path().apply {
            moveTo(x, baseY)
            quadraticTo(x + w * 0.08f, baseY - span * 0.4f, x, baseY - span * 0.7f)
            quadraticTo(x - w * 0.08f, baseY - span * 0.95f, x, baseY - span * 1.2f)
        }
        drawPath(p, color.copy(alpha = a), style = Stroke(sw * 0.7f, cap = cap))
    }
}

/** A little car that bobs while ground dashes stream past underneath. */
private fun DrawScope.glyphCar(color: Color, t: Float) {
    val w = size.width
    val h = size.height
    val sw = size.minDimension * 0.085f
    val cap = StrokeCap.Round
    val bob = sin(t * TwoPi) * h * 0.015f
    translate(0f, bob) {
        val left = w * 0.16f
        val right = w * 0.84f
        val bodyTop = h * 0.46f
        val bodyBot = h * 0.62f
        drawRoundRect(
            color,
            topLeft = Offset(left, bodyTop),
            size = Size(right - left, bodyBot - bodyTop),
            cornerRadius = CornerRadius(sw * 1.2f),
            style = Stroke(sw, cap = cap),
        )
        val roof = Path().apply {
            moveTo(w * 0.30f, bodyTop)
            lineTo(w * 0.38f, h * 0.34f)
            lineTo(w * 0.62f, h * 0.34f)
            lineTo(w * 0.70f, bodyTop)
        }
        drawPath(roof, color, style = Stroke(sw, cap = cap))
        drawCircle(color, sw * 1.1f, Offset(w * 0.34f, bodyBot), style = Stroke(sw * 0.7f))
        drawCircle(color, sw * 1.1f, Offset(w * 0.66f, bodyBot), style = Stroke(sw * 0.7f))
    }
    val gy = h * 0.74f
    for (i in 0..2) {
        val dx = (t + i / 3f) % 1f
        val x = w * 0.86f - dx * w * 0.72f
        drawLine(color.copy(alpha = 0.45f), Offset(x, gy), Offset(x - w * 0.12f, gy), sw * 0.6f, cap)
    }
}

/** A receipt that sways gently, zigzag torn edge at the bottom. */
private fun DrawScope.glyphReceipt(color: Color, t: Float) {
    val w = size.width
    val h = size.height
    val sw = size.minDimension * 0.08f
    val cap = StrokeCap.Round
    rotate(sin(t * TwoPi) * 4f, pivot = Offset(w * 0.5f, h * 0.2f)) {
        val left = w * 0.30f
        val right = w * 0.70f
        val top = h * 0.18f
        val bot = h * 0.74f
        val p = Path().apply {
            moveTo(left, bot)
            lineTo(left, top)
            lineTo(right, top)
            lineTo(right, bot)
            val n = 4
            val step = (right - left) / n
            for (i in 0 until n) {
                val x = right - step * i
                lineTo(x - step * 0.5f, bot - h * 0.045f)
                lineTo(x - step, bot)
            }
            close()
        }
        drawPath(p, color, style = Stroke(sw, cap = cap))
        drawLine(color, Offset(left + w * 0.07f, top + h * 0.16f), Offset(right - w * 0.07f, top + h * 0.16f), sw * 0.7f, cap)
        drawLine(color.copy(alpha = 0.6f), Offset(left + w * 0.07f, top + h * 0.28f), Offset(right - w * 0.16f, top + h * 0.28f), sw * 0.7f, cap)
    }
}

/** A shopping bag whose handle sways. */
private fun DrawScope.glyphBag(color: Color, t: Float) {
    val w = size.width
    val h = size.height
    val sw = size.minDimension * 0.09f
    val cap = StrokeCap.Round
    rotate(sin(t * TwoPi) * 3.5f, pivot = Offset(w * 0.5f, h * 0.30f)) {
        val left = w * 0.26f
        val right = w * 0.74f
        val top = h * 0.40f
        val bot = h * 0.78f
        drawRoundRect(
            color,
            topLeft = Offset(left, top),
            size = Size(right - left, bot - top),
            cornerRadius = CornerRadius(sw),
            style = Stroke(sw, cap = cap),
        )
        drawArc(
            color, 200f, 140f, false,
            topLeft = Offset(w * 0.37f, h * 0.22f),
            size = Size(w * 0.26f, w * 0.26f),
            style = Stroke(sw, cap = cap),
        )
    }
}

/** A heart with a soft heartbeat pulse. */
private fun DrawScope.glyphHeart(color: Color, t: Float) {
    val w = size.width
    val h = size.height
    val cx = w / 2f
    val beat = 1f + 0.09f * sin(t * TwoPi)
    scale(beat, beat, pivot = Offset(cx, h * 0.5f)) {
        val r = w * 0.16f
        val cyTop = h * 0.40f
        val lx = cx - r * 0.92f
        val rx = cx + r * 0.92f
        drawCircle(color, r, Offset(lx, cyTop))
        drawCircle(color, r, Offset(rx, cyTop))
        val tri = Path().apply {
            moveTo(lx - r * 0.96f, cyTop + r * 0.15f)
            lineTo(rx + r * 0.96f, cyTop + r * 0.15f)
            lineTo(cx, cyTop + r * 2.5f)
            close()
        }
        drawPath(tri, color)
    }
}

/** A play button in a rounded frame, pulsing, with an orbiting spark. */
private fun DrawScope.glyphPlay(color: Color, t: Float) {
    val w = size.width
    val h = size.height
    val cx = w / 2f
    val cy = h / 2f
    val sw = size.minDimension * 0.09f
    drawRoundRect(
        color.copy(alpha = 0.9f),
        topLeft = Offset(w * 0.22f, h * 0.22f),
        size = Size(w * 0.56f, w * 0.56f),
        cornerRadius = CornerRadius(sw * 1.6f),
        style = Stroke(sw),
    )
    val pulse = 1f + 0.06f * sin(t * TwoPi)
    scale(pulse, pulse, pivot = Offset(cx, cy)) {
        val tri = Path().apply {
            moveTo(w * 0.43f, h * 0.38f)
            lineTo(w * 0.43f, h * 0.62f)
            lineTo(w * 0.64f, h * 0.50f)
            close()
        }
        drawPath(tri, color)
    }
    val ang = t * TwoPi
    drawCircle(color.copy(alpha = 0.7f), sw * 0.5f, Offset(cx + cos(ang) * w * 0.30f, cy + sin(ang) * w * 0.30f))
}

/** A rising trend line with a dot travelling up it on a loop. */
private fun DrawScope.glyphTrend(color: Color, t: Float) {
    val w = size.width
    val h = size.height
    val sw = size.minDimension * 0.09f
    val cap = StrokeCap.Round
    val pts = listOf(
        Offset(w * 0.20f, h * 0.66f),
        Offset(w * 0.42f, h * 0.48f),
        Offset(w * 0.58f, h * 0.56f),
        Offset(w * 0.80f, h * 0.32f),
    )
    val path = Path().apply {
        moveTo(pts[0].x, pts[0].y)
        pts.drop(1).forEach { lineTo(it.x, it.y) }
    }
    drawPath(path, color, style = Stroke(sw, cap = cap))
    val end = pts.last()
    drawLine(color, end, Offset(w * 0.66f, h * 0.32f), sw, cap)
    drawLine(color, end, Offset(w * 0.80f, h * 0.46f), sw, cap)
    val seg = t * (pts.size - 1)
    val i = seg.toInt().coerceIn(0, pts.size - 2)
    val f = seg - i
    drawCircle(
        color, sw * 0.7f,
        Offset(pts[i].x + (pts[i + 1].x - pts[i].x) * f, pts[i].y + (pts[i + 1].y - pts[i].y) * f),
    )
}

/** Three dots rippling in sequence. */
private fun DrawScope.glyphDots(color: Color, t: Float) {
    val w = size.width
    val h = size.height
    val r = size.minDimension * 0.07f
    for (i in 0..2) {
        val phase = (t + i * 0.18f) % 1f
        val bob = sin(phase * TwoPi) * h * 0.06f
        drawCircle(color, r, Offset(w * (0.30f + i * 0.20f), h * 0.5f + bob))
    }
}
