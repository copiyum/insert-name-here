package com.grove.app.data.repository

import com.grove.app.data.db.dao.MonthlyBudgetDao
import com.grove.app.data.db.entity.MonthlyBudgetEntity
import com.grove.app.data.db.entity.MonthlyCategoryBudgetEntity
import com.grove.app.data.model.MonthlyBudget
import com.grove.app.data.model.MonthlyCategoryBudget
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.util.UUID

class BudgetRepository(
    private val dao: MonthlyBudgetDao,
) {
    fun observeForPeriod(
        year: Int,
        month: Int,
    ): Flow<MonthlyBudget?> = dao.observeForPeriod(year, month).map { it?.toDomain() }

    fun observeAll(): Flow<List<MonthlyBudget>> = dao.observeAll().mapList { it.toDomain() }

    fun observeCategoryBudgets(monthlyBudgetId: UUID): Flow<List<MonthlyCategoryBudget>> =
        dao.observeCategoryBudgets(monthlyBudgetId).mapList { it.toDomain() }

    fun observeAllCategoryBudgets(): Flow<List<MonthlyCategoryBudget>> =
        dao.observeAllCategoryBudgets().mapList { it.toDomain() }

    suspend fun getForPeriod(
        year: Int,
        month: Int,
    ): MonthlyBudget? = dao.getForPeriod(year, month)?.toDomain()

    suspend fun getCategoryBudgets(monthlyBudgetId: UUID): List<MonthlyCategoryBudget> =
        dao.getCategoryBudgets(monthlyBudgetId).map { it.toDomain() }

    suspend fun getCategoryBudget(
        monthlyBudgetId: UUID,
        categoryId: UUID,
    ): MonthlyCategoryBudget? = dao.getCategoryBudget(monthlyBudgetId, categoryId)?.toDomain()

    suspend fun updateCurrencyCode(currencyCode: String) {
        dao.updateCurrencyCode(currencyCode, Instant.now())
    }

    suspend fun upsert(
        budget: MonthlyBudget,
        categoryBudgets: List<MonthlyCategoryBudget>,
    ) {
        val now = Instant.now()
        val existing = dao.getForPeriod(budget.periodYear, budget.periodMonth)
        dao.upsertBudget(
            MonthlyBudgetEntity(
                id = budget.id,
                periodYear = budget.periodYear,
                periodMonth = budget.periodMonth,
                totalMinor = budget.totalMinor,
                currencyCode = budget.currencyCode,
                createdAt = existing?.createdAt ?: budget.createdAt,
                updatedAt = now,
            ),
        )
        if (categoryBudgets.isNotEmpty()) {
            dao.upsertCategoryBudgets(
                categoryBudgets.map { cb ->
                    MonthlyCategoryBudgetEntity(
                        id = cb.id,
                        monthlyBudgetId = cb.monthlyBudgetId,
                        categoryId = cb.categoryId,
                        amountMinor = cb.amountMinor,
                    )
                },
            )
        }
    }
}
