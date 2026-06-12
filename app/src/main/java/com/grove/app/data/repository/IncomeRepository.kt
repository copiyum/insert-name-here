package com.grove.app.data.repository

import com.grove.app.data.db.dao.IncomeDao
import com.grove.app.data.db.entity.IncomeEntity
import com.grove.app.data.model.Income
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.util.UUID

class IncomeRepository(
    private val dao: IncomeDao,
) {
    fun observeAll(): Flow<List<Income>> = dao.observeAll().mapList { it.toDomain() }

    suspend fun upsert(income: Income) {
        val now = Instant.now()
        val existing = dao.getById(income.id)
        dao.upsert(
            IncomeEntity(
                id = income.id,
                amountMinor = income.amountMinor,
                currencyCode = income.currencyCode,
                source = income.source,
                categoryId = income.categoryId,
                occurredAt = income.occurredAt,
                isRecurring = income.isRecurring,
                recurrenceFrequency = income.recurrenceFrequency,
                nextExpectedAt = income.nextExpectedAt,
                note = income.note,
                createdAt = existing?.createdAt ?: income.createdAt,
                updatedAt = now,
            ),
        )
    }

    suspend fun delete(id: UUID) = dao.delete(id)
}
