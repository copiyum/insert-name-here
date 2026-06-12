package com.grove.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.grove.app.data.db.entity.BillEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.util.UUID

@Dao
interface BillDao {
    @Query("SELECT * FROM bills WHERE isActive = 1 ORDER BY name")
    fun observeActive(): Flow<List<BillEntity>>

    @Query("SELECT * FROM bills ORDER BY name")
    fun observeAll(): Flow<List<BillEntity>>

    @Query("SELECT * FROM bills WHERE id = :id")
    suspend fun getById(id: UUID): BillEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: BillEntity)

    @Query("UPDATE bills SET isActive = :active, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setActive(
        id: UUID,
        active: Boolean,
        updatedAt: Instant,
    )

    @Query("UPDATE bills SET currencyCode = :currencyCode, updatedAt = :updatedAt")
    suspend fun updateCurrencyCode(
        currencyCode: String,
        updatedAt: Instant,
    )
}
