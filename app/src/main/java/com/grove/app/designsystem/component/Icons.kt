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
import androidx.compose.ui.unit.dp
import com.grove.app.designsystem.catalog.CategoryVisuals
import com.grove.app.designsystem.theme.GroveSize

@Composable
fun CategoryIcon(iconKey: String, size: Int = GroveSize.CategoryIcon.value.toInt()) {
    val v = CategoryVisuals.of(iconKey)
    val radius = (size * 0.35f).coerceAtMost(14f)
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(radius.dp))
            .background(v.tint),
        contentAlignment = Alignment.Center,
    ) {
        Icon(v.icon, contentDescription = null, tint = v.color, modifier = Modifier.size((size * 0.5f).dp))
    }
}

@Composable
fun LeafGlyph(size: Int, color: Color, alpha: Float, rotation: Float = 0f, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(size.dp).graphicsLayer { rotationZ = rotation; this.alpha = alpha }) {
        val s = this.size.minDimension
        val path = Path().apply {
            moveTo(s * 0.5f, s * 0.04f)
            quadraticBezierTo(s * 0.96f, s * 0.42f, s * 0.5f, s * 0.96f)
            quadraticBezierTo(s * 0.04f, s * 0.42f, s * 0.5f, s * 0.04f)
            close()
        }
        drawPath(path, color)
    }
}
