package com.grove.app.designsystem.component.charts

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grove.app.designsystem.catalog.CategoryVisuals
import com.grove.app.designsystem.component.animatedOnce
import com.grove.app.designsystem.format.Money
import com.grove.app.designsystem.theme.GroveTheme
import com.grove.app.designsystem.theme.GroveShapes
import com.grove.app.designsystem.theme.GroveSpacing
import com.grove.app.designsystem.theme.InterTight
import kotlin.math.atan2

private val DrawInSpec: AnimationSpec<Float> = tween(1100, easing = EaseOutCubic)
private val BarSpec: AnimationSpec<Float> = tween(600, easing = EaseOutCubic)

@Composable
fun ArcProgress(
    pct: Float,
    color: Color,
    colorDeep: Color,
    modifier: Modifier = Modifier,
    stroke: Float = 12f,
    content: @Composable () -> Unit,
) {
    val c = GroveTheme.colors
    val animPct = animatedOnce(pct, DrawInSpec)
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val dim = size.minDimension
            val strokePx = stroke.dp.toPx()
            val radius = (dim - strokePx) / 2
            val center = Offset(size.width / 2, size.height / 2)
            val topLeft = Offset(center.x - radius, center.y - radius)
            val arcSize = Size(radius * 2, radius * 2)
            drawArc(c.bone, 135f, 270f, false, topLeft, arcSize, style = Stroke(strokePx, cap = StrokeCap.Round))
            if (animPct > 0) {
                drawArc(
                    Brush.linearGradient(listOf(color.copy(alpha = 0.85f), colorDeep)),
                    135f, 270f * animPct, false, topLeft, arcSize, style = Stroke(strokePx, cap = StrokeCap.Round),
                )
            }
        }
        content()
    }
}

@Composable
fun DonutChart(
    data: List<Pair<String, Double>>,
    total: Double,
    selected: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = GroveTheme.colors
    if (data.isEmpty() || total <= 0) return
    val selData = selected?.let { sel -> data.find { it.first == sel } }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(
            modifier = Modifier.fillMaxSize().pointerInput(data, total) {
                detectTapGestures { offset ->
                    val angle = (Math.toDegrees(atan2((offset.y - size.height / 2f).toDouble(), (offset.x - size.width / 2f).toDouble())) + 360 + 90) % 360
                    var acc = 0.0
                    for ((id, amount) in data) {
                        val sweep = amount / total * 360.0
                        if (angle >= acc && angle < acc + sweep) { onSelect(id); break }
                        acc += sweep
                    }
                }
            },
        ) {
            val dim = size.minDimension
            val baseStroke = dim * 0.17f
            val radius = (dim - baseStroke - dim * 0.04f) / 2
            val center = Offset(size.width / 2, size.height / 2)
            val topLeft = Offset(center.x - radius, center.y - radius)
            val arcSize = Size(radius * 2, radius * 2)
            drawArc(c.bgMuted, 0f, 360f, false, topLeft, arcSize, style = Stroke(baseStroke))
            var startAngle = -90f
            data.forEach { (id, amount) ->
                val sweep = (amount / total * 360f).toFloat()
                val isSel = selected == id
                val dimmed = selected != null && !isSel
                val strokeW = if (isSel) baseStroke + dim * 0.04f else baseStroke
                drawArc(
                    CategoryVisuals.color(id).copy(alpha = if (dimmed) 0.28f else 1f),
                    startAngle, sweep, false, topLeft, arcSize, style = Stroke(strokeW),
                )
                startAngle += sweep
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                if (selData != null) CategoryVisuals.label(selData.first) else "SPENT",
                fontFamily = InterTight, fontWeight = FontWeight.Medium, fontSize = 11.sp,
                letterSpacing = 0.8.sp, color = c.fg3,
            )
            Text(Money.currency(selData?.second ?: total, 0), fontFamily = InterTight, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, color = c.fg1)
            if (selData != null) {
                Text("${(selData.second / total * 100).toInt()}%", fontFamily = InterTight, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = c.fg3)
            }
        }
    }
}

@Composable
fun LineChart(data: List<Double>, baseline: Double, monthShort: String = "", modifier: Modifier = Modifier) {
    val c = GroveTheme.colors
    if (data.isEmpty()) return
    val density = LocalDensity.current
    var hover by remember { mutableStateOf<Int?>(null) }
    var widthPx by remember { mutableStateOf(0f) }

    Box(
        modifier = modifier.pointerInput(data.size) {
            widthPx = size.width.toFloat()
            detectHorizontalScrub(
                onScrub = { x -> hover = Math.round((x / size.width).coerceIn(0f, 1f) * (data.size - 1)) },
                onEnd = { hover = null },
            )
        },
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            widthPx = size.width
            val maxV = (data.maxOrNull() ?: 0.0).coerceAtLeast(baseline) * 1.1
            val padY = 8f
            val stepX = size.width / (data.size - 1).coerceAtLeast(1)
            val pts = data.mapIndexed { i, v ->
                Offset(i * stepX, size.height - padY - ((v / maxV) * (size.height - padY * 2)).toFloat())
            }
            val baselineY = size.height - padY - ((baseline / maxV) * (size.height - padY * 2)).toFloat()
            drawLine(c.fg3.copy(alpha = 0.4f), Offset(0f, baselineY), Offset(size.width, baselineY), strokeWidth = 1f)
            val area = Path().apply {
                moveTo(pts.first().x, size.height - padY)
                pts.forEach { lineTo(it.x, it.y) }
                lineTo(pts.last().x, size.height - padY); close()
            }
            drawPath(area, c.accent.copy(alpha = 0.22f), style = Fill)
            val line = Path().apply { pts.forEachIndexed { i, pt -> if (i == 0) moveTo(pt.x, pt.y) else lineTo(pt.x, pt.y) } }
            drawPath(line, c.accent, style = Stroke(width = 2.5f))
            hover?.let { h ->
                val pt = pts[h]
                drawLine(c.accent.copy(alpha = 0.4f), Offset(pt.x, 0f), Offset(pt.x, size.height), strokeWidth = 1f)
                drawCircle(c.bgCard, radius = 7f, center = pt)
                drawCircle(c.accent, radius = 5f, center = pt)
            } ?: run { if (pts.isNotEmpty()) drawCircle(c.accent, radius = 4f, center = pts.last()) }
        }
        hover?.let { h ->
            val frac = if (data.size > 1) h.toFloat() / (data.size - 1) else 0f
            val xDp = with(density) { (frac * widthPx).toDp() }
            Box(
                modifier = Modifier
                    .offset(x = xDp - 30.dp, y = (-6).dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(c.fg1)
                    .padding(horizontal = 9.dp, vertical = 5.dp),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "$monthShort ${h + 1}".trim().uppercase(),
                        fontFamily = InterTight, fontSize = 9.5.sp, letterSpacing = 0.4.sp,
                        color = c.bgCard.copy(alpha = 0.7f),
                    )
                    Text(Money.currency(data[h], 2), fontFamily = InterTight, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = c.bgCard)
                }
            }
        }
    }
}

data class MonthBar(val label: String, val value: Double, val now: Boolean = false)

@Composable
fun MonthBars(months: List<MonthBar>, modifier: Modifier = Modifier) {
    val c = GroveTheme.colors
    val max = months.maxOf { it.value }.coerceAtLeast(1.0)
    Row(modifier = modifier.fillMaxWidth().height(120.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Bottom) {
        months.forEach { mo ->
            Column(modifier = Modifier.weight(1f).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
                    val frac = animatedOnce((mo.value / max).toFloat(), BarSpec)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.7f).widthIn(max = 40.dp)
                            .fillMaxHeight(frac.coerceAtLeast(0.05f))
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 4.dp, bottomEnd = 4.dp))
                            .background(if (mo.now) c.accent else c.bone),
                    )
                }
                Text(
                    mo.label,
                    fontFamily = InterTight,
                    fontSize = 11.5.sp,
                    fontWeight = if (mo.now) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (mo.now) c.fg1 else c.fg3,
                    modifier = Modifier.padding(top = GroveSpacing.SM),
                )
            }
        }
    }
}

private suspend fun PointerInputScope.detectHorizontalScrub(onScrub: (Float) -> Unit, onEnd: () -> Unit) {
    awaitPointerEventScope {
        while (true) {
            val event = awaitPointerEvent()
            val change = event.changes.firstOrNull() ?: continue
            if (change.pressed) onScrub(change.position.x) else onEnd()
        }
    }
}
