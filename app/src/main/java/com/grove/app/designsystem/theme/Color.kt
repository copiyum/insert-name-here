package com.grove.app.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

// ─── Raw palette ─────────────────────────────────────────────────────────────

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
    val isDark: Boolean,
    // ─── Accent surface — reusable selection / highlight bg ─────────────────
    val accentSurface: Color,
    // ─── Navigation ────────────────────────────────────────────────────────
    val navBg: Color,
    val navActiveText: Color,
    val navInactiveText: Color,
)

val LightColors = GroveColors(
    bgApp = Color(0xFFF1EDE2),
    bgCard = Color(0xFFFDFCF8),
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
    isDark = false,
    accentSurface = Fern.copy(alpha = 0.08f),
    navBg = Color(0xFFFDFCF8),
    navActiveText = Color(0xFFFFFFFF),
    navInactiveText = Color(0xFF8A988F),
)

val DarkColors = GroveColors(
    bgApp = Color(0xFF161B18),
    bgCard = Color(0xFF1E2521),
    bgMuted = Color(0xFF283029),
    fg1 = Color(0xFFE6E8E0),
    fg2 = Color(0xFFB3BCB1),
    fg3 = Color(0xFF7D877F),
    fgOnFern = Color(0xFFFFFFFF),
    border = Color(0x14DAD7CD),
    borderStrong = Color(0x29DAD7CD),
    accent = Color(0xFF7AAA78),
    accentDeep = Color(0xFF6A9A68),
    accentSoft = Color(0xFFA6C4A3),
    bone = Color(0xFF232B27),
    boneSoft = Color(0xFF283029),
    clay = Color(0xFFC6A07A),
    claySoft = ClaySoft,
    clayBg = Color(0xFF3A2F25),
    isDark = true,
    accentSurface = Color(0xFF7AAA78).copy(alpha = 0.08f),
    navBg = Color(0xFF141A15),
    navActiveText = Color(0xFFF6F3EA),
    navInactiveText = Color(0x8FF2EFE4),
)
