package com.grove.app.feature.bills

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grove.app.data.BudgetState
import com.grove.app.data.model.Bill
import com.grove.app.designsystem.catalog.BillIcons
import com.grove.app.designsystem.component.AppTopBar
import com.grove.app.designsystem.component.FieldLabel
import com.grove.app.designsystem.component.GroveBottomSheet
import com.grove.app.designsystem.component.GroveCard
import com.grove.app.designsystem.component.GroveTextField
import com.grove.app.designsystem.component.PrimaryButton
import com.grove.app.designsystem.component.ProgressBar
import com.grove.app.designsystem.component.SectionHeader
import com.grove.app.designsystem.component.StatusPill
import com.grove.app.designsystem.format.Currencies
import com.grove.app.designsystem.format.Money
import com.grove.app.designsystem.theme.Fraunces
import com.grove.app.designsystem.theme.GroveBorder
import com.grove.app.designsystem.theme.GroveTheme
import com.grove.app.designsystem.theme.GroveShapes
import com.grove.app.designsystem.theme.GroveSpacing
import com.grove.app.designsystem.theme.GroveType
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun BillsScreen(
    state: BudgetState,
    currency: String,
    onToggleBill: (String) -> Unit,
    onAddBill: (Bill) -> Unit,
) {
    val c = GroveTheme.colors
    var showAdd by remember { mutableStateOf(false) }
    val sorted = remember(state.bills) { state.bills.sortedBy { it.dueDay } }
    val total = remember(state.bills) { state.bills.sumOf { it.amount } }
    val paidTotal = remember(state.bills) { state.bills.filter { it.paid }.sumOf { it.amount } }
    val monthName = remember(state.today) { state.today.format(DateTimeFormatter.ofPattern("MMM")) }

    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 120.dp)) {
        item { AppTopBar(title = "Bills", subtitle = "${state.bills.count { !it.paid }} unpaid") }

        item {
            GroveCard(modifier = Modifier.fillMaxWidth(), padding = PaddingValues(GroveSpacing.LG)) {
                Text("MONTHLY RECURRING", style = GroveType.fieldLabel, color = c.fg3)
                Spacer(Modifier.height(GroveSpacing.SM))
                Text(Money.currency(total, 0, currency), fontFamily = Fraunces, fontWeight = FontWeight.Medium, fontSize = 36.sp, letterSpacing = (-0.7).sp, color = c.fg1)
                Spacer(Modifier.height(GroveSpacing.SM + 2.dp))
                ProgressBar((paidTotal / total).toFloat(), c.accent)
                Spacer(Modifier.height(GroveSpacing.SM))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${Money.currency(paidTotal, 0, currency)} paid", style = GroveType.rowSub, color = c.fg3)
                    Text("${Money.currency(total - paidTotal, 0, currency)} remaining", style = GroveType.rowSub, color = c.fg3)
                }
            }
        }

        item {
            SectionHeader(
                "${monthName.uppercase()} BILLS",
                modifier = Modifier.padding(top = GroveSpacing.XL, bottom = GroveSpacing.SM),
                linkText = "+ Add",
                onLink = { showAdd = true },
            )
        }

        item {
            GroveCard(modifier = Modifier.fillMaxWidth(), padding = PaddingValues(horizontal = GroveSpacing.LG)) {
                sorted.forEachIndexed { i, bill ->
                    BillRow(bill, state.dayOfMonth, state.today, currency) { onToggleBill(bill.id) }
                    if (i < sorted.size - 1) HorizontalDivider(color = c.border)
                }
            }
        }
    }

    if (showAdd) AddBillSheet(currency, onDismiss = { showAdd = false }, onSave = { onAddBill(it); showAdd = false })
}

@Composable
private fun BillRow(bill: Bill, dayOfMonth: Int, today: java.time.LocalDateTime, currency: String, onToggle: () -> Unit) {
    val c = GroveTheme.colors
    val (statusLabel, statusKind) = when {
        bill.paid -> "Paid" to "success"
        bill.dueDay < dayOfMonth -> "Overdue" to "warn"
        bill.dueDay - dayOfMonth <= 5 -> "Due soon" to "warn"
        else -> "Upcoming" to "neutral"
    }
    val dueStr = LocalDate.of(today.year, today.month, bill.dueDay.coerceAtMost(today.month.maxLength())).format(DateTimeFormatter.ofPattern("MMM d"))

    Row(modifier = Modifier.fillMaxWidth().padding(vertical = GroveSpacing.MD), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(GroveShapes.SmallTile)
                .background(if (bill.paid) c.accent else c.bgCard)
                .border(GroveBorder.Strong, if (bill.paid) c.accent else c.borderStrong, GroveShapes.SmallTile)
                .clickable { onToggle() },
            contentAlignment = Alignment.Center,
        ) {
            if (bill.paid) Icon(Icons.Default.Check, contentDescription = null, tint = c.fgOnFern, modifier = Modifier.size(15.dp))
        }
        Spacer(Modifier.width(GroveSpacing.SM + 4.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(BillIcons.of(bill.icon), contentDescription = null, tint = c.fg2, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(10.dp))
                Text(bill.name, style = GroveType.rowTitle, color = c.fg1)
            }
            Spacer(Modifier.height(GroveSpacing.XS))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Due $dueStr", style = GroveType.rowSub, color = c.fg3)
                Spacer(Modifier.width(GroveSpacing.SM))
                Box(modifier = Modifier.size(4.dp).clip(GroveShapes.Chip).background(c.fg3))
                Spacer(Modifier.width(GroveSpacing.SM))
                StatusPill(statusLabel, statusKind)
            }
        }
        Spacer(Modifier.width(GroveSpacing.SM))
        Text(
            Money.currency(bill.amount, 0, currency), style = GroveType.amount,
            color = if (bill.paid) c.fg3 else c.fg1,
            textDecoration = if (bill.paid) TextDecoration.LineThrough else TextDecoration.None,
        )
    }
}

@Composable
private fun AddBillSheet(currency: String, onDismiss: () -> Unit, onSave: (Bill) -> Unit) {
    val c = GroveTheme.colors
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var dueDay by remember { mutableStateOf("") }
    val valid = name.isNotBlank() && (amount.toDoubleOrNull() ?: 0.0) > 0

    GroveBottomSheet(onDismiss = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = GroveSpacing.XL)
                .padding(bottom = GroveSpacing.XL),
        ) {
            Text("Add a bill", style = GroveType.sheetTitle, color = c.fg1)
            Spacer(Modifier.height(GroveSpacing.SM + 4.dp))
            FieldLabel("NAME")
            GroveTextField(value = name, onValueChange = { name = it }, placeholder = "e.g. Hulu")
            Spacer(Modifier.height(GroveSpacing.SM + 2.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(GroveSpacing.SM)) {
                Column(modifier = Modifier.weight(1f)) {
                    FieldLabel("AMOUNT (${Currencies.current(currency).symbol})")
                    GroveTextField(value = amount, onValueChange = { amount = it.filter { ch -> ch.isDigit() || ch == '.' } }, placeholder = "0")
                }
                Column(modifier = Modifier.weight(1f)) {
                    FieldLabel("DUE DAY")
                    GroveTextField(value = dueDay, onValueChange = { dueDay = it.filter { ch -> ch.isDigit() } }, placeholder = "15")
                }
            }
            Spacer(Modifier.height(GroveSpacing.SM + 6.dp))
            PrimaryButton(
                "Save bill",
                onClick = {
                    onSave(
                        Bill(
                            id = "b${System.currentTimeMillis()}",
                            name = name.trim(),
                            amount = amount.toDoubleOrNull() ?: 0.0,
                            dueDay = (dueDay.toIntOrNull() ?: 1).coerceIn(1, 28),
                            paid = false,
                            icon = "other",
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = valid,
            )
        }
    }
}
