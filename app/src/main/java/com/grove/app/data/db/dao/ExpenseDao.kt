package com.grove.app.data.db.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.grove.app.data.db.entity.ExpenseEntity
import com.grove.app.data.model.CategoryKind
import kotlinx.coroutines.flow.Flow
import java.util.UUID

data class ExpenseWithCategoryRow(
    @Embedded val expense: ExpenseEntity,
    val categoryName: String?,
    val categoryIconKey: String?,
    val categoryKind: CategoryKind?,
)

@Dao
interface ExpenseDao {
    @Query(
        """
        SELECT e.*, c.displayName AS categoryName, c.iconKey AS categoryIconKey, c.kind AS categoryKind
        FROM expenses e
        LEFT JOIN categories c ON c.id = e.categoryId AND c.archivedAt IS NULL
        ORDER BY e.occurredAt DESC
        """,
    )
    fun observeAllWithCategory(): Flow<List<ExpenseWithCategoryRow>>

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getById(id: UUID): ExpenseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun delete(id: UUID)
}
