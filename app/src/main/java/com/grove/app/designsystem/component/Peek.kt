package com.grove.app.designsystem.component

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.grove.app.designsystem.theme.GroveEase
import com.grove.app.designsystem.theme.GroveSprings
import com.grove.app.designsystem.theme.GroveTheme

@Composable
fun PeekHost(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val c = GroveTheme.colors
    val view = LocalView.current
    val scale = remember { Animatable(0.86f) }

    LaunchedEffect(visible) {
        if (visible) {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            scale.snapTo(0.86f)
            scale.animateTo(1f, GroveSprings.expressive())
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(GroveEase.fast()),
        exit = fadeOut(GroveEase.fast()),
        modifier = modifier.fillMaxSize(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(c.bgApp.copy(alpha = 0.72f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 32.dp)
                    .graphicsLayer {
                        scaleX = scale.value
                        scaleY = scale.value
                    },
            ) {
                content()
            }
        }
    }
}
