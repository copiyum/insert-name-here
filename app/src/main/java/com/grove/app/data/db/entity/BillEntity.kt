package com.grove.app.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.grove.app.data.model.Bill
import com.grove.app.data.model.BillFrequency
import java.time.Instant
import java.util.UUID

@Entity(tableName = "bills", indices = [Index("isActive")])
data class BillEntity(
    @PrimaryKey val id: UUID,
    val name: String,
    val amountMinor: Long,
    val currencyCode: String,
    val frequency: BillFrequency,
    val dueDay: Int?,
    val dueWeekday: Int?,
    val startDate: Instant,
    val endDate: Instant?,
    val iconKey: String,
    val isActive: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    fun toDomain() =
        Bill(
            id = id,
            name = name,
            amountMinor = amountMinor,
            currencyCode = currencyCode,
            frequency = frequency,
            dueDay = dueDay,
            dueWeekday = dueWeekday,
            startDate = startDate,
            endDate = endDate,
            iconKey = iconKey,
            isActive = isActive,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
}
