package com.grove.app.data

import java.time.LocalDate

internal fun LocalDate.withClampedDay(day: Int): LocalDate =
    withDayOfMonth(day.coerceIn(1, 31).coerceAtMost(lengthOfMonth()))
