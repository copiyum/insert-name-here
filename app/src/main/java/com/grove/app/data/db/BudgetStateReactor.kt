package com.grove.app.data.db

import com.grove.app.data.BudgetState
import com.grove.app.data.repository.BillRepository
import com.grove.app.data.repository.BudgetRepository
import com.grove.app.data.repository.CategoryRepository
import com.grove.app.data.repository.ExpenseRepository
import com.grove.app.data.repository.IncomeRepository
import com.grove.app.data.repository.UserRepository
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

/**
 * Combines all DAO flows into a single `StateFlow<BudgetState>` that
 * the existing 6 screens (Dashboard, History, Bills, Reports, Budget,
 * Settings) already read from. This is the keystone of v1: replaces
 * the v0 in-memory `_state: MutableStateFlow<BudgetState>` in
 * SeedBudgetRepository.
 */
class BudgetStateReactor(
    scope: CoroutineScope,
    userRepo: UserRepository,
    categoryRepo: CategoryRepository,
    expenseRepo: ExpenseRepository,
    billRepo: BillRepository,
    incomeRepo: IncomeRepository,
    budgetRepo: BudgetRepository,
) {
    private val today: LocalDate = LocalDate.now()

    val state: StateFlow<BudgetState> =
        combine(
            userRepo.observe(),
            categoryRepo.observeActive(),
            expenseRepo.observeAll(),
            combine(
                billRepo.observeActive(),
                billRepo.observePayments(),
                incomeRepo.observeAll(),
            ) { bills, payments, incomes -> Triple(bills, payments, incomes) },
        ) { user, categories, expenses, (bills, payments, incomes) ->
            val homeCurrency = user?.currencyCode ?: "USD"
            BudgetState(
                today = java.time.LocalDateTime.now(),
                monthBudget = 0.0,
                monthBudgetMinor = 0L,
                homeCurrency = homeCurrency,
                categories =
                    persistentListOf<CategoryLite>()
                        .builder()
                        .apply {
                            categories.forEach { add(CategoryLite(it.id, it.displayName)) }
                        }.build(),
                expenses =
                    persistentListOf<ExpenseLite>()
                        .builder()
                        .apply {
                            expenses.forEach {
                                add(
                                    ExpenseLite(
                                        it.id,
                                        it.amountMinor,
                                        it.currencyCode,
                                        it.categoryId,
                                        it.note,
                                        it.occurredAt,
                                    ),
                                )
                            }
                        }.build(),
                bills =
                    persistentListOf<BillLite>()
                        .builder()
                        .apply {
                            bills.forEach {
                                val dueDay = it.dueDay ?: today.dayOfMonth
                                val paid =
                                    payments.any { p ->
                                        p.billId == it.id && p.periodYear == today.year && p.periodMonth == today.monthValue &&
                                            p.paidAt != null
                                    }
                                add(BillLite(it.id, it.name, it.amountMinor, it.iconKey, dueDay, paid))
                            }
                        }.build(),
                incomes =
                    persistentListOf<IncomeLite>()
                        .builder()
                        .apply {
                            incomes.forEach {
                                add(IncomeLite(it.id, it.amountMinor, it.currencyCode, it.occurredAt, it.source))
                            }
                        }.build(),
                user = user,
            )
        }.stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = BudgetState.empty(),
        )
}
