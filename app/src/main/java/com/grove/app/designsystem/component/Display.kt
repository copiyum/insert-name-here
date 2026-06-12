package com.grove.app.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.tween
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grove.app.designsystem.theme.GroveShapes
import com.grove.app.designsystem.theme.GroveSpacing
import com.grove.app.designsystem.theme.GroveTheme
import com.grove.app.designsystem.theme.GroveType
import com.grove.app.designsystem.theme.InterTight
import com.grove.app.designsystem.theme.SpaceGrotesk
import com.grove.app.designsystem.format.Money
import com.grove.app.designsystem.format.lerpMinor
import kotlinx.coroutines.delay

enum class MoneyTextSize { Hero, Display, Title, Row, Small }

@Composable
fun MoneyText(
    text: String,
    modifier: Modifier = Modifier,
    size: MoneyTextSize = MoneyTextSize.Row,
    color: Color = GroveTheme.colors.fg1,
    textAlign: TextAlign = TextAlign.Start,
    maxLines: Int = 1,
) {
    Text(
        text = text,
        modifier = modifier,
        style = moneyTextStyle(size).copy(color = color, textAlign = textAlign),
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
fun AnimatedMoneyText(
    minor: Long,
    currency: String,
    modifier: Modifier = Modifier,
    decimals: Int = 0,
    size: MoneyTextSize = MoneyTextSize.Row,
    color: Color = GroveTheme.colors.fg1,
    textAlign: TextAlign = TextAlign.Start,
    animationKey: Any? = minor,
    fromMinor: Long? = null,
    startDelayMillis: Int = 0,
    durationMillis: Int = 650,
    easing: Easing = EaseOutCubic,
    progress: Float? = null,
) {
    if (progress != null && fromMinor != null) {
        MoneyText(
            text = Money.currencyLong(lerpMinor(fromMinor, minor, progress), decimals, currency),
            modifier = modifier,
            size = size,
            color = color,
            textAlign = textAlign,
        )
        return
    }

    val progressAnim = remember { Animatable(1f) }
    var startMinor by remember { mutableLongStateOf(fromMinor ?: minor) }
    var targetMinor by remember { mutableLongStateOf(minor) }

    LaunchedEffect(minor, currency, animationKey, fromMinor, startDelayMillis, durationMillis, easing) {
        startMinor = fromMinor ?: lerpMinor(startMinor, targetMinor, progressAnim.value)
        targetMinor = minor
        progressAnim.snapTo(0f)
        if (startDelayMillis > 0) delay(startDelayMillis.toLong())
        progressAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis.coerceAtLeast(0), easing = easing),
        )
    }

    MoneyText(
        text = Money.currencyLong(lerpMinor(startMinor, targetMinor, progressAnim.value), decimals, currency),
        modifier = modifier,
        size = size,
        color = color,
        textAlign = textAlign,
    )
}

@Composable
fun EmptyState(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
) {
    val c = GroveTheme.colors
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = GroveSpacing.XL),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier.size(44.dp).clip(GroveShapes.Tile).background(c.bgMuted),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = c.fg3, modifier = Modifier.size(21.dp))
            }
            Spacer(Modifier.height(GroveSpacing.SM))
        }
        Text(title, style = GroveType.rowTitle, color = c.fg1, textAlign = TextAlign.Center)
        if (subtitle != null) {
            Spacer(Modifier.height(4.dp))
            Text(subtitle, style = GroveType.rowSub, color = c.fg3, textAlign = TextAlign.Center)
        }
    }
}

fun moneyTextStyle(size: MoneyTextSize): TextStyle =
    when (size) {
        MoneyTextSize.Hero -> TextStyle(
            fontFamily = SpaceGrotesk,
            fontSize = 48.sp,
            lineHeight = 50.sp,
            fontWeight = FontWeight.SemiBold,
            fontFeatureSettings = "tnum",
        )
        MoneyTextSize.Display -> TextStyle(
            fontFamily = SpaceGrotesk,
            fontSize = 40.sp,
            lineHeight = 44.sp,
            fontWeight = FontWeight.SemiBold,
            fontFeatureSettings = "tnum",
        )
        MoneyTextSize.Title -> TextStyle(
            fontFamily = SpaceGrotesk,
            fontSize = 24.sp,
            lineHeight = 30.sp,
            fontWeight = FontWeight.SemiBold,
            fontFeatureSettings = "tnum",
        )
        MoneyTextSize.Row -> TextStyle(
            fontFamily = SpaceGrotesk,
            fontSize = 16.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.SemiBold,
            fontFeatureSettings = "tnum",
        )
        MoneyTextSize.Small -> TextStyle(
            fontFamily = InterTight,
            fontSize = 13.sp,
            lineHeight = 17.sp,
            fontWeight = FontWeight.SemiBold,
            fontFeatureSettings = "tnum",
        )
    }
