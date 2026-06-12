package com.grove.app.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.grove.app.designsystem.theme.Fraunces
import com.grove.app.designsystem.theme.GroveShapes
import com.grove.app.designsystem.theme.GroveSpacing
import com.grove.app.designsystem.theme.GroveTheme
import com.grove.app.designsystem.theme.GroveType
import com.grove.app.designsystem.theme.InterTight

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
        style = moneyStyle(size).copy(color = color, textAlign = textAlign),
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
fun MetricBlock(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = GroveTheme.colors.fg1,
    content: (@Composable RowScope.() -> Unit)? = null,
) {
    Column(modifier = modifier) {
        Text(label, style = GroveType.capLabel, color = GroveTheme.colors.fg3)
        Spacer(Modifier.height(3.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(GroveSpacing.XS)) {
            Text(
                value,
                style =
                    TextStyle(
                        fontFamily = InterTight,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFeatureSettings = "tnum",
                    ),
                color = valueColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            content?.invoke(this)
        }
    }
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
        Text(title, style = GroveType.rowTitle, color = c.fg2, textAlign = TextAlign.Center)
        if (subtitle != null) {
            Spacer(Modifier.height(4.dp))
            Text(subtitle, style = GroveType.rowSub, color = c.fg3, textAlign = TextAlign.Center)
        }
    }
}

private fun moneyStyle(size: MoneyTextSize): TextStyle =
    when (size) {
        MoneyTextSize.Hero -> TextStyle(
            fontFamily = Fraunces,
            fontSize = 52.sp,
            lineHeight = 58.sp,
            fontWeight = FontWeight.Medium,
            fontFeatureSettings = "tnum",
        )
        MoneyTextSize.Display -> TextStyle(
            fontFamily = Fraunces,
            fontSize = 40.sp,
            lineHeight = 46.sp,
            fontWeight = FontWeight.Medium,
            fontFeatureSettings = "tnum",
        )
        MoneyTextSize.Title -> TextStyle(
            fontFamily = Fraunces,
            fontSize = 24.sp,
            lineHeight = 30.sp,
            fontWeight = FontWeight.Medium,
            fontFeatureSettings = "tnum",
        )
        MoneyTextSize.Row -> TextStyle(
            fontFamily = InterTight,
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
