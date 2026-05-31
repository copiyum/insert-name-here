package com.grove.app.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grove.app.designsystem.theme.Fraunces
import com.grove.app.designsystem.theme.GroveBorder
import com.grove.app.designsystem.theme.GroveTheme
import com.grove.app.designsystem.theme.GroveShapes
import com.grove.app.designsystem.theme.GroveSize
import com.grove.app.designsystem.theme.GroveSpacing
import com.grove.app.designsystem.theme.InterTight

enum class FieldVariant { Outlined, Filled, Bare }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroveTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    variant: FieldVariant = FieldVariant.Outlined,
    singleLine: Boolean = true,
) {
    val c = GroveTheme.colors
    val place = @Composable { Text(placeholder, color = c.fg3, fontFamily = InterTight) }
    when (variant) {
        FieldVariant.Outlined -> OutlinedTextField(
            value = value, onValueChange = onValueChange, placeholder = place,
            modifier = modifier, singleLine = singleLine, shape = GroveShapes.InputOutlined,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = c.accent, unfocusedBorderColor = c.border,
                focusedContainerColor = c.bgCard, unfocusedContainerColor = c.bgCard,
                focusedTextColor = c.fg1, unfocusedTextColor = c.fg1, cursorColor = c.accent,
            ),
        )
        FieldVariant.Filled -> TextField(
            value = value, onValueChange = onValueChange, placeholder = place,
            modifier = modifier, singleLine = singleLine, shape = GroveShapes.InputFilled,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = c.bgMuted, unfocusedContainerColor = c.bgMuted,
                focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = c.fg1, unfocusedTextColor = c.fg1, cursorColor = c.accent,
            ),
        )
        FieldVariant.Bare -> BasicTextField(
            value = value, onValueChange = onValueChange,
            modifier = modifier.padding(vertical = 13.dp), singleLine = singleLine,
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
    Box(
        modifier = Modifier
            .size(GroveSize.SwitchTrackW, GroveSize.SwitchTrackH)
            .clip(GroveShapes.Toggle)
            .background(if (checked) c.accent else c.bgMuted)
            .border(GroveBorder.Thin, if (checked) c.accent else c.border, GroveShapes.Toggle)
            .clickable { onToggle() },
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .padding(2.dp)
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
    val rows = listOf(
        listOf('1', '2', '3'),
        listOf('4', '5', '6'),
        listOf('7', '8', '9'),
        listOf('.', '0', '\u232b'),
    )
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(GroveSpacing.SM),
    ) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(GroveSpacing.SM, Alignment.CenterHorizontally),
            ) {
                row.forEach { key ->
                    val isBackspace = key == '\u232b'
                    Box(
                        modifier = Modifier
                            .size(GroveSize.KeypadButtonH, GroveSize.KeypadButtonH)
                            .clip(GroveShapes.Keypad)
                            .background(c.bgCard)
                            .border(GroveBorder.Thin, c.border, GroveShapes.Keypad)
                            .clickable {
                                if (isBackspace) onBackspace() else onDigit(key)
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            if (key == '\u232b') "\u232b" else key.toString(),
                            fontFamily = Fraunces, fontWeight = FontWeight.Normal, fontSize = 22.sp,
                            color = c.fg1,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Stepper(value: Int, onChange: (Int) -> Unit, step: Int = 10, min: Int = 0, modifier: Modifier = Modifier) {
    val c = GroveTheme.colors
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = GroveSpacing.SM),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StepperButton(Icons.Outlined.Remove, "Decrease") { onChange((value - step).coerceAtLeast(min)) }
        Text(
            "$${String.format("%,d", value)}",
            fontFamily = Fraunces, fontWeight = FontWeight.Normal, fontSize = 40.sp,
            letterSpacing = (-0.8).sp, color = c.fg1, textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f),
        )
        StepperButton(Icons.Outlined.Add, "Increase") { onChange(value + step) }
    }
}

@Composable
private fun StepperButton(icon: ImageVector, contentDescription: String, onClick: () -> Unit) {
    val c = GroveTheme.colors
    Box(
        modifier = Modifier
            .size(GroveSize.SwitchTrackW, GroveSize.SwitchTrackH)
            .clip(GroveShapes.Stepper)
            .background(c.bgCard)
            .border(GroveBorder.Strong, c.borderStrong, GroveShapes.Stepper)
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = contentDescription, tint = c.fg1, modifier = Modifier.size(22.dp))
    }
}
