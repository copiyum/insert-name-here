package com.grove.app.feature.home

import android.app.Application
import com.grove.app.data.UserPreferencesRepository
import com.grove.app.data.db.BudgetStateReactor
import com.grove.app.data.db.GroveDatabase
import com.grove.app.data.repository.BillRepository
import com.grove.app.data.repository.BudgetRepository
import com.grove.app.data.repository.CategoryRepository
import com.grove.app.data.repository.ExpenseRepository
import com.grove.app.data.repository.IncomeRepository
import com.grove.app.data.repository.NotificationRepository
import com.grove.app.data.repository.UserRepository
import com.grove.app.data.userPreferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

internal object GroveAppGraph {
    @Volatile private var db: GroveDatabase? = null

    @Volatile private var reactor: BudgetStateReactor? = null

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

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
                scope = appScope,
                userRepo = userRepo(application),
                categoryRepo = categoryRepo(application),
                expenseRepo = expenseRepo(application),
                billRepo = billRepo(application),
                incomeRepo = incomeRepo(application),
                budgetRepo = budgetRepo(application),
            ).also { reactor = it }
        }
}
