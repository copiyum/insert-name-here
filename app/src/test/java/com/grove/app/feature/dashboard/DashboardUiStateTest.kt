package com.grove.app.feature.dashboard

import com.grove.app.core.format.Money
import com.grove.app.data.BudgetState
import com.grove.app.data.model.BillFrequency
import com.grove.app.data.model.BillLite
import com.grove.app.data.model.CategoryBudgetLite
import com.grove.app.data.model.CategoryLite
import com.grove.app.data.model.CategoryKind
import com.grove.app.data.model.ExpenseLite
import com.grove.app.data.model.IncomeLite
import com.grove.app.data.model.UserProfile
import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID

class DashboardUiStateTest {
    @Test
    fun budgetLeftSubtractsUnpaidBills() {
        val state =
            state(
                monthBudgetMinor = 100_000,
                totalSpentMinor = 25_000,
                bills = listOf(bill(amountMinor = 30_000, paid = false)),
            )

        val ui = state.dashboardUiState(currency = "USD", snapshot = null, settlementProgress = null)

        assertEquals(45_000L, ui.budgetLeftMinor)
        assertEquals(45_000L, state.dashboardSpendSnapshot().budgetLeftMinor)
        assertEquals(Money.currencyLong(45_000L, 0, "USD"), ui.stats.first { it.label == "BUDGET LEFT" }.value)
    }

    @Test
    fun budgetLeftStillSubtractsPaidBills() {
        val state =
            state(
                monthBudgetMinor = 100_000,
                totalSpentMinor = 25_000,
                bills = listOf(bill(amountMinor = 30_000, paid = true)),
            )

        val ui = state.dashboardUiState(currency = "USD", snapshot = null, settlementProgress = null)

        assertEquals(45_000L, ui.budgetLeftMinor)
        assertEquals(0L, ui.upcomingBillsMinor)
        assertEquals(Money.currencyLong(45_000L, 0, "USD"), ui.stats.first { it.label == "BUDGET LEFT" }.value)
        assertEquals(Money.currencyLong(0L, 0, "USD"), ui.stats.first { it.label == "BILLS DUE" }.value)
    }

    private fun state(
        monthBudgetMinor: Long,
        totalSpentMinor: Long,
        bills: List<BillLite>,
    ) = BudgetState(
        today = LocalDateTime.of(2026, 6, 13, 12, 0),
        monthBudgetMinor = monthBudgetMinor,
        homeCurrency = "USD",
        categories = persistentListOf(CategoryLite(UUID.randomUUID(), "Food", "restaurant", CategoryKind.expense)),
        expenses = persistentListOf<ExpenseLite>(),
        bills = persistentListOf(*bills.toTypedArray()),
        incomes = persistentListOf<IncomeLite>(),
        categoryBudgets = persistentListOf<CategoryBudgetLite>(),
        user = UserProfile("Mae", resetDay = 1, currencyCode = "USD", onboardingCompleted = true),
        totalSpentMinor = totalSpentMinor,
        spentTodayMinor = 0L,
        upcomingBillsMinor = bills.filter { !it.paid }.sumOf { it.amountMinor },
    )

    private fun bill(
        amountMinor: Long,
        paid: Boolean,
    ) = BillLite(
        id = UUID.randomUUID(),
        name = "Rent",
        amountMinor = amountMinor,
        currencyCode = "USD",
        iconKey = "receipt",
        dueAt = LocalDate.of(2026, 6, 20).atStartOfDay(ZoneOffset.UTC).toInstant(),
        dueDay = 20,
        frequency = BillFrequency.monthly,
        paid = paid,
    )
}
