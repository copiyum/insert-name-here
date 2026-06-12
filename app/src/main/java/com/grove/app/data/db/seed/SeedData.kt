package com.grove.app.data.db.seed

import com.grove.app.data.db.GroveDatabase
import com.grove.app.data.db.entity.CategoryEntity
import com.grove.app.data.db.entity.NotificationSettingsEntity
import com.grove.app.data.db.entity.UserProfileEntity
import com.grove.app.data.model.CategoryKind
import com.grove.app.data.model.UserProfile
import java.time.Instant
import java.util.UUID

object SeedData {
    suspend fun seed(db: GroveDatabase) {
        val now = Instant.now()
        db.userProfileDao().upsert(
            UserProfileEntity.fromDomain(
                UserProfile(
                    name = "Mae",
                    resetDay = 1,
                    currencyCode = "INR",
                    onboardingCompleted = false,
                ),
                createdAt = now,
                updatedAt = now,
            ),
        )
        db.notificationDao().upsertSettings(
            NotificationSettingsEntity(
                dailySafeSpendEnabled = true,
                dailySafeSpendTimeMinutes = 480,
                billAlertsEnabled = true,
                billAlertsDaysBefore = 3,
                createdAt = now,
                updatedAt = now,
            ),
        )
        db.categoryDao().insertAll(defaultCategories(now))
    }

    private fun defaultCategories(now: Instant): List<CategoryEntity> =
        listOf(
            category("food", "Food", "restaurant", CategoryKind.expense, 0, now),
            category("transport", "Transport", "car", CategoryKind.expense, 1, now),
            category("bills", "Bills", "receipt", CategoryKind.expense, 2, now),
            category("shopping", "Shopping", "shopping_bag", CategoryKind.expense, 3, now),
            category("health", "Health", "favorite", CategoryKind.expense, 4, now),
            category("entertainment", "Entertainment", "sports_esports", CategoryKind.expense, 5, now),
            category("other", "Other", "more_horiz", CategoryKind.expense, 6, now),
            category("income", "Income", "trending_up", CategoryKind.income, 7, now),
        )

    private fun category(
        idKey: String,
        name: String,
        iconKey: String,
        kind: CategoryKind,
        sort: Int,
        now: Instant,
    ): CategoryEntity {
        val id = UUID.nameUUIDFromBytes("grove.category.$idKey".toByteArray())
        return CategoryEntity(
            id = id,
            displayName = name,
            iconKey = iconKey,
            kind = kind,
            sortOrder = sort,
            archivedAt = null,
            createdAt = now,
            updatedAt = now,
        )
    }

}
