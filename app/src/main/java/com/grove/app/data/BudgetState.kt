package com.grove.app.data

import androidx.compose.runtime.Immutable
import com.grove.app.data.model.Bill
import com.grove.app.data.model.Category
import com.grove.app.data.model.Expense
import com.grove.app.data.model.User
import kotlinx.collections.immutable.ImmutableList
import java.time.LocalDateTime

enum class SpendPace { Healthy, Tight, Over }

// Raw state only.
@Immutable
data class BudgetState(
    val today: LocalDateTime,
    val monthBudget: Double,
    val categories: ImmutableList<Category>,
    val expenses: ImmutableList<Expense>,
    val bills: ImmutableList<Bill>,
    val user: User,
) {
    val daysInMonth: Int get() = today.toLocalDate().lengthOfMonth()
    val dayOfMonth: Int get() = today.dayOfMonth
    val daysLeft: Int get() = (daysInMonth - dayOfMonth + 1).coerceAtLeast(1)
    val totalSpent: Double get() = expenses.sumOf { it.amount }
    val remaining: Double get() = monthBudget - totalSpent
    val upcomingBills: Double get() = bills.filter { !it.paid }.sumOf { it.amount }
    val safeToSpendToday: Double get() = maxOf(0.0, (remaining - upcomingBills) / daysLeft)

    val pace: SpendPace
        get() {
            if (remaining <= 0) return SpendPace.Over
            val spentFrac = if (monthBudget != 0.0) totalSpent / monthBudget else 0.0
            val timeFrac = if (daysInMonth != 0) dayOfMonth.toDouble() / daysInMonth else 0.0
            return if (spentFrac > timeFrac + 0.1) SpendPace.Tight else SpendPace.Healthy
        }
}
