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
import java.time.LocalDateTime

enum class SpendPace { Healthy, Tight, Over }

@Immutable
data class BudgetState(
    val today: LocalDateTime,
    val monthBudget: Double,
    val monthBudgetMinor: Long,
    val homeCurrency: String,
    val categories: ImmutableList<CategoryLite>,
    val expenses: ImmutableList<ExpenseLite>,
    val bills: ImmutableList<BillLite>,
    val incomes: ImmutableList<IncomeLite>,
    val user: UserProfile?,
) {
    val daysInMonth: Int get() = today.toLocalDate().lengthOfMonth()
    val dayOfMonth: Int get() = today.dayOfMonth
    val daysLeft: Int get() = (daysInMonth - dayOfMonth + 1).coerceAtLeast(1)

    val homeCurrencySpentMinor: Long get() =
        expenses
            .filter { it.currencyCode == homeCurrency || it.currencyCode.isEmpty() }
            .sumOf { it.amountMinor }

    val spendByCurrency: Map<String, Long> get() =
        expenses
            .groupBy { it.currencyCode.ifEmpty { homeCurrency } }
            .mapValues { (_, list) -> list.sumOf { it.amountMinor } }

    val totalSpent: Double get() = homeCurrencySpentMinor.toDouble() / minorExponent(homeCurrency)
    val remaining: Double get() = monthBudget - totalSpent
    val upcomingBills: Double get() = bills.filter { !it.paid }.sumOf { it.amountMinor.toDouble() / minorExponent(homeCurrency) }
    val safeToSpendToday: Double get() = maxOf(0.0, (remaining - upcomingBills) / daysLeft)

    val pace: SpendPace
        get() {
            if (remaining <= 0) return SpendPace.Over
            val spentFrac = if (monthBudget != 0.0) totalSpent / monthBudget else 0.0
            val timeFrac = if (daysInMonth != 0) dayOfMonth.toDouble() / daysInMonth else 0.0
            return if (spentFrac > timeFrac + 0.1) SpendPace.Tight else SpendPace.Healthy
        }

    private fun minorExponent(currencyCode: String): Double = Currencies.minorUnitExponent(currencyCode).toDouble()

    companion object {
        fun empty() =
            BudgetState(
                today = LocalDateTime.now(),
                monthBudget = 0.0,
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
