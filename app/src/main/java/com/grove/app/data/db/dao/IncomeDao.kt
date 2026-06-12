package com.grove.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.grove.app.data.db.entity.IncomeEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.util.UUID

@Dao
interface IncomeDao {
    @Query("SELECT * FROM incomes ORDER BY occurredAt DESC")
    fun observeAll(): Flow<List<IncomeEntity>>

    @Query("SELECT * FROM incomes WHERE id = :id")
    suspend fun getById(id: UUID): IncomeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: IncomeEntity)

    @Query("DELETE FROM incomes WHERE id = :id")
    suspend fun delete(id: UUID)
}
