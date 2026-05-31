package com.grove.app.designsystem.catalog

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.Terrain
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.ui.graphics.vector.ImageVector

object BillIcons {
    fun of(key: String): ImageVector = when (key) {
        "home" -> Icons.Outlined.Home
        "wifi" -> Icons.Outlined.Wifi
        "music" -> Icons.Outlined.MusicNote
        "zap" -> Icons.Outlined.Bolt
        "phone" -> Icons.Outlined.Phone
        "mountain" -> Icons.Outlined.Terrain
        else -> Icons.Outlined.Receipt
    }
}
