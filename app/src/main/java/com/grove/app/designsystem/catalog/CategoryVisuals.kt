package com.grove.app.designsystem.catalog

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class CategoryVisual(
    val icon: ImageVector,
    val tint: Color,
    val color: Color,
    val label: String,
)

object CategoryVisuals {
    private val map = mapOf(
        "restaurant" to CategoryVisual(Icons.Outlined.Restaurant, Color(0x26EF7F72), Color(0xFFEF7F72), "Food"),
        "car" to CategoryVisual(Icons.Outlined.DirectionsCar, Color(0x264CB0E5), Color(0xFF4CB0E5), "Transport"),
        "receipt" to CategoryVisual(Icons.AutoMirrored.Outlined.ReceiptLong, Color(0x269F85E5), Color(0xFF9F85E5), "Bills"),
        "shopping_bag" to CategoryVisual(Icons.Outlined.ShoppingBag, Color(0x26DE80C0), Color(0xFFDE80C0), "Shopping"),
        "favorite" to CategoryVisual(Icons.Outlined.FavoriteBorder, Color(0x2654C398), Color(0xFF54C398), "Health"),
        "sports_esports" to CategoryVisual(Icons.Outlined.SportsEsports, Color(0x26ECA851), Color(0xFFECA851), "Entertainment"),
        "trending_up" to CategoryVisual(Icons.AutoMirrored.Outlined.TrendingUp, Color(0x2662C37A), Color(0xFF62C37A), "Income"),
        "more_horiz" to CategoryVisual(Icons.Outlined.MoreHoriz, Color(0x268D90A8), Color(0xFF8D90A8), "Other"),
    )
    private val fallback = CategoryVisual(Icons.Outlined.MoreHoriz, Color(0x268D90A8), Color(0xFF8D90A8), "Other")

    fun of(id: String): CategoryVisual = map[id] ?: fallback
    fun color(id: String): Color = of(id).color
    fun label(id: String): String = of(id).label
}
