package com.grove.app.feature.reports

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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grove.app.data.BudgetState
import com.grove.app.designsystem.catalog.CategoryVisuals
import com.grove.app.designsystem.component.AppTopBar
import com.grove.app.designsystem.component.BotanicalEmptyState
import com.grove.app.designsystem.component.CategoryIcon
import com.grove.app.designsystem.component.Chip
import com.grove.app.designsystem.component.GroveCard
import com.grove.app.designsystem.component.GroveCardVariant
import com.grove.app.designsystem.component.MoneyText
import com.grove.app.designsystem.component.MoneyTextSize
import com.grove.app.designsystem.component.ProgressBar
import com.grove.app.designsystem.component.SectionHeader
import com.grove.app.designsystem.component.rememberFoliageOverscroll
import com.grove.app.designsystem.component.charts.DonutChart
import com.grove.app.designsystem.component.charts.DonutSlice
import com.grove.app.designsystem.component.charts.LineChart
import com.grove.app.designsystem.component.charts.MonthBar
import com.grove.app.designsystem.component.charts.MonthBars
import com.grove.app.designsystem.format.Money
import com.grove.app.designsystem.theme.GroveShapes
import com.grove.app.designsystem.theme.GroveSize
import com.grove.app.designsystem.theme.GroveSpacing
import com.grove.app.designsystem.theme.GroveTheme
import com.grove.app.designsystem.theme.GroveType
import com.grove.app.designsystem.theme.InterTight
import java.time.temporal.ChronoUnit
import kotlin.math.abs

private data class CategorySpend(
    val id: String,
    val label: String,
    val iconKey: String,
    val amount: Double,
) {
    val color: Color get() = CategoryVisuals.color(iconKey)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReportsScreen(
    state: BudgetState,
    currency: String,
) {
    val c = GroveTheme.colors
    val listState = rememberLazyListState()
    val sway = rememberFoliageOverscroll()
    var range by rememberSaveable { mutableStateOf("month") }
    var selected by rememberSaveable { mutableStateOf<String?>(null) }
    val monthName =
        state.today.month
            .toString()
            .lowercase()
            .replaceFirstChar { it.uppercase() }

    val filteredExpenses =
        remember(state.expenses, range, state.period, state.today) {
            val today = state.today.toLocalDate()
            state.spendingExpenses
                .filter { expense ->
                    val date = expense.occurredAt.atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                    when (range) {
                        "week" -> !date.isBefore(today.minusDays(6)) && !date.isAfter(today)
                        "3m" -> !date.isBefore(today.minusMonths(3)) && !date.isAfter(today)
                        else -> state.period.contains(expense.occurredAt)
                    }
                }
        }

    val byCategory: List<CategorySpend> =
        remember(filteredExpenses, currency) {
            filteredExpenses
                .groupBy { it.categoryId }
                .map { (_, expenses) ->
                    val first = expenses.first()
                    CategorySpend(
                        id = first.categoryId.toString(),
                        label = first.categoryName,
                        iconKey = first.iconKey,
                        amount = Money.fromMinor(expenses.sumOf { it.amountMinor }, currency),
                    )
                }
                .sortedByDescending { it.amount }
        }
    val filteredTotal = remember(byCategory) { byCategory.sumOf { it.amount } }
    val donutData =
        remember(byCategory) {
            byCategory.map { DonutSlice(it.id, it.label, it.color, it.amount) }
        }

    val dailyData =
        remember(state.expenses, state.period, state.dayOfBudgetPeriod, currency) {
            DoubleArray(state.dayOfBudgetPeriod) { 0.0 }
                .also { arr ->
                    state.spendingExpenses.forEach { e ->
                        val date = e.occurredAt.atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                        val day = ChronoUnit.DAYS.between(state.period.start, date).toInt()
                        if (day in arr.indices) arr[day] += Money.fromMinor(e.amountMinor, currency)
                    }
                }.toList()
        }

    val months: List<MonthBar> =
        remember(state.pastMonths, state.totalSpent, state.today, currency, monthName) {
            val raw = state.pastMonths.take(4).reversed().map { mt ->
                MonthBar(
                    mt.monthName.take(3),
                    Money.fromMinor(mt.totalMinor, currency),
                    mt.year == state.today.year && mt.month == state.today.monthValue,
                )
            }
            if (raw.none { it.now }) {
                raw + MonthBar(monthName.take(3), state.totalSpent, now = true)
            } else {
                raw.map { if (it.now) it.copy(value = state.totalSpent) else it }
            }
        }
    val prevMonthTotal = state.pastMonths
        .firstOrNull { !(it.year == state.today.year && it.month == state.today.monthValue) }
        ?.totalMinor?.let { Money.fromMinor(it, currency) }
    val hasPrevMonth = prevMonthTotal != null && prevMonthTotal > 0
    val momDelta = if (hasPrevMonth) {
        (state.totalSpent - prevMonthTotal) / prevMonthTotal
    } else 0.0
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    CompositionLocalProvider(LocalOverscrollConfiguration provides null) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize().nestedScroll(sway.connection).then(sway.modifier()),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = GroveSize.NavClearance + bottomInset),
    ) {
        item { AppTopBar(title = "Reports", subtitle = "$monthName ${state.today.year}") }

        item {
            LazyRow(modifier = Modifier.padding(bottom = GroveSpacing.SM), horizontalArrangement = Arrangement.spacedBy(GroveSpacing.SM)) {
                item { Chip("This week", range == "week") { range = "week" } }
                item { Chip("This month", range == "month") { range = "month" } }
                item { Chip("3 months", range == "3m") { range = "3m" } }
            }
        }

        item {
            GroveCard(modifier = Modifier.fillMaxWidth(), variant = GroveCardVariant.Elevated) {
                Text("Where it went", style = GroveType.rowTitle, fontWeight = FontWeight.Medium, color = c.fg1)
                Text(if (selected != null) "Tap again to clear" else "Tap a slice to focus", style = GroveType.rowSub, color = c.fg3)
                Spacer(Modifier.height(GroveSpacing.SM))
                if (byCategory.isEmpty()) {
                    BotanicalEmptyState("No growth to chart yet", subtitle = "Spend a little, then watch the patterns.")
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(GroveSpacing.SM + 2.dp)) {
                        DonutChart(data = donutData, total = filteredTotal, selected = selected, onSelect = {
                            selected =
                                if (selected == it) null else it
                        }, modifier = Modifier.size(132.dp), currency = currency)
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(GroveSpacing.SM)) {
                            for (entry in byCategory.take(5)) {
                                val dim = selected != null && selected != entry.id
                                Row(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .graphicsLayer { alpha = if (dim) 0.4f else 1f }
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { selected = if (selected == entry.id) null else entry.id }
                                            .padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(4.dp)).background(entry.color))
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        entry.label,
                                        fontFamily = InterTight,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp,
                                        color = c.fg1,
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    MoneyText(
                                        Money.currency(entry.amount, 0, currency),
                                        size = MoneyTextSize.Small,
                                        color = c.fg2,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item { SectionHeader("DAILY PACE", modifier = Modifier.padding(top = GroveSpacing.XL, bottom = GroveSpacing.SM)) }

        item {
            GroveCard(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("AVG / DAY", style = GroveType.capLabel, color = c.fg3)
                        MoneyText(
                            Money.currency(state.totalSpent / state.daysSinceFirstExpense, 0, currency),
                            size = MoneyTextSize.Title,
                            color = c.fg1,
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("SAFE / DAY", style = GroveType.capLabel, color = c.fg3)
                        MoneyText(
                            Money.currency(state.safePerDay, 0, currency),
                            size = MoneyTextSize.Title,
                            color = c.accentDeep,
                        )
                    }
                }
                Spacer(Modifier.height(GroveSpacing.SM))
                LineChart(
                    dailyData,
                    baseline = state.monthBudget / state.period.days,
                    monthShort = monthName.take(3),
                    modifier = Modifier.fillMaxWidth().height(130.dp),
                    currency = currency,
                )
                Spacer(Modifier.height(GroveSpacing.SM))
                Row(horizontalArrangement = Arrangement.spacedBy(GroveSpacing.SM + 2.dp)) {
                    LegendDot(c.accent, "Daily spend")
                    LegendDot(c.fg3.copy(alpha = 0.5f), "Safe amount")
                }
            }
        }

        item { SectionHeader("MONTH OVER MONTH", modifier = Modifier.padding(top = GroveSpacing.XL, bottom = GroveSpacing.SM)) }

        item {
            GroveCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom,
                ) {
                        MoneyText(
                            Money.currency(state.totalSpent, 0, currency),
                            size = MoneyTextSize.Title,
                            color = c.fg1,
                        )
                    MomDelta(momDelta, hasPrevious = hasPrevMonth, prevMonthName = state.pastMonths
                        .firstOrNull { !(it.year == state.today.year && it.month == state.today.monthValue) }
                        ?.monthName?.take(3) ?: "previous")
                }
                Spacer(Modifier.height(GroveSpacing.SM + 2.dp))
                MonthBars(months, currency = currency)
            }
        }

        item { SectionHeader("TOP CATEGORIES", modifier = Modifier.padding(top = GroveSpacing.XL, bottom = GroveSpacing.SM)) }

        item {
            GroveCard(modifier = Modifier.fillMaxWidth(), padding = PaddingValues(horizontal = GroveSpacing.LG)) {
                val maxAmount: Double = byCategory.firstOrNull()?.amount ?: 1.0
                val top = byCategory.take(5)
                if (top.isEmpty()) {
                    BotanicalEmptyState("Nothing to rank yet", subtitle = "Your top categories will sprout here as you spend.")
                } else {
                    for ((i, entry) in top.withIndex()) {
                        val dim = selected != null && selected != entry.id
                        Row(
                            modifier =
                                Modifier.fillMaxWidth().graphicsLayer { alpha = if (dim) 0.4f else 1f }.padding(
                                    vertical = GroveSpacing.MD,
                                ),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            CategoryIcon(entry.iconKey)
                            Spacer(Modifier.width(GroveSpacing.MD))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(entry.label, style = GroveType.rowTitle, color = c.fg1)
                                Spacer(Modifier.height(GroveSpacing.SM))
                                ProgressBar((entry.amount / maxAmount).toFloat(), entry.color, height = 5)
                            }
                            Spacer(Modifier.width(GroveSpacing.SM + 4.dp))
                            MoneyText(Money.currency(entry.amount, 0, currency), size = MoneyTextSize.Row, color = c.fg1)
                        }
                        if (i < top.size - 1) HorizontalDivider(color = c.border)
                    }
                }
            }
        }
    }
    }
}

@Composable
private fun MomDelta(delta: Double, hasPrevious: Boolean, prevMonthName: String = "Apr") {
    val c = GroveTheme.colors
    val down = delta <= 0
    val bg = if (!hasPrevious) c.bgMuted else if (down) c.successBg else c.dangerBg
    val fg = if (!hasPrevious) c.fg3 else if (down) c.success else c.danger
    Box(
        modifier =
            Modifier
                .clip(
                    GroveShapes.Toggle,
                ).background(bg)
                .padding(horizontal = GroveSpacing.SM + 2.dp, vertical = GroveSpacing.XS),
    ) {
        Text(
            if (hasPrevious) "${if (down) "↓" else "↑"} ${abs(delta * 100).toInt()}% vs $prevMonthName" else "No $prevMonthName data yet",
            fontFamily = InterTight,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = fg,
        )
    }
}

@Composable
private fun LegendDot(
    color: Color,
    label: String,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(16.dp, 2.dp).background(color))
        Spacer(Modifier.width(GroveSpacing.XS + 2.dp))
        Text(label, fontFamily = InterTight, fontSize = 11.5.sp, color = GroveTheme.colors.fg3)
    }
}
