package com.grove.app.data.repository

import com.grove.app.data.db.dao.BillDao
import com.grove.app.data.db.dao.BillPaymentDao
import com.grove.app.data.db.entity.BillEntity
import com.grove.app.data.db.entity.BillPaymentEntity
import com.grove.app.data.model.Bill
import com.grove.app.data.model.BillPayment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.util.UUID

class BillRepository(
    private val billDao: BillDao,
    private val paymentDao: BillPaymentDao,
) {
    fun observeActive(): Flow<List<Bill>> = billDao.observeActive().map { list -> list.map { it.toDomain() } }

    fun observeAll(): Flow<List<Bill>> = billDao.observeAll().map { list -> list.map { it.toDomain() } }

    fun observePayments(): Flow<List<BillPayment>> = paymentDao.observeAll().map { list -> list.map { it.toDomain() } }

    fun observePaymentsDueBetween(
        start: Instant,
        end: Instant,
    ): Flow<List<BillPayment>> = paymentDao.observeDueBetween(start, end).map { list -> list.map { it.toDomain() } }

    suspend fun upsert(bill: Bill) {
        val now = Instant.now()
        billDao.upsert(
            BillEntity(
                id = bill.id,
                name = bill.name,
                amountMinor = bill.amountMinor,
                currencyCode = bill.currencyCode,
                frequency = bill.frequency,
                dueDay = bill.dueDay,
                dueWeekday = bill.dueWeekday,
                startDate = bill.startDate,
                endDate = bill.endDate,
                iconKey = bill.iconKey,
                isActive = bill.isActive,
                createdAt = now,
                updatedAt = now,
            ),
        )
    }

    suspend fun upsertPayment(payment: BillPayment) {
        val now = Instant.now()
        paymentDao.upsert(
            BillPaymentEntity(
                id = payment.id,
                billId = payment.billId,
                periodYear = payment.periodYear,
                periodMonth = payment.periodMonth,
                periodStart = payment.periodStart,
                periodEnd = payment.periodEnd,
                dueAt = payment.dueAt,
                paidAt = payment.paidAt,
                amountPaidMinor = payment.amountPaidMinor,
                note = payment.note,
                createdAt = now,
                updatedAt = now,
            ),
        )
    }

    suspend fun markPaymentPaid(
        id: UUID,
        paidAt: Instant,
        amountPaidMinor: Long?,
    ) {
        paymentDao.markPaid(id, paidAt, amountPaidMinor, Instant.now())
    }
}
