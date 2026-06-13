package com.grove.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.grove.app.data.db.entity.NotificationSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notification_settings WHERE id = 1")
    fun observeSettings(): Flow<NotificationSettingsEntity?>

    @Query("SELECT * FROM notification_settings WHERE id = 1")
    suspend fun getSettings(): NotificationSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSettings(entity: NotificationSettingsEntity)
}
