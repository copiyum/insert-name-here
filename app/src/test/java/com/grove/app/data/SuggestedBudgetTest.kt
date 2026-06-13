package com.grove.app.data

import com.grove.app.data.model.ExpenseLite
import com.grove.app.data.model.CategoryKind
import com.grove.app.data.model.UserProfile
import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.UUID

class SuggestedBudgetTest {
    @Test
    fun monthSuggestionIsNullWithFewerThanTwoMonthsOfHistory() {
        assertNull(state().suggestedMonthBudgetMinor())
        assertNull(state(pastMonths = listOf(month(2026, 5, 12_000))).suggestedMonthBudgetMinor())
        assertNull(
            state(pastMonths = listOf(month(2026, 4, 0), month(2026, 5, 12_000))).suggestedMonthBudgetMinor(),
        )
    }

    @Test
    fun monthSuggestionPicksMedianOfThreeMonths() {
        val suggestion =
            state(
                pastMonths =
                    listOf(
                        month(2026, 3, 10_000),
                        month(2026, 4, 30_000),
                        month(2026, 5, 20_000),
                    ),
            ).suggestedMonthBudgetMinor()

        assertEquals(20_000L, suggestion)
    }

    @Test
    fun monthSuggestionRoundsUpToTwoSignificantFigures() {
        val suggestion =
            state(
                pastMonths =
                    listOf(
                        month(2026, 3, 9_800),
                        month(2026, 4, 12_377),
                        month(2026, 5, 14_100),
                    ),
            ).suggestedMonthBudgetMinor()

        assertEquals(13_000L, suggestion)
    }

    @Test
    fun monthSuggestionUsesNewestCompletedMonthsWhenHistoryIsNewestFirst() {
        val suggestion =
            state(
                pastMonths =
                    listOf(
                        month(2026, 6, 99_000),
                        month(2026, 5, 30_000),
                        month(2026, 4, 20_000),
                        month(2026, 3, 10_000),
                        month(2026, 2, 200_000),
                    ),
            ).suggestedMonthBudgetMinor()

        assertEquals(20_000L, suggestion)
    }

    @Test
    fun categorySuggestionTakesMedianAcrossMonths() {
        val cat = UUID.randomUUID()
        val suggestion =
            state(
                expenses =
                    listOf(
                        expense(cat, 5_000, LocalDate.of(2026, 3, 10)),
                        expense(cat, 3_000, LocalDate.of(2026, 3, 20)),
                        expense(cat, 12_000, LocalDate.of(2026, 4, 10)),
                        expense(cat, 4_000, LocalDate.of(2026, 5, 10)),
                        expense(UUID.randomUUID(), 99_000, LocalDate.of(2026, 4, 11)),
                        expense(cat, 70_000, LocalDate.of(2026, 6, 1), kind = CategoryKind.income),
                    ),
            ).suggestedCategoryBudgetMinor(cat)

        assertEquals(8_000L, suggestion)
    }

    @Test
    fun categorySuggestionIsNullWithFewerThanTwoMonths() {
        val cat = UUID.randomUUID()
        val suggestion =
            state(expenses = listOf(expense(cat, 5_000, LocalDate.of(2026, 5, 10))))
                .suggestedCategoryBudgetMinor(cat)

        assertNull(suggestion)
    }

    private fun state(
        pastMonths: List<MonthlyTotal> = emptyList(),
        expenses: List<ExpenseLite> = emptyList(),
    ) = BudgetState(
        today = LocalDateTime.of(2026, 6, 10, 12, 0),
        monthBudgetMinor = 30_000,
        homeCurrency = "USD",
        categories = persistentListOf(),
        expenses = persistentListOf(*expenses.toTypedArray()),
        bills = persistentListOf(),
        incomes = persistentListOf(),
        categoryBudgets = persistentListOf(),
        user = UserProfile("Mae", 1, "USD", true),
        pastMonths = persistentListOf(*pastMonths.toTypedArray()),
    )

    private fun month(
        year: Int,
        month: Int,
        totalMinor: Long,
    ) = MonthlyTotal(year, month, totalMinor, java.time.Month.of(month).name.take(3))

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
        occurredAt = date.atTime(LocalTime.NOON).atZone(ZoneId.systemDefault()).toInstant(),
    )
}
