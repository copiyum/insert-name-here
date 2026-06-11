package com.grove.app.data

import androidx.compose.runtime.Immutable
import com.grove.app.data.db.BillLite
import com.grove.app.data.db.CategoryLite
import com.grove.app.data.db.ExpenseLite
import com.grove.app.data.db.IncomeLite
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
    val user: UserProfile?,
    val pastMonths: ImmutableList<MonthlyTotal> = persistentListOf(),
) {
    val daysInMonth: Int get() = today.toLocalDate().lengthOfMonth()
    val dayOfMonth: Int get() = today.dayOfMonth
    val daysLeft: Int get() = (daysInMonth - dayOfMonth + 1).coerceAtLeast(1)

    val monthBudget: Double get() = monthBudgetMinor.toDouble() / minorUnit
    private val minorUnit: Double get() = Math.pow(10.0, Currencies.minorUnitExponent(homeCurrency).toDouble())

    val totalSpentMinor: Long
        get() =
            expenses
                .filter { it.currencyCode == homeCurrency || it.currencyCode.isEmpty() }
                .filter {
                    val d = it.occurredAt.atZone(ZoneId.systemDefault()).toLocalDate()
                    d.monthValue == today.monthValue && d.year == today.year
                }
                .sumOf { it.amountMinor }

    val spentTodayMinor: Long
        get() =
            expenses
                .filter { it.currencyCode == homeCurrency || it.currencyCode.isEmpty() }
                .filter { it.occurredAt.atZone(ZoneId.systemDefault()).toLocalDate() == today.toLocalDate() }
                .sumOf { it.amountMinor }

    val totalSpent: Double get() = totalSpentMinor.toDouble() / minorUnit
    val spentToday: Double get() = spentTodayMinor.toDouble() / minorUnit
    val startRemainingMinor: Long get() = monthBudgetMinor - (totalSpentMinor - spentTodayMinor)
    val upcomingBillsMinor: Long get() = bills.filter { !it.paid }.sumOf { it.amountMinor }
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
        get() = expenses.minByOrNull { it.occurredAt }?.occurredAt?.atZone(ZoneId.systemDefault())?.toLocalDate()

    val daysSinceFirstExpense: Int
        get() {
            val first = firstExpenseDay ?: return 1
            val days = ChronoUnit.DAYS.between(first, today.toLocalDate()).toInt() + 1
            return days.coerceAtLeast(1)
        }

    val pace: SpendPace
        get() {
            if (remainingMinor <= 0) return SpendPace.Over
            if (monthBudgetMinor == 0L) return SpendPace.Healthy
            val spentFrac = totalSpentMinor.toDouble() / monthBudgetMinor.toDouble()
            val timeFrac = if (daysInMonth != 0) dayOfMonth.toDouble() / daysInMonth else 0.0
            return if (spentFrac > timeFrac + 0.1) SpendPace.Tight else SpendPace.Healthy
        }

    val dayPace: SpendPace
        get() {
            if (safePerDayMinor <= 0L && spentTodayMinor > 0L) return SpendPace.Over
            if (spentTodayMinor > safePerDayMinor && safePerDayMinor > 0L) return SpendPace.Over
            if (safePerDayMinor > 0L && spentTodayMinor > (safePerDayMinor * 0.85).toLong()) return SpendPace.Tight
            return SpendPace.Healthy
        }

    private fun minorExponent(currencyCode: String): Double = Math.pow(10.0, Currencies.minorUnitExponent(currencyCode).toDouble())
    val homeCurrencySpentMinor: Long get() = totalSpentMinor

    companion object {
        fun empty() =
            BudgetState(
                today = LocalDateTime.now(),
                monthBudgetMinor = 0L,
                homeCurrency = "USD",
                categories = persistentListOf(),
                expenses = persistentListOf(),
                bills = persistentListOf(),
                incomes = persistentListOf(),
                user = null,
            )
    }
}
