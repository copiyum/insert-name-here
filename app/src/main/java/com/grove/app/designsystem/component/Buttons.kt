package com.grove.app.designsystem.component

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
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
import com.grove.app.designsystem.theme.GroveSprings
import com.grove.app.designsystem.theme.InterTight

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    val view = LocalView.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (enabled && pressed) 0.96f else 1f,
        animationSpec = GroveSprings.snappy(),
        label = "primaryButtonScale",
    )
    Button(
        onClick = {
            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            onClick()
        },
        modifier =
            modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .height(GroveSize.NavItemHeight),
        enabled = enabled,
        interactionSource = interactionSource,
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
            .size(48.dp)
            .groveClick(role = Role.Button) { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(GroveSize.IconCircle)
                .clip(GroveShapes.Chip)
                .background(GroveTheme.colors.bgCardRaised)
                .border(GroveBorder.Thin, GroveTheme.colors.border, GroveShapes.Chip),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = contentDescription, tint = GroveTheme.colors.fg2, modifier = Modifier.size(18.dp))
        }
    }
}
