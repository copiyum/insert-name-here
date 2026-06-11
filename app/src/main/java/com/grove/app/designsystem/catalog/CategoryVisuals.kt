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
        "restaurant" to CategoryVisual(Icons.Outlined.Restaurant, Color(0xFFEFE2D3), Color(0xFFA47148), "Food"),
        "car" to CategoryVisual(Icons.Outlined.DirectionsCar, Color(0xFFD8E2CF), Color(0xFF4A6F49), "Transport"),
        "receipt" to CategoryVisual(Icons.AutoMirrored.Outlined.ReceiptLong, Color(0xFFE1DFD0), Color(0xFF6B6A52), "Bills"),
        "shopping_bag" to CategoryVisual(Icons.Outlined.ShoppingBag, Color(0xFFE8DCCF), Color(0xFF8D6E5A), "Shopping"),
        "favorite" to CategoryVisual(Icons.Outlined.FavoriteBorder, Color(0xFFE8D8D4), Color(0xFF8C5A52), "Health"),
        "sports_esports" to CategoryVisual(Icons.Outlined.SportsEsports, Color(0xFFD4DDE2), Color(0xFF5A7080), "Entertainment"),
        "trending_up" to CategoryVisual(Icons.AutoMirrored.Outlined.TrendingUp, Color(0xFFD3E4CF), Color(0xFF3A6940), "Income"),
        "more_horiz" to CategoryVisual(Icons.Outlined.MoreHoriz, Color(0xFFDAD7CD), Color(0xFF6B6F66), "Other"),
    )
    private val fallback = CategoryVisual(Icons.Outlined.MoreHoriz, Color(0xFFDAD7CD), Color(0xFF6B6F66), "Other")

    fun of(id: String): CategoryVisual = map[id] ?: fallback
    fun color(id: String): Color = of(id).color
    fun label(id: String): String = of(id).label
}
