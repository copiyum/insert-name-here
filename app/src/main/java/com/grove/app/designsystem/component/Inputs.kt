package com.grove.app.designsystem.component

import android.view.HapticFeedbackConstants
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grove.app.designsystem.format.Currencies
import com.grove.app.designsystem.theme.GroveBorder
import com.grove.app.designsystem.theme.GroveEase
import com.grove.app.designsystem.theme.GroveTheme
import com.grove.app.designsystem.theme.GroveShapes
import com.grove.app.designsystem.theme.GroveSize
import com.grove.app.designsystem.theme.GroveSpacing
import com.grove.app.designsystem.theme.GroveSprings
import com.grove.app.designsystem.theme.InterTight
import java.util.Locale

enum class FieldVariant { Outlined, Bare }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroveTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    variant: FieldVariant = FieldVariant.Outlined,
    singleLine: Boolean = true,
) {
    val c = GroveTheme.colors
    val place = @Composable { Text(placeholder, color = c.fg3, fontFamily = InterTight) }
    when (variant) {
        FieldVariant.Outlined -> OutlinedTextField(
            value = value, onValueChange = onValueChange, placeholder = place,
            modifier = modifier.fillMaxWidth(), singleLine = singleLine, shape = GroveShapes.InputOutlined,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = c.accent, unfocusedBorderColor = c.border,
                focusedContainerColor = c.bgCard, unfocusedContainerColor = c.bgCard,
                focusedTextColor = c.fg1, unfocusedTextColor = c.fg1, cursorColor = c.accent,
            ),
        )
        FieldVariant.Bare -> BasicTextField(
            value = value, onValueChange = onValueChange,
            modifier = modifier.fillMaxWidth().padding(vertical = 13.dp), singleLine = singleLine,
            textStyle = TextStyle(color = c.fg1, fontFamily = InterTight, fontSize = 15.sp),
            cursorBrush = SolidColor(c.accent),
            decorationBox = { inner ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty()) Text(placeholder, color = c.fg3, fontFamily = InterTight, fontSize = 15.sp)
                    inner()
                }
            },
        )
    }
}

@Composable
fun GroveSwitch(checked: Boolean, onToggle: () -> Unit) {
    val c = GroveTheme.colors
    val trackColor by animateColorAsState(
        targetValue = if (checked) c.accent else c.bgMuted,
        animationSpec = GroveEase.normal(),
        label = "switchTrackColor",
    )
    val borderColor by animateColorAsState(
        targetValue = if (checked) c.accent else c.border,
        animationSpec = GroveEase.normal(),
        label = "switchBorderColor",
    )
    val knobOffset by animateDpAsState(
        targetValue = if (checked) GroveSize.SwitchTrackW - GroveSize.SwitchKnob - 4.dp else 0.dp,
        animationSpec = GroveSprings.snappy(),
        label = "switchKnobOffset",
    )
    Box(
        modifier = Modifier
            .size(GroveSize.SwitchTrackW, GroveSize.SwitchTrackH)
            .clip(GroveShapes.Toggle)
            .background(trackColor)
            .border(GroveBorder.Thin, borderColor, GroveShapes.Toggle)
            .semantics { stateDescription = if (checked) "On" else "Off" }
            .groveClick(role = Role.Switch) { onToggle() },
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .padding(2.dp)
                .offset { IntOffset(knobOffset.roundToPx(), 0) }
                .size(GroveSize.SwitchKnob)
                .clip(GroveShapes.Toggle)
                .background(Color.White),
        )
    }
}

@Composable
fun Keypad(
    onDigit: (Char) -> Unit,
    onBackspace: () -> Unit,
    onDecimal: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = GroveTheme.colors
    val view = LocalView.current
    val rows = listOf(
        listOf('1', '2', '3'),
        listOf('4', '5', '6'),
        listOf('7', '8', '9'),
        listOf('.', '0', '\u232b'),
    )
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally),
            ) {
                row.forEach { key ->
                    val isBackspace = key == '\u232b'
                    val interactionSource = remember { MutableInteractionSource() }
                    val pressed by interactionSource.collectIsPressedAsState()
                    val scale by animateFloatAsState(
                        targetValue = if (pressed) 0.96f else 1f,
                        animationSpec = GroveSprings.snappy(),
                        label = "keypadScale",
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                alpha = if (pressed) 0.85f else 1f
                            }
                            .clip(GroveShapes.Stepper)
                            .then(
                                if (pressed) Modifier.background(c.bone) else Modifier
                            )
                            .clickable(interactionSource = interactionSource, indication = null, role = Role.Button) {
                                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                                when {
                                    isBackspace -> onBackspace()
                                    key == '.' -> onDecimal()
                                    else -> onDigit(key)
                                }
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (isBackspace) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Backspace",
                                tint = c.fg1,
                                modifier = Modifier.size(20.dp),
                            )
                        } else {
                            Text(
                                key.toString(),
                                fontFamily = InterTight,
                                fontWeight = FontWeight.Medium,
                                fontSize = 22.sp,
                                color = c.fg1,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Stepper(value: Int, onChange: (Int) -> Unit, modifier: Modifier = Modifier, step: Int = 10, min: Int = 0, currency: String = "INR") {
    val c = GroveTheme.colors
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = GroveSpacing.SM),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StepperButton(Icons.Outlined.Remove, "Decrease") { onChange((value - step).coerceAtLeast(min)) }
        MoneyText(
            "${Currencies.current(currency).symbol}${String.format(Locale.US, "%,d", value)}",
            size = MoneyTextSize.Display,
            color = c.fg1,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f),
        )
        StepperButton(Icons.Outlined.Add, "Increase") { onChange(value + step) }
    }
}

@Composable
private fun StepperButton(icon: ImageVector, contentDescription: String, onClick: () -> Unit) {
    val c = GroveTheme.colors
    // 48dp minimum touch target; the visual pill stays at its original size.
    Box(
        modifier = Modifier
            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
            .groveClick(role = Role.Button, haptic = GroveHaptic.Light) { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(GroveSize.SwitchTrackW, GroveSize.SwitchTrackH)
                .clip(GroveShapes.Stepper)
                .background(c.bgCard)
                .border(GroveBorder.Strong, c.borderStrong, GroveShapes.Stepper),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = contentDescription, tint = c.fg1, modifier = Modifier.size(22.dp))
        }
    }
}
