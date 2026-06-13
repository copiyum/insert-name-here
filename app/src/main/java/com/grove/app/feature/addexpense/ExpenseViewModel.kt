package com.grove.app.feature.addexpense

import androidx.lifecycle.viewModelScope
import com.grove.app.core.format.Money
import com.grove.app.core.ui.EffectViewModel
import com.grove.app.data.db.BudgetStateReactor
import com.grove.app.data.model.Expense
import com.grove.app.data.model.ExpenseInput
import com.grove.app.data.repository.ExpenseRepository
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID

class ExpenseViewModel(
    private val expenseRepo: ExpenseRepository,
    private val reactor: BudgetStateReactor,
) : EffectViewModel() {
    fun saveExpense(input: ExpenseInput) {
        viewModelScope.launch {
            val id = input.id ?: UUID.randomUUID()
            val now = Instant.now()
            val existed = reactor.state.value.expenses.any { it.id == id }
            val expense =
                Expense(
                    id = id,
                    amountMinor = input.amountMinor,
                    currencyCode = input.currencyCode,
                    categoryId = input.categoryId,
                    note = input.note,
                    occurredAt = input.occurredAt,
                    createdAt = now,
                    updatedAt = now,
                )
            runCatching { expenseRepo.upsert(expense) }
                .onSuccess {
                    val display = Money.currencyLong(input.amountMinor, 2, input.currencyCode)
                    toast("${if (existed) "Updated" else "Saved"} · $display")
                }.onFailure { toast("Hmm, that didn't save — try again") }
        }
    }

    fun deleteExpense(id: UUID) = viewModelScope.launch {
        runCatching { expenseRepo.delete(id) }
            .onFailure { toast("That one wouldn't budge — try again") }
    }

    fun findExpenseForEdit(
        id: UUID,
        onFound: (Expense) -> Unit,
    ) {
        viewModelScope.launch { expenseRepo.get(id)?.let(onFound) }
    }
}
