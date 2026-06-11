package com.grove.app.data.repository

import com.grove.app.data.db.dao.UserProfileDao
import com.grove.app.data.db.entity.UserProfileEntity
import com.grove.app.data.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant

class UserRepository(
    private val dao: UserProfileDao,
) {
    fun observe(): Flow<UserProfile?> = dao.observe().map { it?.toDomain() }

    suspend fun get(): UserProfile? = dao.get()?.toDomain()

    suspend fun upsert(profile: UserProfile) {
        val now = Instant.now()
        dao.upsert(
            UserProfileEntity.fromDomain(
                profile,
                createdAt = dao.get()?.createdAt ?: now,
                updatedAt = now,
            ),
        )
    }
}
