package com.grove.app.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

val Pine = Color(0xFF212121)
val Fern = Color(0xFF328759)
val FernDeep = Color(0xFF43A26D)
val FernSoft = Color(0xFF62C37A)
val Clay = Color(0xFFECA851)
val ClaySoft = Color(0xFFE8AA4E)
val ClayBg = Color(0x1FE8AA4E)
val Bone = Color(0xFF191A1B)
val BoneSoft = Color(0xFF23252A)

@Immutable
data class GroveColors(
    val bgApp: Color,
    val bgCard: Color,
    val bgCardRaised: Color,
    val bgMuted: Color,
    val fg1: Color,
    val fg2: Color,
    val fg3: Color,
    val fgOnFern: Color,
    val border: Color,
    val borderStrong: Color,
    val accent: Color,
    val accentDeep: Color,
    val accentSoft: Color,
    val bone: Color,
    val boneSoft: Color,
    val clay: Color,
    val claySoft: Color,
    val clayBg: Color,
    val success: Color,
    val successBg: Color,
    val warn: Color,
    val warnBg: Color,
    val danger: Color,
    val dangerBg: Color,
    val info: Color,
    val infoBg: Color,
    val isDark: Boolean,
    val accentSurface: Color,
    val navBg: Color,
    val navActiveText: Color,
    val navInactiveText: Color,
)

val LightColors = GroveColors(
    bgApp = Color(0xFFFFFFFF),
    bgCard = Color(0xFFF4F3EF),
    bgCardRaised = Color(0xFFF4F3EF),
    bgMuted = Color(0xFFEEECE7),
    fg1 = Color(0xFF212121),
    fg2 = Color(0xFF616161),
    fg3 = Color(0xFF93939F),
    fgOnFern = Color(0xFFFFFFFF),
    border = Color(0xFFE5E3DD),
    borderStrong = Color(0xFFD9D9DD),
    accent = Color(0xFF047745),
    accentDeep = Color(0xFF006535),
    accentSoft = Color(0xFF328759),
    bone = Color(0xFFEEECE7),
    boneSoft = Color(0xFFE5E3DD),
    clay = Clay,
    claySoft = ClaySoft,
    clayBg = ClayBg,
    success = Color(0xFF27A644),
    successBg = Color(0x2627A644),
    warn = Color(0xFFC66C00),
    warnBg = Color(0x29C66C00),
    danger = Color(0xFFB30000),
    dangerBg = Color(0x24B30000),
    info = Color(0xFF4CB0E5),
    infoBg = Color(0x244CB0E5),
    isDark = false,
    accentSurface = Color(0xFFEDFCE9),
    navBg = Color(0xFFFFFFFF),
    navActiveText = Color(0xFF047745),
    navInactiveText = Color(0xFF93939F),
)

val DarkColors = GroveColors(
    bgApp = Color(0xFF010102),
    bgCard = Color(0xFF0F1011),
    bgCardRaised = Color(0xFF0F1011),
    bgMuted = Color(0xFF18191A),
    fg1 = Color(0xFFF7F8F8),
    fg2 = Color(0xFFD0D6E0),
    fg3 = Color(0xFF8A8F98),
    fgOnFern = Color(0xFFFFFFFF),
    border = Color(0xFF23252A),
    borderStrong = Color(0xFF34343A),
    accent = Color(0xFF328759),
    accentDeep = Color(0xFF43A26D),
    accentSoft = Color(0xFF62C37A),
    bone = Color(0xFF191A1B),
    boneSoft = Color(0xFF23252A),
    clay = Color(0xFFECA851),
    claySoft = Color(0xFFE8AA4E),
    clayBg = Color(0x1FE8AA4E),
    success = Color(0xFF27A644),
    successBg = Color(0x2627A644),
    warn = Color(0xFFE8AA4E),
    warnBg = Color(0x29E8AA4E),
    danger = Color(0xFFEF5350),
    dangerBg = Color(0x24EF5350),
    info = Color(0xFF4CB0E5),
    infoBg = Color(0x244CB0E5),
    isDark = true,
    accentSurface = Color(0x26328759),
    navBg = Color(0xFF0A0B0D),
    navActiveText = Color(0xFF328759),
    navInactiveText = Color(0xFF8A8F98),
)

fun spendProgressColorStops(colors: GroveColors): List<Pair<Float, Color>> {
    val lime = if (colors.isDark) Color(0xFF8BC34A) else Color(0xFF6F9F1B)
    val orange = if (colors.isDark) Color(0xFFFF7A1A) else Color(0xFFD45A00)
    return listOf(
        0.00f to colors.success,
        0.55f to lime,
        0.72f to colors.warn,
        0.88f to orange,
        0.96f to colors.danger,
    )
}

fun spendProgressColorAt(colors: GroveColors, progress: Float): Color =
    colorAtProgressStop(spendProgressColorStops(colors), progress)

fun spendProgressDeepColorAt(colors: GroveColors, progress: Float): Color {
    val base = spendProgressColorAt(colors, progress)
    val shade = if (colors.isDark) Color.White else Color.Black
    return lerp(base, shade, if (colors.isDark) 0.14f else 0.12f)
}

private fun colorAtProgressStop(stops: List<Pair<Float, Color>>, progress: Float): Color {
    val sorted = stops.sortedBy { it.first }
    val p = progress.coerceIn(0f, 1f)
    val first = sorted.firstOrNull() ?: return Color.Unspecified
    if (p <= first.first) return first.second

    sorted.zipWithNext().forEach { (start, end) ->
        if (p <= end.first) {
            val span = (end.first - start.first).coerceAtLeast(0.0001f)
            return lerp(start.second, end.second, ((p - start.first) / span).coerceIn(0f, 1f))
        }
    }

    return sorted.last().second
}
