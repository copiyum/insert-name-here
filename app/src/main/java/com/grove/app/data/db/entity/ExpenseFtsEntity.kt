package com.grove.app.data.db.entity

import androidx.room.Entity
import androidx.room.Fts4

/**
 * FTS4 virtual table mirroring `expenses.note` for search.
 * Room generates the sync triggers at compile time from the
 * @Fts4(contentEntity = ...) annotation — do NOT hand-write
 * CREATE TRIGGER blocks. See design §"Behavior note 10".
 */
@Fts4(contentEntity = ExpenseEntity::class)
@Entity(tableName = "expenses_fts")
data class ExpenseFtsEntity(
    val note: String,
)
