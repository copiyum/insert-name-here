package com.grove.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.grove.app.data.db.entity.NotificationSettingsEntity
import com.grove.app.data.db.entity.ScheduledNotificationEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notification_settings WHERE id = 1")
    fun observeSettings(): Flow<NotificationSettingsEntity?>

    @Query("SELECT * FROM notification_settings WHERE id = 1")
    suspend fun getSettings(): NotificationSettingsEntity?

    @Query("SELECT * FROM scheduled_notifications WHERE firedAt IS NULL ORDER BY triggerAt")
    fun observePending(): Flow<List<ScheduledNotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSettings(entity: NotificationSettingsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertScheduled(entity: ScheduledNotificationEntity)

    @Query("DELETE FROM scheduled_notifications WHERE id = :id")
    suspend fun deleteScheduled(id: UUID)
}
