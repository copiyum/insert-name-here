package com.grove.app.data.db.seed

import com.grove.app.data.db.GroveDatabase
import com.grove.app.data.db.entity.CategoryEntity
import com.grove.app.data.db.entity.NotificationSettingsEntity
import com.grove.app.data.db.entity.PaymentMethodEntity
import com.grove.app.data.db.entity.UserProfileEntity
import com.grove.app.data.model.CategoryKind
import com.grove.app.data.model.PaymentKind
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
                    currencyCode = "USD",
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
        db.paymentMethodDao().insertAll(defaultPaymentMethods(now))
    }

    private fun defaultCategories(now: Instant): List<CategoryEntity> =
        listOf(
            category("food", "Food", "restaurant", "#A47148", CategoryKind.expense, 0, now),
            category("transport", "Transport", "car", "#4A6F49", CategoryKind.expense, 1, now),
            category("bills", "Bills", "receipt", "#6B6A52", CategoryKind.expense, 2, now),
            category("shopping", "Shopping", "shopping_bag", "#8D6E5A", CategoryKind.expense, 3, now),
            category("health", "Health", "favorite", "#8C5A52", CategoryKind.expense, 4, now),
            category("entertainment", "Entertainment", "sports_esports", "#5A7080", CategoryKind.expense, 5, now),
            category("other", "Other", "more_horiz", "#6B6F66", CategoryKind.expense, 6, now),
            category("income", "Income", "trending_up", "#3A6940", CategoryKind.income, 7, now),
        )

    private fun category(
        idKey: String,
        name: String,
        iconKey: String,
        colorHex: String,
        kind: CategoryKind,
        sort: Int,
        now: Instant,
    ): CategoryEntity {
        val id = UUID.nameUUIDFromBytes("grove.category.$idKey".toByteArray())
        return CategoryEntity(
            id = id,
            displayName = name,
            iconKey = iconKey,
            colorHex = colorHex,
            kind = kind,
            sortOrder = sort,
            archivedAt = null,
            createdAt = now,
            updatedAt = now,
        )
    }

    private fun defaultPaymentMethods(now: Instant): List<PaymentMethodEntity> =
        listOf(
            PaymentMethodEntity(
                id = UUID.nameUUIDFromBytes("grove.paymentmethod.cash".toByteArray()),
                displayName = "Cash",
                kind = PaymentKind.cash,
                last4 = null,
                colorHex = "#DAD7CD",
                iconKey = "wallet",
                sortOrder = 0,
                archivedAt = null,
                createdAt = now,
                updatedAt = now,
            ),
        )
}
