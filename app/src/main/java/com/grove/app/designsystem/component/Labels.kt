package com.grove.app.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import com.grove.app.designsystem.theme.GroveTheme
import com.grove.app.designsystem.theme.GroveSpacing
import com.grove.app.designsystem.theme.GroveType

@Composable
fun FieldLabel(text: String) {
    Text(text, style = GroveType.fieldLabel, color = GroveTheme.colors.fg3)
    Spacer(Modifier.height(GroveSpacing.SM))
}

@Composable
fun LinkText(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Text(
        text,
        style = GroveType.link,
        color = GroveTheme.colors.accent,
        modifier = modifier.groveClick(role = Role.Button, onClick = onClick),
    )
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    linkText: String? = null,
    onLink: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = GroveType.sectionLabel, color = GroveTheme.colors.fg2)
        if (linkText != null && onLink != null) LinkText(linkText, onLink)
    }
}
