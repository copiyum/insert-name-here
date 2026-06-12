package com.grove.app.data.repository

import com.grove.app.data.db.dao.NotificationDao
import com.grove.app.data.db.entity.NotificationSettingsEntity
import com.grove.app.data.model.NotificationSettings
import com.grove.app.data.model.ScheduledNotification
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant

class NotificationRepository(
    private val dao: NotificationDao,
) {
    fun observeSettings(): Flow<NotificationSettings?> = dao.observeSettings().map { it?.toDomain() }

    fun observePending(): Flow<List<ScheduledNotification>> = dao.observePending().map { list -> list.map { it.toDomain() } }

    suspend fun getSettings(): NotificationSettings? = dao.getSettings()?.toDomain()

    suspend fun upsertSettings(settings: NotificationSettings) {
        val now = Instant.now()
        val existing = dao.getSettings()
        dao.upsertSettings(
            NotificationSettingsEntity.fromDomain(
                settings,
                createdAt = existing?.createdAt ?: now,
                updatedAt = now,
            ),
        )
    }
}
