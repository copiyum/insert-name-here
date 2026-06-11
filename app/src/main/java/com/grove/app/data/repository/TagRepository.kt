package com.grove.app.data.repository

import com.grove.app.data.db.dao.TagDao
import com.grove.app.data.model.Tag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TagRepository(
    private val dao: TagDao,
) {
    fun observeAll(): Flow<List<Tag>> = dao.observeAll().map { list -> list.map { it.toDomain() } }
}
