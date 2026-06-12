package com.grove.app.data.repository

import com.grove.app.data.BudgetPeriod
import com.grove.app.data.withClampedDay
import com.grove.app.data.db.dao.BillDao
import com.grove.app.data.db.dao.BillPaymentDao
import com.grove.app.data.db.entity.BillEntity
import com.grove.app.data.db.entity.BillPaymentEntity
import com.grove.app.data.model.Bill
import com.grove.app.data.model.BillPayment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.UUID

class BillRepository(
    private val billDao: BillDao,
    private val paymentDao: BillPaymentDao,
) {
    fun observeActive(): Flow<List<Bill>> = billDao.observeActive().mapList { it.toDomain() }

    fun observeAll(): Flow<List<Bill>> = billDao.observeAll().mapList { it.toDomain() }

    fun observePayments(): Flow<List<BillPayment>> = paymentDao.observeAll().mapList { it.toDomain() }

    suspend fun get(id: UUID): Bill? = billDao.getById(id)?.toDomain()

    suspend fun upsert(bill: Bill) {
        val now = Instant.now()
        val existing = billDao.getById(bill.id)
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
                createdAt = existing?.createdAt ?: bill.createdAt,
                updatedAt = now,
            ),
        )
    }

    suspend fun delete(id: UUID) {
        billDao.setActive(id, false, Instant.now())
    }

    suspend fun updateCurrencyCode(currencyCode: String) {
        billDao.updateCurrencyCode(currencyCode, Instant.now())
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

    suspend fun togglePaymentForPeriod(
        bill: Bill,
        period: BudgetPeriod,
        paidAt: Instant,
    ) {
        val periodStart = period.start.atStartOfDay(ZoneOffset.UTC).toInstant()
        val periodEnd = period.endExclusive.atStartOfDay(ZoneOffset.UTC).toInstant()
        val id = UUID.nameUUIDFromBytes("billpayment.${bill.id}.$periodStart.$periodEnd".toByteArray())
        val existing = paymentDao.getById(id)
        val now = Instant.now()
        if (existing?.paidAt != null) {
            paymentDao.markUnpaid(id, now)
        } else {
            val dueAt = dueDateInPeriod(period, bill.dueDay ?: 1).atStartOfDay(ZoneOffset.UTC).toInstant()
            paymentDao.upsert(
                BillPaymentEntity(
                    id = id,
                    billId = bill.id,
                    periodYear = period.start.year,
                    periodMonth = period.start.monthValue,
                    periodStart = periodStart,
                    periodEnd = periodEnd,
                    dueAt = dueAt,
                    paidAt = paidAt,
                    amountPaidMinor = bill.amountMinor,
                    note = "",
                    createdAt = existing?.createdAt ?: now,
                    updatedAt = now,
                ),
            )
        }
    }
}

private fun dueDateInPeriod(
    period: BudgetPeriod,
    dueDay: Int,
): LocalDate {
    val normalized = dueDay.coerceIn(1, 31)
    val first = period.start.withClampedDay(normalized)
    val second = period.start.plusMonths(1).withClampedDay(normalized)
    return listOf(first, second).firstOrNull { !it.isBefore(period.start) && it.isBefore(period.endExclusive) } ?: first
}
