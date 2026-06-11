package com.grove.app.data.repository

import com.grove.app.data.db.dao.CategoryDao
import com.grove.app.data.model.Category
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.util.UUID

class CategoryRepository(
    private val dao: CategoryDao,
) {
    fun observeActive(): Flow<List<Category>> = dao.observeActive().map { list -> list.map { it.toDomain() } }

    fun observeAll(): Flow<List<Category>> = dao.observeAll().map { list -> list.map { it.toDomain() } }

    suspend fun archive(id: UUID) = dao.archive(id, Instant.now())
}
