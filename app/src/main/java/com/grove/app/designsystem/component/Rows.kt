package com.grove.app.designsystem.component

import androidx.compose.foundation.background
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
import com.grove.app.data.model.ExpenseLite
import com.grove.app.data.model.CategoryKind
import com.grove.app.core.format.Dates
import com.grove.app.core.format.Money
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
    currency: String = "INR",
    sharedElementKey: Any? = null,
) {
    val c = GroveTheme.colors
    val isIncome = expense.categoryKind == CategoryKind.income
    val title = expense.note.ifBlank { expense.categoryName }
    val amount = Money.currencyLong(
        expense.amountMinor,
        2,
        expense.currencyCode.ifEmpty {
            currency
        },
    )
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = GroveSpacing.MD),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CategoryIcon(
            expense.iconKey,
            contentDescription = expense.categoryName,
            modifier = if (sharedElementKey != null) Modifier.groveSharedElement(sharedElementKey) else Modifier,
        )
        Spacer(Modifier.width(GroveSpacing.MD))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = GroveType.rowTitle, color = c.fg1, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                "${Dates.relative(
                    expense.occurredAt.atZone(ZoneId.systemDefault()).toLocalDateTime(),
                    today,
                )} · ${Dates.time(expense.occurredAt.atZone(ZoneId.systemDefault()).toLocalDateTime())}",
                style = GroveType.rowSub,
                color = c.fg3,
            )
        }
        Text(
            if (isIncome) "+$amount" else amount,
            style = GroveType.amount,
            color = if (isIncome) c.success else c.fg1,
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
                .then(if (onClick != null) Modifier.groveClick(role = Role.Button) { onClick() } else Modifier)
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
