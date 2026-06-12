package com.grove.app.data

import androidx.compose.runtime.Immutable
import com.grove.app.data.db.BillLite
import com.grove.app.data.db.CategoryBudgetLite
import com.grove.app.data.db.CategoryLite
import com.grove.app.data.db.ExpenseLite
import com.grove.app.data.db.IncomeLite
import com.grove.app.data.model.CategoryKind
import com.grove.app.data.model.UserProfile
import com.grove.app.designsystem.format.Currencies
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

enum class SpendPace { Healthy, Tight, Over }

@Immutable
data class MonthlyTotal(val year: Int, val month: Int, val totalMinor: Long, val monthName: String)

@Immutable
data class BudgetState(
    val today: LocalDateTime,
    val monthBudgetMinor: Long,
    val homeCurrency: String,
    val categories: ImmutableList<CategoryLite>,
    val expenses: ImmutableList<ExpenseLite>,
    val bills: ImmutableList<BillLite>,
    val incomes: ImmutableList<IncomeLite>,
    val categoryBudgets: ImmutableList<CategoryBudgetLite>,
    val user: UserProfile?,
    val pastMonths: ImmutableList<MonthlyTotal> = persistentListOf(),
    val totalSpentMinor: Long = 0L,
    val spentTodayMinor: Long = 0L,
    val upcomingBillsMinor: Long = 0L,
) {
    val daysInMonth: Int get() = today.toLocalDate().lengthOfMonth()
    val dayOfMonth: Int get() = today.dayOfMonth
    val period: BudgetPeriod get() = BudgetPeriod.forDate(today.toLocalDate(), user?.resetDay ?: 1)
    val dayOfBudgetPeriod: Int get() = period.dayIndex(today.toLocalDate())
    val daysLeft: Int get() = period.daysLeft(today.toLocalDate())

    val monthBudget: Double get() = monthBudgetMinor.toDouble() / minorUnit
    private val minorUnit: Double get() = Math.pow(10.0, Currencies.minorUnitExponent(homeCurrency).toDouble())
    val spendingExpenses: List<ExpenseLite> get() = expenses.filter { it.categoryKind != CategoryKind.income }

    val totalSpent: Double get() = totalSpentMinor.toDouble() / minorUnit
    val spentToday: Double get() = spentTodayMinor.toDouble() / minorUnit
    val startRemainingMinor: Long get() = monthBudgetMinor - (totalSpentMinor - spentTodayMinor)
    val remainingMinor: Long get() = monthBudgetMinor - totalSpentMinor - upcomingBillsMinor
    val remaining: Double get() = remainingMinor.toDouble() / minorUnit
    val upcomingBills: Double get() = upcomingBillsMinor.toDouble() / minorUnit

    val safePerDayMinor: Long get() {
        val numerator = startRemainingMinor - upcomingBillsMinor
        if (numerator <= 0) return 0L
        return numerator / daysLeft
    }
    val safeToSpendTodayMinor: Long get() = safePerDayMinor - spentTodayMinor
    val safePerDay: Double get() = safePerDayMinor.toDouble() / minorUnit
    val safeToSpendToday: Double get() = safeToSpendTodayMinor.toDouble() / minorUnit

    val firstExpenseDay: LocalDate?
        get() = spendingExpenses.minByOrNull { it.occurredAt }?.occurredAt?.atZone(ZoneId.systemDefault())?.toLocalDate()

    val daysSinceFirstExpense: Int
        get() = firstExpenseDay?.let { ChronoUnit.DAYS.between(it, today.toLocalDate()).toInt() + 1 }?.coerceAtLeast(1) ?: 1

    val elapsedPeriodDays: Int
        get() = dayOfBudgetPeriod

    val pace: SpendPace
        get() {
            if (remainingMinor <= 0) return SpendPace.Over
            if (monthBudgetMinor == 0L) return SpendPace.Healthy
            val spentFrac = totalSpentMinor.toDouble() / monthBudgetMinor.toDouble()
            val timeFrac = dayOfBudgetPeriod.toDouble() / period.days
            return if (spentFrac > timeFrac + 0.1) SpendPace.Tight else SpendPace.Healthy
        }

    val dayPace: SpendPace
        get() {
            if (safePerDayMinor <= 0L && spentTodayMinor > 0L) return SpendPace.Over
            if (spentTodayMinor > safePerDayMinor && safePerDayMinor > 0L) return SpendPace.Over
            if (safePerDayMinor > 0L && spentTodayMinor > (safePerDayMinor * 0.85).toLong()) return SpendPace.Tight
            return SpendPace.Healthy
        }

    val homeCurrencySpentMinor: Long get() = totalSpentMinor

    companion object {
        fun empty() =
            BudgetState(
                today = LocalDateTime.now(),
                monthBudgetMinor = 0L,
                homeCurrency = "INR",
                categories = persistentListOf(),
                expenses = persistentListOf(),
                bills = persistentListOf(),
                incomes = persistentListOf(),
                categoryBudgets = persistentListOf(),
                user = null,
            )
    }
}
