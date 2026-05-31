package com.grove.app.designsystem.format

import java.time.LocalDateTime
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object Dates {
    fun relative(date: LocalDateTime, today: LocalDateTime): String {
        val d = date.toLocalDate()
        val t = today.toLocalDate()
        return when {
            d == t -> "Today"
            d == t.minusDays(1) -> "Yesterday"
            d.isAfter(t.minusDays(7)) -> d.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
            else -> d.format(DateTimeFormatter.ofPattern("MMM d"))
        }
    }

    fun time(date: LocalDateTime): String {
        return date.format(DateTimeFormatter.ofPattern("h:mm a"))
    }

    fun format(date: LocalDate): String {
        return date.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
    }
}
