package com.grove.app.designsystem.component

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import com.grove.app.designsystem.theme.GroveSprings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Replaces the stock overscroll glow with a foliage sway: the list translates with
 * rubber-band resistance and leans a fraction of a degree, then springs back with
 * a touch of overshoot — like a branch let go. Pair with
 * `LocalOverscrollConfiguration provides null` so the glow doesn't double up.
 */
@Composable
fun rememberFoliageOverscroll(): FoliageOverscroll {
    val scope = rememberCoroutineScope()
    return remember { FoliageOverscroll(scope) }
}

class FoliageOverscroll internal constructor(private val scope: CoroutineScope) {
    private val offset = Animatable(0f)

    val connection: NestedScrollConnection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            // While stretched, dragging back unwinds the stretch before the list scrolls.
            if (source == NestedScrollSource.UserInput && offset.value != 0f &&
                available.y != 0f && (available.y < 0f) == (offset.value > 0f)
            ) {
                val consume = if (offset.value > 0f) {
                    available.y.coerceAtLeast(-offset.value)
                } else {
                    available.y.coerceAtMost(-offset.value)
                }
                scope.launch { offset.snapTo(offset.value + consume) }
                return Offset(0f, consume)
            }
            return Offset.Zero
        }

        override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
            if (source == NestedScrollSource.UserInput && available.y != 0f) {
                val resistance = 1f / (1f + abs(offset.value) / 80f)
                val damped = available.y * resistance * 0.5f
                scope.launch { offset.snapTo(offset.value + damped) }
                return Offset(0f, available.y)
            }
            return Offset.Zero
        }

        override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
            if (offset.value != 0f || abs(available.y) > 0f) {
                offset.animateTo(0f, GroveSprings.expressive(), initialVelocity = available.y * 0.25f)
            }
            return available
        }
    }

    val modifier: Modifier
        get() = Modifier.graphicsLayer {
            translationY = offset.value
            rotationZ = (offset.value / 900f).coerceIn(-0.8f, 0.8f)
        }
}
