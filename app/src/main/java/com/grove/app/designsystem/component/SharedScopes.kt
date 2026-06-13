package com.grove.app.designsystem.component

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier

@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }
val LocalNavAnimatedScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.groveSharedElement(key: Any): Modifier {
    val shared = LocalSharedTransitionScope.current ?: return this
    val animated = LocalNavAnimatedScope.current ?: return this
    val navigating = animated.transition.isRunning ||
        animated.transition.currentState != animated.transition.targetState
    if (!navigating) return this
    return with(shared) {
        this@groveSharedElement.sharedElement(
            rememberSharedContentState(key),
            animatedVisibilityScope = animated,
        )
    }
}
