package com.grove.app.feature.bills

import androidx.lifecycle.viewModelScope
import com.grove.app.core.ui.EffectViewModel
import com.grove.app.data.db.BudgetStateReactor
import com.grove.app.data.model.Bill
import com.grove.app.data.model.BillInput
import com.grove.app.data.model.BillLite
import com.grove.app.data.repository.BillRepository
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID

class BillViewModel(
    private val billRepo: BillRepository,
    private val reactor: BudgetStateReactor,
) : EffectViewModel() {
    fun addBill(input: BillInput) = viewModelScope.launch {
        val now = Instant.now()
        val bill =
            Bill(
                id = UUID.randomUUID(),
                name = input.name,
                amountMinor = input.amountMinor,
                currencyCode = input.currencyCode,
                frequency = input.frequency,
                dueDay = input.dueDay,
                dueWeekday = input.dueWeekday,
                startDate = input.startDate,
                endDate = null,
                iconKey = input.iconKey,
                isActive = true,
                createdAt = now,
                updatedAt = now,
            )
        runCatching { billRepo.upsert(bill) }
            .onSuccess { toast("Bill added · ${input.name}") }
            .onFailure { toast("Hmm, that didn't save — try again") }
    }

    fun deleteBill(id: UUID) = viewModelScope.launch {
        runCatching { billRepo.delete(id) }
            .onFailure { toast("That one wouldn't budge — try again") }
    }

    fun toggleBill(occurrence: BillLite) {
        viewModelScope.launch {
            val bill = billRepo.get(occurrence.id) ?: return@launch
            billRepo.togglePaymentForOccurrence(bill, reactor.state.value.period, occurrence.dueAt, Instant.now())
        }
    }
}
