package com.grove.app.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.grove.app.designsystem.theme.GroveSpacing
import com.grove.app.designsystem.theme.GroveTheme
import com.grove.app.designsystem.theme.GroveType

@Composable
fun BotanicalEmptyState(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    ctaText: String? = null,
    onCta: (() -> Unit)? = null,
) {
    val c = GroveTheme.colors
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = GroveSpacing.XL),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Canvas(modifier = Modifier.size(96.dp)) {
            val w = size.width
            val cx = w / 2f
            val cy = size.height / 2f
            val center = Offset(cx, cy)
            val sw = w * 0.03f
            val baseR = w * 0.30f

            drawCircle(c.border, radius = baseR, center = center, style = Stroke(sw))
            drawCircle(c.borderStrong.copy(alpha = 0.55f), radius = baseR * 0.64f, center = center, style = Stroke(sw))

            drawArc(
                color = c.accent,
                startAngle = -90f,
                sweepAngle = 92f,
                useCenter = false,
                topLeft = Offset(cx - baseR, cy - baseR),
                size = androidx.compose.ui.geometry.Size(baseR * 2, baseR * 2),
                style = Stroke(width = sw, cap = StrokeCap.Round),
            )

            drawCircle(c.accent.copy(alpha = if (c.isDark) 0.22f else 0.14f), radius = baseR * 0.36f, center = center)
            drawCircle(c.accent, radius = w * 0.03f, center = center)
        }
        Spacer(Modifier.height(GroveSpacing.MD))
        Text(title, style = GroveType.rowTitle, color = c.fg1, textAlign = TextAlign.Center)
        if (subtitle != null) {
            Spacer(Modifier.height(4.dp))
            Text(subtitle, style = GroveType.rowSub, color = c.fg3, textAlign = TextAlign.Center)
        }
        if (ctaText != null && onCta != null) {
            Spacer(Modifier.height(GroveSpacing.LG))
            PrimaryButton(ctaText, onCta)
        }
    }
}
