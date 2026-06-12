package com.grove.app.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grove.app.designsystem.theme.GroveBorder
import com.grove.app.designsystem.theme.GroveSprings
import com.grove.app.designsystem.theme.GroveTheme
import com.grove.app.designsystem.theme.GroveSize
import com.grove.app.designsystem.theme.GroveShapes
import com.grove.app.designsystem.theme.GroveSpacing
import com.grove.app.designsystem.theme.GroveType
import com.grove.app.designsystem.theme.InterTight
import com.grove.app.designsystem.theme.SpendTone

@Composable
fun ProgressBar(
    pct: Float,
    color: Color,
    modifier: Modifier = Modifier,
    height: Int = 6,
    animate: Boolean = true,
) {
    val animatedPct by animateFloatAsState(
        targetValue = pct.coerceIn(0f, 1f),
        animationSpec = if (animate) GroveSprings.standard() else snap(),
        label = "progressBarPct",
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height.dp)
            .clip(GroveShapes.Toggle)
            .background(GroveTheme.colors.bgMuted),
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animatedPct)
                .clip(GroveShapes.Toggle)
                .background(color),
        )
    }
}

@Composable
fun StatusPill(label: String, kind: String) {
    val (bg, fg) = when (kind) {
        "success" -> GroveTheme.colors.successBg to GroveTheme.colors.success
        "warn" -> GroveTheme.colors.warnBg to GroveTheme.colors.warn
        "danger" -> GroveTheme.colors.dangerBg to GroveTheme.colors.danger
        else -> GroveTheme.colors.bgMuted to GroveTheme.colors.fg2
    }
    Box(
        modifier = Modifier
            .clip(GroveShapes.Toggle)
            .background(bg)
            .padding(horizontal = GroveSpacing.SM + 2.dp, vertical = GroveSpacing.XS),
    ) {
        Text(
            label,
            fontFamily = InterTight,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Medium,
            color = fg,
        )
    }
}

@Composable
fun HeroStatusChip(tone: SpendTone) {
    val fg = tone.color
    val bg = tone.color.copy(alpha = if (GroveTheme.colors.isDark) 0.15f else 0.12f)
    Row(
        modifier = Modifier
            .clip(GroveShapes.Toggle)
            .background(bg)
            .padding(start = GroveSpacing.SM, end = GroveSpacing.SM + 2.dp, top = 5.dp, bottom = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Box(
            modifier = Modifier
                .size(GroveSize.StatusDot)
                .clip(GroveShapes.Chip)
                .background(fg),
        )
        Text(
            tone.label,
            fontFamily = InterTight,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.5.sp,
            color = fg,
        )
    }
}
