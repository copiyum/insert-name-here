package com.grove.app.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.grove.app.data.model.MonthlyBudget
import java.time.Instant
import java.util.UUID

@Entity(
    tableName = "monthly_budgets",
    indices = [Index(value = ["periodYear", "periodMonth"], unique = true)],
)
data class MonthlyBudgetEntity(
    @PrimaryKey val id: UUID,
    val periodYear: Int,
    val periodMonth: Int,
    val totalMinor: Long,
    val currencyCode: String,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    fun toDomain() =
        MonthlyBudget(
            id = id,
            periodYear = periodYear,
            periodMonth = periodMonth,
            totalMinor = totalMinor,
            currencyCode = currencyCode,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
}
