package com.grove.app.data.repository

import com.grove.app.data.BudgetPeriod
import com.grove.app.data.withClampedDay
import com.grove.app.data.model.BillFrequency
import com.grove.app.data.db.dao.BillDao
import com.grove.app.data.db.dao.BillPaymentDao
import com.grove.app.data.db.entity.BillEntity
import com.grove.app.data.db.entity.BillPaymentEntity
import com.grove.app.data.model.Bill
import com.grove.app.data.model.BillPayment
import com.grove.app.data.model.BillLite
import kotlinx.coroutines.flow.Flow
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
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

    suspend fun togglePaymentForOccurrence(
        bill: Bill,
        period: BudgetPeriod,
        dueAt: Instant,
        paidAt: Instant,
    ) {
        val periodStart = period.start.atStartOfDay(ZoneOffset.UTC).toInstant()
        val periodEnd = period.endExclusive.atStartOfDay(ZoneOffset.UTC).toInstant()
        val existing = paymentDao.getForOccurrence(bill.id, periodStart, periodEnd, dueAt)
        val id = existing?.id ?: UUID.nameUUIDFromBytes("billpayment.${bill.id}.$periodStart.$periodEnd.$dueAt".toByteArray())
        val now = Instant.now()
        if (existing?.paidAt != null) {
            paymentDao.markUnpaid(id, now)
        } else {
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

fun projectBillOccurrences(
    bills: List<Bill>,
    payments: List<BillPayment>,
    period: BudgetPeriod,
): List<BillLite> {
    val paidKeys =
        payments
            .asSequence()
            .filter { it.periodStart == period.start.atStartOfDay(ZoneOffset.UTC).toInstant() }
            .filter { it.periodEnd == period.endExclusive.atStartOfDay(ZoneOffset.UTC).toInstant() }
            .filter { it.paidAt != null }
            .map { BillOccurrenceKey(it.billId, it.dueAt) }
            .toSet()
    return bills
        .flatMap { bill ->
            bill.dueDatesIn(period).map { dueDate ->
                val dueAt = dueDate.atStartOfDay(ZoneOffset.UTC).toInstant()
                BillLite(
                    id = bill.id,
                    name = bill.name,
                    amountMinor = bill.amountMinor,
                    currencyCode = bill.currencyCode,
                    iconKey = bill.iconKey,
                    dueAt = dueAt,
                    dueDay = dueDate.dayOfMonth,
                    frequency = bill.frequency,
                    paid = BillOccurrenceKey(bill.id, dueAt) in paidKeys,
                )
            }
        }
        .sortedBy { it.dueAt }
}

private data class BillOccurrenceKey(
    val billId: UUID,
    val dueAt: Instant,
)

private fun Bill.dueDatesIn(period: BudgetPeriod): List<LocalDate> =
    when (frequency) {
        BillFrequency.monthly -> monthIntervalDates(period, monthStep = 1)
        BillFrequency.quarterly -> monthIntervalDates(period, monthStep = 3)
        BillFrequency.yearly -> monthIntervalDates(period, monthStep = 12)
        BillFrequency.weekly -> weekIntervalDates(period, weekStep = 1)
        BillFrequency.biweekly -> weekIntervalDates(period, weekStep = 2)
        BillFrequency.one_off -> listOf(startDate.atZone(ZoneOffset.UTC).toLocalDate()).filterActiveIn(period, this)
    }

private fun Bill.monthIntervalDates(
    period: BudgetPeriod,
    monthStep: Long,
): List<LocalDate> {
    val anchor = startDate.atZone(ZoneOffset.UTC).toLocalDate()
    val dueDay = dueDay ?: anchor.dayOfMonth
    var cursor = YearMonth.from(anchor)
    val periodStartMonth = YearMonth.from(period.start)
    while (cursor.plusMonths(monthStep).let { it <= periodStartMonth }) {
        cursor = cursor.plusMonths(monthStep)
    }
    return buildList {
        while (!cursor.atDay(1).isAfter(period.endExclusive)) {
            add(cursor.atClampedDay(dueDay))
            cursor = cursor.plusMonths(monthStep)
        }
    }.filterActiveIn(period, this)
}

private fun Bill.weekIntervalDates(
    period: BudgetPeriod,
    weekStep: Long,
): List<LocalDate> {
    val anchor = startDate.atZone(ZoneOffset.UTC).toLocalDate()
    val targetDay = DayOfWeek.of(dueWeekday?.coerceIn(1, 7) ?: anchor.dayOfWeek.value)
    var cursor = anchor.nextOrSame(targetDay)
    while (cursor.plusWeeks(weekStep).isBefore(period.start) || cursor.plusWeeks(weekStep) == period.start) {
        cursor = cursor.plusWeeks(weekStep)
    }
    return buildList {
        while (cursor.isBefore(period.endExclusive)) {
            add(cursor)
            cursor = cursor.plusWeeks(weekStep)
        }
    }.filterActiveIn(period, this)
}

private fun YearMonth.atClampedDay(day: Int): LocalDate =
    atDay(1).withClampedDay(day)

private fun LocalDate.nextOrSame(day: DayOfWeek): LocalDate {
    val delta = (day.value - dayOfWeek.value + 7) % 7
    return plusDays(delta.toLong())
}

private fun Bill.activeOn(date: LocalDate): Boolean {
    val start = startDate.atZone(ZoneOffset.UTC).toLocalDate()
    val end = endDate?.atZone(ZoneOffset.UTC)?.toLocalDate()
    return !date.isBefore(start) && (end == null || !date.isAfter(end))
}

private fun List<LocalDate>.filterActiveIn(
    period: BudgetPeriod,
    bill: Bill,
): List<LocalDate> =
    filter { bill.activeOn(it) && !it.isBefore(period.start) && it.isBefore(period.endExclusive) }
