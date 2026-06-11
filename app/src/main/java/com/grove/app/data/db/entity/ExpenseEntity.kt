package com.grove.app.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.grove.app.data.model.Expense
import java.time.Instant
import java.util.UUID

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT,
        ),
        ForeignKey(
            entity = PaymentMethodEntity::class,
            parentColumns = ["id"],
            childColumns = ["paymentMethodId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index("occurredAt"),
        Index("categoryId"),
        Index("paymentMethodId"),
        Index(value = ["occurredAt", "categoryId"]),
    ],
)
data class ExpenseEntity(
    @PrimaryKey val id: UUID,
    val amountMinor: Long,
    val currencyCode: String,
    val categoryId: UUID,
    val paymentMethodId: UUID?,
    val note: String,
    val occurredAt: Instant,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    fun toDomain() =
        Expense(
            id = id,
            amountMinor = amountMinor,
            currencyCode = currencyCode,
            categoryId = categoryId,
            paymentMethodId = paymentMethodId,
            note = note,
            occurredAt = occurredAt,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
}
