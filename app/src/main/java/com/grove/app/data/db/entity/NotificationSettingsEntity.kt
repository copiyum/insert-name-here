package com.grove.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.grove.app.data.model.NotificationSettings
import java.time.Instant

@Entity(tableName = "notification_settings")
data class NotificationSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val dailySafeSpendEnabled: Boolean,
    val dailySafeSpendTimeMinutes: Int,
    val billAlertsEnabled: Boolean,
    val billAlertsDaysBefore: Int,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    fun toDomain() =
        NotificationSettings(
            dailySafeSpendEnabled = dailySafeSpendEnabled,
            dailySafeSpendTimeMinutes = dailySafeSpendTimeMinutes,
            billAlertsEnabled = billAlertsEnabled,
            billAlertsDaysBefore = billAlertsDaysBefore,
        )

    companion object {
        fun defaults() =
            NotificationSettings(
                dailySafeSpendEnabled = true,
                dailySafeSpendTimeMinutes = 480,
                billAlertsEnabled = true,
                billAlertsDaysBefore = 3,
            )
    }
}
