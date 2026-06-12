package com.grove.app.data.model

import androidx.compose.runtime.Immutable
import java.time.Instant
import java.util.UUID

enum class CategoryKind { expense, income, both }

enum class BillFrequency { monthly, weekly, biweekly, quarterly, yearly, one_off }

enum class NotificationKind { daily_safe_spend, bill_due }

@Immutable
data class UserProfile(
    val name: String,
    val resetDay: Int,
    val currencyCode: String,
    val onboardingCompleted: Boolean = false,
    val onboardingCompletedAt: Instant? = null,
)

@Immutable
data class Category(
    val id: UUID,
    val displayName: String,
    val iconKey: String,
    val kind: CategoryKind,
    val sortOrder: Int = 0,
    val archivedAt: Instant? = null,
)

@Immutable
data class Expense(
    val id: UUID,
    val amountMinor: Long,
    val currencyCode: String,
    val categoryId: UUID,
    val note: String,
    val occurredAt: Instant,
    val createdAt: Instant,
    val updatedAt: Instant,
)

@Immutable
data class ExpenseInput(
    val id: UUID?,
    val amountMinor: Long,
    val currencyCode: String,
    val categoryId: UUID,
    val note: String,
    val occurredAt: Instant,
)

@Immutable
data class Bill(
    val id: UUID,
    val name: String,
    val amountMinor: Long,
    val currencyCode: String,
    val frequency: BillFrequency,
    val dueDay: Int?,
    val dueWeekday: Int?,
    val startDate: Instant,
    val endDate: Instant?,
    val iconKey: String,
    val isActive: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
)

@Immutable
data class BillInput(
    val name: String,
    val amountMinor: Long,
    val currencyCode: String,
    val frequency: BillFrequency,
    val dueDay: Int?,
    val dueWeekday: Int?,
    val startDate: Instant,
    val iconKey: String,
)

@Immutable
data class BillPayment(
    val id: UUID,
    val billId: UUID,
    val periodYear: Int,
    val periodMonth: Int?,
    val periodStart: Instant,
    val periodEnd: Instant,
    val dueAt: Instant,
    val paidAt: Instant?,
    val amountPaidMinor: Long?,
    val note: String,
)

@Immutable
data class Income(
    val id: UUID,
    val amountMinor: Long,
    val currencyCode: String,
    val source: String,
    val categoryId: UUID,
    val occurredAt: Instant,
    val isRecurring: Boolean,
    val recurrenceFrequency: BillFrequency?,
    val nextExpectedAt: Instant?,
    val note: String,
    val createdAt: Instant,
    val updatedAt: Instant,
)

@Immutable
data class MonthlyBudget(
    val id: UUID,
    val periodYear: Int,
    val periodMonth: Int,
    val totalMinor: Long,
    val currencyCode: String,
    val createdAt: Instant,
    val updatedAt: Instant,
)

@Immutable
data class MonthlyCategoryBudget(
    val id: UUID,
    val monthlyBudgetId: UUID,
    val categoryId: UUID,
    val amountMinor: Long,
)

@Immutable
data class NotificationSettings(
    val dailySafeSpendEnabled: Boolean,
    val dailySafeSpendTimeMinutes: Int,
    val billAlertsEnabled: Boolean,
    val billAlertsDaysBefore: Int,
)

@Immutable
data class ScheduledNotification(
    val id: UUID,
    val kind: NotificationKind,
    val triggerAt: Instant,
    val relatedBillId: UUID?,
    val firedAt: Instant?,
    val createdAt: Instant,
)
