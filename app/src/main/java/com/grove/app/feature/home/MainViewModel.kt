package com.grove.app.feature.home

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grove.app.data.BudgetPeriod
import com.grove.app.data.BudgetState
import com.grove.app.data.GroveDefaults
import com.grove.app.data.UserPreferences
import com.grove.app.data.UserPreferencesRepository
import com.grove.app.data.db.BudgetStateReactor
import com.grove.app.data.model.Bill
import com.grove.app.data.model.BillInput
import com.grove.app.data.model.BillLite
import com.grove.app.data.model.Expense
import com.grove.app.data.model.ExpenseInput
import com.grove.app.data.model.MonthlyBudget
import com.grove.app.data.model.MonthlyCategoryBudget
import com.grove.app.data.model.NotificationSettings
import com.grove.app.data.model.UserProfile
import com.grove.app.data.repository.BillRepository
import com.grove.app.data.repository.BudgetRepository
import com.grove.app.data.repository.CategoryRepository
import com.grove.app.data.repository.ExpenseRepository
import com.grove.app.data.repository.IncomeRepository
import com.grove.app.data.repository.NotificationRepository
import com.grove.app.data.repository.UserRepository
import com.grove.app.core.format.Money
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class ThemePrefs(val loaded: Boolean, val darkOverride: Boolean?)

class MainViewModel(
    private val userRepo: UserRepository,
    private val categoryRepo: CategoryRepository,
    private val expenseRepo: ExpenseRepository,
    private val billRepo: BillRepository,
    private val incomeRepo: IncomeRepository,
    private val budgetRepo: BudgetRepository,
    private val notificationRepo: NotificationRepository,
    private val prefs: UserPreferencesRepository,
    private val reactor: BudgetStateReactor,
) : ViewModel() {
    constructor(application: Application) : this(
        userRepo = GroveAppGraph.userRepo(application),
        categoryRepo = GroveAppGraph.categoryRepo(application),
        expenseRepo = GroveAppGraph.expenseRepo(application),
        billRepo = GroveAppGraph.billRepo(application),
        incomeRepo = GroveAppGraph.incomeRepo(application),
        budgetRepo = GroveAppGraph.budgetRepo(application),
        notificationRepo = GroveAppGraph.notificationRepo(application),
        prefs = GroveAppGraph.prefsRepo(application),
        reactor = GroveAppGraph.reactor(application),
    )

    private val _toast = MutableStateFlow<String?>(null)
    val toast: StateFlow<String?> = _toast.asStateFlow()
    private var toastJob: Job? = null

    val state: StateFlow<BudgetState> = reactor.state

    val preferences: StateFlow<UserPreferences> =
        prefs.preferences
            .stateIn(viewModelScope, SharingStarted.Eagerly, UserPreferences())

    val currency: StateFlow<String> =
        state
            .map { it.homeCurrency }
            .stateIn(viewModelScope, SharingStarted.Eagerly, GroveDefaults.DEFAULT_CURRENCY)

    val themePrefs: StateFlow<ThemePrefs> =
        prefs.preferences
            .map { ThemePrefs(loaded = true, darkOverride = it.darkModeOverride) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, ThemePrefs(loaded = false, darkOverride = null))

    val notificationSettings: StateFlow<NotificationSettings> =
        notificationRepo
            .observeSettings()
            .map { it ?: GroveDefaults.DEFAULT_NOTIFICATION_SETTINGS }
            .stateIn(viewModelScope, SharingStarted.Eagerly, GroveDefaults.DEFAULT_NOTIFICATION_SETTINGS)

    val soundsEnabled: StateFlow<Boolean> =
        preferences
            .map { it.soundsEnabled }
            .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    fun toggleDark(current: Boolean) {
        viewModelScope.launch { prefs.updateDarkMode(if (current) "light" else "dark") }
    }

    fun toggleSounds(enabled: Boolean) {
        viewModelScope.launch { prefs.updateSounds(enabled) }
    }

    fun saveExpense(input: ExpenseInput) {
        viewModelScope.launch {
            val id = input.id ?: UUID.randomUUID()
            val now = Instant.now()
            val existed = state.value.expenses.any { it.id == id }
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

    fun deleteBill(id: UUID) = viewModelScope.launch {
        runCatching { billRepo.delete(id) }
            .onFailure { toast("That one wouldn't budge — try again") }
    }

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

    fun toggleBill(occurrence: BillLite) {
        viewModelScope.launch {
            val bill = billRepo.get(occurrence.id) ?: return@launch
            billRepo.togglePaymentForOccurrence(bill, state.value.period, occurrence.dueAt, Instant.now())
        }
    }

    fun updateMonthBudget(value: Double) {
        viewModelScope.launch {
            val minor = Money.toMinor(value, currency.value)
            val period = state.value.period
            runCatching {
                val existing = budgetRepo.getForPeriod(period.start.year, period.start.monthValue)
                val now = Instant.now()
                val budget =
                    MonthlyBudget(
                        id = existing?.id ?: UUID.randomUUID(),
                        periodYear = period.start.year,
                        periodMonth = period.start.monthValue,
                        totalMinor = minor,
                        currencyCode = currency.value,
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
            val period = state.value.period
            val now = Instant.now()
            runCatching {
                val budget =
                    budgetRepo.getForPeriod(period.start.year, period.start.monthValue) ?: MonthlyBudget(
                        id = UUID.randomUUID(),
                        periodYear = period.start.year,
                        periodMonth = period.start.monthValue,
                        totalMinor = state.value.monthBudgetMinor,
                        currencyCode = currency.value,
                        createdAt = now,
                        updatedAt = now,
                    )
                val existing = budgetRepo.getCategoryBudget(budget.id, categoryId)
                val categoryBudget =
                    MonthlyCategoryBudget(
                        id = existing?.id ?: UUID.randomUUID(),
                        monthlyBudgetId = budget.id,
                        categoryId = categoryId,
                        amountMinor = Money.toMinor(value, currency.value),
                    )
                budgetRepo.upsert(budget, listOf(categoryBudget))
            }.onFailure { toast("Hmm, that didn't save — try again") }
        }
    }

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

    fun updateDailySafeSpend(enabled: Boolean) {
        viewModelScope.launch {
            val current = notificationRepo.getSettings() ?: GroveDefaults.DEFAULT_NOTIFICATION_SETTINGS
            notificationRepo.upsertSettings(current.copy(dailySafeSpendEnabled = enabled))
        }
    }

    fun updateBillAlerts(enabled: Boolean) {
        viewModelScope.launch {
            val current = notificationRepo.getSettings() ?: GroveDefaults.DEFAULT_NOTIFICATION_SETTINGS
            notificationRepo.upsertSettings(current.copy(billAlertsEnabled = enabled))
        }
    }

    fun updateCurrency(code: String) {
        viewModelScope.launch {
            val current =
                userRepo.get() ?: UserProfile(
                    name = GroveDefaults.DEFAULT_USER_NAME,
                    resetDay = 1,
                    currencyCode = GroveDefaults.DEFAULT_CURRENCY,
                    onboardingCompleted = false,
                )
            userRepo.upsert(current.copy(currencyCode = code))
        }
    }

    fun updateUserName(name: String) {
        viewModelScope.launch {
            val current = userRepo.get() ?: return@launch
            val trimmed = name.trim().take(GroveDefaults.MAX_USER_NAME_LENGTH)
            if (trimmed.isNotEmpty()) {
                userRepo.upsert(current.copy(name = trimmed))
            }
        }
    }

    fun updateResetDay(day: Int) {
        viewModelScope.launch {
            val current = userRepo.get() ?: return@launch
            userRepo.upsert(current.copy(resetDay = day.coerceIn(1, 28)))
        }
    }

    fun findExpenseForEdit(
        id: UUID,
        onFound: (Expense) -> Unit,
    ) {
        viewModelScope.launch { expenseRepo.get(id)?.let(onFound) }
    }

    private fun toast(message: String) {
        toastJob?.cancel()
        _toast.value = message
        toastJob = viewModelScope.launch {
            delay(GroveDefaults.TOAST_DURATION_MS)
            _toast.value = null
        }
    }
}
