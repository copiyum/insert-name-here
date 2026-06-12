package com.grove.app.feature.addexpense

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grove.app.data.db.CategoryLite
import com.grove.app.data.model.Expense
import com.grove.app.designsystem.component.CategoryIcon
import com.grove.app.designsystem.component.GroveBottomSheet
import com.grove.app.designsystem.component.Keypad
import com.grove.app.designsystem.component.MoneyText
import com.grove.app.designsystem.component.MoneyTextSize
import com.grove.app.designsystem.component.PrimaryButton
import com.grove.app.designsystem.format.Currencies
import com.grove.app.designsystem.format.Money
import com.grove.app.designsystem.theme.Fraunces
import com.grove.app.designsystem.theme.GroveSpacing
import com.grove.app.designsystem.theme.GroveTheme
import com.grove.app.designsystem.theme.InterTight
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseSheet(
    categories: List<CategoryLite>,
    currency: String,
    editing: Expense? = null,
    onSave: (Expense) -> Unit,
    onDismiss: () -> Unit,
    ) {
    val c = GroveTheme.colors
    val minorExponent = Currencies.minorUnitExponent(currency)
    var amount by remember(editing) {
        mutableStateOf(
            editing?.let {
                val d = Money.fromMinor(it.amountMinor, currency)
                if (minorExponent == 0) d.toLong().toString() else String.format(Locale.US, "%.${minorExponent}f", d)
            } ?: "",
        )
    }
    val amountNum = amount.toDoubleOrNull() ?: 0.0
    val amountMinor = Money.toMinor(amountNum, currency)
    var selectedCategoryId by remember(editing) {
        mutableStateOf(editing?.categoryId?.toString() ?: categories.firstOrNull()?.id?.toString())
    }
    var note by remember(editing) { mutableStateOf(editing?.note ?: "") }
    var isDetailsStep by remember { mutableStateOf(false) }
    var selectedDate by remember(editing) {
        mutableStateOf(editing?.occurredAt?.atZone(ZoneId.systemDefault())?.toLocalDate() ?: LocalDate.now())
    }
    var showDatePicker by remember { mutableStateOf(false) }

    fun saveExpense() {
        val now = Instant.now()
        val catId = selectedCategoryId?.let { UUID.fromString(it) } ?: categories.firstOrNull()?.id ?: return
        val editingDate = editing?.occurredAt?.atZone(ZoneId.systemDefault())?.toLocalDate()
        val occurredAt =
            if (editing != null && selectedDate == editingDate) {
                editing.occurredAt
            } else {
                selectedDate.atTime(LocalTime.now()).atZone(ZoneId.systemDefault()).toInstant()
            }
        onSave(
            Expense(
                id = editing?.id ?: UUID.randomUUID(),
                amountMinor = amountMinor,
                currencyCode = currency,
                categoryId = catId,
                paymentMethodId = editing?.paymentMethodId,
                note = note,
                occurredAt = occurredAt,
                createdAt = editing?.createdAt ?: now,
                updatedAt = now,
            ),
        )
        onDismiss()
    }

    GroveBottomSheet(onDismiss = onDismiss) {
        if (!isDetailsStep) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(bottom = GroveSpacing.MD),
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
                        enabled = amountNum > 0,
                    ) {
                        Text("Next", color = if (amountNum > 0) c.fg1 else c.fg2, fontFamily = InterTight, fontSize = 16.sp)
                    }
                }

                Spacer(Modifier.height(GroveSpacing.XL))

                Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.Center) {
                    Text(Currencies.current(currency).symbol, fontFamily = Fraunces, fontSize = 32.sp, color = c.fg2, modifier = Modifier.padding(top = 8.dp, end = 4.dp))
                    MoneyText(amount.ifEmpty { "0" }, size = MoneyTextSize.Hero, color = c.fg1)
                }

                Spacer(Modifier.height(GroveSpacing.LG))

                Keypad(
                    onDigit = { digit ->
                        val fractional = amount.substringAfter(".", "")
                        if (amount.length < 10 && (!amount.contains(".") || fractional.length < minorExponent)) amount += digit
                    },
                    onBackspace = {
                        if (amount.isNotEmpty()) amount = amount.dropLast(1)
                    },
                    onDecimal = {
                        if (minorExponent > 0 && !amount.contains(".")) amount += if (amount.isEmpty()) "0." else "."
                    },
                    modifier = Modifier.padding(horizontal = GroveSpacing.XL),
                )

                Spacer(Modifier.height(GroveSpacing.XL))
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = GroveSpacing.LG, vertical = GroveSpacing.MD),
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
                        modifier = Modifier.offset(x = 8.dp),
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
                                .clickable { isDetailsStep = false }
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
                    fontFamily = InterTight,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = c.fg3,
                    letterSpacing = 1.sp,
                )
                Spacer(Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    categories.chunked(4).forEach { rowCats ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            rowCats.forEach { cat ->
                                val isSelected = cat.id.toString() == selectedCategoryId
                                Box(
                                    modifier =
                                        Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(if (isSelected) c.accentSurface else c.bgCardRaised)
                                            .border(
                                                1.dp,
                                                if (isSelected) c.accent else c.border,
                                                RoundedCornerShape(16.dp),
                                            ).clickable(role = Role.Button) { selectedCategoryId = cat.id.toString() },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        CategoryIcon(iconKey = cat.iconKey, size = 38)
                                        Spacer(Modifier.height(8.dp))
                                        Text(text = cat.displayName, fontFamily = InterTight, fontSize = 12.sp, color = c.fg1)
                                    }
                                }
                            }
                            repeat(4 - rowCats.size) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }

                Spacer(Modifier.height(GroveSpacing.XL))

                Text(
                    "NOTE",
                    fontFamily = InterTight,
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
                    fontFamily = InterTight,
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
                            .clickable(role = Role.Button) { showDatePicker = true }
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
                    modifier = Modifier.fillMaxWidth(),
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
