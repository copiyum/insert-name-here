package com.grove.app.data

import com.grove.app.data.model.Bill
import com.grove.app.data.model.Category
import com.grove.app.data.model.Expense
import com.grove.app.data.model.User
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDateTime

interface BudgetRepository {
    val state: StateFlow<BudgetState>
    fun saveExpense(expense: Expense)
    fun deleteExpense(id: String)
    fun addBill(bill: Bill)
    fun toggleBill(id: String)
    fun updateMonthBudget(value: Double)
    fun updateCategoryBudget(id: String, value: Double)
    fun applyOnboarding(monthBudget: Double, resetDay: Int)
    fun updateCurrency(code: String)
}

// In-memory seed store. Swap for a DataStore/Room impl behind this interface for persistence.
class SeedBudgetRepository : BudgetRepository {
    private val _state = MutableStateFlow(Seed.state())
    override val state: StateFlow<BudgetState> = _state.asStateFlow()

    override fun saveExpense(expense: Expense) = _state.update { s ->
        val exists = s.expenses.any { it.id == expense.id }
        val next = if (exists) s.expenses.map { if (it.id == expense.id) expense else it }
        else listOf(expense) + s.expenses
        s.copy(expenses = next.toPersistentList())
    }

    override fun deleteExpense(id: String) = _state.update { s ->
        s.copy(expenses = s.expenses.filter { it.id != id }.toPersistentList())
    }

    override fun addBill(bill: Bill) = _state.update { s ->
        s.copy(bills = (s.bills + bill).toPersistentList())
    }

    override fun toggleBill(id: String) = _state.update { s ->
        s.copy(bills = s.bills.map { if (it.id == id) it.copy(paid = !it.paid) else it }.toPersistentList())
    }

    override fun updateMonthBudget(value: Double) = _state.update { it.copy(monthBudget = value) }

    override fun updateCategoryBudget(id: String, value: Double) = _state.update { s ->
        s.copy(categories = s.categories.map { if (it.id == id) it.copy(budget = value) else it }.toPersistentList())
    }

    override fun applyOnboarding(monthBudget: Double, resetDay: Int) = _state.update { s ->
        s.copy(monthBudget = monthBudget, user = s.user.copy(resetDay = resetDay))
    }

    override fun updateCurrency(code: String) = _state.update { it.copy(user = it.user.copy(currency = code)) }
}

private object Seed {
    private val today = LocalDateTime.of(2026, 5, 18, 12, 0)

    private fun d(offset: Int, hour: Int = 12, minute: Int = 0) =
        today.minusDays(offset.toLong()).withHour(hour).withMinute(minute)

    fun state() = BudgetState(
        today = today,
        monthBudget = 1850.0,
        categories = persistentListOf(
            Category("food", "Food", 480.0),
            Category("transport", "Transport", 220.0),
            Category("bills", "Bills", 520.0),
            Category("shopping", "Shopping", 280.0),
            Category("health", "Health", 150.0),
            Category("entertainment", "Entertainment", 150.0),
            Category("other", "Other", 50.0),
        ),
        expenses = persistentListOf(
            Expense("e1", 14.20, "food", "Morning oat latte", d(0, 8, 12)),
            Expense("e2", 28.50, "transport", "Gas — Shell", d(0, 11, 30)),
            Expense("e3", 42.80, "food", "Trader Joe's", d(1, 18, 45)),
            Expense("e4", 9.99, "entertainment", "Spotify", d(1, 9, 0)),
            Expense("e5", 62.00, "shopping", "Linen shirt", d(2, 15, 22)),
            Expense("e6", 18.40, "food", "Lunch — Sweetgreen", d(2, 12, 50)),
            Expense("e7", 24.00, "health", "Yoga drop-in", d(3, 7, 0)),
            Expense("e8", 32.10, "transport", "Lyft to airport", d(4, 16, 10)),
            Expense("e9", 11.25, "food", "Coffee + croissant", d(4, 8, 30)),
            Expense("e10", 89.40, "food", "Dinner — Aurora", d(5, 19, 15)),
            Expense("e11", 15.00, "entertainment", "Movie ticket", d(6, 20, 0)),
            Expense("e12", 47.20, "food", "Whole Foods", d(7, 17, 40)),
            Expense("e13", 120.00, "bills", "Internet — Comcast", d(8, 9, 0)),
            Expense("e14", 36.80, "shopping", "Cat tree base", d(9, 14, 0)),
            Expense("e15", 22.10, "food", "Brunch — Mira", d(10, 11, 0)),
            Expense("e16", 14.50, "transport", "Subway pass top-up", d(11, 8, 0)),
            Expense("e17", 480.00, "bills", "Rent (utilities)", d(12, 9, 0)),
            Expense("e18", 18.00, "entertainment", "Book — Braiding Sweetgrass", d(13, 16, 30)),
            Expense("e19", 9.50, "food", "Bakery", d(14, 9, 30)),
            Expense("e20", 41.20, "health", "Pharmacy", d(15, 13, 0)),
            Expense("e21", 36.40, "food", "Dinner with Mae", d(16, 20, 0)),
            Expense("e22", 12.80, "other", "Print shop", d(17, 14, 0)),
        ),
        bills = persistentListOf(
            Bill("b1", "Rent", 1450.0, 1, true, "home"),
            Bill("b2", "Internet", 75.0, 8, true, "wifi"),
            Bill("b3", "Spotify Family", 16.99, 12, true, "music"),
            Bill("b4", "Electric", 64.0, 22, false, "zap"),
            Bill("b5", "Phone", 45.0, 25, false, "phone"),
            Bill("b6", "Climbing gym", 89.0, 28, false, "mountain"),
        ),
        user = User(),
    )
}
