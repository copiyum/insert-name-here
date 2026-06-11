package com.grove.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.grove.app.data.model.UserProfile
import java.time.Instant

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val name: String,
    val resetDay: Int,
    val currencyCode: String,
    val onboardingCompleted: Boolean,
    val onboardingCompletedAt: Instant?,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    fun toDomain() =
        UserProfile(
            name = name,
            resetDay = resetDay,
            currencyCode = currencyCode,
            onboardingCompleted = onboardingCompleted,
            onboardingCompletedAt = onboardingCompletedAt,
        )

    companion object {
        fun fromDomain(
            p: UserProfile,
            createdAt: Instant,
            updatedAt: Instant,
        ) = UserProfileEntity(
            id = 1,
            name = p.name,
            resetDay = p.resetDay,
            currencyCode = p.currencyCode,
            onboardingCompleted = p.onboardingCompleted,
            onboardingCompletedAt = p.onboardingCompletedAt,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }
}
