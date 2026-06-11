package com.grove.app.feature.reports

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grove.app.data.BudgetState
import com.grove.app.designsystem.catalog.CategoryVisuals
import com.grove.app.designsystem.component.AppTopBar
import com.grove.app.designsystem.component.CategoryIcon
import com.grove.app.designsystem.component.Chip
import com.grove.app.designsystem.component.GroveCard
import com.grove.app.designsystem.component.ProgressBar
import com.grove.app.designsystem.component.SectionHeader
import com.grove.app.designsystem.component.charts.DonutChart
import com.grove.app.designsystem.component.charts.LineChart
import com.grove.app.designsystem.component.charts.MonthBar
import com.grove.app.designsystem.component.charts.MonthBars
import com.grove.app.designsystem.format.Money
import com.grove.app.designsystem.theme.GroveShapes
import com.grove.app.designsystem.theme.GroveSpacing
import com.grove.app.designsystem.theme.GroveTheme
import com.grove.app.designsystem.theme.GroveType
import com.grove.app.designsystem.theme.InterTight
import kotlin.math.abs

@Composable
fun ReportsScreen(
    state: BudgetState,
    currency: String,
) {
    val c = GroveTheme.colors
    var range by rememberSaveable { mutableStateOf("month") }
    var selected by rememberSaveable { mutableStateOf<String?>(null) }
    val monthName =
        state.today.month
            .toString()
            .lowercase()
            .replaceFirstChar { it.uppercase() }

    val byCategory: List<Pair<String, Double>> =
        remember(state.expenses) {
            state.expenses
                .groupBy { it.iconKey }
                .map { e -> Pair(e.key, e.value.sumOf { it.amountMinor } / 100.0) }
                .sortedByDescending { it.second }
        }

    val dailyData =
        remember(state.expenses, state.dayOfMonth) {
            DoubleArray(state.dayOfMonth) { 0.0 }
                .also { arr ->
                    state.expenses.forEach { e ->
                        val day = e.occurredAt.atZone(java.time.ZoneId.systemDefault()).dayOfMonth - 1
                        if (day in 0 until state.dayOfMonth) arr[day] += e.amountMinor / 100.0
                    }
                }.toList()
        }

    val months: List<MonthBar> =
        remember(state.pastMonths, state.totalSpent) {
            val raw = state.pastMonths.take(4).reversed().map { mt ->
                MonthBar(
                    mt.monthName.take(3),
                    mt.totalMinor.toDouble() / Math.pow(10.0, com.grove.app.designsystem.format.Currencies.minorUnitExponent(currency).toDouble()),
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
        ?.totalMinor?.toDouble()?.let { it / Math.pow(10.0, com.grove.app.designsystem.format.Currencies.minorUnitExponent(currency).toDouble()) }
    val momDelta = if (prevMonthTotal != null && prevMonthTotal > 0) {
        (state.totalSpent - prevMonthTotal) / prevMonthTotal
    } else 0.0

    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 120.dp)) {
        item { AppTopBar(title = "Reports", subtitle = "$monthName 2026") }

        item {
            LazyRow(modifier = Modifier.padding(bottom = GroveSpacing.SM), horizontalArrangement = Arrangement.spacedBy(GroveSpacing.SM)) {
                item { Chip("This week", range == "week") { range = "week" } }
                item { Chip("This month", range == "month") { range = "month" } }
                item { Chip("3 months", range == "3m") { range = "3m" } }
            }
        }

        item {
            GroveCard(modifier = Modifier.fillMaxWidth()) {
                Text("Where it went", style = GroveType.rowTitle, fontWeight = FontWeight.Medium, color = c.fg1)
                Text(if (selected != null) "Tap again to clear" else "Tap a slice to focus", style = GroveType.rowSub, color = c.fg3)
                Spacer(Modifier.height(GroveSpacing.SM))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(GroveSpacing.SM + 2.dp)) {
                    DonutChart(data = byCategory, total = state.totalSpent, selected = selected, onSelect = {
                        selected =
                            if (selected == it) null else it
                    }, modifier = Modifier.size(132.dp), currency = currency)
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(GroveSpacing.SM)) {
                        for (entry in byCategory.take(5)) {
                            val cat = entry.first
                            val amount = entry.second
                            val dim = selected != null && selected != cat
                            Row(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .graphicsLayer { alpha = if (dim) 0.4f else 1f }
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { selected = if (selected == cat) null else cat }
                                        .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(4.dp)).background(CategoryVisuals.color(cat)))
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    CategoryVisuals.label(cat),
                                    fontFamily = InterTight,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = c.fg1,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    Money.currency(amount, 0, currency),
                                    fontFamily = InterTight,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = c.fg2,
                                )
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
                        Text(
                            Money.currency(state.totalSpent / state.daysSinceFirstExpense, 0, currency),
                            style = GroveType.rowTitle.copy(fontSize = 24.sp),
                            color = c.fg1,
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("SAFE / DAY", style = GroveType.capLabel, color = c.fg3)
                        Text(
                            Money.currency(state.safePerDay, 0, currency),
                            style = GroveType.rowTitle.copy(fontSize = 24.sp),
                            color = c.accentDeep,
                        )
                    }
                }
                Spacer(Modifier.height(GroveSpacing.SM))
                LineChart(
                    dailyData,
                    baseline = state.monthBudget / state.daysInMonth,
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
                    Text(
                        Money.currency(state.totalSpent, 0, currency),
                        style = GroveType.rowTitle.copy(fontSize = 24.sp, letterSpacing = (-0.5).sp),
                        color = c.fg1,
                    )
                    MomDelta(momDelta, prevMonthName = state.pastMonths
                        .firstOrNull { !(it.year == state.today.year && it.month == state.today.monthValue) }
                        ?.monthName?.take(3) ?: "Apr")
                }
                Spacer(Modifier.height(GroveSpacing.SM + 2.dp))
                MonthBars(months)
            }
        }

        item { SectionHeader("TOP CATEGORIES", modifier = Modifier.padding(top = GroveSpacing.XL, bottom = GroveSpacing.SM)) }

        item {
            GroveCard(modifier = Modifier.fillMaxWidth(), padding = PaddingValues(horizontal = GroveSpacing.LG)) {
                val maxAmount: Double = byCategory.firstOrNull()?.second ?: 1.0
                val top: List<Pair<String, Double>> = byCategory.take(5)
                for ((i, entry) in top.withIndex()) {
                    val cat = entry.first
                    val amount = entry.second
                    val dim = selected != null && selected != cat
                    Row(
                        modifier =
                            Modifier.fillMaxWidth().graphicsLayer { alpha = if (dim) 0.4f else 1f }.padding(
                                vertical = GroveSpacing.MD,
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CategoryIcon(cat)
                        Spacer(Modifier.width(GroveSpacing.MD))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(CategoryVisuals.label(cat), style = GroveType.rowTitle, color = c.fg1)
                            Spacer(Modifier.height(GroveSpacing.SM))
                            ProgressBar((amount / maxAmount).toFloat(), CategoryVisuals.color(cat), height = 5)
                        }
                        Spacer(Modifier.width(GroveSpacing.SM + 4.dp))
                        Text(Money.currency(amount, 0, currency), style = GroveType.amount, color = c.fg1)
                    }
                    if (i < top.size - 1) HorizontalDivider(color = c.border)
                }
            }
        }
    }
}

@Composable
private fun MomDelta(delta: Double, prevMonthName: String = "Apr") {
    val c = GroveTheme.colors
    val down = delta <= 0
    val bg = if (down) c.accent.copy(alpha = 0.14f) else c.clayBg
    val fg = if (down) (if (c.isDark) c.accentSoft else c.accentDeep) else c.clay
    Box(
        modifier =
            Modifier
                .clip(
                    GroveShapes.Toggle,
                ).background(bg)
                .padding(horizontal = GroveSpacing.SM + 2.dp, vertical = GroveSpacing.XS),
    ) {
        Text(
            "${if (down) "↓" else "↑"} ${abs(delta * 100).toInt()}% vs $prevMonthName",
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
