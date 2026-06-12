package com.grove.app.designsystem.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.grove.app.designsystem.theme.GroveShapes
import com.grove.app.designsystem.theme.GroveSize
import com.grove.app.designsystem.theme.GroveSpacing
import com.grove.app.designsystem.theme.GroveTheme

data class SwipeAction(
    val icon: ImageVector,
    val label: String,
    val background: Color,
    val foreground: Color,
    val onClick: () -> Unit,
)

@Composable
fun SwipeActionRow(
    actions: List<SwipeAction>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(vertical = GroveSpacing.MD),
    content: @Composable RowScope.() -> Unit,
) {
    val c = GroveTheme.colors
    val density = LocalDensity.current
    val revealPx =
        with(density) {
            GroveSize.SwipeAction.toPx() * actions.size +
                GroveSpacing.SM.toPx() * actions.size
        }
    val maxDragPx = revealPx + with(density) { 12.dp.toPx() }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var dragging by remember { mutableStateOf(false) }
    val animatedX by animateFloatAsState(
        targetValue = offsetX,
        animationSpec =
            if (dragging) {
                snap()
            } else {
                spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
            },
        label = "swipeActionRow",
    )

    Box(modifier = modifier.fillMaxWidth().clipToBounds()) {
        Row(
            modifier = Modifier.fillMaxSize().padding(end = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(GroveSpacing.SM, Alignment.End),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            actions.forEach { action ->
                SwipeActionButton(action) {
                    action.onClick()
                    offsetX = 0f
                }
            }
        }
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .offset { IntOffset(animatedX.toInt(), 0) }
                    .background(c.bgCard)
                    .pointerInput(actions.size) {
                        detectHorizontalDragGestures(
                            onDragStart = { dragging = true },
                            onDragEnd = {
                                dragging = false
                                offsetX = if (offsetX < -revealPx / 2f) -revealPx else 0f
                            },
                            onDragCancel = {
                                dragging = false
                                offsetX = 0f
                            },
                            onHorizontalDrag = { _, delta ->
                                offsetX = (offsetX + delta).coerceIn(-maxDragPx, 0f)
                            },
                        )
                    }.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) {
                        if (offsetX != 0f) offsetX = 0f
                    }.padding(contentPadding),
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
    }
}

@Composable
private fun SwipeActionButton(
    action: SwipeAction,
    onClick: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .size(GroveSize.SwipeAction)
                .clip(GroveShapes.Chip)
                .background(action.background)
                .clickable(role = Role.Button) { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(action.icon, contentDescription = action.label, tint = action.foreground, modifier = Modifier.size(19.dp))
    }
}
