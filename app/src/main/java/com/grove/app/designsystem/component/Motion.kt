package com.grove.app.designsystem.component

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.grove.app.designsystem.theme.GroveEase
import com.grove.app.designsystem.theme.GroveSprings
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class GroveHaptic { None, Light, Tick, Heavy }

fun GroveHaptic.perform(view: android.view.View) {
    when (this) {
        GroveHaptic.None -> Unit
        GroveHaptic.Light -> view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        GroveHaptic.Tick -> view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
        GroveHaptic.Heavy -> view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
    }
}

@Composable
fun rememberMotionEnabled(): Boolean {
    val context = androidx.compose.ui.platform.LocalContext.current
    return remember(context) {
        android.provider.Settings.Global.getFloat(
            context.contentResolver,
            android.provider.Settings.Global.ANIMATOR_DURATION_SCALE,
            1f,
        ) != 0f
    }
}

@Composable
fun animatedOnce(
    target: Float,
    spec: AnimationSpec<Float> = tween(520, easing = GroveEase.Out),
): Float {
    var played by rememberSaveable { mutableStateOf(false) }
    val anim = remember { Animatable(if (played) target else 0f) }
    LaunchedEffect(target) {
        anim.animateTo(target, spec)
        played = true
    }
    return anim.value
}

fun Modifier.groveClick(
    enabled: Boolean = true,
    role: Role? = null,
    haptic: GroveHaptic = GroveHaptic.Tick,
    onClick: () -> Unit,
): Modifier =
    composed {
        val view = LocalView.current
        val interactionSource = remember { MutableInteractionSource() }
        val pressed by interactionSource.collectIsPressedAsState()
        val scale by animateFloatAsState(
            targetValue = if (enabled && pressed) 0.96f else 1f,
            animationSpec = GroveSprings.snappy(),
            label = "groveClickScale",
        )
        val alpha by animateFloatAsState(
            targetValue = if (enabled && pressed) 0.85f else 1f,
            animationSpec = GroveEase.fast(),
            label = "groveClickAlpha",
        )
        graphicsLayer {
            scaleX = scale
            scaleY = scale
            this.alpha = alpha
        }.clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            role = role,
        ) {
            haptic.perform(view)
            onClick()
        }
    }

fun Modifier.groveFadeSlide(
    key: Any? = Unit,
    delayMillis: Int = 0,
    distance: Dp = 12.dp,
    durationMillis: Int = 420,
): Modifier =
    composed {
        val density = LocalDensity.current
        val distancePx = with(density) { distance.toPx() }
        val alpha = remember { Animatable(0f) }
        val y = remember { Animatable(distancePx) }
        LaunchedEffect(key) {
            alpha.snapTo(0f)
            y.snapTo(distancePx)
            if (delayMillis > 0) delay(delayMillis.toLong())
            coroutineScope {
                launch { alpha.animateTo(1f, tween(durationMillis, easing = GroveEase.Out)) }
                launch { y.animateTo(0f, tween(durationMillis, easing = GroveEase.Out)) }
            }
        }
        graphicsLayer {
            this.alpha = alpha.value
            translationY = y.value
        }
    }

@Composable
fun rememberBottomNavVisibilityConnection(onVisibleChange: (Boolean) -> Unit): NestedScrollConnection {
    val latestOnVisibleChange by rememberUpdatedState(onVisibleChange)
    var visible by remember { mutableStateOf(true) }
    var accumulated by remember { mutableFloatStateOf(0f) }
    return remember {
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                accumulated = (accumulated + available.y).coerceIn(-48f, 48f)
                val nextVisible =
                    when {
                        accumulated < -12f -> false
                        accumulated > 10f -> true
                        else -> visible
                    }
                if (nextVisible != visible) {
                    visible = nextVisible
                    latestOnVisibleChange(nextVisible)
                }
                return Offset.Zero
            }
        }
    }
}
