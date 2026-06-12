package com.grove.app.feature.home

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grove.app.data.BudgetPeriod
import com.grove.app.data.BudgetState
import com.grove.app.data.UserPreferences
import com.grove.app.data.UserPreferencesRepository
import com.grove.app.data.db.BudgetStateReactor
import com.grove.app.data.db.GroveDatabase
import com.grove.app.data.model.Bill
import com.grove.app.data.model.Expense
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
import com.grove.app.data.userPreferencesDataStore
import com.grove.app.designsystem.format.Money
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
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
        userRepo = Graph.userRepo(application),
        categoryRepo = Graph.categoryRepo(application),
        expenseRepo = Graph.expenseRepo(application),
        billRepo = Graph.billRepo(application),
        incomeRepo = Graph.incomeRepo(application),
        budgetRepo = Graph.budgetRepo(application),
        notificationRepo = Graph.notificationRepo(application),
        prefs = Graph.prefsRepo(application),
        reactor = Graph.reactor(application),
    )

    private val _toast = MutableStateFlow<String?>(null)
    val toast: StateFlow<String?> = _toast.asStateFlow()

    val state: StateFlow<BudgetState> = reactor.state

    val preferences: StateFlow<UserPreferences> =
        prefs.preferences
            .stateIn(viewModelScope, SharingStarted.Eagerly, UserPreferences())

    val currency: StateFlow<String> =
        state
            .map { it.homeCurrency }
            .stateIn(viewModelScope, SharingStarted.Eagerly, "USD")

    val darkOverride: StateFlow<Boolean?> =
        preferences
            .map { it.darkModeOverride }
            .stateIn(viewModelScope, SharingStarted.Eagerly, UserPreferences().darkModeOverride)

    val notificationSettings: StateFlow<NotificationSettings> =
        notificationRepo
            .observeSettings()
            .map { it ?: NotificationSettings(true, 480, true, 3) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, NotificationSettings(true, 480, true, 3))

    fun toggleDark(current: Boolean) {
        viewModelScope.launch { prefs.updateDarkMode(if (current) "light" else "dark") }
    }

    fun saveExpense(expense: Expense) {
        viewModelScope.launch {
            val existed = state.value.expenses.any { it.id == expense.id }
            expenseRepo.upsert(expense)
            val display = Money.currencyLong(expense.amountMinor, 2, currency.value)
            toast("${if (existed) "Updated" else "Saved"} · $display")
        }
    }

    fun deleteExpense(id: UUID) = viewModelScope.launch { expenseRepo.delete(id) }

    fun deleteBill(id: UUID) = viewModelScope.launch { billRepo.delete(id) }

    fun addBill(bill: Bill) = viewModelScope.launch {
        billRepo.upsert(bill)
        toast("Bill added · ${bill.name}")
    }

    fun toggleBill(id: UUID) {
        viewModelScope.launch {
            val bill = billRepo.get(id) ?: return@launch
            billRepo.togglePaymentForPeriod(bill, state.value.period, Instant.now())
        }
    }

    fun updateMonthBudget(value: Double) {
        viewModelScope.launch {
            val minor = Money.toMinor(value, currency.value)
            val period = state.value.period
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
            val existing =
                budgetRepo
                    .getCategoryBudgets(budget.id)
                    .firstOrNull { it.categoryId == categoryId }
            val categoryBudget =
                MonthlyCategoryBudget(
                    id = existing?.id ?: UUID.randomUUID(),
                    monthlyBudgetId = budget.id,
                    categoryId = categoryId,
                    amountMinor = Money.toMinor(value, currency.value),
                )
            budgetRepo.upsert(budget, listOf(categoryBudget))
        }
    }

    fun applyOnboarding(
        monthBudget: Double,
        resetDay: Int,
    ) {
        viewModelScope.launch {
            val current =
                userRepo.get() ?: UserProfile(
                    name = "Mae",
                    resetDay = resetDay,
                    currencyCode = "USD",
                    onboardingCompleted = false,
                )
            val minor = Money.toMinor(monthBudget, current.currencyCode)
            userRepo.upsert(
                current.copy(
                    name = "Mae",
                    resetDay = resetDay,
                    onboardingCompleted = true,
                    onboardingCompletedAt = Instant.now(),
                ),
            )
            val today = LocalDate.now()
            val period = BudgetPeriod.forDate(today, resetDay)
            budgetRepo.upsert(
                MonthlyBudget(
                    id = UUID.randomUUID(),
                    periodYear = period.start.year,
                    periodMonth = period.start.monthValue,
                    totalMinor = minor,
                    currencyCode = current.currencyCode,
                    createdAt = Instant.now(),
                    updatedAt = Instant.now(),
                ),
                emptyList(),
            )
        }
    }

    fun skipOnboarding() {
        viewModelScope.launch {
            val current = userRepo.get() ?: return@launch
            userRepo.upsert(current.copy(onboardingCompleted = true, onboardingCompletedAt = Instant.now()))
        }
    }

    fun updateDailySafeSpend(enabled: Boolean) {
        viewModelScope.launch {
            val current = notificationRepo.getSettings() ?: NotificationSettings(true, 480, true, 3)
            notificationRepo.upsertSettings(current.copy(dailySafeSpendEnabled = enabled))
        }
    }

    fun updateBillAlerts(enabled: Boolean) {
        viewModelScope.launch {
            val current = notificationRepo.getSettings() ?: NotificationSettings(true, 480, true, 3)
            notificationRepo.upsertSettings(current.copy(billAlertsEnabled = enabled))
        }
    }

    fun updateCurrency(code: String) {
        viewModelScope.launch {
            val current =
                userRepo.get() ?: UserProfile(
                    name = "Mae",
                    resetDay = 1,
                    currencyCode = "USD",
                    onboardingCompleted = false,
                )
            expenseRepo.updateCurrencyCode(code)
            billRepo.updateCurrencyCode(code)
            incomeRepo.updateCurrencyCode(code)
            budgetRepo.updateCurrencyCode(code)
            userRepo.upsert(current.copy(currencyCode = code))
        }
    }

    fun updateUserName(name: String) {
        viewModelScope.launch {
            val current = userRepo.get() ?: return@launch
            val trimmed = name.trim().take(30)
            if (trimmed.isNotEmpty()) {
                userRepo.upsert(current.copy(name = trimmed))
            }
        }
    }

    fun updateResetDay(day: Int) {
        viewModelScope.launch {
            val current = userRepo.get() ?: return@launch
            userRepo.upsert(current.copy(resetDay = day.coerceIn(1, 31)))
        }
    }

    fun findExpenseForEdit(
        id: UUID,
        onFound: (Expense) -> Unit,
    ) {
        viewModelScope.launch { expenseRepo.get(id)?.let(onFound) }
    }

    private fun toast(message: String) {
        _toast.value = message
        viewModelScope.launch {
            delay(1800)
            _toast.value = null
        }
    }
}


object Graph {
    @Volatile private var db: GroveDatabase? = null

    @Volatile private var reactor: BudgetStateReactor? = null

    private fun db(application: Application): GroveDatabase =
        db ?: synchronized(this) {
            db ?: GroveDatabase.build(application).also { db = it }
        }

    fun userRepo(application: Application) = UserRepository(db(application).userProfileDao())

    fun categoryRepo(application: Application) = CategoryRepository(db(application).categoryDao())

    fun expenseRepo(application: Application) = ExpenseRepository(db(application).expenseDao())

    fun billRepo(application: Application) = BillRepository(db(application).billDao(), db(application).billPaymentDao())

    fun incomeRepo(application: Application) = IncomeRepository(db(application).incomeDao())

    fun budgetRepo(application: Application) = BudgetRepository(db(application).monthlyBudgetDao())

    fun notificationRepo(application: Application) = NotificationRepository(db(application).notificationDao())

    fun prefsRepo(application: Application) = UserPreferencesRepository(application.userPreferencesDataStore)

    fun reactor(application: Application): BudgetStateReactor =
        reactor ?: synchronized(this) {
            reactor ?: BudgetStateReactor(
                scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
                userRepo = userRepo(application),
                categoryRepo = categoryRepo(application),
                expenseRepo = expenseRepo(application),
                billRepo = billRepo(application),
                incomeRepo = incomeRepo(application),
                budgetRepo = budgetRepo(application),
            ).also { reactor = it }
        }
}
