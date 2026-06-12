package com.grove.app.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grove.app.designsystem.theme.GroveTheme
import com.grove.app.designsystem.theme.GroveBorder
import com.grove.app.designsystem.theme.GroveShapes
import com.grove.app.designsystem.theme.GroveSize
import com.grove.app.designsystem.theme.GroveSpacing
import com.grove.app.designsystem.theme.InterTight

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        modifier = modifier.height(GroveSize.NavItemHeight),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = GroveTheme.colors.accent,
            contentColor = GroveTheme.colors.fgOnFern,
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp, pressedElevation = 0.dp),
        shape = GroveShapes.Button,
    ) {
        Text(text, fontFamily = InterTight, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}

@Composable
fun IconCircleButton(icon: ImageVector, contentDescription: String?, onClick: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .size(GroveSize.IconCircle)
            .clip(GroveShapes.Chip)
            .background(GroveTheme.colors.bgCardRaised)
            .border(GroveBorder.Thin, GroveTheme.colors.border, GroveShapes.Chip)
            .clickable(role = Role.Button) { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = contentDescription, tint = GroveTheme.colors.fg2, modifier = Modifier.size(18.dp))
    }
}
