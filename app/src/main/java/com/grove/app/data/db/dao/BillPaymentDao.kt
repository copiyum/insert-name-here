package com.grove.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.grove.app.data.db.entity.BillPaymentEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.util.UUID

@Dao
interface BillPaymentDao {
    @Query("SELECT * FROM bill_payments ORDER BY dueAt DESC")
    fun observeAll(): Flow<List<BillPaymentEntity>>

    @Query(
        """
        SELECT * FROM bill_payments
        WHERE billId = :billId
            AND periodStart = :periodStart
            AND periodEnd = :periodEnd
            AND dueAt = :dueAt
        LIMIT 1
        """,
    )
    suspend fun getForOccurrence(
        billId: UUID,
        periodStart: Instant,
        periodEnd: Instant,
        dueAt: Instant,
    ): BillPaymentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: BillPaymentEntity)

    @Query("UPDATE bill_payments SET paidAt = NULL, amountPaidMinor = NULL, updatedAt = :updatedAt WHERE id = :id")
    suspend fun markUnpaid(
        id: UUID,
        updatedAt: Instant,
    )
}
