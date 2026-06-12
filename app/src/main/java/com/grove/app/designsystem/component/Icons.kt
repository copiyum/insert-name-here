package com.grove.app.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import com.grove.app.designsystem.catalog.CategoryVisuals
import com.grove.app.designsystem.theme.GroveSize
import com.grove.app.designsystem.theme.GroveTheme

@Composable
fun CategoryIcon(
    iconKey: String,
    size: Int = GroveSize.CategoryIcon.value.toInt(),
    contentDescription: String? = null,
    modifier: Modifier = Modifier,
) {
    val v = CategoryVisuals.of(iconKey)
    val c = GroveTheme.colors
    val radius = (size * 0.35f).coerceAtMost(14f)
    val tint = if (c.isDark) v.color.copy(alpha = 0.38f) else v.tint
    val color = if (c.isDark) lerp(v.color, Color.White, 0.22f) else v.color
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(radius.dp))
            .background(tint),
        contentAlignment = Alignment.Center,
    ) {
        Icon(v.icon, contentDescription = contentDescription, tint = color, modifier = Modifier.size((size * 0.5f).dp))
    }
}

@Composable
fun LeafGlyph(size: Int, color: Color, alpha: Float, rotation: Float = 0f, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(size.dp).graphicsLayer { rotationZ = rotation; this.alpha = alpha }) {
        val s = this.size.minDimension
        val path = Path().apply {
            moveTo(s * 0.5f, s * 0.04f)
            quadraticTo(s * 0.96f, s * 0.42f, s * 0.5f, s * 0.96f)
            quadraticTo(s * 0.04f, s * 0.42f, s * 0.5f, s * 0.04f)
            close()
        }
        drawPath(path, color)
    }
}
