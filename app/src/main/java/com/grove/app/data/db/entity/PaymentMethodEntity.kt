package com.grove.app.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.grove.app.data.model.PaymentKind
import com.grove.app.data.model.PaymentMethod
import java.time.Instant
import java.util.UUID

@Entity(tableName = "payment_methods", indices = [Index("archivedAt")])
data class PaymentMethodEntity(
    @PrimaryKey val id: UUID,
    val displayName: String,
    val kind: PaymentKind,
    val last4: String?,
    val colorHex: String,
    val iconKey: String,
    val sortOrder: Int = 0,
    val archivedAt: Instant? = null,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    fun toDomain() =
        PaymentMethod(
            id = id,
            displayName = displayName,
            kind = kind,
            last4 = last4,
            colorHex = colorHex,
            iconKey = iconKey,
            sortOrder = sortOrder,
            archivedAt = archivedAt,
        )
}
