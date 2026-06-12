package com.grove.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneOffset

class BudgetPeriodTest {
    @Test
    fun periodForFirstDayResetUsesCalendarMonth() {
        val period = BudgetPeriod.forDate(LocalDate.of(2026, 6, 12), 1)

        assertEquals(LocalDate.of(2026, 6, 1), period.start)
        assertEquals(LocalDate.of(2026, 7, 1), period.endExclusive)
        assertEquals(30, period.days)
        assertEquals(12, period.dayIndex(LocalDate.of(2026, 6, 12)))
        assertEquals(19, period.daysLeft(LocalDate.of(2026, 6, 12)))
    }

    @Test
    fun periodForMidMonthResetSpansTwoMonths() {
        val period = BudgetPeriod.forDate(LocalDate.of(2026, 6, 12), 15)

        assertEquals(LocalDate.of(2026, 5, 15), period.start)
        assertEquals(LocalDate.of(2026, 6, 15), period.endExclusive)
        assertEquals(29, period.dayIndex(LocalDate.of(2026, 6, 12)))
        assertEquals(3, period.daysLeft(LocalDate.of(2026, 6, 12)))
    }

    @Test
    fun containsUsesStartInclusiveEndExclusive() {
        val period = BudgetPeriod.forDate(LocalDate.of(2026, 6, 12), 1)
        val start = LocalDate.of(2026, 6, 1).atStartOfDay(ZoneOffset.UTC).toInstant()
        val end = LocalDate.of(2026, 7, 1).atStartOfDay(ZoneOffset.UTC).toInstant()

        assertTrue(period.contains(start, ZoneOffset.UTC))
        assertFalse(period.contains(end, ZoneOffset.UTC))
    }
}
