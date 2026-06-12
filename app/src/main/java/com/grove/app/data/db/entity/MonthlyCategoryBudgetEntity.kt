package com.grove.app.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.grove.app.data.model.MonthlyCategoryBudget
import java.util.UUID

@Entity(
    tableName = "monthly_category_budgets",
    foreignKeys = [
        ForeignKey(
            entity = MonthlyBudgetEntity::class,
            parentColumns = ["id"],
            childColumns = ["monthlyBudgetId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [
        Index(value = ["monthlyBudgetId", "categoryId"], unique = true),
        Index("categoryId"),
    ],
)
data class MonthlyCategoryBudgetEntity(
    @PrimaryKey val id: UUID,
    val monthlyBudgetId: UUID,
    val categoryId: UUID,
    val amountMinor: Long,
) {
    fun toDomain() =
        MonthlyCategoryBudget(
            id = id,
            monthlyBudgetId = monthlyBudgetId,
            categoryId = categoryId,
            amountMinor = amountMinor,
        )
}
