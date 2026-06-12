package com.grove.app.designsystem.format

import kotlin.math.roundToLong

fun lerpMinor(
    start: Long,
    end: Long,
    progress: Float,
): Long {
    val t = progress.coerceIn(0f, 1f)
    return (start.toDouble() + (end - start).toDouble() * t).roundToLong()
}

fun lerpFloat(
    start: Float,
    end: Float,
    progress: Float,
): Float {
    val t = progress.coerceIn(0f, 1f)
    return start + (end - start) * t
}
