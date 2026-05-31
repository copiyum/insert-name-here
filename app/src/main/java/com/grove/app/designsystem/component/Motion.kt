package com.grove.app.designsystem.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

@Composable
fun animatedOnce(target: Float, spec: AnimationSpec<Float> = tween(1000, easing = EaseOutCubic)): Float {
    var played by rememberSaveable { mutableStateOf(false) }
    val anim = remember { Animatable(if (played) target else 0f) }
    LaunchedEffect(target) {
        anim.animateTo(target, spec)
        played = true
    }
    return anim.value
}
