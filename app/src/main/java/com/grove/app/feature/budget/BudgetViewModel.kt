package com.grove.app.feature.budget

import androidx.lifecycle.viewModelScope
import com.grove.app.core.format.Money
import com.grove.app.core.ui.EffectViewModel
import com.grove.app.data.db.BudgetStateReactor
import com.grove.app.data.model.MonthlyBudget
import com.grove.app.data.model.MonthlyCategoryBudget
import com.grove.app.data.repository.BudgetRepository
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID

class BudgetViewModel(
    private val budgetRepo: BudgetRepository,
    private val reactor: BudgetStateReactor,
) : EffectViewModel() {
    fun updateMonthBudget(value: Double) {
        viewModelScope.launch {
            val state = reactor.state.value
            val currency = state.homeCurrency
            val minor = Money.toMinor(value, currency)
            val period = state.period
            runCatching {
                val existing = budgetRepo.getForPeriod(period.start.year, period.start.monthValue)
                val now = Instant.now()
                val budget =
                    MonthlyBudget(
                        id = existing?.id ?: UUID.randomUUID(),
                        periodYear = period.start.year,
                        periodMonth = period.start.monthValue,
                        totalMinor = minor,
                        currencyCode = currency,
                        createdAt = existing?.createdAt ?: now,
                        updatedAt = now,
                    )
                budgetRepo.upsert(budget, emptyList())
            }.onFailure { toast("Hmm, that didn't save — try again") }
        }
    }

    fun updateCategoryBudget(
        id: String,
        value: Double,
    ) {
        viewModelScope.launch {
            val categoryId = runCatching { UUID.fromString(id) }.getOrNull() ?: return@launch
            val state = reactor.state.value
            val currency = state.homeCurrency
            val period = state.period
            val now = Instant.now()
            runCatching {
                val budget =
                    budgetRepo.getForPeriod(period.start.year, period.start.monthValue) ?: MonthlyBudget(
                        id = UUID.randomUUID(),
                        periodYear = period.start.year,
                        periodMonth = period.start.monthValue,
                        totalMinor = state.monthBudgetMinor,
                        currencyCode = currency,
                        createdAt = now,
                        updatedAt = now,
                    )
                val existing = budgetRepo.getCategoryBudget(budget.id, categoryId)
                val categoryBudget =
                    MonthlyCategoryBudget(
                        id = existing?.id ?: UUID.randomUUID(),
                        monthlyBudgetId = budget.id,
                        categoryId = categoryId,
                        amountMinor = Money.toMinor(value, currency),
                    )
                budgetRepo.upsert(budget, listOf(categoryBudget))
            }.onFailure { toast("Hmm, that didn't save — try again") }
        }
    }
}
