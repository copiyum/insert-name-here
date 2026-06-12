package com.grove.app.feature.history

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.draw.drawBehind
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.grove.app.data.BudgetState
import com.grove.app.data.db.ExpenseLite
import com.grove.app.data.model.CategoryKind
import com.grove.app.designsystem.catalog.CategoryVisuals
import com.grove.app.designsystem.component.AppTopBar
import com.grove.app.designsystem.component.BotanicalEmptyState
import com.grove.app.designsystem.component.CategoryIcon
import com.grove.app.designsystem.component.Chip
import com.grove.app.designsystem.component.FieldVariant
import com.grove.app.designsystem.component.GroveCard
import com.grove.app.designsystem.component.GroveHaptic
import com.grove.app.designsystem.component.GroveTextField
import com.grove.app.designsystem.component.MoneyText
import com.grove.app.designsystem.component.MoneyTextSize
import com.grove.app.designsystem.component.PeekHost
import com.grove.app.designsystem.component.groveSharedElement
import com.grove.app.designsystem.component.SwipeAction
import com.grove.app.designsystem.component.SwipeActionRow
import com.grove.app.designsystem.component.rememberFoliageOverscroll
import com.grove.app.designsystem.format.Dates
import com.grove.app.designsystem.format.Money
import com.grove.app.designsystem.theme.GroveBorder
import com.grove.app.designsystem.theme.GroveShapes
import com.grove.app.designsystem.theme.GroveSize
import com.grove.app.designsystem.theme.GroveSpacing
import com.grove.app.designsystem.theme.GroveTheme
import com.grove.app.designsystem.theme.GroveType
import com.grove.app.designsystem.theme.InterTight
import java.time.ZoneId
import java.util.UUID

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistoryScreen(
    state: BudgetState,
    currency: String,
    onDelete: (UUID) -> Unit,
    onEdit: (ExpenseLite) -> Unit,
) {
    val c = GroveTheme.colors
    val listState = rememberLazyListState()
    val sway = rememberFoliageOverscroll()
    var filter by rememberSaveable { mutableStateOf("all") }
    var query by rememberSaveable { mutableStateOf("") }
    var peeked by remember { mutableStateOf<ExpenseLite?>(null) }
    val chips =
        remember(state.categories) {
            listOf("All" to "all", "This week" to "week") +
                state.categories.map { it.displayName to it.id.toString() }
        }

    val filtered =
        remember(state.expenses, filter, query, state.today) {
            state.expenses
                .filter { e ->
                    val matchesFilter =
                        when (filter) {
                            "week" -> {
                                val date = e.occurredAt.atZone(ZoneId.systemDefault()).toLocalDate()
                                val today = state.today.toLocalDate()
                                !date.isBefore(today.minusDays(6)) && !date.isAfter(today)
                            }

                            "all" -> {
                                true
                            }

                            else -> {
                                e.categoryId.toString() == filter
                            }
                        }
                    val categoryLabel = e.categoryName
                    (query.isEmpty() ||
                        e.note.contains(query, ignoreCase = true) ||
                        categoryLabel.contains(query, ignoreCase = true)) && matchesFilter
                }.sortedByDescending { it.occurredAt }
        }
    val grouped =
        remember(filtered) {
            filtered.groupBy { it.occurredAt.atZone(ZoneId.systemDefault()).toLocalDate() }.toList().sortedByDescending { it.first }
        }
    Box(modifier = Modifier.fillMaxSize()) {
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
                Icon(Icons.Outlined.Search, contentDescription = "Search", tint = c.fg3, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(GroveSpacing.SM))
                GroveTextField(value = query, onValueChange = { query = it }, placeholder = "Search expenses", variant = FieldVariant.Bare)
            }
            Spacer(Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(GroveSpacing.SM), contentPadding = PaddingValues(vertical = 4.dp)) {
                items(chips, key = { it.second }) { (label, id) -> Chip(label, selected = filter == id, onClick = { filter = id }) }
            }
        }

        Spacer(Modifier.height(GroveSpacing.MD))

        if (filtered.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                if (state.expenses.isEmpty()) {
                    BotanicalEmptyState("Nothing planted yet", subtitle = "Expenses you log will grow here.")
                } else {
                    BotanicalEmptyState(
                        "Nothing here just now",
                        subtitle = if (query.isBlank()) "Try a different category filter." else "Try a shorter search.",
                    )
                }
            }
        } else {
            val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            CompositionLocalProvider(LocalOverscrollConfiguration provides null) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().nestedScroll(sway.connection).then(sway.modifier()),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = GroveSize.NavClearance + bottomInset),
            ) {
                grouped.forEach { (date, expenses) ->
                    item(key = "h-$date") {
                        Row(
                            modifier = Modifier.fillMaxWidth().animateItem().padding(top = GroveSpacing.XL, bottom = GroveSpacing.SM),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom,
                        ) {
                            Text(
                                Dates.relative(
                                    expenses
                                        .first()
                                        .occurredAt
                                        .atZone(ZoneId.systemDefault())
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
                    item(key = "c-$date") {
                        GroveCard(modifier = Modifier.fillMaxWidth().animateItem(), padding = PaddingValues(horizontal = GroveSpacing.LG)) {
                            expenses.forEachIndexed { i, expense ->
                                ExpenseHistoryRow(
                                    expense,
                                    currency,
                                    onDelete = { onDelete(expense.id) },
                                    onEdit = { onEdit(expense) },
                                    onPeek = { peeked = expense },
                                )
                                if (i < expenses.size - 1) HorizontalDivider(color = c.border)
                            }
                        }
                    }
                }
            }
            }
        }
        }

        PeekHost(visible = peeked != null, onDismiss = { peeked = null }) {
            peeked?.let { expense -> ExpensePeekCard(expense, currency, state.today) }
        }
    }
}

@Composable
private fun ExpenseHistoryRow(
    expense: ExpenseLite,
    currency: String,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onPeek: () -> Unit,
) {
    val c = GroveTheme.colors
    val isIncome = expense.categoryKind == CategoryKind.income
    val title = expense.note.ifBlank { expense.categoryName }
    val amount = Money.currencyLong(expense.amountMinor, 2, expense.currencyCode.ifEmpty { currency })
    SwipeActionRow(
        actions =
            listOf(
                SwipeAction(Icons.Outlined.Edit, "Edit", c.bgMuted, c.fg1) { onEdit() },
                SwipeAction(Icons.Outlined.DeleteOutline, "Delete", c.danger, Color.White, haptic = GroveHaptic.Heavy) { onDelete() },
            ),
        onClick = onEdit,
    ) {
        Row(
            // Long-press peek: watches the gesture without consuming it, so tap-to-edit
            // (parent click) and horizontal swipe actions keep working. Once a long press
            // lands, the rest of the gesture is swallowed so release doesn't open the editor.
            modifier =
                Modifier.weight(1f).pointerInput(expense) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val longPress = awaitLongPressOrCancellation(down.id)
                        if (longPress != null) {
                            onPeek()
                            while (true) {
                                val event = awaitPointerEvent()
                                event.changes.forEach { it.consume() }
                                if (event.changes.none { it.pressed }) break
                            }
                        }
                    }
                },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CategoryIcon(
                expense.iconKey,
                contentDescription = expense.categoryName,
                modifier = Modifier.groveSharedElement("expense-${'$'}{expense.id}"),
            )
            Spacer(Modifier.width(GroveSpacing.MD))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = GroveType.rowTitle, color = c.fg1, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    "${Dates.time(
                        expense.occurredAt.atZone(ZoneId.systemDefault()).toLocalDateTime(),
                    )} · ${expense.categoryName}",
                    style = GroveType.rowSub,
                    color = c.fg3,
                )
            }
            MoneyText(
                if (isIncome) "+$amount" else amount,
                size = MoneyTextSize.Row,
                color = if (isIncome) c.success else c.fg1,
            )
        }
    }
}

@Composable
private fun ExpensePeekCard(
    expense: ExpenseLite,
    currency: String,
    today: java.time.LocalDateTime,
) {
    val c = GroveTheme.colors
    val isIncome = expense.categoryKind == CategoryKind.income
    val amount = Money.currencyLong(expense.amountMinor, 2, expense.currencyCode.ifEmpty { currency })
    val occurred = expense.occurredAt.atZone(ZoneId.systemDefault()).toLocalDateTime()
    // The category owns this card: its color washes the surface (Ivy Wallet pattern).
    val categoryColor = CategoryVisuals.color(expense.iconKey)
    GroveCard(
        modifier = Modifier
            .fillMaxWidth()
            .border(GroveBorder.Thin, categoryColor.copy(alpha = 0.45f), GroveShapes.Container)
            .drawBehind { drawRect(categoryColor.copy(alpha = if (c.isDark) 0.10f else 0.07f)) },
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CategoryIcon(expense.iconKey, contentDescription = expense.categoryName)
            Spacer(Modifier.width(GroveSpacing.MD))
            Text(expense.categoryName, style = GroveType.rowTitle, color = c.fg1, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Spacer(Modifier.height(GroveSpacing.MD))
        MoneyText(
            if (isIncome) "+$amount" else amount,
            size = MoneyTextSize.Title,
            color = if (isIncome) c.success else c.fg1,
        )
        if (expense.note.isNotBlank()) {
            Spacer(Modifier.height(GroveSpacing.XS))
            Text(expense.note, style = GroveType.rowTitle, color = c.fg2)
        }
        Spacer(Modifier.height(GroveSpacing.SM))
        Text(
            "${Dates.relative(occurred, today)} · ${Dates.time(occurred)}",
            style = GroveType.rowSub,
            color = c.fg3,
        )
    }
}
