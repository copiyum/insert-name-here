package com.grove.app.feature.addexpense

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grove.app.data.model.CategoryLite
import com.grove.app.data.model.Expense
import com.grove.app.data.model.ExpenseInput
import com.grove.app.designsystem.catalog.CategoryVisuals
import com.grove.app.designsystem.component.GroveBottomSheet
import com.grove.app.designsystem.component.GroveHaptic
import com.grove.app.designsystem.component.Keypad
import com.grove.app.designsystem.component.MoneyText
import com.grove.app.designsystem.component.MoneyTextSize
import com.grove.app.designsystem.component.PrimaryButton
import com.grove.app.designsystem.component.groveClick
import com.grove.app.designsystem.component.rememberMoneyInputState
import com.grove.app.core.format.Currencies
import com.grove.app.core.format.Money
import com.grove.app.designsystem.theme.GroveShapes
import com.grove.app.designsystem.theme.GroveSpacing
import com.grove.app.designsystem.theme.GroveSprings
import com.grove.app.designsystem.theme.GroveTheme
import com.grove.app.designsystem.theme.InterTight
import com.grove.app.designsystem.theme.JetBrainsMono
import com.grove.app.designsystem.theme.SpaceGrotesk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseSheet(
    categories: List<CategoryLite>,
    currency: String,
    editing: Expense? = null,
    onSave: (ExpenseInput, Rect?) -> Unit,
    onDismiss: () -> Unit,
) {
    val c = GroveTheme.colors
    val amountState = rememberMoneyInputState(currency, editing?.amountMinor, editing?.id)
    val amountMinor = amountState.amountMinor
    var selectedCategoryId by remember(editing) {
        mutableStateOf(editing?.categoryId?.toString() ?: categories.firstOrNull()?.id?.toString())
    }
    var note by remember(editing) { mutableStateOf(editing?.note ?: "") }
    var isDetailsStep by remember(editing) { mutableStateOf(editing != null) }
    var selectedDate by remember(editing) {
        mutableStateOf(editing?.occurredAt?.atZone(ZoneId.systemDefault())?.toLocalDate() ?: LocalDate.now())
    }
    var showDatePicker by remember { mutableStateOf(false) }
    var saveOriginBounds by remember { mutableStateOf<Rect?>(null) }

    val backProgress = remember { Animatable(0f) }
    val backScope = rememberCoroutineScope()

    fun saveExpense() {
        val catId = selectedCategoryId?.let { runCatching { UUID.fromString(it) }.getOrNull() } ?: categories.firstOrNull()?.id ?: return
        val editingDate = editing?.occurredAt?.atZone(ZoneId.systemDefault())?.toLocalDate()
        val occurredAt =
            if (editing != null && selectedDate == editingDate) {
                editing.occurredAt
            } else {
                selectedDate.atTime(LocalTime.now()).atZone(ZoneId.systemDefault()).toInstant()
            }
        val input =
            ExpenseInput(
                id = editing?.id,
                amountMinor = amountMinor,
                currencyCode = currency,
                categoryId = catId,
                note = note,
                occurredAt = occurredAt,
            )
        onSave(
            input,
            saveOriginBounds,
        )
        onDismiss()
    }

    val backShrink =
        Modifier.graphicsLayer {
            val p = backProgress.value
            val scale = 1f - 0.06f * p
            scaleX = scale
            scaleY = scale
            alpha = 1f - 0.1f * p
        }

    GroveBottomSheet(onDismiss = onDismiss) {
        PredictiveBackHandler { progress ->
            try {
                progress.collect { event -> backProgress.snapTo(event.progress) }
                onDismiss()
            } catch (e: CancellationException) {
                backScope.launch { backProgress.animateTo(0f, GroveSprings.standard()) }
            }
        }

        if (!isDetailsStep) {
            Column(
                modifier = backShrink.fillMaxWidth().padding(bottom = GroveSpacing.MD),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = GroveSpacing.MD),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = c.fg1)
                    }
                    Text(
                        if (editing != null) "Edit expense" else "New expense",
                        fontFamily = InterTight,
                        fontSize = 16.sp,
                        color = c.fg1,
                    )
                    TextButton(
                        onClick = { isDetailsStep = true },
                        enabled = amountState.hasAmount && categories.isNotEmpty(),
                    ) {
                        Text("Next", color = if (amountState.hasAmount && categories.isNotEmpty()) c.fg1 else c.fg2, fontFamily = InterTight, fontSize = 16.sp)
                    }
                }

                Spacer(Modifier.height(GroveSpacing.XL))

                Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.Center) {
                    Text(Currencies.current(currency).symbol, fontFamily = SpaceGrotesk, fontSize = 32.sp, color = c.fg2, modifier = Modifier.padding(top = 8.dp, end = 4.dp))
                    MoneyText(amountState.text.ifEmpty { "0" }, size = MoneyTextSize.Hero, color = c.fg1)
                }

                Spacer(Modifier.height(GroveSpacing.LG))

                Keypad(
                    onDigit = amountState::appendDigit,
                    onBackspace = amountState::backspace,
                    onDecimal = amountState::appendDecimal,
                    modifier = Modifier.padding(horizontal = GroveSpacing.XL),
                )

                Spacer(Modifier.height(GroveSpacing.XL))
            }
        } else {
            Column(
                modifier = backShrink.fillMaxWidth().padding(horizontal = GroveSpacing.LG, vertical = GroveSpacing.MD),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onDismiss, modifier = Modifier.offset(x = (-8).dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = c.fg1)
                    }
                    Text("Details", fontFamily = InterTight, fontSize = 16.sp, color = c.fg1)
                    TextButton(
                        onClick = { saveExpense() },
                        modifier =
                            Modifier
                                .offset(x = 8.dp)
                                .onGloballyPositioned { saveOriginBounds = it.boundsInRoot() },
                    ) {
                        Text("Save", color = c.accent, fontFamily = InterTight, fontSize = 16.sp)
                    }
                }

                Spacer(Modifier.height(GroveSpacing.XL))

                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    MoneyText(
                        text =
                            Money.currencyLong(
                                amountMinor,
                                2,
                                currency,
                            ),
                        size = MoneyTextSize.Display,
                        color = c.fg1,
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier =
                            Modifier
                                .clip(RoundedCornerShape(50))
                                .groveClick(haptic = GroveHaptic.Light) { isDetailsStep = false }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Icon(Icons.Outlined.Edit, contentDescription = null, tint = c.fg2, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Edit", fontFamily = InterTight, fontSize = 14.sp, color = c.fg2)
                    }
                }

                Spacer(Modifier.height(GroveSpacing.XL))

                Text(
                    "CATEGORY",
                    fontFamily = JetBrainsMono,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = c.fg3,
                    letterSpacing = 1.sp,
                )
                Spacer(Modifier.height(8.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { cat ->
                        val isSelected = cat.id.toString() == selectedCategoryId
                        val color = CategoryVisuals.color(cat.iconKey)
                        Row(
                            modifier =
                                Modifier
                                    .clip(GroveShapes.Chip)
                                    .background(if (isSelected) color.copy(alpha = 0.18f) else c.bgCard)
                                    .border(1.dp, if (isSelected) color else c.border, GroveShapes.Chip)
                                    .groveClick(role = Role.Button, haptic = GroveHaptic.Light) { selectedCategoryId = cat.id.toString() }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(99.dp)).background(color))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = cat.displayName,
                                fontFamily = InterTight,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isSelected) c.fg1 else c.fg2,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(GroveSpacing.XL))

                Text(
                    "NOTE",
                    fontFamily = JetBrainsMono,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = c.fg3,
                    letterSpacing = 1.sp,
                )
                Spacer(Modifier.height(8.dp))
                BasicTextField(
                    value = note,
                    onValueChange = { note = it },
                    textStyle = TextStyle(fontFamily = InterTight, fontSize = 16.sp, color = c.fg1),
                    cursorBrush = SolidColor(c.accent),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(c.bgCard)
                                    .padding(16.dp),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            if (note.isEmpty()) {
                                Text("What was it?", fontFamily = InterTight, fontSize = 16.sp, color = c.fg2)
                            }
                            innerTextField()
                        }
                    },
                )

                Spacer(Modifier.height(GroveSpacing.XL))

                Text(
                    "DATE",
                    fontFamily = JetBrainsMono,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = c.fg3,
                    letterSpacing = 1.sp,
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(c.bgCard)
                            .groveClick(role = Role.Button) { showDatePicker = true }
                            .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Outlined.CalendarToday, contentDescription = null, tint = c.fg1, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(selectedDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy")), fontFamily = InterTight, fontSize = 16.sp, color = c.fg1)
                }

                Spacer(Modifier.height(GroveSpacing.XL))
                Spacer(Modifier.height(GroveSpacing.MD))

                PrimaryButton(
                    text = "Save expense",
                    onClick = { saveExpense() },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned { saveOriginBounds = it.boundsInRoot() },
                )

                Spacer(Modifier.height(GroveSpacing.LG))
            }
        }
    }

    if (showDatePicker) {
        val datePickerState =
            rememberDatePickerState(
                initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
            )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            selectedDate = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                        }
                        showDatePicker = false
                    },
                ) {
                    Text("Done", fontFamily = InterTight, color = c.accent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", fontFamily = InterTight, color = c.fg2)
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
