package com.grove.app.designsystem.component.charts

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grove.app.designsystem.catalog.CategoryVisuals
import com.grove.app.designsystem.component.animatedOnce
import com.grove.app.core.format.Currencies
import com.grove.app.core.format.Money
import com.grove.app.designsystem.theme.GroveEase
import com.grove.app.designsystem.theme.GroveColors
import com.grove.app.designsystem.theme.GroveTheme
import com.grove.app.designsystem.theme.GroveShapes
import com.grove.app.designsystem.theme.GroveSpacing
import com.grove.app.designsystem.theme.JetBrainsMono
import com.grove.app.designsystem.theme.SpaceGrotesk
import kotlinx.coroutines.delay
import kotlin.math.atan2

private const val ArcDefaultDurationMillis = 800
private val BarSpec = GroveEase.slow<Float>()

data class DonutSlice(
    val id: String,
    val label: String,
    val color: Color,
    val amount: Double,
)

@Composable
fun ArcProgress(
    pct: Float,
    color: Color,
    colorDeep: Color,
    modifier: Modifier = Modifier,
    stroke: Float = 12f,
    animationKey: Any? = pct,
    fromPct: Float? = null,
    startDelayMillis: Int = 0,
    durationMillis: Int = ArcDefaultDurationMillis,
    easing: Easing = GroveEase.Out,
    progressOverride: Float? = null,
    snapToTargetWhenOverrideClears: Boolean = false,
    content: @Composable () -> Unit,
) {
    val c = GroveTheme.colors
    var played by remember { mutableStateOf(false) }
    var hadProgressOverride by remember { mutableStateOf(false) }
    val animPct = remember { Animatable(if (fromPct != null) fromPct.coerceIn(0f, 1f) else 0f) }
    val drawPct = progressOverride?.coerceIn(0f, 1f) ?: animPct.value

    LaunchedEffect(
        pct,
        animationKey,
        fromPct,
        startDelayMillis,
        durationMillis,
        easing,
        progressOverride,
        snapToTargetWhenOverrideClears,
    ) {
        if (progressOverride != null) {
            animPct.snapTo(progressOverride.coerceIn(0f, 1f))
            played = true
            hadProgressOverride = true
            return@LaunchedEffect
        }
        if (hadProgressOverride && snapToTargetWhenOverrideClears) {
            animPct.snapTo(pct.coerceIn(0f, 1f))
            played = true
            hadProgressOverride = false
            return@LaunchedEffect
        }
        hadProgressOverride = false
        if (fromPct != null) {
            animPct.snapTo(fromPct.coerceIn(0f, 1f))
        } else if (!played) {
            animPct.snapTo(0f)
        }
        if (startDelayMillis > 0) delay(startDelayMillis.toLong())
        animPct.animateTo(
            targetValue = pct.coerceIn(0f, 1f),
            animationSpec = tween(durationMillis.coerceAtLeast(0), easing = easing),
        )
        played = true
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val dim = size.minDimension
            val strokePx = stroke.dp.toPx()
            drawGroveArcProgress(
                colors = c,
                pct = drawPct,
                color = color,
                colorDeep = colorDeep,
                strokePx = strokePx,
                center = Offset(size.width / 2, size.height / 2),
                diameterPx = dim,
            )
        }
        content()
    }
}

internal fun DrawScope.drawGroveArcProgress(
    colors: GroveColors,
    pct: Float,
    color: Color,
    colorDeep: Color,
    strokePx: Float,
    center: Offset,
    diameterPx: Float,
    drawTrack: Boolean = true,
) {
    val drawPct = pct.coerceIn(0f, 1f)
    val radius = ((diameterPx - strokePx) / 2f).coerceAtLeast(0f)
    val topLeft = Offset(center.x - radius, center.y - radius)
    val arcSize = Size(radius * 2f, radius * 2f)

    if (drawTrack) {
        drawCircle(colors.bgCard, radius = radius, center = center, style = Stroke(strokePx))
        drawCircle(
            lerp(colors.bgCard, if (colors.isDark) Color.Black else colors.borderStrong, 0.35f).copy(alpha = 0.5f),
            radius = radius - strokePx * 0.40f,
            center = center,
            style = Stroke(strokePx * 0.10f),
        )
        drawCircle(
            lerp(colors.bgCard, Color.White, if (colors.isDark) 0.06f else 0.7f).copy(alpha = 0.4f),
            radius = radius + strokePx * 0.40f,
            center = center,
            style = Stroke(strokePx * 0.08f),
        )
    }

    if (drawPct <= 0f || radius <= 0f) return

    val sweep = 360f * drawPct
    val faceHi = lerp(color, Color.White, 0.45f)
    val innerGlow = lerp(color, Color.White, 0.65f)

    drawArc(color.copy(alpha = 0.10f), -90f, sweep, false, topLeft, arcSize, style = Stroke(strokePx * 1.30f, cap = StrokeCap.Round))
    drawArc(color.copy(alpha = 0.05f), -90f, sweep, false, topLeft, arcSize, style = Stroke(strokePx * 1.75f, cap = StrokeCap.Round))

    drawArc(Brush.linearGradient(listOf(color, colorDeep)), -90f, sweep, false, topLeft, arcSize, style = Stroke(strokePx, cap = StrokeCap.Round))

    drawArc(faceHi.copy(alpha = 0.30f), -90f, sweep, false, topLeft, arcSize, style = Stroke(strokePx * 0.46f, cap = StrokeCap.Round))
    drawArc(innerGlow.copy(alpha = 0.12f), -90f, sweep, false, topLeft, arcSize, style = Stroke(strokePx * 0.26f, cap = StrokeCap.Round))

    drawArc(
        Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.24f), Color.Transparent)),
        -90f,
        sweep,
        false,
        Offset(center.x - (radius + strokePx * 0.28f), center.y - (radius + strokePx * 0.28f)),
        Size((radius + strokePx * 0.28f) * 2, (radius + strokePx * 0.28f) * 2),
        style = Stroke(strokePx * 0.13f, cap = StrokeCap.Round),
    )

    val endRad = Math.toRadians((-90f + sweep).toDouble())
    val capCenter = Offset(
        center.x + (radius * kotlin.math.cos(endRad)).toFloat(),
        center.y + (radius * kotlin.math.sin(endRad)).toFloat(),
    )
    drawCircle(faceHi.copy(alpha = 0.55f), radius = strokePx * 0.13f, center = capCenter)
}

@Composable
fun DonutChart(
    data: List<DonutSlice>,
    total: Double,
    selected: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    currency: String = "INR",
) {
    val c = GroveTheme.colors
    val view = LocalView.current
    if (data.isEmpty() || total <= 0) return
    val selData = selected?.let { sel -> data.find { it.id == sel } }
    val chartSummary = remember(data, total) {
        data.sortedByDescending { it.amount }.take(4).joinToString(
            prefix = "Spending by category: ",
            separator = ", ",
        ) { slice -> "${slice.label} ${(slice.amount / total * 100).toInt()} percent" }
    }

    Box(modifier = modifier.semantics { contentDescription = chartSummary }, contentAlignment = Alignment.Center) {
        Canvas(
            modifier = Modifier.fillMaxSize().pointerInput(data, total) {
                detectTapGestures { offset ->
                    val angle = (Math.toDegrees(atan2((offset.y - size.height / 2f).toDouble(), (offset.x - size.width / 2f).toDouble())) + 360 + 90) % 360
                    var acc = 0.0
                    for (slice in data) {
                        val sweep = slice.amount / total * 360.0
                        if (angle >= acc && angle < acc + sweep) {
                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                            onSelect(slice.id)
                            break
                        }
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
            data.forEach { slice ->
                val sweep = (slice.amount / total * 360f).toFloat()
                val isSel = selected == slice.id
                val dimmed = selected != null && !isSel
                val strokeW = if (isSel) baseStroke + dim * 0.04f else baseStroke
                drawArc(
                    slice.color.copy(alpha = if (dimmed) 0.28f else 1f),
                    startAngle, sweep, false, topLeft, arcSize, style = Stroke(strokeW),
                )
                startAngle += sweep
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                if (selData != null) selData.label else "SPENT",
                fontFamily = JetBrainsMono, fontWeight = FontWeight.Medium, fontSize = 11.sp,
                letterSpacing = 0.8.sp, color = c.fg3,
            )
            Text(Money.currency(selData?.amount ?: total, 0, currency), fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, color = c.fg1)
            if (selData != null) {
                Text("${(selData.amount / total * 100).toInt()}%", fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = c.fg3)
            }
        }
    }
}

@Composable
fun LineChart(data: List<Double>, baseline: Double, modifier: Modifier = Modifier, monthShort: String = "", currency: String = "INR") {
    val c = GroveTheme.colors
    if (data.isEmpty()) return
    val density = LocalDensity.current
    val view = LocalView.current
    var hover by remember { mutableStateOf<Int?>(null) }
    var widthPx by remember { mutableFloatStateOf(0f) }
    val chartSummary = remember(data, currency) {
        val maxIdx = data.indices.maxByOrNull { data[it] } ?: 0
        "Daily spending this period, highest ${Money.currency(data[maxIdx], 0, currency)} on day ${maxIdx + 1}"
    }

    Box(
        modifier =
            modifier
                .semantics { contentDescription = chartSummary }
                .onSizeChanged { widthPx = it.width.toFloat() }
                .pointerInput(data.size) {
                    fun scrub(x: Float) {
                        val idx = Math.round((x / size.width).coerceIn(0f, 1f) * (data.size - 1))
                        if (idx != hover) {
                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                            hover = idx
                        }
                    }
                    detectDragGestures(
                        onDragStart = { offset -> scrub(offset.x) },
                        onDragEnd = { hover = null },
                        onDragCancel = { hover = null },
                        onDrag = { change, _ ->
                            scrub(change.position.x)
                            change.consume()
                        },
                    )
                },
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
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
                lineTo(pts.last().x, size.height - padY)
                close()
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
                        fontFamily = JetBrainsMono, fontSize = 9.5.sp, letterSpacing = 0.4.sp,
                        color = c.bgCard.copy(alpha = 0.7f),
                    )
                    Text(Money.currency(data[h], 2, currency), fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = c.bgCard)
                }
            }
        }
    }
}

data class MonthBar(val label: String, val value: Double, val now: Boolean = false)

@Composable
fun MonthBars(months: List<MonthBar>, modifier: Modifier = Modifier, currency: String = "INR") {
    val c = GroveTheme.colors
    val max = months.maxOf { it.value }.coerceAtLeast(1.0)
    val chartSummary = remember(months, currency) {
        months.joinToString(prefix = "Monthly totals: ", separator = ", ") { mo ->
            "${mo.label} ${Money.currency(mo.value, 0, currency)}" + if (mo.now) ", current month" else ""
        }
    }
    Row(
        modifier = modifier.fillMaxWidth().height(120.dp).semantics { contentDescription = chartSummary },
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
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
                    fontFamily = JetBrainsMono,
                    fontSize = 11.5.sp,
                    fontWeight = if (mo.now) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (mo.now) c.fg1 else c.fg3,
                    modifier = Modifier.padding(top = GroveSpacing.SM),
                )
            }
        }
    }
}
