package com.grove.app.data

import com.grove.app.data.model.CategoryBudgetLite
import com.grove.app.data.model.CategoryLite
import com.grove.app.data.model.ExpenseLite
import com.grove.app.data.model.IncomeLite
import com.grove.app.data.model.BillFrequency
import com.grove.app.data.model.BillLite
import com.grove.app.data.model.CategoryKind
import com.grove.app.data.model.UserProfile
import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
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
                bills = listOf(bill(bill, "Rent", 10_000, LocalDate.of(2026, 6, 15))),
            )

        assertEquals(15_000, state.remainingMinor)
        assertEquals(952, state.safePerDayMinor)
        assertEquals(-4_048, state.safeToSpendTodayMinor)
    }

    @Test
    fun paidBillsStillReduceBalanceAndSafeToSpend() {
        val cat = UUID.randomUUID()
        val bill = UUID.randomUUID()
        val state =
            state(
                today = LocalDateTime.of(2026, 6, 10, 12, 0),
                resetDay = 1,
                budgetMinor = 30_000,
                expenses = listOf(expense(cat, 5_000, LocalDate.of(2026, 6, 10))),
                bills = listOf(bill(bill, "Rent", 10_000, LocalDate.of(2026, 6, 15), paid = true)),
            )

        assertEquals(0, state.upcomingBillsMinor)
        assertEquals(10_000, state.billObligationsMinor)
        assertEquals(15_000, state.remainingMinor)
        assertEquals(952, state.safePerDayMinor)
        assertEquals(-4_048, state.safeToSpendTodayMinor)
    }

    @Test
    fun incomeCategoryEntriesDoNotCountAsSpend() {
        val food = UUID.randomUUID()
        val income = UUID.randomUUID()
        val state =
            state(
                today = LocalDateTime.of(2026, 6, 10, 12, 0),
                resetDay = 1,
                expenses =
                    listOf(
                        expense(food, 5_000, LocalDate.of(2026, 6, 10)),
                        expense(income, 20_000, LocalDate.of(2026, 6, 10), kind = CategoryKind.income),
                    ),
            )

        assertEquals(5_000, state.totalSpentMinor)
        assertEquals(5_000, state.spentTodayMinor)
    }

    private fun state(
        today: LocalDateTime,
        resetDay: Int,
        budgetMinor: Long = 50_000,
        expenses: List<ExpenseLite> = emptyList(),
        bills: List<BillLite> = emptyList(),
    ): BudgetState {
        val period = BudgetPeriod.forDate(today.toLocalDate(), resetDay)
        val spending = expenses.filter { it.categoryKind != CategoryKind.income }
        return BudgetState(
            today = today,
            monthBudgetMinor = budgetMinor,
            homeCurrency = "USD",
            categories = persistentListOf(CategoryLite(UUID.randomUUID(), "Food", "restaurant", CategoryKind.expense)),
            expenses = persistentListOf(*expenses.toTypedArray()),
            bills = persistentListOf(*bills.toTypedArray()),
            incomes = persistentListOf<IncomeLite>(),
            categoryBudgets = persistentListOf<CategoryBudgetLite>(),
            user = UserProfile("Mae", resetDay, "USD", true),
            totalSpentMinor = spending.filter { period.contains(it.occurredAt) }.sumOf { it.amountMinor },
            spentTodayMinor =
                spending
                    .filter { it.occurredAt.atZone(ZoneId.systemDefault()).toLocalDate() == today.toLocalDate() }
                    .sumOf { it.amountMinor },
            upcomingBillsMinor = bills.filter { !it.paid }.sumOf { it.amountMinor },
        )
    }

    private fun expense(
        categoryId: UUID,
        amountMinor: Long,
        date: LocalDate,
        kind: CategoryKind = CategoryKind.expense,
    ) = ExpenseLite(
        id = UUID.randomUUID(),
        amountMinor = amountMinor,
        currencyCode = "USD",
        categoryId = categoryId,
        categoryName = "Food",
        iconKey = "restaurant",
        categoryKind = kind,
        note = "Test",
        occurredAt = date.atTime(LocalTime.NOON).atZone(ZoneOffset.UTC).toInstant(),
    )

    private fun bill(
        id: UUID,
        name: String,
        amountMinor: Long,
        dueDate: LocalDate,
        paid: Boolean = false,
    ) = BillLite(
        id = id,
        name = name,
        amountMinor = amountMinor,
        currencyCode = "USD",
        iconKey = "receipt",
        dueAt = dueDate.atStartOfDay(ZoneOffset.UTC).toInstant(),
        dueDay = dueDate.dayOfMonth,
        frequency = BillFrequency.monthly,
        paid = paid,
    )
}
