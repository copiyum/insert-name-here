package com.grove.app.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grove.app.data.db.ExpenseLite
import com.grove.app.designsystem.format.Dates
import com.grove.app.designsystem.format.Money
import com.grove.app.designsystem.theme.GroveBorder
import com.grove.app.designsystem.theme.GroveShapes
import com.grove.app.designsystem.theme.GroveSize
import com.grove.app.designsystem.theme.GroveSpacing
import com.grove.app.designsystem.theme.GroveTheme
import com.grove.app.designsystem.theme.GroveType
import com.grove.app.designsystem.theme.InterTight
import java.time.LocalDateTime
import java.time.ZoneId

@Composable
fun ExpenseRow(
    expense: ExpenseLite,
    today: LocalDateTime,
    modifier: Modifier = Modifier,
    currency: String = "USD",
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = GroveSpacing.MD),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CategoryIcon(expense.iconKey)
        Spacer(Modifier.width(GroveSpacing.MD))
        Column(modifier = Modifier.weight(1f)) {
            Text(expense.note, style = GroveType.rowTitle, color = GroveTheme.colors.fg1, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                "${Dates.relative(
                    expense.occurredAt.atZone(ZoneId.systemDefault()).toLocalDateTime(),
                    today,
                )} · ${Dates.time(expense.occurredAt.atZone(ZoneId.systemDefault()).toLocalDateTime())}",
                style = GroveType.rowSub,
                color = GroveTheme.colors.fg3,
            )
        }
        Text(
            Money.currencyLong(
                expense.amountMinor,
                2,
                expense.currencyCode.ifEmpty {
                    currency
                },
            ),
            style = GroveType.amount,
            color = GroveTheme.colors.fg1,
        )
    }
}

@Composable
fun IconTileRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    trailing: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = GroveSpacing.MD),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier
                    .size(GroveSize.IconTile)
                    .clip(GroveShapes.Tile)
                    .background(GroveTheme.colors.bone),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = GroveTheme.colors.fg2, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(GroveSpacing.MD))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = GroveType.rowTitle, color = GroveTheme.colors.fg1)
            Text(subtitle, style = GroveType.rowSub, color = GroveTheme.colors.fg3)
        }
        trailing()
    }
}

@Composable
fun SwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onToggle: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = GroveSpacing.MD),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = GroveTheme.colors.fg2, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(GroveSpacing.MD))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = GroveType.rowTitle, color = GroveTheme.colors.fg1)
            Text(subtitle, style = GroveType.rowSub, color = GroveTheme.colors.fg3)
        }
        GroveSwitch(checked = checked, onToggle = onToggle)
    }
}

@Composable
fun SettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    value: String? = null,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .then(if (onClick != null) Modifier.clickable(role = Role.Button) { onClick() } else Modifier)
                .padding(vertical = GroveSpacing.MD),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = GroveTheme.colors.fg2, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(GroveSpacing.MD))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = GroveType.rowTitle, color = GroveTheme.colors.fg1)
            if (subtitle != null) Text(subtitle, style = GroveType.rowSub, color = GroveTheme.colors.fg3)
        }
        if (value != null) {
            Text(value, fontFamily = InterTight, fontWeight = FontWeight.Medium, fontSize = 13.5.sp, color = GroveTheme.colors.fg2)
        }
        if (onClick != null || value != null) {
            Spacer(Modifier.width(GroveSpacing.SM))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = GroveTheme.colors.fg3, modifier = Modifier.size(16.dp))
        }
    }
}
