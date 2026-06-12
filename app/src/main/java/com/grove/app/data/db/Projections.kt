package com.grove.app.data.db

import androidx.compose.runtime.Immutable
import com.grove.app.data.model.CategoryKind
import java.time.Instant
import java.util.UUID

@Immutable
data class CategoryLite(
    val id: UUID,
    val displayName: String,
    val iconKey: String,
    val kind: CategoryKind,
)

@Immutable
data class ExpenseLite(
    val id: UUID,
    val amountMinor: Long,
    val currencyCode: String,
    val categoryId: UUID,
    val categoryName: String,
    val iconKey: String,
    val categoryKind: CategoryKind,
    val note: String,
    val occurredAt: Instant,
)

@Immutable
data class BillLite(
    val id: UUID,
    val name: String,
    val amountMinor: Long,
    val currencyCode: String,
    val iconKey: String,
    val dueDay: Int,
    val paid: Boolean,
)

@Immutable
data class IncomeLite(
    val id: UUID,
    val amountMinor: Long,
    val currencyCode: String,
    val occurredAt: Instant,
    val source: String,
)

@Immutable
data class CategoryBudgetLite(
    val categoryId: UUID,
    val amountMinor: Long,
)
