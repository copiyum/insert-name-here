package com.grove.app.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.grove.app.data.model.NotificationKind
import com.grove.app.data.model.ScheduledNotification
import java.time.Instant
import java.util.UUID

@Entity(
    tableName = "scheduled_notifications",
    foreignKeys = [
        ForeignKey(
            entity = BillEntity::class,
            parentColumns = ["id"],
            childColumns = ["relatedBillId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["triggerAt", "firedAt"])],
)
data class ScheduledNotificationEntity(
    @PrimaryKey val id: UUID,
    val kind: NotificationKind,
    val triggerAt: Instant,
    val relatedBillId: UUID?,
    val firedAt: Instant?,
    val createdAt: Instant,
) {
    fun toDomain() =
        ScheduledNotification(
            id = id,
            kind = kind,
            triggerAt = triggerAt,
            relatedBillId = relatedBillId,
            firedAt = firedAt,
            createdAt = createdAt,
        )
}
