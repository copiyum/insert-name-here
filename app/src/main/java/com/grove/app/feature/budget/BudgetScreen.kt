package com.grove.app.feature.budget

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grove.app.data.BudgetState
import com.grove.app.data.model.CategoryKind
import com.grove.app.data.suggestedCategoryBudgetMinor
import com.grove.app.data.suggestedMonthBudgetMinor
import com.grove.app.designsystem.catalog.CategoryVisuals
import com.grove.app.designsystem.component.AppTopBar
import com.grove.app.designsystem.component.CategoryIcon
import com.grove.app.designsystem.component.EmptyState
import com.grove.app.designsystem.component.GroveBottomSheet
import com.grove.app.designsystem.component.GroveCard
import com.grove.app.designsystem.component.GroveHaptic
import com.grove.app.designsystem.component.GroveCardVariant
import com.grove.app.designsystem.component.IconTileRow
import com.grove.app.designsystem.component.MoneyText
import com.grove.app.designsystem.component.MoneyTextSize
import com.grove.app.designsystem.component.PresetChipRow
import com.grove.app.designsystem.component.PrimaryButton
import com.grove.app.designsystem.component.ProgressBar
import com.grove.app.designsystem.component.SectionHeader
import com.grove.app.designsystem.component.Stepper
import com.grove.app.designsystem.component.groveClick
import com.grove.app.designsystem.component.groveScreenContentPadding
import com.grove.app.designsystem.component.rememberBounceOverscroll
import com.grove.app.core.format.Money
import com.grove.app.designsystem.theme.GroveShapes
import com.grove.app.designsystem.theme.GroveSpacing
import com.grove.app.designsystem.theme.GroveTheme
import com.grove.app.designsystem.theme.GroveType
import java.util.UUID
import kotlin.math.abs

private sealed interface EditTarget {
    data object Monthly : EditTarget

    data class Cat(
        val id: String,
        val name: String,
        val value: Double,
    ) : EditTarget
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BudgetScreen(
    state: BudgetState,
    currency: String,
    onUpdateBudget: (Double) -> Unit,
    onUpdateCatBudget: (String, Double) -> Unit,
) {
    val c = GroveTheme.colors
    val listState = rememberLazyListState()
    val bounce = rememberBounceOverscroll()
    var edit by remember { mutableStateOf<EditTarget?>(null) }
    val budgetCategories = remember(state.categories) { state.categories.filter { it.kind != CategoryKind.income } }
    val budgetCategoryIds = remember(budgetCategories) { budgetCategories.map { it.id }.toSet() }

    val spentByCat =
        remember(state.expenses, state.period) {
            state.spendingExpenses
                .filter { state.period.contains(it.occurredAt) }
                .groupBy { it.categoryId }
                .mapValues { (_, e) -> e.sumOf { it.amountMinor } }
        }
    val limitsByCat = remember(state.categoryBudgets) { state.categoryBudgets.associate { it.categoryId to it.amountMinor } }
    val allocated = limitsByCat.filterKeys { it in budgetCategoryIds }.values.sum()
    val unallocated = (state.monthBudgetMinor - allocated).coerceAtLeast(0L)
    val segments =
        remember(allocated, unallocated) {
            buildList {
                if (allocated > 0) add(allocated to c.boneSoft)
                if (unallocated > 0) add(unallocated to c.bgMuted)
            }
        }
    CompositionLocalProvider(LocalOverscrollConfiguration provides null) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize().nestedScroll(bounce.connection).then(bounce.modifier),
        contentPadding = groveScreenContentPadding(),
    ) {
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
                if (budgetCategories.isEmpty()) {
                    EmptyState("No categories yet", subtitle = "Categories will appear here after setup.")
                } else {
                    budgetCategories.forEachIndexed { i, cat ->
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
                                        "${Money.currencyLong(spent, 0, currency)} / ${if (limit > 0) Money.currencyLong(limit, 0, currency) else "—"}",
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
                        if (i < budgetCategories.size - 1) HorizontalDivider(color = c.border)
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
    }

    edit?.let { target ->
        val monthly = target is EditTarget.Monthly
        // Adaptive suggestion from spending history; shown only when it meaningfully
        // (>10%) differs from what's currently set.
        val suggestionMinor =
            remember(target, state) {
                when (target) {
                    is EditTarget.Monthly ->
                        state.suggestedMonthBudgetMinor()?.takeIf { s ->
                            abs(s - state.monthBudgetMinor) > state.monthBudgetMinor * 0.10
                        }
                    is EditTarget.Cat ->
                        runCatching { UUID.fromString(target.id) }
                            .getOrNull()
                            ?.let { state.suggestedCategoryBudgetMinor(it) }
                            ?.takeIf { s ->
                                val current = Money.toMinor(target.value, currency)
                                abs(s - current) > current * 0.10
                            }
                }
            }
        BudgetEditSheet(
            title = if (monthly) "Monthly budget" else "${(target as EditTarget.Cat).name} budget",
            sub = if (monthly) "Your total to spend each month" else "A monthly ceiling for this category",
            value =
                when (target) {
                    is EditTarget.Monthly -> state.monthBudget.toInt()
                    is EditTarget.Cat -> target.value.toInt()
                },
            step = if (monthly) 1000 else 500,
            presets = if (monthly) listOf(1000, 2500, 5000) else listOf(500, 1000, 2500),
            currency = currency,
            suggestionMinor = suggestionMinor,
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
    Box(modifier = Modifier.size(size.dp).clip(GroveShapes.SmallTile).groveClick(role = Role.Button) { onClick() }, contentAlignment = Alignment.Center) {
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
    suggestionMinor: Long? = null,
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
            if (suggestionMinor != null) {
                val suggestedMajor = Money.fromMinor(suggestionMinor, currency).toInt()
                if (suggestedMajor != v) {
                    Row(
                        modifier =
                            Modifier
                                .padding(top = GroveSpacing.SM)
                                .clip(GroveShapes.Chip)
                                .background(c.accentSurface)
                                .groveClick(haptic = GroveHaptic.Light) {
                                    v = suggestedMajor
                                    onSave(suggestedMajor)
                                }.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "Suggested · ${Money.currencyLong(suggestionMinor, 0, currency)}",
                            style = GroveType.rowSub,
                            color = if (c.isDark) c.accentSoft else c.accentDeep,
                        )
                    }
                }
            }
            Spacer(Modifier.height(GroveSpacing.SM + 4.dp))
            PrimaryButton("Save", onClick = {
                onSave(v)
                onClose()
            }, modifier = Modifier.fillMaxWidth())
        }
    }
}
