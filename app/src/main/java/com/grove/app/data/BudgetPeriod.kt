package com.grove.app.data

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

data class BudgetPeriod(
    val start: LocalDate,
    val endExclusive: LocalDate,
) {
    val days: Int get() = ChronoUnit.DAYS.between(start, endExclusive).toInt().coerceAtLeast(1)

    fun dayIndex(today: LocalDate): Int =
        (ChronoUnit.DAYS.between(start, today).toInt() + 1).coerceIn(1, days)

    fun daysLeft(today: LocalDate): Int =
        (ChronoUnit.DAYS.between(today, endExclusive).toInt()).coerceAtLeast(1)

    fun contains(
        instant: Instant,
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): Boolean {
        val date = instant.atZone(zoneId).toLocalDate()
        return !date.isBefore(start) && date.isBefore(endExclusive)
    }

    companion object {
        fun forDate(
            date: LocalDate,
            resetDay: Int,
        ): BudgetPeriod {
            val normalized = resetDay.coerceIn(1, 31)
            val thisStart = date.withClampedDay(normalized)
            val start = if (date.isBefore(thisStart)) date.minusMonths(1).withClampedDay(normalized) else thisStart
            val end = start.plusMonths(1).withClampedDay(normalized)
            return BudgetPeriod(start, end)
        }
    }
}

private fun LocalDate.withClampedDay(day: Int): LocalDate =
    withDayOfMonth(day.coerceAtMost(lengthOfMonth()))
