package com.grove.app.data

import com.grove.app.data.model.CategoryKind
import java.time.ZoneId
import java.util.UUID

fun BudgetState.suggestedMonthBudgetMinor(): Long? {
    val totals =
        pastMonths
            .asSequence()
            .filterNot { it.year == today.year && it.month == today.monthValue }
            .take(3)
            .map { it.totalMinor }
            .filter { it > 0L }
            .toList()
    if (totals.size < 2) return null
    val sorted = totals.sorted()
    val median = sorted[sorted.size / 2]
    var magnitude = 1L
    while (median / magnitude >= 100) magnitude *= 10
    return ((median + magnitude - 1) / magnitude) * magnitude
}

fun BudgetState.suggestedCategoryBudgetMinor(categoryId: UUID): Long? {
    val zone = ZoneId.systemDefault()
    val byMonth = expenses
        .asSequence()
        .filter { it.categoryId == categoryId && it.categoryKind != CategoryKind.income }
        .map { it.occurredAt.atZone(zone).toLocalDate() to it.amountMinor }
        .filterNot { (date, _) -> date.year == today.year && date.monthValue == today.monthValue }
        .groupBy({ (date, _) -> date.year * 100 + date.monthValue }, { (_, amount) -> amount })
        .mapValues { (_, amounts) -> amounts.sum() }
        .toSortedMap(compareByDescending<Int> { it })
        .values
        .take(3)
        .filter { it > 0L }
    if (byMonth.size < 2) return null
    val sorted = byMonth.sorted()
    return sorted[sorted.size / 2]
}
