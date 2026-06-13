package com.grove.app.feature.home

import android.graphics.Matrix as AndroidMatrix
import android.graphics.Paint as AndroidPaint
import android.graphics.Path as AndroidPath
import android.graphics.PathMeasure as AndroidPathMeasure
import android.graphics.RectF as AndroidRectF
import android.graphics.Typeface
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.core.content.res.ResourcesCompat
import com.grove.app.R
import com.grove.app.designsystem.component.charts.ArcProgress
import com.grove.app.designsystem.theme.GroveTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.min

/**
 * Cold-start launch ceremony, played as a full-screen Compose overlay above the app.
 *
 * The splash ring starts as a compact sibling of the live dashboard hero ring, then
 * scales and translates into the measured [heroRingBounds]. The dashboard ring holds
 * its real value underneath, so once the overlay dissolves the dashboard simply
 * appears instead of running a second launch animation.
 */
private const val SMALL_RING_SCALE = 0.42f
private const val INITIAL_STROKE_DP = 11f
private const val HERO_STROKE_DP = 18f
private const val DRAW_DURATION = 760
private const val WORD_WRITE_DURATION = 540
private const val WORD_FILL_DURATION = 200
private const val WORD_SETTLE_MS = 120
private const val MARK_OUT_DURATION = 220
private const val INITIAL_VISUAL_WAIT_MS = 240
private const val READY_POLL_MS = 40
private const val MAX_READY_WAIT_MS = 2200
private const val SETTLE_AFTER_READY_MS = 200
// The morph + the locked dashboard reveal share this duration — kept deliberately
// graceful so the hand-off reads as a premium settle rather than a snap.
private const val MORPH_DURATION = 820
private const val MORPH_PROGRESS_REBASE_DURATION = 160
private const val REVEAL_FADE_DURATION = 260

@Composable
fun SplashRing(
    heroRingBounds: Rect?,
    heroRingColor: Color?,
    heroRingColorDeep: Color?,
    heroRingPct: Float?,
    contentReady: Boolean,
    onRevealProgressChange: (Float) -> Unit,
    onRingHandoffReady: () -> Unit,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = GroveTheme.colors
    val density = LocalDensity.current
    val finished = rememberUpdatedState(onFinished)
    val ready = rememberUpdatedState(contentReady)
    val latestHeroBounds = rememberUpdatedState(heroRingBounds)
    val latestHeroColor = rememberUpdatedState(heroRingColor)
    val latestHeroColorDeep = rememberUpdatedState(heroRingColorDeep)
    val latestHeroPct = rememberUpdatedState(heroRingPct)
    val revealProgress = rememberUpdatedState(onRevealProgressChange)
    val ringHandoffReady = rememberUpdatedState(onRingHandoffReady)

    val ringFraction = remember { Animatable(0f) }
    val wordWrite = remember { Animatable(0f) }
    val wordFill = remember { Animatable(0f) }
    val wordAlpha = remember { Animatable(1f) }
    val wordRise = remember { Animatable(3f) }
    val morph = remember { Animatable(0f) }
    val overlayAlpha = remember { Animatable(1f) }
    val dashboardReveal = remember { Animatable(0f) }

    val emphDecel = remember { CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f) }
    val emphAccel = remember { CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f) }
    // Balanced ease-in-out — distributes motion across the whole duration instead of the
    // front-loaded emphDecel (which whips to ~90% then crawls and reads as a near-pop).
    val smooth = remember { CubicBezierEasing(0.4f, 0f, 0.2f, 1f) }

    var rootSize by remember { mutableStateOf(IntSize.Zero) }
    var morphTarget by remember { mutableStateOf<Rect?>(null) }
    var morphColor by remember { mutableStateOf<Color?>(null) }
    var morphColorDeep by remember { mutableStateOf<Color?>(null) }
    var morphPct by remember { mutableStateOf<Float?>(null) }

    SideEffect {
        revealProgress.value(dashboardReveal.value.coerceIn(0f, 1f))
    }

    LaunchedEffect(Unit) {
        var visualWaited = 0
        while (
            (latestHeroColor.value == null ||
                latestHeroColorDeep.value == null ||
                latestHeroPct.value == null) &&
            visualWaited < INITIAL_VISUAL_WAIT_MS
        ) {
            delay(READY_POLL_MS.toLong())
            visualWaited += READY_POLL_MS
        }

        morphColor = latestHeroColor.value
        morphColorDeep = latestHeroColorDeep.value
        morphPct = latestHeroPct.value?.coerceIn(0f, 1f)

        ringFraction.animateTo(1f, tween(DRAW_DURATION, easing = smooth))

        launch { wordRise.animateTo(0f, tween(WORD_WRITE_DURATION, easing = emphDecel)) }
        wordWrite.animateTo(1f, tween(WORD_WRITE_DURATION, easing = emphDecel))
        wordFill.animateTo(1f, tween(WORD_FILL_DURATION, easing = emphDecel))
        delay(WORD_SETTLE_MS.toLong())

        var waited = 0
        while (
            (!ready.value ||
                latestHeroBounds.value == null ||
                latestHeroColor.value == null ||
                latestHeroColorDeep.value == null ||
                latestHeroPct.value == null) &&
            waited < MAX_READY_WAIT_MS
        ) {
            delay(READY_POLL_MS.toLong())
            waited += READY_POLL_MS
        }
        delay(SETTLE_AFTER_READY_MS.toLong())

        morphTarget = latestHeroBounds.value
        morphColor = latestHeroColor.value
        morphColorDeep = latestHeroColorDeep.value
        morphPct = latestHeroPct.value?.coerceIn(0f, 1f)

        val fadeWord = launch { wordAlpha.animateTo(0f, tween(MARK_OUT_DURATION, easing = emphAccel)) }

        if (morphTarget == null) {
            val revealDashboard = launch {
                dashboardReveal.animateTo(1f, tween(REVEAL_FADE_DURATION, easing = emphDecel))
            }
            overlayAlpha.animateTo(0f, tween(REVEAL_FADE_DURATION, easing = emphAccel))
            revealDashboard.join()
            ringHandoffReady.value()
            finished.value()
            return@LaunchedEffect
        }

        // During handoff, the dashboard owns the empty groove and the splash owns the
        // fill. Rebase the splash fill early, then draw it into the dashboard ring slot
        // so the final stroke reads as one continuous object rather than two rings.
        val settleProgress = launch {
            ringFraction.animateTo(0f, tween(MORPH_PROGRESS_REBASE_DURATION, easing = emphAccel))
            ringFraction.animateTo(
                morphPct ?: 1f,
                tween((MORPH_DURATION - MORPH_PROGRESS_REBASE_DURATION).coerceAtLeast(0), easing = smooth),
            )
        }
        val revealDashboard = launch {
            dashboardReveal.animateTo(1f, tween(MORPH_DURATION, easing = smooth))
        }
        morph.animateTo(1f, tween(MORPH_DURATION, easing = smooth))
        settleProgress.join()
        ringHandoffReady.value()
        revealDashboard.join()
        fadeWord.join()
        overlayAlpha.animateTo(0f, tween(REVEAL_FADE_DURATION, easing = emphAccel))
        finished.value()
    }

    val m = morph.value
    val dashboardRevealValue = dashboardReveal.value.coerceIn(0f, 1f)
    val splashObjectAlpha = overlayAlpha.value

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { rootSize = it },
    ) {
        // No opaque background here on purpose: HomeScaffold already paints a constant
        // opaque bgApp behind everything, and the dashboard content is hidden via its
        // own reveal alpha during the draw. Painting a *second* fading bg on top is what
        // caused the "fade through black" — the half-faded dashboard was being darkened
        // by this overlay. With it gone, the dashboard simply fades in over one constant
        // background while the splash ring hands off.

        val rootCx = rootSize.width / 2f
        val rootCy = rootSize.height / 2f
        val drawCx = rootCx
        val drawCy = rootSize.height * 0.45f
        val target = morphTarget
        val fallbackHeroPx = with(density) {
            val widthPx = if (rootSize.width > 0) rootSize.width.toFloat() else 360.dp.toPx()
            (widthPx * 0.64f).coerceIn(216.dp.toPx(), 280.dp.toPx())
        }
        val heroDiameterPx = target?.let { min(it.width, it.height).coerceAtLeast(1f) } ?: fallbackHeroPx
        val heroDiameterDp = with(density) { heroDiameterPx.toDp() }
        val initialDiameterPx = heroDiameterPx * SMALL_RING_SCALE
        val initialDiameterDp = with(density) { initialDiameterPx.toDp() }
        val scale = lerp(SMALL_RING_SCALE, 1f, m).coerceAtLeast(0.01f)
        val visualStroke = lerp(INITIAL_STROKE_DP, HERO_STROKE_DP, m)
        val strokeBeforeScale = visualStroke / scale
        val curCx = lerp(drawCx, target?.center?.x ?: drawCx, m)
        val curCy = lerp(drawCy, target?.center?.y ?: drawCy, m)
        val markFade = 1f - (m / 0.32f).coerceIn(0f, 1f)
        val ringColor = morphColor ?: heroRingColor ?: c.success
        val ringColorDeep = morphColorDeep ?: heroRingColorDeep ?: ringColor

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(heroDiameterDp)
                .graphicsLayer {
                    alpha = splashObjectAlpha
                    translationX = curCx - rootCx
                    translationY = curCy - rootCy
                    scaleX = scale
                    scaleY = scale
                },
        ) {
            ArcProgress(
                pct = 1f,
                color = ringColor,
                colorDeep = ringColorDeep,
                modifier = Modifier.fillMaxSize(),
                stroke = strokeBeforeScale,
                animationKey = "splash",
                progressOverride = ringFraction.value,
            ) {}
        }

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(initialDiameterDp)
                .graphicsLayer {
                    translationX = curCx - rootCx
                    translationY = curCy - rootCy + wordRise.value.dp.toPx()
                    alpha = wordAlpha.value * markFade * splashObjectAlpha
                },
            contentAlignment = Alignment.Center,
        ) {
            GroveWordmarkWriteOn(
                progress = wordWrite.value,
                fillAlpha = wordFill.value,
                color = c.fg1,
                modifier = Modifier.size(
                    width = initialDiameterDp * 0.62f,
                    height = initialDiameterDp * 0.22f,
                ),
            )
        }
    }
}

@Composable
private fun GroveWordmarkWriteOn(
    progress: Float,
    fillAlpha: Float,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val typeface = remember(context) {
        ResourcesCompat.getFont(context, R.font.inter_tight)
            ?: Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
    }
    val drawProgress = progress.coerceIn(0f, 1f)
    val fillProgress = fillAlpha.coerceIn(0f, 1f)
    val strokeColor = color.copy(alpha = 0.68f * (1f - fillProgress * 0.45f))
    val fillColor = color.copy(alpha = fillProgress * 0.9f)

    Canvas(modifier = modifier) {
        val textPath = buildWordmarkPath(typeface, size.width, size.height)
        val contours = measureWordmarkContours(textPath)
        val totalLength = contours.sumOf { it.length.toDouble() }.toFloat()
        val strokeWidth = size.height * 0.035f
        val revealWidth = size.width * drawProgress
        val revealFillAlpha = (drawProgress * 0.76f + fillProgress * 0.24f).coerceIn(0f, 1f)
        val strokePaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            style = AndroidPaint.Style.STROKE
            strokeCap = AndroidPaint.Cap.ROUND
            strokeJoin = AndroidPaint.Join.ROUND
            this.strokeWidth = strokeWidth
            this.color = strokeColor.toArgb()
        }
        val fillPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            style = AndroidPaint.Style.FILL
            this.color = fillColor.copy(alpha = revealFillAlpha).toArgb()
        }
        val inkPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            style = AndroidPaint.Style.FILL
            this.color = strokeColor.copy(alpha = strokeColor.alpha * 0.85f).toArgb()
        }

        var remaining = totalLength * drawProgress
        var lead: Offset? = null

        drawIntoCanvas { canvas ->
            if (revealWidth > 0f) {
                canvas.nativeCanvas.save()
                canvas.nativeCanvas.clipRect(0f, 0f, revealWidth, size.height)
                canvas.nativeCanvas.drawPath(textPath, fillPaint)
                canvas.nativeCanvas.restore()
            }

            contours.forEach { contour ->
                if (remaining <= 0f) return@forEach
                val stop = remaining.coerceAtMost(contour.length)
                val segment = AndroidPath()
                val measure = AndroidPathMeasure(contour.path, false)
                if (measure.getSegment(0f, stop, segment, true)) {
                    segment.rLineTo(0f, 0f)
                    canvas.nativeCanvas.drawPath(segment, strokePaint)
                    if (stop < contour.length || drawProgress >= 1f) {
                        val pos = FloatArray(2)
                        if (measure.getPosTan(stop, pos, null)) {
                            lead = Offset(pos[0], pos[1])
                        }
                    }
                }
                remaining -= contour.length
            }

            lead?.let { point ->
                if (drawProgress < 0.995f) {
                    canvas.nativeCanvas.drawCircle(point.x, point.y, strokeWidth * 0.72f, inkPaint)
                }
            }
        }
    }
}

private data class WordmarkContour(
    val path: AndroidPath,
    val length: Float,
)

private fun buildWordmarkPath(
    typeface: Typeface,
    width: Float,
    height: Float,
): AndroidPath {
    val paint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
        this.typeface = typeface
        textSize = height * 0.78f
        textAlign = AndroidPaint.Align.LEFT
    }
    val raw = AndroidPath()
    paint.getTextPath("Grove", 0, "Grove".length, 0f, 0f, raw)

    val bounds = AndroidRectF()
    raw.computeBounds(bounds, true)
    if (bounds.isEmpty) return raw

    val targetWidth = width * 0.9f
    val targetHeight = height * 0.72f
    val scale = min(targetWidth / bounds.width(), targetHeight / bounds.height())
    val matrix = AndroidMatrix().apply {
        setTranslate(-bounds.left, -bounds.top)
        postScale(scale, scale)
        postTranslate(
            (width - bounds.width() * scale) / 2f,
            (height - bounds.height() * scale) / 2f,
        )
    }
    return AndroidPath().also { raw.transform(matrix, it) }
}

private fun measureWordmarkContours(path: AndroidPath): List<WordmarkContour> {
    val measure = AndroidPathMeasure(path, false)
    val contours = mutableListOf<WordmarkContour>()
    do {
        val length = measure.length
        if (length > 0f) {
            val contourPath = AndroidPath()
            measure.getSegment(0f, length, contourPath, true)
            contourPath.rLineTo(0f, 0f)
            contours += WordmarkContour(contourPath, length)
        }
    } while (measure.nextContour())
    return contours
}
