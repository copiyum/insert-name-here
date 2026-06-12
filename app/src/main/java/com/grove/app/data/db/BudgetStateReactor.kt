package com.grove.app.data.db

import com.grove.app.data.BudgetPeriod
import com.grove.app.data.BudgetState
import com.grove.app.data.MonthlyTotal
import com.grove.app.data.model.Bill
import com.grove.app.data.model.BillPayment
import com.grove.app.data.model.Category
import com.grove.app.data.model.CategoryKind
import com.grove.app.data.model.Income
import com.grove.app.data.model.MonthlyBudget
import com.grove.app.data.model.MonthlyCategoryBudget
import com.grove.app.data.model.UserProfile
import com.grove.app.data.repository.BillRepository
import com.grove.app.data.repository.BudgetRepository
import com.grove.app.data.repository.CategoryRepository
import com.grove.app.data.repository.ExpenseRepository
import com.grove.app.data.repository.IncomeRepository
import com.grove.app.data.repository.UserRepository
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

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
            combine(
                userRepo.observe(),
                categoryRepo.observeActive(),
                expenseRepo.observeAllLite(),
                budgetRepo.observeAll(),
                budgetRepo.observeAllCategoryBudgets(),
            ) { user, categories, expenses, budgets, categoryBudgets ->
                BudgetInputs(user, categories, expenses, budgets, categoryBudgets)
            },
            combine(
                billRepo.observeActive(),
                billRepo.observePayments(),
                incomeRepo.observeAll(),
            ) { bills, payments, incomes -> CashflowInputs(bills, payments, incomes) },
            dateTicker(),
        ) { budgetInputs, cashflowInputs, now ->
            val (user, categories, expenses, budgets, categoryBudgets) = budgetInputs
            val (bills, payments, incomes) = cashflowInputs
            val homeCurrency = user?.currencyCode ?: "INR"
            val period = BudgetPeriod.forDate(now, user?.resetDay ?: 1)
            val todayStart = now
            // Category fields arrive pre-joined from the DAO (see ExpenseDao.observeAllWithCategory).
            val spendingExpenses = expenses.filter { it.categoryKind != CategoryKind.income }
            val periodSpend = spendingExpenses.filter { period.contains(it.occurredAt) }
            val spentTodayMinor =
                spendingExpenses
                    .filter { it.occurredAt.atZone(ZoneId.systemDefault()).toLocalDate() == todayStart }
                    .sumOf { it.amountMinor }
            val monthlyBudget =
                budgets.firstOrNull {
                    it.periodYear == period.start.year && it.periodMonth == period.start.monthValue
                }
            val monthBudgetMinor = monthlyBudget?.totalMinor ?: 0L
            val paidBillKeys =
                payments
                    .asSequence()
                    .filter { it.periodYear == period.start.year && it.periodMonth == period.start.monthValue && it.paidAt != null }
                    .map { it.billId }
                    .toSet()
            BudgetState(
                today = now.atStartOfDay(),
                monthBudgetMinor = monthBudgetMinor,
                homeCurrency = homeCurrency,
                categories =
                    persistentListOf<CategoryLite>()
                        .builder()
                        .apply {
                            categories.forEach { add(CategoryLite(it.id, it.displayName, it.iconKey, it.kind)) }
                        }.build(),
                expenses =
                    persistentListOf<ExpenseLite>()
                        .builder()
                        .apply { addAll(expenses) }
                        .build(),
                bills =
                    persistentListOf<BillLite>()
                        .builder()
                        .apply {
                            bills.forEach {
                                val dueDay = it.dueDay ?: now.dayOfMonth
                                val paid = it.id in paidBillKeys
                                add(BillLite(it.id, it.name, it.amountMinor, it.currencyCode, it.iconKey, dueDay, paid))
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
                categoryBudgets =
                    persistentListOf<CategoryBudgetLite>()
                        .builder()
                        .apply {
                            if (monthlyBudget != null) {
                                categoryBudgets
                                    .filter { it.monthlyBudgetId == monthlyBudget.id }
                                    .forEach { add(CategoryBudgetLite(it.categoryId, it.amountMinor)) }
                            }
                        }.build(),
                user = user,
                pastMonths =
                    persistentListOf<MonthlyTotal>()
                        .builder()
                        .apply {
                            spendingExpenses
                                .groupBy { e ->
                                    val dt = e.occurredAt.atZone(ZoneId.systemDefault())
                                    dt.year to dt.monthValue
                                }
                                .map { (ym, exps) ->
                                    MonthlyTotal(
                                        year = ym.first,
                                        month = ym.second,
                                        totalMinor = exps.sumOf { it.amountMinor },
                                        monthName = java.time.Month.of(ym.second).getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                                    )
                                }
                                .sortedByDescending { it.year * 100 + it.month }
                                .take(12)
                                .forEach { add(it) }
                        }.build(),
                totalSpentMinor = periodSpend.sumOf { it.amountMinor },
                spentTodayMinor = spentTodayMinor,
                upcomingBillsMinor = bills.filter { it.id !in paidBillKeys }.sumOf { it.amountMinor },
            )
        }.stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = BudgetState.empty(),
        )
}

private fun dateTicker(): Flow<LocalDate> =
    flow {
        while (currentCoroutineContext().isActive) {
            val now = LocalDateTime.now()
            emit(now.toLocalDate())
            delay(millisUntilNextDay(now))
        }
    }.distinctUntilChanged()

private fun millisUntilNextDay(now: LocalDateTime): Long {
    val nextDay = now.toLocalDate().plusDays(1).atStartOfDay()
    return Duration.between(now, nextDay).toMillis().coerceAtLeast(1_000L)
}

private data class BudgetInputs(
    val user: UserProfile?,
    val categories: List<Category>,
    val expenses: List<ExpenseLite>,
    val budgets: List<MonthlyBudget>,
    val categoryBudgets: List<MonthlyCategoryBudget>,
)

private data class CashflowInputs(
    val bills: List<Bill>,
    val payments: List<BillPayment>,
    val incomes: List<Income>,
)
