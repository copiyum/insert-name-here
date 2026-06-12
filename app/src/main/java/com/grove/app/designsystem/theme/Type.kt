package com.grove.app.designsystem.theme

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.grove.app.R

@OptIn(ExperimentalTextApi::class)
val InterTight = FontFamily(
    Font(R.font.inter_tight, FontWeight.Normal, variationSettings = FontVariation.Settings(FontVariation.weight(400))),
    Font(R.font.inter_tight, FontWeight.Medium, variationSettings = FontVariation.Settings(FontVariation.weight(500))),
    Font(R.font.inter_tight, FontWeight.SemiBold, variationSettings = FontVariation.Settings(FontVariation.weight(600))),
    Font(R.font.inter_tight, FontWeight.Bold, variationSettings = FontVariation.Settings(FontVariation.weight(700))),
)

@OptIn(ExperimentalTextApi::class)
val SpaceGrotesk = FontFamily(
    Font(R.font.space_grotesk, FontWeight.Normal, variationSettings = FontVariation.Settings(FontVariation.weight(400))),
    Font(R.font.space_grotesk, FontWeight.Medium, variationSettings = FontVariation.Settings(FontVariation.weight(500))),
    Font(R.font.space_grotesk, FontWeight.SemiBold, variationSettings = FontVariation.Settings(FontVariation.weight(600))),
    Font(R.font.space_grotesk, FontWeight.Bold, variationSettings = FontVariation.Settings(FontVariation.weight(700))),
)

@OptIn(ExperimentalTextApi::class)
val JetBrainsMono = FontFamily(
    Font(R.font.jetbrains_mono, FontWeight.Normal, variationSettings = FontVariation.Settings(FontVariation.weight(400))),
    Font(R.font.jetbrains_mono, FontWeight.Medium, variationSettings = FontVariation.Settings(FontVariation.weight(500))),
    Font(R.font.jetbrains_mono, FontWeight.SemiBold, variationSettings = FontVariation.Settings(FontVariation.weight(600))),
    Font(R.font.jetbrains_mono, FontWeight.Bold, variationSettings = FontVariation.Settings(FontVariation.weight(700))),
)
