package com.grove.app.feature.dashboard

import com.grove.app.data.BudgetState
import com.grove.app.data.model.ExpenseLite
import com.grove.app.core.format.Money
import com.grove.app.core.format.lerpFloat
import com.grove.app.core.format.lerpMinor

internal data class HomeStat(
    val label: String,
    val value: String,
    val subtitle: String,
)

data class DashboardSpendSnapshot(
    val safeTodayMinor: Long,
    val spentTodayMinor: Long,
    val budgetLeftMinor: Long,
    val upcomingBillsMinor: Long,
    val unpaidBillsCount: Int,
    val dailyAvgMinor: Long,
    val safePerDayMinor: Long,
    val pctSpent: Float,
    val recent: List<ExpenseLite>,
)

internal data class DashboardUiState(
    val recent: List<ExpenseLite>,
    val safeTodayMinor: Long,
    val spentTodayMinor: Long,
    val budgetLeftMinor: Long,
    val upcomingBillsMinor: Long,
    val unpaidBillsCount: Int,
    val dailyAvgMinor: Long,
    val safePerDayMinor: Long,
    val pctSpent: Float,
    val stats: List<HomeStat>,
)

fun BudgetState.dashboardSpendSnapshot(): DashboardSpendSnapshot {
    val budgetLeftMinor = monthBudgetMinor - totalSpentMinor
    val days = daysSinceFirstExpense.coerceAtLeast(1)
    val pctSpent =
        if (monthBudgetMinor > 0L) {
            (totalSpentMinor.toDouble() / monthBudgetMinor.toDouble()).toFloat().coerceIn(0f, 1f)
        } else {
            0f
        }
    return DashboardSpendSnapshot(
        safeTodayMinor = safeToSpendTodayMinor.coerceAtLeast(0L),
        spentTodayMinor = spentTodayMinor,
        budgetLeftMinor = budgetLeftMinor,
        upcomingBillsMinor = upcomingBillsMinor,
        unpaidBillsCount = bills.count { !it.paid },
        dailyAvgMinor = totalSpentMinor / days,
        safePerDayMinor = safePerDayMinor,
        pctSpent = pctSpent,
        recent = expenses.sortedByDescending { it.occurredAt }.take(4),
    )
}

internal fun BudgetState.dashboardUiState(
    currency: String,
    snapshot: DashboardSpendSnapshot?,
    settlementProgress: Float?,
): DashboardUiState {
    val progress = settlementProgress?.coerceIn(0f, 1f)
    val activeSnapshot = snapshot.takeIf { progress != null }
    val targetRecent = expenses.sortedByDescending { it.occurredAt }.take(4)
    val targetBudgetLeftMinor = monthBudgetMinor - totalSpentMinor
    val targetSafeTodayMinor = safeToSpendTodayMinor.coerceAtLeast(0L)
    val targetSpentTodayMinor = spentTodayMinor
    val targetUpcomingBillsMinor = upcomingBillsMinor
    val targetDailyAvgMinor = totalSpentMinor / daysSinceFirstExpense.coerceAtLeast(1)
    val targetSafePerDayMinor = safePerDayMinor
    val rawPctSpent =
        if (monthBudgetMinor > 0L) {
            (totalSpentMinor.toDouble() / monthBudgetMinor.toDouble()).toFloat().coerceIn(0f, 1f)
        } else {
            0f
        }

    val budgetLeftMinor = activeSnapshot?.let { lerpMinor(it.budgetLeftMinor, targetBudgetLeftMinor, progress ?: 1f) } ?: targetBudgetLeftMinor
    val safeTodayMinor = activeSnapshot?.let { lerpMinor(it.safeTodayMinor, targetSafeTodayMinor, progress ?: 1f) } ?: targetSafeTodayMinor
    val spentTodayMinor = activeSnapshot?.let { lerpMinor(it.spentTodayMinor, targetSpentTodayMinor, progress ?: 1f) } ?: targetSpentTodayMinor
    val upcomingBillsMinor = activeSnapshot?.let { lerpMinor(it.upcomingBillsMinor, targetUpcomingBillsMinor, progress ?: 1f) } ?: targetUpcomingBillsMinor
    val dailyAvgMinor = activeSnapshot?.let { lerpMinor(it.dailyAvgMinor, targetDailyAvgMinor, progress ?: 1f) } ?: targetDailyAvgMinor
    val safePerDayMinor = activeSnapshot?.let { lerpMinor(it.safePerDayMinor, targetSafePerDayMinor, progress ?: 1f) } ?: targetSafePerDayMinor
    val pctSpent = activeSnapshot?.let { lerpFloat(it.pctSpent, rawPctSpent, progress ?: 1f) } ?: rawPctSpent
    val unpaidBillsCount = activeSnapshot?.unpaidBillsCount ?: bills.count { !it.paid }
    val recent = if (activeSnapshot != null && (progress ?: 1f) < 1f) activeSnapshot.recent else targetRecent

    return DashboardUiState(
        recent = recent,
        safeTodayMinor = safeTodayMinor,
        spentTodayMinor = spentTodayMinor,
        budgetLeftMinor = budgetLeftMinor,
        upcomingBillsMinor = upcomingBillsMinor,
        unpaidBillsCount = unpaidBillsCount,
        dailyAvgMinor = dailyAvgMinor,
        safePerDayMinor = safePerDayMinor,
        pctSpent = pctSpent,
        stats =
            listOf(
                HomeStat("TODAY", Money.currencyLong(spentTodayMinor, 0, currency), "spent today"),
                HomeStat("BUDGET LEFT", Money.currencyLong(budgetLeftMinor, 0, currency), "this period"),
                HomeStat("BILLS DUE", Money.currencyLong(upcomingBillsMinor, 0, currency), "$unpaidBillsCount upcoming"),
                HomeStat("DAILY AVG", Money.currencyLong(dailyAvgMinor, 0, currency), "over ${daysSinceFirstExpense} days"),
            ),
    )
}
