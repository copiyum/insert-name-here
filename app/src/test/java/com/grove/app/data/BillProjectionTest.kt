package com.grove.app.data

import com.grove.app.data.model.Bill
import com.grove.app.data.model.BillFrequency
import com.grove.app.data.model.BillPayment
import com.grove.app.data.repository.projectBillOccurrences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.UUID

class BillProjectionTest {
    @Test
    fun monthlyBillProjectsOnlyDueDatesInsideBudgetPeriod() {
        val bill = bill(BillFrequency.monthly, dueDay = 1, start = LocalDate.of(2025, 12, 1))
        val period = BudgetPeriod(LocalDate.of(2026, 1, 15), LocalDate.of(2026, 2, 15))

        val occurrences = projectBillOccurrences(listOf(bill), emptyList(), period)

        assertEquals(listOf(LocalDate.of(2026, 2, 1)), occurrences.map { it.dueAt.toDate() })
    }

    @Test
    fun weeklyBillProjectsEachWeekdayOccurrenceInsidePeriod() {
        val bill = bill(BillFrequency.weekly, dueWeekday = 1, start = LocalDate.of(2026, 1, 1))
        val period = BudgetPeriod(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 16))

        val occurrences = projectBillOccurrences(listOf(bill), emptyList(), period)

        assertEquals(
            listOf(LocalDate.of(2026, 1, 5), LocalDate.of(2026, 1, 12)),
            occurrences.map { it.dueAt.toDate() },
        )
    }

    @Test
    fun paidStatusMatchesBillAndDueDate() {
        val bill = bill(BillFrequency.weekly, dueWeekday = 1, start = LocalDate.of(2026, 1, 1))
        val period = BudgetPeriod(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 16))
        val paidDueAt = LocalDate.of(2026, 1, 12).atStartOfDay(ZoneOffset.UTC).toInstant()
        val payment =
            BillPayment(
                id = UUID.randomUUID(),
                billId = bill.id,
                periodYear = 2026,
                periodMonth = 1,
                periodStart = period.start.atStartOfDay(ZoneOffset.UTC).toInstant(),
                periodEnd = period.endExclusive.atStartOfDay(ZoneOffset.UTC).toInstant(),
                dueAt = paidDueAt,
                paidAt = Instant.now(),
                amountPaidMinor = 1_000,
                note = "",
            )

        val occurrences = projectBillOccurrences(listOf(bill), listOf(payment), period)

        assertFalse(occurrences[0].paid)
        assertTrue(occurrences[1].paid)
    }

    private fun bill(
        frequency: BillFrequency,
        dueDay: Int? = null,
        dueWeekday: Int? = null,
        start: LocalDate,
    ) = Bill(
        id = UUID.randomUUID(),
        name = "Test bill",
        amountMinor = 1_000,
        currencyCode = "USD",
        frequency = frequency,
        dueDay = dueDay,
        dueWeekday = dueWeekday,
        startDate = start.atStartOfDay(ZoneOffset.UTC).toInstant(),
        endDate = null,
        iconKey = "receipt",
        isActive = true,
        createdAt = Instant.now(),
        updatedAt = Instant.now(),
    )

    private fun Instant.toDate(): LocalDate =
        atZone(ZoneOffset.UTC).toLocalDate()
}
