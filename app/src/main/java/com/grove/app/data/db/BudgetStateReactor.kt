package com.grove.app.data.db

import com.grove.app.data.BudgetState
import com.grove.app.data.MonthlyTotal
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
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class BudgetStateReactor(
    scope: CoroutineScope,
    userRepo: UserRepository,
    categoryRepo: CategoryRepository,
    expenseRepo: ExpenseRepository,
    billRepo: BillRepository,
    incomeRepo: IncomeRepository,
    budgetRepo: BudgetRepository,
) {

    val state: StateFlow<BudgetState> =
        combine(
            userRepo.observe(),
            categoryRepo.observeActive(),
            expenseRepo.observeAll(),
            budgetRepo.observeForPeriod(LocalDate.now().year, LocalDate.now().monthValue),
            combine(
                billRepo.observeActive(),
                billRepo.observePayments(),
                incomeRepo.observeAll(),
            ) { bills, payments, incomes -> Triple(bills, payments, incomes) },
        ) { user, categories, expenses, monthlyBudget, (bills, payments, incomes) ->
            val now = LocalDate.now()
            val homeCurrency = user?.currencyCode ?: "USD"
            val monthBudgetMinor = monthlyBudget?.totalMinor ?: 0L
            BudgetState(
                today = java.time.LocalDateTime.now(),
                monthBudgetMinor = monthBudgetMinor,
                homeCurrency = homeCurrency,
                categories =
                    persistentListOf<CategoryLite>()
                        .builder()
                        .apply {
                            categories.forEach { add(CategoryLite(it.id, it.displayName, it.iconKey)) }
                        }.build(),
                expenses =
                    persistentListOf<ExpenseLite>()
                        .builder()
                        .apply {
                            val catIconKeys = categories.associate { it.id to it.iconKey }
                            expenses.forEach {
                                add(
                                    ExpenseLite(
                                        it.id,
                                        it.amountMinor,
                                        it.currencyCode,
                                        it.categoryId,
                                        catIconKeys[it.categoryId] ?: "more_horiz",
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
                                val dueDay = it.dueDay ?: now.dayOfMonth
                                val paid =
                                    payments.any { p ->
                                        p.billId == it.id && p.periodYear == now.year && p.periodMonth == now.monthValue &&
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
                pastMonths =
                    persistentListOf<MonthlyTotal>()
                        .builder()
                        .apply {
                            val fmt = DateTimeFormatter.ofPattern("MMM")
                            expenses
                                .groupBy { e ->
                                    val dt = e.occurredAt.atZone(ZoneId.systemDefault())
                                    dt.year to dt.monthValue
                                }
                                .map { (ym, exps) ->
                                    MonthlyTotal(
                                        year = ym.first,
                                        month = ym.second,
                                        totalMinor = exps.sumOf { it.amountMinor },
                                        monthName = java.time.Month.of(ym.second).name.lowercase()
                                            .replaceFirstChar { it.uppercase() }.take(3),
                                    )
                                }
                                .sortedByDescending { it.year * 100 + it.month }
                                .take(12)
                                .forEach { add(it) }
                        }.build(),
            )
        }.stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = BudgetState.empty(),
        )
}
