package com.grove.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.grove.app.data.db.entity.TagEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface TagDao {
    @Query("SELECT * FROM tags ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<TagEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun upsert(entity: TagEntity)

    @Query("SELECT * FROM tags WHERE id = :id")
    suspend fun getById(id: UUID): TagEntity?
}
