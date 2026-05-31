package com.grove.app.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grove.app.designsystem.theme.GroveTheme
import com.grove.app.designsystem.theme.GroveBorder
import com.grove.app.designsystem.theme.GroveShapes
import com.grove.app.designsystem.theme.GroveSpacing
import com.grove.app.designsystem.theme.InterTight

@Composable
fun Chip(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(GroveShapes.Chip)
            .background(if (selected) GroveTheme.colors.fg1 else GroveTheme.colors.bgCard)
            .border(GroveBorder.Thin, if (selected) GroveTheme.colors.fg1 else GroveTheme.colors.border, GroveShapes.Chip)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = GroveSpacing.SM),
    ) {
        Text(
            text,
            fontFamily = InterTight,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = if (selected) GroveTheme.colors.bgCard else GroveTheme.colors.fg2,
        )
    }
}

@Composable
fun <T> PresetChipRow(
    items: List<T>,
    label: (T) -> String,
    onClick: (T) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    selected: (T) -> Boolean = { false },
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(GroveSpacing.SM)) {
        items.forEach { item ->
            Chip(text = label(item), selected = selected(item), onClick = { onClick(item) })
        }
    }
}
