package com.grove.app.feature.bills

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DeleteOutline
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.grove.app.data.BudgetState
import com.grove.app.data.db.BillLite
import com.grove.app.data.model.Bill
import com.grove.app.data.model.BillFrequency
import com.grove.app.designsystem.catalog.BillIcons
import com.grove.app.designsystem.component.AppTopBar
import com.grove.app.designsystem.component.BotanicalEmptyState
import com.grove.app.designsystem.component.FieldLabel
import com.grove.app.designsystem.component.GroveBottomSheet
import com.grove.app.designsystem.component.GroveCard
import com.grove.app.designsystem.component.GroveCardVariant
import com.grove.app.designsystem.component.GroveTextField
import com.grove.app.designsystem.component.MoneyText
import com.grove.app.designsystem.component.MoneyTextSize
import com.grove.app.designsystem.component.PrimaryButton
import com.grove.app.designsystem.component.ProgressBar
import com.grove.app.designsystem.component.SectionHeader
import com.grove.app.designsystem.component.GroveHaptic
import com.grove.app.designsystem.component.SwipeAction
import com.grove.app.designsystem.component.SwipeActionRow
import com.grove.app.designsystem.component.StatusPill
import com.grove.app.designsystem.component.groveClick
import com.grove.app.designsystem.component.rememberMoneyInputState
import com.grove.app.designsystem.format.Currencies
import com.grove.app.designsystem.format.Money
import com.grove.app.designsystem.theme.GroveBorder
import com.grove.app.designsystem.theme.GroveEase
import com.grove.app.designsystem.theme.GroveShapes
import com.grove.app.designsystem.theme.GroveSize
import com.grove.app.designsystem.theme.GroveSpacing
import com.grove.app.designsystem.theme.GroveSprings
import com.grove.app.designsystem.theme.GroveTheme
import com.grove.app.designsystem.theme.GroveType
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.UUID

@Composable
fun BillsScreen(
    state: BudgetState,
    currency: String,
    onToggleBill: (UUID) -> Unit,
    onAddBill: (Bill) -> Unit,
    onDeleteBill: (UUID) -> Unit,
) {
    val c = GroveTheme.colors
    val listState = rememberLazyListState()
    var showAdd by remember { mutableStateOf(false) }
    val sorted = remember(state.bills) { state.bills.sortedBy { it.dueDay } }
    val total = remember(state.bills) { state.bills.sumOf { it.amountMinor } }
    val paidTotal = remember(state.bills) { state.bills.filter { it.paid }.sumOf { it.amountMinor } }
    val monthName = remember(state.today) { state.today.format(DateTimeFormatter.ofPattern("MMM")) }
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = GroveSize.NavClearance + bottomInset),
    ) {
        item { AppTopBar(title = "Bills", subtitle = "${state.bills.count { !it.paid }} unpaid") }

        item {
            GroveCard(modifier = Modifier.fillMaxWidth(), padding = PaddingValues(GroveSpacing.LG), variant = GroveCardVariant.Elevated) {
                Text("MONTHLY RECURRING", style = GroveType.fieldLabel, color = c.fg3)
                Spacer(Modifier.height(GroveSpacing.SM))
                MoneyText(
                    Money.currencyLong(total, 0, currency),
                    size = MoneyTextSize.Display,
                    color = c.fg1,
                )
                Spacer(Modifier.height(GroveSpacing.SM + 2.dp))
                ProgressBar((paidTotal.toFloat() / total.coerceAtLeast(1L).toFloat()), c.accent)
                Spacer(Modifier.height(GroveSpacing.SM))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${Money.currencyLong(paidTotal, 0, currency)} paid", style = GroveType.rowSub, color = c.fg3)
                    Text("${Money.currencyLong(total - paidTotal, 0, currency)} remaining", style = GroveType.rowSub, color = c.fg3)
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
                if (sorted.isEmpty()) {
                    BotanicalEmptyState(
                        title = "No bills planted",
                        subtitle = "Add recurring bills so safe-to-spend stays honest.",
                        ctaText = "Add a bill",
                        onCta = { showAdd = true },
                    )
                } else {
                    sorted.forEachIndexed { i, bill ->
                        BillRow(bill, state.dayOfMonth, state.today, currency, onToggle = { onToggleBill(bill.id) }, onDelete = { onDeleteBill(bill.id) })
                        if (i < sorted.size - 1) HorizontalDivider(color = c.border)
                    }
                }
            }
        }
    }

    if (showAdd) {
        AddBillSheet(currency, onDismiss = { showAdd = false }, onSave = {
            onAddBill(it)
            showAdd = false
        })
    }
}

@Composable
private fun BillRow(
    bill: BillLite,
    dayOfMonth: Int,
    today: java.time.LocalDateTime,
    currency: String,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
) {
    val c = GroveTheme.colors
    val (statusLabel, statusKind) =
        when {
            bill.paid -> "Paid" to "success"
            bill.dueDay < dayOfMonth -> "Overdue" to "danger"
            bill.dueDay - dayOfMonth <= 3 -> "Due soon" to "warn"
            else -> "Upcoming" to "neutral"
        }
    val dueMonth = YearMonth.of(today.year, today.month)
    val dueStr = dueMonth.atDay(bill.dueDay.coerceAtMost(dueMonth.lengthOfMonth())).format(DateTimeFormatter.ofPattern("MMM d"))
    val checkBg by animateColorAsState(
        targetValue = if (bill.paid) c.accent else c.bgCard,
        animationSpec = GroveEase.normal(),
        label = "billCheckBg",
    )
    val checkBorder by animateColorAsState(
        targetValue = if (bill.paid) c.accent else c.borderStrong,
        animationSpec = GroveEase.normal(),
        label = "billCheckBorder",
    )
    val tickScale by animateFloatAsState(
        targetValue = if (bill.paid) 1f else 0.78f,
        animationSpec = GroveSprings.snappy(),
        label = "billTickScale",
    )
    val tickAlpha by animateFloatAsState(
        targetValue = if (bill.paid) 1f else 0f,
        animationSpec = GroveEase.fast(),
        label = "billTickAlpha",
    )

    SwipeActionRow(
        actions = listOf(SwipeAction(Icons.Outlined.DeleteOutline, "Delete", c.danger, Color.White, haptic = GroveHaptic.Heavy) { onDelete() }),
        verticalAlignment = Alignment.Top,
        onClick = onToggle,
    ) {
            Box(
                modifier = Modifier
                    .padding(top = 1.dp)
                    .size(28.dp)
                    .clip(GroveShapes.SmallTile)
                    .background(checkBg)
                    .border(GroveBorder.Strong, checkBorder, GroveShapes.SmallTile)
                    .groveClick(role = Role.Checkbox) { onToggle() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = c.fgOnFern,
                    modifier =
                        Modifier
                            .size(15.dp)
                            .graphicsLayer {
                                scaleX = tickScale
                                scaleY = tickScale
                                alpha = tickAlpha
                            },
                )
            }
            Spacer(Modifier.width(GroveSpacing.SM + 4.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(BillIcons.of(bill.iconKey), contentDescription = bill.name, tint = c.fg2, modifier = Modifier.size(18.dp))
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
                Money.currencyLong(bill.amountMinor, 0, bill.currencyCode.ifEmpty { currency }),
                style = GroveType.amount,
                color = if (bill.paid) c.fg3 else c.fg1,
                textDecoration = if (bill.paid) TextDecoration.LineThrough else TextDecoration.None,
            )
    }
}

@Composable
private fun AddBillSheet(
    currency: String,
    onDismiss: () -> Unit,
    onSave: (Bill) -> Unit,
) {
    val c = GroveTheme.colors
    var name by remember { mutableStateOf("") }
    val amountState = rememberMoneyInputState(currency)
    var dueDay by remember { mutableStateOf("15") }
    val parsedDueDay = dueDay.toIntOrNull()
    val validDueDay = parsedDueDay in 1..28
    val valid = name.isNotBlank() && amountState.hasAmount && validDueDay

    GroveBottomSheet(onDismiss = onDismiss) {
        Column(
            modifier =
                Modifier
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
                    GroveTextField(value = amountState.text, onValueChange = amountState::updateText, placeholder = "0")
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
                    val amountMinor = amountState.amountMinor
                    val now = Instant.now()
                    onSave(
                        Bill(
                            id = UUID.randomUUID(),
                            name = name.trim(),
                            amountMinor = amountMinor,
                            currencyCode = currency,
                            frequency = BillFrequency.monthly,
                            dueDay = (parsedDueDay ?: 15).coerceIn(1, 28),
                            dueWeekday = null,
                            startDate = now,
                            endDate = null,
                            iconKey = "other",
                            isActive = true,
                            createdAt = now,
                            updatedAt = now,
                        ),
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = valid,
            )
        }
    }
}
