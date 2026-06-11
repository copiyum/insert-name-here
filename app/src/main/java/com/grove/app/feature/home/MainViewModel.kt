package com.grove.app.feature.home

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grove.app.data.BudgetState
import com.grove.app.data.UserPreferences
import com.grove.app.data.UserPreferencesRepository
import com.grove.app.data.db.BudgetStateReactor
import com.grove.app.data.db.GroveDatabase
import com.grove.app.data.model.Bill
import com.grove.app.data.model.Expense
import com.grove.app.data.model.MonthlyBudget
import com.grove.app.data.model.UserProfile
import com.grove.app.data.repository.BillRepository
import com.grove.app.data.repository.BudgetRepository
import com.grove.app.data.repository.CategoryRepository
import com.grove.app.data.repository.ExpenseRepository
import com.grove.app.data.repository.IncomeRepository
import com.grove.app.data.repository.UserRepository
import com.grove.app.data.userPreferencesDataStore
import com.grove.app.designsystem.format.Currencies
import com.grove.app.designsystem.format.Money
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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
        prefs = Graph.prefsRepo(application),
        reactor = Graph.reactor(application),
    )

    private val _toast = MutableStateFlow<String?>(null)
    val toast: StateFlow<String?> = _toast.asStateFlow()

    private val _debugDateOffset = MutableStateFlow(0)
    val debugDateOffset: StateFlow<Int> = _debugDateOffset.asStateFlow()
    val debugDate: StateFlow<String> =
        _debugDateOffset.map { offset ->
            val d = LocalDate.now().plusDays(offset.toLong())
            d.format(java.time.format.DateTimeFormatter.ofPattern("MMM d"))
        }.stateIn(viewModelScope, SharingStarted.Eagerly, LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("MMM d")))

    val state: StateFlow<BudgetState> =
        combine(reactor.state, _debugDateOffset) { st, offset ->
            val shiftedToday = java.time.LocalDateTime.now().plusDays(offset.toLong())
            st.copy(today = shiftedToday)
        }.stateIn(viewModelScope, SharingStarted.Eagerly, reactor.state.value)

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

    fun shiftDebugDate(days: Int) {
        _debugDateOffset.value += days
    }

    fun toggleDark(current: Boolean) {
        viewModelScope.launch { prefs.updateDarkMode(if (current) "light" else "dark") }
    }

    fun saveExpense(expense: Expense) {
        viewModelScope.launch {
            val offset = _debugDateOffset.value
            val adjusted = if (offset != 0) {
                val shiftedInstant = expense.occurredAt.plusSeconds(offset * 86400L)
                expense.copy(occurredAt = shiftedInstant)
            } else expense
            val existed = state.value.expenses.any { it.id == expense.id }
            expenseRepo.upsert(adjusted)
            val display = Money.currencyLong(adjusted.amountMinor, 2, currency.value)
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
            val now = Instant.now()
            val today = LocalDate.now()
            val periodStart =
                LocalDate
                    .of(today.year, today.monthValue, 1)
                    .atStartOfDay(java.time.ZoneOffset.UTC)
                    .toInstant()
            val periodEnd =
                LocalDate
                    .of(today.year, today.monthValue, 1)
                    .plusMonths(1)
                    .atStartOfDay(java.time.ZoneOffset.UTC)
                    .toInstant()
            val pid = UUID.nameUUIDFromBytes("billpayment.$id.$periodStart.$periodEnd".toByteArray())
            billRepo.markPaymentPaid(pid, now, null)
        }
    }

    fun updateMonthBudget(value: Double) {
        viewModelScope.launch {
            val minor = (value * Math.pow(10.0, Currencies.minorUnitExponent(currency.value).toDouble())).toLong()
            val today = LocalDate.now()
            val budget =
                MonthlyBudget(
                    id = UUID.randomUUID(),
                    periodYear = today.year,
                    periodMonth = today.monthValue,
                    totalMinor = minor,
                    currencyCode = currency.value,
                    createdAt = Instant.now(),
                    updatedAt = Instant.now(),
                )
            budgetRepo.upsert(budget, emptyList())
        }
    }

    fun updateCategoryBudget(
        id: String,
        value: Double,
    ) {
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
            val minor =
                (monthBudget * Math.pow(10.0, Currencies.minorUnitExponent(current.currencyCode).toDouble())).toLong()
            userRepo.upsert(
                current.copy(
                    name = "Mae",
                    resetDay = resetDay,
                    onboardingCompleted = true,
                    onboardingCompletedAt = Instant.now(),
                ),
            )
            val today = LocalDate.now()
            budgetRepo.upsert(
                MonthlyBudget(
                    id = UUID.randomUUID(),
                    periodYear = today.year,
                    periodMonth = today.monthValue,
                    totalMinor = minor,
                    currencyCode = current.currencyCode,
                    createdAt = Instant.now(),
                    updatedAt = Instant.now(),
                ),
                emptyList(),
            )
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
