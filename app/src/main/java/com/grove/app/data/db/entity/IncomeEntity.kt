package com.grove.app.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.grove.app.data.model.BillFrequency
import com.grove.app.data.model.Income
import java.time.Instant
import java.util.UUID

@Entity(
    tableName = "incomes",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [
        Index("occurredAt"),
        Index("categoryId"),
        Index(value = ["isRecurring", "nextExpectedAt"]),
    ],
)
data class IncomeEntity(
    @PrimaryKey val id: UUID,
    val amountMinor: Long,
    val currencyCode: String,
    val source: String,
    val categoryId: UUID,
    val occurredAt: Instant,
    val isRecurring: Boolean,
    val recurrenceFrequency: BillFrequency?,
    val nextExpectedAt: Instant?,
    val note: String,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    fun toDomain() =
        Income(
            id = id,
            amountMinor = amountMinor,
            currencyCode = currencyCode,
            source = source,
            categoryId = categoryId,
            occurredAt = occurredAt,
            isRecurring = isRecurring,
            recurrenceFrequency = recurrenceFrequency,
            nextExpectedAt = nextExpectedAt,
            note = note,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
}
