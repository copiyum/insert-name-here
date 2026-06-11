package com.grove.app.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.grove.app.data.model.BillPayment
import java.time.Instant
import java.util.UUID

@Entity(
    tableName = "bill_payments",
    foreignKeys = [
        ForeignKey(
            entity = BillEntity::class,
            parentColumns = ["id"],
            childColumns = ["billId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("billId"),
        Index(value = ["periodYear", "periodMonth"]),
        Index("dueAt"),
    ],
)
data class BillPaymentEntity(
    @PrimaryKey val id: UUID,
    val billId: UUID,
    val periodYear: Int,
    val periodMonth: Int?,
    val periodStart: Instant,
    val periodEnd: Instant,
    val dueAt: Instant,
    val paidAt: Instant?,
    val amountPaidMinor: Long?,
    val note: String,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    fun toDomain() =
        BillPayment(
            id = id,
            billId = billId,
            periodYear = periodYear,
            periodMonth = periodMonth,
            periodStart = periodStart,
            periodEnd = periodEnd,
            dueAt = dueAt,
            paidAt = paidAt,
            amountPaidMinor = amountPaidMinor,
            note = note,
        )
}
