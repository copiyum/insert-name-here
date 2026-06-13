package com.grove.app.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.grove.app.designsystem.catalog.CategoryVisuals
import com.grove.app.designsystem.theme.GroveSize
import com.grove.app.designsystem.theme.GroveTheme

@Composable
fun CategoryIcon(
    iconKey: String,
    modifier: Modifier = Modifier,
    size: Int = GroveSize.CategoryIcon.value.toInt(),
    contentDescription: String? = null,
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
            .background(tint)
            .then(
                if (contentDescription != null) {
                    Modifier.semantics { this.contentDescription = contentDescription }
                } else {
                    Modifier
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        CategoryGlyph(iconKey = iconKey, color = color, sizeDp = (size * 0.58f).toInt())
    }
}
