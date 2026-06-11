package com.grove.app.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.grove.app.data.model.Category
import com.grove.app.data.model.CategoryKind
import java.time.Instant
import java.util.UUID

@Entity(tableName = "categories", indices = [Index(value = ["kind", "archivedAt"])])
data class CategoryEntity(
    @PrimaryKey val id: UUID,
    val displayName: String,
    val iconKey: String,
    val colorHex: String,
    val kind: CategoryKind,
    val sortOrder: Int = 0,
    val archivedAt: Instant? = null,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    fun toDomain() =
        Category(
            id = id,
            displayName = displayName,
            iconKey = iconKey,
            colorHex = colorHex,
            kind = kind,
            sortOrder = sortOrder,
            archivedAt = archivedAt,
        )
}
