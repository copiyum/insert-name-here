package com.grove.app.designsystem.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.grove.app.core.format.Money
import com.grove.app.core.format.lerpMinor
import com.grove.app.designsystem.theme.GroveEase
import kotlinx.coroutines.delay

@Composable
fun TickerText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
) {
    var previousValue by remember { mutableLongStateOf(numericValue(text)) }
    val currentValue = numericValue(text)
    val rollingUp = currentValue >= previousValue
    LaunchedEffect(currentValue) { previousValue = currentValue }

    Row(modifier = modifier) {
        val chars = text.toList()
        chars.forEachIndexed { index, ch ->
            val fromRight = chars.size - index
            androidx.compose.runtime.key(fromRight) {
                AnimatedContent(
                    targetState = ch,
                    transitionSpec = { tickerTransform(rollingUp, staggerIndex = index) },
                    label = "tickerChar",
                ) { target ->
                    Text(text = target.toString(), style = style, color = color, maxLines = 1)
                }
            }
        }
    }
}

private fun tickerTransform(rollingUp: Boolean, staggerIndex: Int): ContentTransform {
    val enterSpec = tween<androidx.compose.ui.unit.IntOffset>(
        durationMillis = 360,
        delayMillis = staggerIndex * 24,
        easing = GroveEase.Out,
    )
    val exitSpec = tween<androidx.compose.ui.unit.IntOffset>(
        durationMillis = 300,
        delayMillis = staggerIndex * 24,
        easing = GroveEase.Out,
    )
    return if (rollingUp) {
        (slideInVertically(enterSpec) { it } + fadeIn(GroveEase.normal())) togetherWith
            (slideOutVertically(exitSpec) { -it } + fadeOut(GroveEase.fast()))
    } else {
        (slideInVertically(enterSpec) { -it } + fadeIn(GroveEase.normal())) togetherWith
            (slideOutVertically(exitSpec) { it } + fadeOut(GroveEase.fast()))
    }
}

private fun numericValue(text: String): Long =
    text.filter(Char::isDigit).take(17).toLongOrNull() ?: 0L

@Composable
fun TickerMoneyText(
    minor: Long,
    currency: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    decimals: Int = 0,
    color: Color = Color.Unspecified,
    countUpFrom: Long? = 0L,
    entryDelayMillis: Long = 180L,
    fromMinor: Long? = null,
    progress: Float? = null,
) {
    var shown by remember { mutableLongStateOf(countUpFrom ?: minor) }
    var entered by remember { mutableStateOf(countUpFrom == null) }
    val settlementMinor = if (progress != null && fromMinor != null) lerpMinor(fromMinor, minor, progress) else null

    if (settlementMinor != null) {
        SideEffect {
            shown = minor
            entered = true
        }
        Text(
            text = Money.currencyLong(settlementMinor, decimals, currency),
            style = style,
            color = color,
            modifier = modifier,
            maxLines = 1,
        )
        return
    }

    LaunchedEffect(minor) {
        if (!entered) {
            delay(entryDelayMillis)
            entered = true
        }
        shown = minor
    }
    TickerText(
        text = Money.currencyLong(shown, decimals, currency),
        style = style,
        color = color,
        modifier = modifier,
    )
}
