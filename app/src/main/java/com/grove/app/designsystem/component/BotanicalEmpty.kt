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

/**
 * Illustrated empty state: a hand-drawn-feeling sprout in a pot under a dashed
 * sun, with encouraging copy and an optional CTA. Procedural so it tints itself
 * for both themes.
 */
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
            val h = size.height
            val cx = w / 2f
            val stroke = Stroke(width = w * 0.028f, cap = StrokeCap.Round)

            // Dashed sun arc, top-right.
            drawArc(
                color = c.clay.copy(alpha = 0.75f),
                startAngle = 180f,
                sweepAngle = 200f,
                useCenter = false,
                topLeft = Offset(w * 0.62f, h * 0.04f),
                size = androidx.compose.ui.geometry.Size(w * 0.26f, w * 0.26f),
                style = Stroke(
                    width = stroke.width,
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(w * 0.05f, w * 0.05f)),
                ),
            )

            // Pot.
            val potTop = h * 0.66f
            val pot = Path().apply {
                moveTo(cx - w * 0.2f, potTop)
                lineTo(cx + w * 0.2f, potTop)
                lineTo(cx + w * 0.14f, h * 0.92f)
                lineTo(cx - w * 0.14f, h * 0.92f)
                close()
            }
            drawPath(pot, c.bgMuted)
            drawPath(pot, c.borderStrong, style = stroke)
            drawLine(c.borderStrong, Offset(cx - w * 0.23f, potTop), Offset(cx + w * 0.23f, potTop), stroke.width, StrokeCap.Round)

            // Sprout: stem with two leaves.
            val stem = Path().apply {
                moveTo(cx, potTop)
                quadraticTo(cx + w * 0.02f, h * 0.5f, cx - w * 0.02f, h * 0.34f)
            }
            drawPath(stem, c.accent, style = stroke)
            val leafL = Path().apply {
                moveTo(cx - w * 0.015f, h * 0.46f)
                quadraticTo(cx - w * 0.2f, h * 0.4f, cx - w * 0.16f, h * 0.26f)
                quadraticTo(cx - w * 0.04f, h * 0.3f, cx - w * 0.015f, h * 0.46f)
                close()
            }
            val leafR = Path().apply {
                moveTo(cx - w * 0.01f, h * 0.38f)
                quadraticTo(cx + w * 0.17f, h * 0.34f, cx + w * 0.14f, h * 0.2f)
                quadraticTo(cx + w * 0.02f, h * 0.22f, cx - w * 0.01f, h * 0.38f)
                close()
            }
            drawPath(leafL, c.accentSoft)
            drawPath(leafR, c.accent.copy(alpha = 0.9f))
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
