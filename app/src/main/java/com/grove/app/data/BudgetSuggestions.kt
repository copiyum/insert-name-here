package com.grove.app.data

import com.grove.app.data.model.CategoryKind
import java.time.ZoneId
import java.util.UUID

/**
 * Adaptive budget suggestion: the median of the last three completed months'
 * spending, rounded to a friendly figure. Median resists one-off splurges.
 * Returns null until there's enough history to be honest about it.
 */
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
    // Round up to two significant figures so suggestions read clean (12,377 -> 13,000).
    var magnitude = 1L
    while (median / magnitude >= 100) magnitude *= 10
    return ((median + magnitude - 1) / magnitude) * magnitude
}

/**
 * Per-category suggestion with the same median-of-history approach, scoped to one
 * category's spend across the months present in [BudgetState.expenses].
 */
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
