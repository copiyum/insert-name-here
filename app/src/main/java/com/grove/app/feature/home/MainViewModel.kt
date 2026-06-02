package com.grove.app.feature.home

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grove.app.data.BudgetRepository
import com.grove.app.data.SeedBudgetRepository
import com.grove.app.data.UserPreferences
import com.grove.app.data.UserPreferencesRepository
import com.grove.app.data.userPreferencesDataStore
import com.grove.app.data.model.Expense
import com.grove.app.designsystem.format.Money
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    private val repo: BudgetRepository,
    private val prefs: UserPreferencesRepository,
) : ViewModel() {
    constructor(application: Application) : this(
        SeedBudgetRepository(),
        UserPreferencesRepository(application.userPreferencesDataStore),
    )

    val state = repo.state

    val preferences: StateFlow<UserPreferences> = prefs.preferences
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserPreferences())

    val currency: StateFlow<String> = preferences
        .map { it.currency }
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserPreferences().currency)

    val darkOverride: StateFlow<Boolean?> = preferences
        .map { it.darkModeOverride }
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserPreferences().darkModeOverride)

    private val _toast = MutableStateFlow<String?>(null)
    val toast: StateFlow<String?> = _toast.asStateFlow()

    fun toggleDark(current: Boolean) {
        viewModelScope.launch { prefs.updateDarkMode(if (current) "light" else "dark") }
    }

    fun saveExpense(expense: Expense) {
        val existed = state.value.expenses.any { it.id == expense.id }
        repo.saveExpense(expense)
        viewModelScope.launch {
            toast("${if (existed) "Updated" else "Saved"} · ${Money.currency(expense.amount, currencyCode = currency.value)}")
        }
    }

    fun deleteExpense(id: String) = repo.deleteExpense(id)
    fun addBill(bill: com.grove.app.data.model.Bill) = repo.addBill(bill)
    fun toggleBill(id: String) = repo.toggleBill(id)
    fun updateMonthBudget(value: Double) = repo.updateMonthBudget(value)
    fun updateCategoryBudget(id: String, value: Double) = repo.updateCategoryBudget(id, value)
    fun applyOnboarding(monthBudget: Double, resetDay: Int) {
        repo.applyOnboarding(monthBudget, resetDay)
        viewModelScope.launch { prefs.updateAll(name = state.value.user.name, resetDay = resetDay, currency = currency.value) }
    }

    fun updateCurrency(code: String) {
        repo.updateCurrency(code)
        viewModelScope.launch { prefs.updateCurrency(code) }
    }

    private fun toast(message: String) {
        _toast.value = message
        viewModelScope.launch {
            delay(1800)
            _toast.value = null
        }
    }
}
