package com.grove.app.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.grove.app.R

/**
 * Space Grotesk ships as a variable font — this exposes its weight axis so the
 * hero number can physically lighten as the budget thins. Weights are quantized
 * to steps of 25 so animation doesn't allocate a typeface per frame.
 */
@OptIn(ExperimentalTextApi::class)
@Composable
fun spaceGroteskAtWeight(weight: Int): FontFamily {
    val quantized = ((weight.coerceIn(300, 700) + 12) / 25) * 25
    return remember(quantized) {
        FontFamily(
            Font(
                resId = R.font.space_grotesk,
                weight = FontWeight(quantized),
                variationSettings = FontVariation.Settings(FontVariation.weight(quantized)),
            ),
        )
    }
}

/** Weight for the hero number: confident at full budget, lighter as it drains. */
fun heroWeightFor(budgetLeftFraction: Float): Int =
    (450 + (250 * budgetLeftFraction.coerceIn(0f, 1f))).toInt()
