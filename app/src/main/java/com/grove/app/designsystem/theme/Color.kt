package com.grove.app.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color


val Pine = Color(0xFF344E41)
val Fern = Color(0xFF588157)
val FernDeep = Color(0xFF4A6F49)
val FernSoft = Color(0xFF7A9D78)
val Clay = Color(0xFFB08968)
val ClaySoft = Color(0xFFD4B89D)
val ClayBg = Color(0xFFEFE2D3)
val Bone = Color(0xFFDAD7CD)
val BoneSoft = Color(0xFFE5E3DA)

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
    bgApp = Color(0xFFF1EDE2),
    bgCard = Color(0xFFFDFCF8),
    bgCardRaised = Color(0xFFFFFFFF),
    bgMuted = Color(0xFFEBE7DA),
    fg1 = Color(0xFF2A3A32),
    fg2 = Color(0xFF4F5F55),
    fg3 = Color(0xFF8A988F),
    fgOnFern = Color(0xFFFFFFFF),
    border = Color(0x1A3A5A40),
    borderStrong = Color(0x2E3A5A40),
    accent = Fern,
    accentDeep = FernDeep,
    accentSoft = FernSoft,
    bone = Bone,
    boneSoft = BoneSoft,
    clay = Clay,
    claySoft = ClaySoft,
    clayBg = ClayBg,
    success = FernDeep,
    successBg = Fern.copy(alpha = 0.13f),
    warn = Color(0xFF9A6C2F),
    warnBg = Color(0xFFF1E3CA),
    danger = Color(0xFFB65F4F),
    dangerBg = Color(0xFFF2D9D3),
    info = Color(0xFF4D7384),
    infoBg = Color(0xFFD9E6EA),
    isDark = false,
    accentSurface = Fern.copy(alpha = 0.08f),
    navBg = Color(0xFFFDFCF8),
    navActiveText = Color(0xFFFFFFFF),
    navInactiveText = Color(0xFF8A988F),
)

val DarkColors = GroveColors(
    bgApp = Color(0xFF0F1512),
    bgCard = Color(0xFF17201B),
    bgCardRaised = Color(0xFF1D2922),
    bgMuted = Color(0xFF223029),
    fg1 = Color(0xFFF0F3E9),
    fg2 = Color(0xFFC6D0C4),
    fg3 = Color(0xFF87948B),
    fgOnFern = Color(0xFF0C1F13),
    border = Color(0x14DAD7CD),
    borderStrong = Color(0x29DAD7CD),
    accent = Color(0xFF93E49A),
    accentDeep = Color(0xFF66C873),
    accentSoft = Color(0xFFC2F0C0),
    bone = Color(0xFF26362E),
    boneSoft = Color(0xFF2C3D34),
    clay = Color(0xFFD8A16D),
    claySoft = ClaySoft,
    clayBg = Color(0xFF402F22),
    success = Color(0xFF93E49A),
    successBg = Color(0x1F93E49A),
    warn = Color(0xFFE0B15E),
    warnBg = Color(0x244D351A),
    danger = Color(0xFFE47C68),
    dangerBg = Color(0x2CE47C68),
    info = Color(0xFF8BC7D8),
    infoBg = Color(0x248BC7D8),
    isDark = true,
    accentSurface = Color(0xFF93E49A).copy(alpha = 0.14f),
    navBg = Color(0xFF121A15),
    navActiveText = Color(0xFF0C1F13),
    navInactiveText = Color(0xBFF0F3E9),
)
