package com.grove.app.data.repository

import com.grove.app.data.db.dao.ExpenseDao
import com.grove.app.data.db.entity.ExpenseEntity
import com.grove.app.data.model.Expense
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.util.UUID

class ExpenseRepository(
    private val dao: ExpenseDao,
) {
    fun observeAll(): Flow<List<Expense>> = dao.observeAll().map { list -> list.map { it.toDomain() } }

    fun observeBetween(
        start: Instant,
        end: Instant,
    ): Flow<List<Expense>> = dao.observeBetween(start, end).map { list -> list.map { it.toDomain() } }

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
