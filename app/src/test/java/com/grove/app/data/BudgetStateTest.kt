package com.grove.app.data

import com.grove.app.data.db.BillLite
import com.grove.app.data.db.CategoryBudgetLite
import com.grove.app.data.db.CategoryLite
import com.grove.app.data.db.ExpenseLite
import com.grove.app.data.db.IncomeLite
import com.grove.app.data.model.UserProfile
import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.util.UUID

class BudgetStateTest {
    @Test
    fun totalsOnlyUseActiveBudgetPeriod() {
        val cat = UUID.randomUUID()
        val state =
            state(
                today = LocalDateTime.of(2026, 6, 12, 12, 0),
                resetDay = 15,
                expenses =
                    listOf(
                        expense(cat, 1_000, LocalDate.of(2026, 5, 15)),
                        expense(cat, 2_000, LocalDate.of(2026, 6, 14)),
                        expense(cat, 4_000, LocalDate.of(2026, 5, 14)),
                    ),
            )

        assertEquals(3_000, state.totalSpentMinor)
        assertEquals(29, state.dayOfBudgetPeriod)
        assertEquals(3, state.daysLeft)
    }

    @Test
    fun safeToSpendSubtractsSpentAndUnpaidBills() {
        val cat = UUID.randomUUID()
        val bill = UUID.randomUUID()
        val state =
            state(
                today = LocalDateTime.of(2026, 6, 10, 12, 0),
                resetDay = 1,
                budgetMinor = 30_000,
                expenses = listOf(expense(cat, 5_000, LocalDate.of(2026, 6, 10))),
                bills = listOf(BillLite(bill, "Rent", 10_000, "USD", "receipt", 15, false)),
            )

        assertEquals(15_000, state.remainingMinor)
        assertEquals(952, state.safePerDayMinor)
        assertEquals(-4_048, state.safeToSpendTodayMinor)
    }

    private fun state(
        today: LocalDateTime,
        resetDay: Int,
        budgetMinor: Long = 50_000,
        expenses: List<ExpenseLite> = emptyList(),
        bills: List<BillLite> = emptyList(),
    ) = BudgetState(
        today = today,
        monthBudgetMinor = budgetMinor,
        homeCurrency = "USD",
        categories = persistentListOf(CategoryLite(UUID.randomUUID(), "Food", "restaurant")),
        expenses = persistentListOf(*expenses.toTypedArray()),
        bills = persistentListOf(*bills.toTypedArray()),
        incomes = persistentListOf<IncomeLite>(),
        categoryBudgets = persistentListOf<CategoryBudgetLite>(),
        user = UserProfile("Mae", resetDay, "USD", true),
    )

    private fun expense(
        categoryId: UUID,
        amountMinor: Long,
        date: LocalDate,
    ) = ExpenseLite(
        id = UUID.randomUUID(),
        amountMinor = amountMinor,
        currencyCode = "USD",
        categoryId = categoryId,
        iconKey = "restaurant",
        note = "Test",
        occurredAt = date.atTime(LocalTime.NOON).atZone(ZoneOffset.UTC).toInstant(),
    )
}
