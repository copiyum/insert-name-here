package com.grove.app.feature.budget

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grove.app.data.BudgetState
import com.grove.app.designsystem.catalog.CategoryVisuals
import com.grove.app.designsystem.component.AppTopBar
import com.grove.app.designsystem.component.CategoryIcon
import com.grove.app.designsystem.component.EmptyState
import com.grove.app.designsystem.component.GroveBottomSheet
import com.grove.app.designsystem.component.GroveCard
import com.grove.app.designsystem.component.GroveCardVariant
import com.grove.app.designsystem.component.IconTileRow
import com.grove.app.designsystem.component.MoneyText
import com.grove.app.designsystem.component.MoneyTextSize
import com.grove.app.designsystem.component.PresetChipRow
import com.grove.app.designsystem.component.PrimaryButton
import com.grove.app.designsystem.component.ProgressBar
import com.grove.app.designsystem.component.SectionHeader
import com.grove.app.designsystem.component.Stepper
import com.grove.app.designsystem.format.Money
import com.grove.app.designsystem.theme.GroveShapes
import com.grove.app.designsystem.theme.GroveSpacing
import com.grove.app.designsystem.theme.GroveTheme
import com.grove.app.designsystem.theme.GroveType

private sealed interface EditTarget {
    data object Monthly : EditTarget

    data class Cat(
        val id: String,
        val name: String,
        val value: Double,
    ) : EditTarget
}

@Composable
fun BudgetScreen(
    state: BudgetState,
    currency: String,
    onUpdateBudget: (Double) -> Unit,
    onUpdateCatBudget: (String, Double) -> Unit,
) {
    val c = GroveTheme.colors
    var edit by remember { mutableStateOf<EditTarget?>(null) }

    val spentByCat =
        remember(state.expenses, state.period) {
            state.expenses
                .filter { state.period.contains(it.occurredAt) }
                .groupBy { it.categoryId }
                .mapValues { (_, e) -> e.sumOf { it.amountMinor } }
        }
    val limitsByCat = remember(state.categoryBudgets) { state.categoryBudgets.associate { it.categoryId to it.amountMinor } }
    val allocated = limitsByCat.values.sum()
    val unallocated = (state.monthBudgetMinor - allocated).coerceAtLeast(0L)
    val segments =
        remember(allocated, unallocated) {
            buildList {
                if (allocated > 0) add(allocated to c.boneSoft)
                if (unallocated > 0) add(unallocated to c.bgMuted)
            }
        }

    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 120.dp)) {
        item { AppTopBar(title = "Budget", subtitle = "Monthly plan") }

        item {
            GroveCard(modifier = Modifier.fillMaxWidth(), padding = PaddingValues(GroveSpacing.LG), variant = GroveCardVariant.Elevated) {
                Text("MONTHLY BUDGET", style = GroveType.fieldLabel, color = c.fg3)
                Spacer(Modifier.height(GroveSpacing.SM))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MoneyText(
                        Money.currency(state.monthBudget, 0, currency),
                        size = MoneyTextSize.Display,
                        color = c.fg1,
                    )
                    Spacer(Modifier.width(GroveSpacing.SM))
                    EditButton(size = 32) { edit = EditTarget.Monthly }
                }
                Spacer(Modifier.height(GroveSpacing.SM + 2.dp))
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(GroveShapes.Toggle)
                            .background(c.bgMuted),
                ) {
                    segments.forEach { (value, color) ->
                        if (value > 0) {
                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxHeight()
                                        .weight(
                                            (value.toDouble() / state.monthBudgetMinor.coerceAtLeast(1L).toDouble()).toFloat().coerceAtLeast(0.001f),
                                        ).background(color),
                            )
                        }
                    }
                }
                Spacer(Modifier.height(GroveSpacing.SM))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${Money.currencyLong(allocated, 0, currency)} allocated", style = GroveType.rowSub, color = c.fg3)
                    Text("${Money.currencyLong(unallocated, 0, currency)} free", style = GroveType.rowSub, color = c.fg3)
                }
            }
        }

        item { SectionHeader("BY CATEGORY", modifier = Modifier.padding(top = GroveSpacing.XL, bottom = GroveSpacing.SM)) }

        item {
            GroveCard(modifier = Modifier.fillMaxWidth(), padding = PaddingValues(horizontal = GroveSpacing.LG)) {
                if (state.categories.isEmpty()) {
                    EmptyState("No categories yet", subtitle = "Categories will appear here after setup.")
                } else {
                    state.categories.forEachIndexed { i, cat ->
                        val spent = spentByCat[cat.id] ?: 0L
                        val limit = limitsByCat[cat.id] ?: 0L
                        val pct = if (limit > 0) spent.toDouble() / limit.toDouble() else 0.0
                        val barColor =
                            when {
                                limit == 0L -> c.bgMuted
                                pct > 1.0 -> c.danger
                                pct > 0.85 -> c.warn
                                else -> CategoryVisuals.color(cat.iconKey)
                            }
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = GroveSpacing.MD),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            CategoryIcon(cat.iconKey)
                            Spacer(Modifier.width(GroveSpacing.SM))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(cat.displayName, style = GroveType.rowTitle, color = c.fg1)
                                    Text(
                                        "${Money.currencyLong(spent, 0, currency)} / ${Money.currencyLong(limit, 0, currency)}",
                                        style = GroveType.rowSub,
                                        color = c.fg3,
                                    )
                                }
                                Spacer(Modifier.height(GroveSpacing.SM))
                                ProgressBar(pct.toFloat().coerceIn(0f, 1f), barColor, height = 5)
                            }
                            Spacer(Modifier.width(GroveSpacing.SM))
                            EditButton(size = 32) { edit = EditTarget.Cat(cat.id.toString(), cat.displayName, Money.fromMinor(limit, currency)) }
                        }
                        if (i < state.categories.size - 1) HorizontalDivider(color = c.border)
                    }
                }
            }
        }

        item { SectionHeader("RECURRING BILLS", modifier = Modifier.padding(top = GroveSpacing.XL, bottom = GroveSpacing.SM)) }

        item {
            GroveCard(modifier = Modifier.fillMaxWidth(), padding = PaddingValues(horizontal = GroveSpacing.LG)) {
                if (state.bills.isEmpty()) {
                    EmptyState("No recurring bills", subtitle = "Add bills to keep them reserved in your budget.")
                } else {
                    state.bills.take(4).forEachIndexed { i, bill ->
                        IconTileRow(
                            com.grove.app.designsystem.catalog.BillIcons
                                .of(bill.iconKey),
                            bill.name,
                            "Day ${bill.dueDay}",
                        ) { MoneyText(Money.currencyLong(bill.amountMinor, 0, currency), size = MoneyTextSize.Row, color = c.fg1) }
                        if (i < state.bills.take(4).size - 1) HorizontalDivider(color = c.border)
                    }
                }
            }
        }
    }

    edit?.let { target ->
        val monthly = target is EditTarget.Monthly
        BudgetEditSheet(
            title = if (monthly) "Monthly budget" else "${(target as EditTarget.Cat).name} budget",
            sub = if (monthly) "Your total to spend each month" else "Set a limit for this category",
            value =
                when (target) {
                    is EditTarget.Monthly -> state.monthBudget.toInt()
                    is EditTarget.Cat -> target.value.toInt()
                },
            step = if (monthly) 50 else 10,
            presets = if (monthly) listOf(50, 100, 250) else listOf(10, 25, 50),
            currency = currency,
            onClose = { edit = null },
            onSave = { v ->
                when (target) {
                    is EditTarget.Monthly -> onUpdateBudget(v.toDouble())
                    is EditTarget.Cat -> onUpdateCatBudget(target.id, v.toDouble())
                }
            },
        )
    }
}

@Composable
private fun EditButton(
    size: Int = 40,
    onClick: () -> Unit,
) {
    Box(modifier = Modifier.size(size.dp).clip(GroveShapes.SmallTile).clickable(role = Role.Button) { onClick() }, contentAlignment = Alignment.Center) {
        Icon(Icons.Outlined.Edit, contentDescription = "Edit", tint = GroveTheme.colors.fg3, modifier = Modifier.size((size * 0.45f).dp))
    }
}

@Composable
private fun BudgetEditSheet(
    title: String,
    sub: String,
    value: Int,
    step: Int,
    presets: List<Int>,
    currency: String,
    onClose: () -> Unit,
    onSave: (Int) -> Unit,
) {
    val c = GroveTheme.colors
    var v by remember { mutableIntStateOf(value) }
    GroveBottomSheet(onDismiss = onClose) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = GroveSpacing.LG).padding(bottom = GroveSpacing.XL)) {
            Text(title, style = GroveType.sheetTitle, color = c.fg1, modifier = Modifier.padding(bottom = GroveSpacing.XS))
            Text(sub, style = GroveType.rowSub, color = c.fg3, modifier = Modifier.padding(bottom = GroveSpacing.SM))
            Stepper(value = v, onChange = { v = it }, step = step, min = 0, currency = currency)
            PresetChipRow(items = presets, label = {
                "+${it}"
            }, onClick = { v += it }, modifier = Modifier.fillMaxWidth().padding(top = GroveSpacing.XS))
            Spacer(Modifier.height(GroveSpacing.SM + 4.dp))
            PrimaryButton("Save", onClick = {
                onSave(v)
                onClose()
            }, modifier = Modifier.fillMaxWidth())
        }
    }
}
