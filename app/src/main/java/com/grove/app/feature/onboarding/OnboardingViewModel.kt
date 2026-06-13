package com.grove.app.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grove.app.core.format.Money
import com.grove.app.data.BudgetPeriod
import com.grove.app.data.GroveDefaults
import com.grove.app.data.model.MonthlyBudget
import com.grove.app.data.model.UserProfile
import com.grove.app.data.repository.BudgetRepository
import com.grove.app.data.repository.UserRepository
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

class OnboardingViewModel(
    private val userRepo: UserRepository,
    private val budgetRepo: BudgetRepository,
) : ViewModel() {
    fun applyOnboarding(
        monthBudget: Double,
        resetDay: Int,
    ) {
        viewModelScope.launch {
            val current =
                userRepo.get() ?: UserProfile(
                    name = GroveDefaults.DEFAULT_USER_NAME,
                    resetDay = resetDay,
                    currencyCode = GroveDefaults.DEFAULT_CURRENCY,
                    onboardingCompleted = false,
                )
            val minor = Money.toMinor(monthBudget, current.currencyCode)
            userRepo.upsert(
                current.copy(
                    name = current.name.ifBlank { GroveDefaults.DEFAULT_USER_NAME },
                    resetDay = resetDay,
                    onboardingCompleted = true,
                    onboardingCompletedAt = Instant.now(),
                ),
            )
            val today = LocalDate.now()
            val period = BudgetPeriod.forDate(today, resetDay)
            val existingBudget = budgetRepo.getForPeriod(period.start.year, period.start.monthValue)
            budgetRepo.upsert(
                MonthlyBudget(
                    id = existingBudget?.id ?: UUID.randomUUID(),
                    periodYear = period.start.year,
                    periodMonth = period.start.monthValue,
                    totalMinor = minor,
                    currencyCode = current.currencyCode,
                    createdAt = existingBudget?.createdAt ?: Instant.now(),
                    updatedAt = Instant.now(),
                ),
                emptyList(),
            )
        }
    }
}
