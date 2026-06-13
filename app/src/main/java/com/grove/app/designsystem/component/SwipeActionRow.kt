package com.grove.app.designsystem.component

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.grove.app.designsystem.theme.GroveShapes
import com.grove.app.designsystem.theme.GroveSize
import com.grove.app.designsystem.theme.GroveSpacing
import com.grove.app.designsystem.theme.GroveTheme
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private enum class SwipeValue { Closed, Open }

data class SwipeAction(
    val icon: ImageVector,
    val label: String,
    val background: Color,
    val foreground: Color,
    val haptic: GroveHaptic = GroveHaptic.Tick,
    val onClick: () -> Unit,
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SwipeActionRow(
    actions: List<SwipeAction>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(vertical = GroveSpacing.MD),
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    onClick: (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit,
) {
    val c = GroveTheme.colors
    val density = LocalDensity.current
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    val revealPx =
        with(density) {
            GroveSize.SwipeAction.toPx() * actions.size +
                GroveSpacing.SM.toPx() * (actions.size - 1).coerceAtLeast(0) +
                4.dp.toPx()
        }
    val swipeState =
        remember(revealPx) {
            val anchors =
            DraggableAnchors {
                SwipeValue.Closed at 0f
                SwipeValue.Open at -revealPx
            }
            AnchoredDraggableState(
                initialValue = SwipeValue.Closed,
                anchors = anchors,
                positionalThreshold = { distance -> distance * 0.5f },
                velocityThreshold = { with(density) { 80.dp.toPx() } },
                snapAnimationSpec = spring(dampingRatio = 0.68f, stiffness = Spring.StiffnessMediumLow),
                decayAnimationSpec = exponentialDecay(),
            )
        }
    LaunchedEffect(swipeState.currentValue) {
        if (swipeState.currentValue == SwipeValue.Open) {
            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
        }
    }
    val offsetX = if (swipeState.offset.isNaN()) 0f else swipeState.requireOffset()
    val revealProgress = (-offsetX / revealPx).coerceIn(0f, 1f)

    var lastNotch by remember { mutableIntStateOf(0) }
    LaunchedEffect(revealProgress) {
        val notch = (revealProgress * 4f).toInt()
        if (notch != lastNotch) {
            if (notch > lastNotch) view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            lastNotch = notch
        }
    }

    Box(modifier = modifier.fillMaxWidth().clipToBounds()) {
        Row(
            modifier = Modifier.matchParentSize().padding(end = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(GroveSpacing.SM, Alignment.End),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            actions.forEach { action ->
                SwipeActionButton(
                    action = action,
                    modifier = Modifier.graphicsLayer {
                        val emphasis = 0.6f + 0.4f * revealProgress
                        scaleX = emphasis
                        scaleY = emphasis
                        alpha = (revealProgress * 1.8f).coerceAtMost(1f)
                    },
                ) {
                    action.onClick()
                    scope.launch { swipeState.animateTo(SwipeValue.Closed) }
                }
            }
        }
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .offset { IntOffset(offsetX.roundToInt(), 0) }
                    .background(c.bgCard)
                    .anchoredDraggable(
                        state = swipeState,
                        reverseDirection = false,
                        orientation = Orientation.Horizontal,
                    ).groveClick(
                        haptic =
                            if (onClick != null && swipeState.currentValue == SwipeValue.Closed) {
                                GroveHaptic.Tick
                            } else {
                                GroveHaptic.None
                            },
                    ) {
                        if (swipeState.currentValue != SwipeValue.Closed || swipeState.targetValue != SwipeValue.Closed) {
                            scope.launch { swipeState.animateTo(SwipeValue.Closed) }
                        } else {
                            onClick?.invoke()
                        }
                    }.padding(contentPadding),
            verticalAlignment = verticalAlignment,
            content = content,
        )
    }
}

@Composable
private fun SwipeActionButton(
    action: SwipeAction,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier =
            modifier
                .size(GroveSize.SwipeAction)
                .clip(GroveShapes.Chip)
                .background(action.background)
                .groveClick(role = Role.Button, haptic = action.haptic) { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(action.icon, contentDescription = action.label, tint = action.foreground, modifier = Modifier.size(19.dp))
    }
}
