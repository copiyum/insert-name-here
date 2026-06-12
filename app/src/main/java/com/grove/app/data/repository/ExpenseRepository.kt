package com.grove.app.data.repository

import com.grove.app.data.db.ExpenseLite
import com.grove.app.data.db.dao.ExpenseDao
import com.grove.app.data.db.entity.ExpenseEntity
import com.grove.app.data.model.CategoryKind
import com.grove.app.data.model.Expense
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.util.UUID

class ExpenseRepository(
    private val dao: ExpenseDao,
) {
    /** Expenses pre-joined with their category in SQL, ready for [ExpenseLite] consumers. */
    fun observeAllLite(): Flow<List<ExpenseLite>> =
        dao.observeAllWithCategory().map { rows ->
            rows.map { row ->
                ExpenseLite(
                    id = row.expense.id,
                    amountMinor = row.expense.amountMinor,
                    currencyCode = row.expense.currencyCode,
                    categoryId = row.expense.categoryId,
                    categoryName = row.categoryName ?: "Other",
                    iconKey = row.categoryIconKey ?: "more_horiz",
                    categoryKind = row.categoryKind ?: CategoryKind.expense,
                    note = row.expense.note,
                    occurredAt = row.expense.occurredAt,
                )
            }
        }

    suspend fun upsert(expense: Expense) {
        val now = Instant.now()
        val existing = dao.getById(expense.id)
        dao.upsert(
            ExpenseEntity(
                id = expense.id,
                amountMinor = expense.amountMinor,
                currencyCode = expense.currencyCode,
                categoryId = expense.categoryId,
                paymentMethodId = expense.paymentMethodId,
                note = expense.note,
                occurredAt = expense.occurredAt,
                createdAt = existing?.createdAt ?: now,
                updatedAt = now,
            ),
        )
    }

    suspend fun delete(id: UUID) = dao.delete(id)

    suspend fun get(id: UUID): Expense? = dao.getById(id)?.toDomain()

    suspend fun updateCurrencyCode(currencyCode: String) {
        dao.updateCurrencyCode(currencyCode, Instant.now())
    }
}
