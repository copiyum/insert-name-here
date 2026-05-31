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
val Fraunces = FontFamily(
    fraunces(FontWeight.Normal, 400),
    fraunces(FontWeight.Medium, 500),
    fraunces(FontWeight.SemiBold, 560),
)

@OptIn(ExperimentalTextApi::class)
private fun fraunces(weight: FontWeight, axis: Int) = Font(
    R.font.fraunces,
    weight,
    variationSettings = FontVariation.Settings(
        FontVariation.weight(axis),
        FontVariation.Setting("opsz", 60f),
        FontVariation.Setting("SOFT", 30f),
        FontVariation.Setting("WONK", 0f),
    ),
)
