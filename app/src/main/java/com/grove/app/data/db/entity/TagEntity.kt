package com.grove.app.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.grove.app.data.model.Tag
import java.time.Instant
import java.util.UUID

@Entity(tableName = "tags", indices = [Index(value = ["name"], unique = true)])
data class TagEntity(
    @PrimaryKey val id: UUID,
    val name: String,
    val colorHex: String,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    fun toDomain() =
        Tag(
            id = id,
            name = name,
            colorHex = colorHex,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
}
