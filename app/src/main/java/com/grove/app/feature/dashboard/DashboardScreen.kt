package com.grove.app.feature.dashboard

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grove.app.data.BudgetState
import com.grove.app.designsystem.component.AppTopBar
import com.grove.app.designsystem.component.ExpenseRow
import com.grove.app.designsystem.component.GroveCard
import com.grove.app.designsystem.component.HeroStatusChip
import com.grove.app.designsystem.component.IconCircleButton
import com.grove.app.designsystem.component.LeafGlyph
import com.grove.app.designsystem.component.SectionHeader
import com.grove.app.designsystem.component.animatedOnce
import com.grove.app.designsystem.component.charts.ArcProgress
import com.grove.app.designsystem.format.Money
import com.grove.app.designsystem.theme.Fraunces
import com.grove.app.designsystem.theme.GroveSpacing
import com.grove.app.designsystem.theme.GroveTheme
import com.grove.app.designsystem.theme.GroveType
import com.grove.app.designsystem.theme.toneOf
import java.time.format.DateTimeFormatter

@Composable
fun DashboardScreen(
    state: BudgetState,
    currency: String,
    onNavigate: (String) -> Unit,
    onAddExpense: () -> Unit = {},
) {
    val c = GroveTheme.colors
    val monthName = remember(state.today) { state.today.format(DateTimeFormatter.ofPattern("MMMM")) }
    val pctSpent = if (state.monthBudget > 0.0) {
        (state.totalSpent / state.monthBudget).coerceIn(0.0, 1.0).toFloat()
    } else {
        0.0f
    }
    val tone = toneOf(state.pace)
    val recent = remember(state.expenses) { state.expenses.sortedByDescending { it.occurredAt }.take(4) }
    val safe = animatedOnce(state.safeToSpendToday.toFloat())
    val safeDollars = safe.toInt()
    val safeCents = ((safe - safeDollars) * 100).toInt().toString().padStart(2, '0')

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 120.dp),
    ) {
        item {
            AppTopBar(
                title = "Hi, ${state.user?.name ?: "there"}",
                subtitle = "$monthName ${state.today.dayOfMonth} · ${state.daysLeft} days left",
                actions = { IconCircleButton(Icons.Outlined.Settings, "Settings") { onNavigate("settings") } },
            )
        }

        item {
            GroveCard(
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                padding = PaddingValues(top = 28.dp, bottom = 24.dp, start = 20.dp, end = 20.dp),
            ) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Box(modifier = Modifier.matchParentSize()) {
                        LeafGlyph(120, tone.color, 0.05f, modifier = Modifier.align(Alignment.TopStart).offset(x = (-20).dp, y = (-20).dp))
                        LeafGlyph(
                            140,
                            tone.deep,
                            0.04f,
                            rotation = 140f,
                            modifier = Modifier.align(Alignment.BottomEnd).offset(x = 30.dp, y = 40.dp),
                        )
                    }
                    ArcProgress(pctSpent, tone.color, tone.deep, modifier = Modifier.size(244.dp)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "SAFE TO SPEND TODAY",
                                style = GroveType.capLabel.copy(fontSize = 11.sp, letterSpacing = 1.2.sp),
                                color = c.fg3,
                            )
                            Spacer(Modifier.height(GroveSpacing.SM))
                            Row(verticalAlignment = Alignment.Top) {
                                Text("$", fontFamily = Fraunces, fontSize = 26.sp, color = c.fg2, modifier = Modifier.padding(top = 6.dp))
                                Text(
                                    safeDollars.toString(),
                                    fontFamily = Fraunces,
                                    fontSize = 56.sp,
                                    letterSpacing = (-1.5).sp,
                                    color = c.fg1,
                                )
                                Text(
                                    ".$safeCents",
                                    fontFamily = Fraunces,
                                    fontSize = 26.sp,
                                    color = c.fg2,
                                    modifier = Modifier.padding(top = 6.dp),
                                )
                            }
                            Spacer(Modifier.height(2.dp))
                            Row {
                                Text(
                                    Money.short(state.remaining, currency),
                                    style = GroveType.rowSub,
                                    fontWeight = FontWeight.Medium,
                                    color = c.fg2,
                                )
                                Text(" left · ", style = GroveType.rowSub, color = c.fg3)
                                Text("${state.daysLeft}d", style = GroveType.rowSub, fontWeight = FontWeight.Medium, color = c.fg2)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(GroveSpacing.SM + 4.dp))
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { HeroStatusChip(tone) }

                Spacer(Modifier.height(GroveSpacing.LG + 4.dp))
                HorizontalDivider(color = c.border)
                Spacer(Modifier.height(GroveSpacing.SM + 6.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    HeroStat("SPENT", Money.currency(state.totalSpent, 0, currency), muted = false, modifier = Modifier.weight(1f))
                    Box(modifier = Modifier.width(1.dp).height(36.dp).background(c.border))
                    HeroStat("BUDGET", Money.currency(state.monthBudget, 0, currency), muted = true, modifier = Modifier.weight(1f))
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth().padding(top = GroveSpacing.SM), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CalloutCard(
                    "BILLS DUE",
                    Money.currencyLong(
                        state.upcomingBills.toLong(),
                        0,
                        currency,
                    ),
                    "${state.bills.count { !it.paid }} upcoming",
                    {
                        onNavigate("bills")
                    },
                    Modifier.weight(1f),
                )
                CalloutCard(
                    "DAILY AVG",
                    Money.currency(
                        state.totalSpent / state.dayOfMonth,
                        0,
                        currency,
                    ),
                    "over ${state.dayOfMonth} days",
                    {
                        onNavigate("reports")
                    },
                    Modifier.weight(1f),
                )
            }
        }

        item {
            SectionHeader(
                "RECENT",
                modifier = Modifier.padding(top = GroveSpacing.XL, bottom = GroveSpacing.SM),
                linkText = "See all",
                onLink = { onNavigate("history") },
            )
        }

        item {
            GroveCard(modifier = Modifier.fillMaxWidth(), padding = PaddingValues(horizontal = GroveSpacing.LG)) {
                recent.forEachIndexed { i, expense ->
                    ExpenseRow(expense, state.today, currency)
                    if (i < recent.size - 1) HorizontalDivider(color = c.border)
                }
            }
        }
    }
}

@Composable
private fun HeroStat(
    label: String,
    value: String,
    muted: Boolean,
    modifier: Modifier = Modifier,
) {
    val c = GroveTheme.colors
    Column(modifier = modifier.padding(horizontal = 14.dp)) {
        Text(label, style = GroveType.capLabel, color = c.fg3)
        Spacer(Modifier.height(3.dp))
        Text(
            value,
            style =
                GroveType.rowTitle.copy(
                    fontSize = 17.sp,
                    fontWeight = if (muted) FontWeight.Medium else FontWeight.SemiBold,
                    color = if (muted) c.fg2 else c.fg1,
                ),
        )
    }
}

@Composable
private fun CalloutCard(
    label: String,
    value: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = GroveTheme.colors
    GroveCard(modifier = modifier.clickable(onClick = onClick), padding = PaddingValues(GroveSpacing.SM)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, style = GroveType.capLabel, color = c.fg3)
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = c.fg3, modifier = Modifier.size(14.dp))
        }
        Spacer(Modifier.height(GroveSpacing.SM))
        Text(value, style = GroveType.rowTitle.copy(fontSize = 18.sp), color = c.fg1)
        Spacer(Modifier.height(2.dp))
        Text(subtitle, style = GroveType.rowSub, color = c.fg3)
    }
}
