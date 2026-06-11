package com.grove.app.feature.history

import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.grove.app.data.BudgetState
import com.grove.app.data.db.ExpenseLite
import com.grove.app.designsystem.catalog.CategoryVisuals
import com.grove.app.designsystem.component.AppTopBar
import com.grove.app.designsystem.component.CategoryIcon
import com.grove.app.designsystem.component.Chip
import com.grove.app.designsystem.component.FieldVariant
import com.grove.app.designsystem.component.GroveCard
import com.grove.app.designsystem.component.GroveTextField
import com.grove.app.designsystem.format.Dates
import com.grove.app.designsystem.format.Money
import com.grove.app.designsystem.theme.GroveBorder
import com.grove.app.designsystem.theme.GroveShapes
import com.grove.app.designsystem.theme.GroveSize
import com.grove.app.designsystem.theme.GroveSpacing
import com.grove.app.designsystem.theme.GroveTheme
import com.grove.app.designsystem.theme.GroveType
import com.grove.app.designsystem.theme.InterTight
import java.time.ZoneOffset
import java.util.UUID

private val Chips =
    listOf(
        "All" to "all",
        "This week" to "week",
        "Food" to "food",
        "Transport" to "transport",
        "Bills" to "bills",
        "Shopping" to "shopping",
        "Health" to "health",
        "Fun" to "entertainment",
    )

@Composable
fun HistoryScreen(
    state: BudgetState,
    currency: String,
    onDelete: (UUID) -> Unit,
    onEdit: (ExpenseLite) -> Unit,
) {
    val c = GroveTheme.colors
    var filter by rememberSaveable { mutableStateOf("all") }
    var query by rememberSaveable { mutableStateOf("") }

    val filtered =
        remember(state.expenses, filter, query) {
            state.expenses
                .filter { e ->
                    val matchesFilter =
                        when (filter) {
                            "week" -> {
                                e.occurredAt.isAfter(
                                    java.time.Instant
                                        .now()
                                        .minusSeconds(7 * 24 * 3600),
                                )
                            }

                            "all", "month" -> {
                                true
                            }

                            else -> {
                                e.categoryId.toString() == filter
                            }
                        }
                    (query.isEmpty() || e.note.contains(query, ignoreCase = true)) && matchesFilter
                }.sortedByDescending { it.occurredAt }
        }
    val grouped =
        remember(filtered) {
            filtered.groupBy { it.occurredAt.atZone(ZoneOffset.UTC).toLocalDate() }.toList().sortedByDescending { it.first }
        }

    Column(modifier = Modifier.fillMaxSize()) {
        AppTopBar(title = "History", subtitle = "${filtered.size} entries")

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(GroveShapes.InputOutlined)
                        .background(c.bgCard)
                        .border(GroveBorder.Thin, c.border, GroveShapes.InputOutlined)
                        .padding(start = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.Search, contentDescription = null, tint = c.fg3, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(GroveSpacing.SM))
                GroveTextField(value = query, onValueChange = { query = it }, placeholder = "Search expenses", variant = FieldVariant.Bare)
            }
            Spacer(Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(GroveSpacing.SM), contentPadding = PaddingValues(vertical = 4.dp)) {
                items(Chips) { (label, id) -> Chip(label, selected = filter == id, onClick = { filter = id }) }
            }
        }

        Spacer(Modifier.height(GroveSpacing.MD))

        if (filtered.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text("No expenses match", style = GroveType.body, fontWeight = FontWeight.Medium, color = c.fg3)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 120.dp)) {
                grouped.forEach { (_, expenses) ->
                    item(key = "h-${expenses.first().id}") {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = GroveSpacing.XL, bottom = GroveSpacing.SM),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom,
                        ) {
                            Text(
                                Dates.relative(
                                    expenses
                                        .first()
                                        .occurredAt
                                        .atZone(ZoneOffset.UTC)
                                        .toLocalDateTime(),
                                    state.today,
                                ),
                                style = GroveType.sectionLabel,
                                color = c.fg2,
                            )
                            Text(
                                Money.currencyLong(
                                    expenses.sumOf {
                                        it.amountMinor
                                    },
                                    2,
                                    expenses.first().currencyCode.ifEmpty { currency },
                                ),
                                style = GroveType.rowSub,
                                color = c.fg3,
                            )
                        }
                    }
                    item(key = "c-${expenses.first().id}") {
                        GroveCard(modifier = Modifier.fillMaxWidth(), padding = PaddingValues(horizontal = GroveSpacing.LG)) {
                            expenses.forEachIndexed { i, expense ->
                                SwipeableExpenseRow(expense, currency, onDelete = { onDelete(expense.id) }, onEdit = { onEdit(expense) })
                                if (i < expenses.size - 1) HorizontalDivider(color = c.border)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SwipeableExpenseRow(
    expense: ExpenseLite,
    currency: String,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
) {
    val c = GroveTheme.colors
    val density = LocalDensity.current
    val revealPx = with(density) { 116.dp.toPx() }
    val maxDragPx = with(density) { 132.dp.toPx() }

    var offsetX by remember { mutableFloatStateOf(0f) }
    var dragging by remember { mutableStateOf(false) }
    val animatedX by animateFloatAsState(offsetX, if (dragging) snap() else tween(280, easing = EaseOutCubic), label = "swipe")

    Box(modifier = Modifier.fillMaxWidth().clipToBounds()) {
        Row(
            modifier = Modifier.matchParentSize().padding(end = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(GroveSpacing.SM, Alignment.End),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircleAction(Icons.Outlined.Edit, "Edit", c.bgMuted, c.fg1) {
                onEdit()
                offsetX = 0f
            }
            CircleAction(Icons.Outlined.DeleteOutline, "Delete", c.clay, Color.White) { onDelete() }
        }
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .offset { IntOffset(animatedX.toInt(), 0) }
                    .background(c.bgCard)
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragStart = { dragging = true },
                            onDragEnd = {
                                dragging = false
                                offsetX = if (offsetX < -revealPx / 2f) -revealPx else 0f
                            },
                            onDragCancel = {
                                dragging = false
                                offsetX = 0f
                            },
                            onHorizontalDrag = { _, delta -> offsetX = (offsetX + delta).coerceIn(-maxDragPx, 0f) },
                        )
                    }.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                        if (offsetX != 0f) offsetX = 0f
                    }.padding(vertical = GroveSpacing.MD),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CategoryIcon(expense.categoryId.toString())
            Spacer(Modifier.width(GroveSpacing.MD))
            Column(modifier = Modifier.weight(1f)) {
                Text(expense.note, style = GroveType.rowTitle, color = c.fg1, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    "${Dates.time(
                        expense.occurredAt.atZone(ZoneOffset.UTC).toLocalDateTime(),
                    )} · ${CategoryVisuals.label(expense.categoryId.toString())}",
                    style = GroveType.rowSub,
                    color = c.fg3,
                )
            }
            Text(
                Money.currencyLong(expense.amountMinor, 2, expense.currencyCode.ifEmpty { currency }),
                style = GroveType.amount,
                color = c.fg1,
            )
        }
    }
}

@Composable
private fun CircleAction(
    icon: ImageVector,
    label: String,
    bg: Color,
    fg: Color,
    onClick: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .size(GroveSize.SwipeAction)
                .clip(GroveShapes.Chip)
                .background(bg)
                .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = label, tint = fg, modifier = Modifier.size(19.dp))
    }
}
