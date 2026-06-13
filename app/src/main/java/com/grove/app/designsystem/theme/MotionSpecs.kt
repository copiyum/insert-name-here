package com.grove.app.designsystem.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

object GroveSprings {
    fun <T> snappy(): SpringSpec<T> = spring(dampingRatio = 0.9f, stiffness = 1400f)

    fun <T> standard(): SpringSpec<T> = spring(dampingRatio = 0.85f, stiffness = 420f)

    fun <T> expressive(): SpringSpec<T> = spring(dampingRatio = 0.65f, stiffness = 320f)

    fun <T> gentle(): SpringSpec<T> = spring(dampingRatio = 1f, stiffness = 220f)
}

object GroveEase {
    val Out: Easing = CubicBezierEasing(0.33f, 1f, 0.68f, 1f)
    val InOut: Easing = CubicBezierEasing(0.65f, 0f, 0.35f, 1f)

    fun <T> fast(): TweenSpec<T> = tween(durationMillis = 120, easing = Out)

    fun <T> normal(): TweenSpec<T> = tween(durationMillis = 180, easing = Out)

    fun <T> slow(): TweenSpec<T> = tween(durationMillis = 320, easing = Out)
}
