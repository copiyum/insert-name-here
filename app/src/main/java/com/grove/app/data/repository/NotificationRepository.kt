package com.grove.app.data.repository

import com.grove.app.data.db.dao.NotificationDao
import com.grove.app.data.model.NotificationSettings
import com.grove.app.data.model.ScheduledNotification
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NotificationRepository(
    private val dao: NotificationDao,
) {
    fun observeSettings(): Flow<NotificationSettings?> = dao.observeSettings().map { it?.toDomain() }

    fun observePending(): Flow<List<ScheduledNotification>> = dao.observePending().map { list -> list.map { it.toDomain() } }
}
