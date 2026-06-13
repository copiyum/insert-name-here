package com.grove.app.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.grove.app.designsystem.theme.GroveBorder
import com.grove.app.designsystem.theme.GroveTheme
import com.grove.app.designsystem.theme.GroveShapes
import com.grove.app.designsystem.theme.GroveSpacing

enum class GroveCardVariant { Default, Elevated, Muted, Accent, Danger }

@Composable
fun GroveCard(
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues(GroveSpacing.LG),
    variant: GroveCardVariant = GroveCardVariant.Default,
    content: @Composable ColumnScope.() -> Unit,
) {
    val c = GroveTheme.colors
    val background =
        when (variant) {
            GroveCardVariant.Default -> c.bgCard
            GroveCardVariant.Elevated -> c.bgCardRaised
            GroveCardVariant.Muted -> c.bgMuted
            GroveCardVariant.Accent -> c.accentSurface
            GroveCardVariant.Danger -> c.dangerBg
        }
    val border =
        when (variant) {
            GroveCardVariant.Accent -> c.accent.copy(alpha = if (c.isDark) 0.28f else 0.18f)
            GroveCardVariant.Danger -> c.danger.copy(alpha = if (c.isDark) 0.32f else 0.22f)
            GroveCardVariant.Muted -> Color.Transparent
            else -> c.border
        }
    Surface(
        modifier = modifier,
        shape = GroveShapes.Container,
        color = background,
        shadowElevation = 0.dp,
        border = if (border == Color.Transparent) null else BorderStroke(GroveBorder.Thin, border),
    ) {
        Column(modifier = Modifier.padding(padding), content = content)
    }
}

@Composable
fun <T> GroveCardList(
    items: List<T>,
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues(horizontal = GroveSpacing.LG),
    itemContent: @Composable (T) -> Unit,
) {
    GroveCard(modifier = modifier.fillMaxWidth(), padding = padding) {
        items.forEachIndexed { index, item ->
            itemContent(item)
            if (index < items.size - 1) {
                androidx.compose.material3.HorizontalDivider(color = GroveTheme.colors.border)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroveBottomSheet(onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    val c = GroveTheme.colors
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = c.bgCard,
        shape = GroveShapes.SheetTop,
    ) {
        Column(modifier = Modifier.fillMaxWidth().groveFadeSlide(distance = 20.dp, durationMillis = 240), content = content)
    }
}
