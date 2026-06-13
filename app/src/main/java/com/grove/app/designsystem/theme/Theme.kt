package com.grove.app.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.grove.app.data.SpendPace

private val LocalGroveColors = staticCompositionLocalOf { LightColors }

object GroveTheme {
    val colors: GroveColors
        @Composable get() = LocalGroveColors.current
}

@Composable
fun GroveTheme(dark: Boolean, content: @Composable () -> Unit) {
    val palette = if (dark) DarkColors else LightColors
    CompositionLocalProvider(LocalGroveColors provides palette) {
        MaterialTheme(
            colorScheme = if (dark) {
                darkColorScheme(primary = palette.accent, background = palette.bgApp, surface = palette.bgCard)
            } else {
                lightColorScheme(primary = palette.accent, background = palette.bgApp, surface = palette.bgCard)
            },
            typography = Typography(),
            content = content,
        )
    }
}

@androidx.compose.runtime.Immutable
data class SpendTone(
    val color: Color,
    val deep: Color,
    val label: String,
    val healthy: Boolean,
)

@Composable
fun toneOf(pace: SpendPace): SpendTone {
    val c = GroveTheme.colors
    return when (pace) {
        SpendPace.Over -> SpendTone(c.danger, c.danger, SpendStatusCopy.label(pace, 0), healthy = false)
        SpendPace.Tight -> SpendTone(c.warn, c.warn, SpendStatusCopy.label(pace, 0), healthy = false)
        SpendPace.Healthy -> SpendTone(c.accent, c.accentDeep, SpendStatusCopy.label(pace, 0), healthy = true)
    }
}

/**
 * Abstract, non-cute status copy for the spend pace. Each pace has a small pool of
 * phrasings; [label] picks one deterministically from a seed (use the day so it
 * stays stable through the day but varies day to day, keeping it from going stale).
 */
object SpendStatusCopy {
    private val healthy = listOf(
        "On track", "Comfortable", "Plenty of room", "Cruising",
        "All clear", "Steady", "Well within", "Easy pace",
    )
    private val tight = listOf(
        "Getting tight", "Close to the edge", "Ease off", "Watch the pace",
        "Near the line", "Tightening", "Slow it down",
    )
    private val over = listOf(
        "Over today", "Past the line", "In the red", "Over budget",
        "Eased over", "Resets tomorrow",
    )

    fun label(pace: SpendPace, seed: Int): String {
        val pool = when (pace) {
            SpendPace.Healthy -> healthy
            SpendPace.Tight -> tight
            SpendPace.Over -> over
        }
        return pool[((seed % pool.size) + pool.size) % pool.size]
    }
}
