package com.grove.app.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
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
    val c = GroveTheme.colors
    // Outer box guarantees a 48dp touch target; the visual pill stays compact and centered.
    Box(
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .groveClick(role = Role.Button, haptic = GroveHaptic.Light) { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .heightIn(min = 32.dp)
                .clip(GroveShapes.Chip)
                .background(if (selected) c.fg1 else c.bgCard)
                .border(GroveBorder.Thin, if (selected) c.fg1 else c.border, GroveShapes.Chip)
                .padding(horizontal = 14.dp, vertical = GroveSpacing.SM),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text,
                fontFamily = InterTight,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = if (selected) c.bgApp else c.fg2,
            )
        }
    }
}

@Composable
fun <T> PresetChipRow(
    items: List<T>,
    label: (T) -> String,
    onClick: (T) -> Unit,
    modifier: Modifier = Modifier,
    selected: (T) -> Boolean = { false },
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(GroveSpacing.SM)) {
        items.forEach { item ->
            Chip(text = label(item), selected = selected(item), onClick = { onClick(item) })
        }
    }
}
