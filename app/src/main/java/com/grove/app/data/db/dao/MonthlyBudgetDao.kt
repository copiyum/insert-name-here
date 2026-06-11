package com.grove.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.grove.app.data.db.entity.MonthlyBudgetEntity
import com.grove.app.data.db.entity.MonthlyCategoryBudgetEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface MonthlyBudgetDao {
    @Query("SELECT * FROM monthly_budgets WHERE periodYear = :year AND periodMonth = :month LIMIT 1")
    fun observeForPeriod(
        year: Int,
        month: Int,
    ): Flow<MonthlyBudgetEntity?>

    @Query("SELECT * FROM monthly_budgets ORDER BY periodYear DESC, periodMonth DESC")
    fun observeAll(): Flow<List<MonthlyBudgetEntity>>

    @Query("SELECT * FROM monthly_category_budgets WHERE monthlyBudgetId = :monthlyBudgetId")
    fun observeCategoryBudgets(monthlyBudgetId: UUID): Flow<List<MonthlyCategoryBudgetEntity>>

    @Query("SELECT * FROM monthly_category_budgets WHERE monthlyBudgetId = :monthlyBudgetId")
    suspend fun getCategoryBudgets(monthlyBudgetId: UUID): List<MonthlyCategoryBudgetEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBudget(entity: MonthlyBudgetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCategoryBudgets(entities: List<MonthlyCategoryBudgetEntity>)
}
