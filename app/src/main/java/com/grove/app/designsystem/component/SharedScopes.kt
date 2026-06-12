package com.grove.app.designsystem.component

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier

/**
 * Plumbing for shared element transitions across navigation destinations.
 * GroveApp provides both scopes; any composable can then tag itself with
 * [groveSharedElement] and matching keys morph between screens. Degrades to a
 * no-op wherever the scopes aren't available (overlays, sheets, previews).
 */
@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }
val LocalNavAnimatedScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.groveSharedElement(key: Any): Modifier {
    val shared = LocalSharedTransitionScope.current ?: return this
    val animated = LocalNavAnimatedScope.current ?: return this
    // Attach only while this destination's enter/exit transition is running.
    // Permanently attached, sharedElement() positions its content through
    // lookahead geometry, which fights LazyColumn scrolling and
    // Modifier.animateItem() placement animation — icons visibly drift away
    // from their rows. The transition states are snapshot reads, so attachment
    // flips on the first frame of each navigation and detaches once settled.
    // (Gating on SharedTransitionScope.isTransitionActive would deadlock:
    // it only becomes true after elements are attached and matched.)
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
