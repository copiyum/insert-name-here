package com.grove.app.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import java.util.UUID

@Entity(
    tableName = "income_tags",
    primaryKeys = ["incomeId", "tagId"],
    foreignKeys = [
        ForeignKey(
            entity = IncomeEntity::class,
            parentColumns = ["id"],
            childColumns = ["incomeId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("tagId")],
)
data class IncomeTagCrossRef(
    val incomeId: UUID,
    val tagId: UUID,
)
