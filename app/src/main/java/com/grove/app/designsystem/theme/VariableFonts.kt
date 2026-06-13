package com.grove.app.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.grove.app.R

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

fun heroWeightFor(budgetLeftFraction: Float): Int =
    (450 + (250 * budgetLeftFraction.coerceIn(0f, 1f))).toInt()
