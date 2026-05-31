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
        SpendPace.Over -> SpendTone(c.clay, Color(0xFF8F6A4C), "Over budget", healthy = false)
        SpendPace.Tight -> SpendTone(c.claySoft, c.clay, "Spending fast", healthy = false)
        SpendPace.Healthy -> SpendTone(c.accent, c.accentDeep, "On track", healthy = true)
    }
}
