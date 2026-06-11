package com.grove.app.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grove.app.data.BudgetState
import com.grove.app.designsystem.component.AppTopBar
import com.grove.app.designsystem.component.FieldLabel
import com.grove.app.designsystem.component.GroveBottomSheet
import com.grove.app.designsystem.component.GroveCard
import com.grove.app.designsystem.component.GroveTextField
import com.grove.app.designsystem.component.PrimaryButton
import com.grove.app.designsystem.component.SettingRow
import com.grove.app.designsystem.component.SwitchRow
import com.grove.app.designsystem.format.Currencies
import com.grove.app.designsystem.format.ordinal
import com.grove.app.designsystem.theme.Fraunces
import com.grove.app.designsystem.theme.GroveShapes
import com.grove.app.designsystem.theme.GroveSpacing
import com.grove.app.designsystem.theme.GroveTheme
import com.grove.app.designsystem.theme.GroveType
import com.grove.app.designsystem.theme.InterTight
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    state: BudgetState,
    currency: String,
    dark: Boolean,
    debugDate: String = "",
    debugDateOffset: Int = 0,
    onToggleDark: () -> Unit,
    onReplayOnboarding: () -> Unit,
    onOpenBudget: () -> Unit,
    onUpdateCurrency: (String) -> Unit,
    onUpdateName: (String) -> Unit,
    onShiftDebugDate: (Int) -> Unit = {},
) {
    val c = GroveTheme.colors
    var reminders by rememberSaveable { mutableStateOf(true) }
    var billAlerts by rememberSaveable { mutableStateOf(true) }
    var showCurrencyPicker by rememberSaveable { mutableStateOf(false) }
    var showEditName by rememberSaveable { mutableStateOf(false) }
    val resetOrdinal = ordinal(state.user?.resetDay ?: 1)
    val currentCurrency = Currencies.current(currency)

    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 120.dp)) {
        item { AppTopBar(title = "Settings") }

        item {
            GroveCard(modifier = Modifier.fillMaxWidth().clickable { showEditName = true }, padding = PaddingValues(GroveSpacing.SM + 2.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(56.dp).clip(CircleShape).background(c.accent), contentAlignment = Alignment.Center) {
                        Text(
                            (state.user?.name ?: "Mae")
                                .first()
                                .toString(),
                            fontFamily = Fraunces,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Medium,
                            color = c.fgOnFern,
                        )
                    }
                    Spacer(Modifier.width(GroveSpacing.SM))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(state.user?.name ?: "Mae", style = GroveType.rowTitle, fontWeight = FontWeight.Medium, color = c.fg1)
                        Text("${currentCurrency.code} · Resets on the $resetOrdinal", style = GroveType.rowSub, color = c.fg3)
                    }
                    Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = c.fg3, modifier = Modifier.size(16.dp))
                }
            }
        }

        item { SectionLabel("PREFERENCES") }
        item {
            GroveCard(modifier = Modifier.fillMaxWidth(), padding = PaddingValues(horizontal = GroveSpacing.LG)) {
                SwitchRow(
                    if (dark) Icons.Outlined.DarkMode else Icons.Outlined.LightMode,
                    "Dark mode",
                    "Easier on the eyes at night",
                    dark,
                    onToggleDark,
                )
                HorizontalDivider(color = c.border)
                SwitchRow(Icons.Outlined.NotificationsNone, "Daily safe-to-spend", "Gentle nudge each morning at 8am", reminders) {
                    reminders =
                        !reminders
                }
                HorizontalDivider(color = c.border)
                SwitchRow(Icons.Outlined.Receipt, "Bill due alerts", "3 days before each bill", billAlerts) { billAlerts = !billAlerts }
            }
        }

        item { SectionLabel("BUDGET") }
        item {
            GroveCard(modifier = Modifier.fillMaxWidth(), padding = PaddingValues(horizontal = GroveSpacing.LG)) {
                SettingRow(
                    Icons.Outlined.PieChart,
                    "Budget & categories",
                    subtitle = "Set monthly and per-category limits",
                    onClick = onOpenBudget,
                )
                HorizontalDivider(color = c.border)
                SettingRow(
                    Icons.Outlined.CalendarToday,
                    "Reset day",
                    subtitle = "When your budget resets",
                    value = "$resetOrdinal of month",
                )
                HorizontalDivider(color = c.border)
                SettingRow(Icons.Outlined.AttachMoney, "Currency", subtitle = currentCurrency.name, value = "${currentCurrency.symbol} ${currentCurrency.code}", onClick = {
                    showCurrencyPicker =
                        true
                })
            }
        }

        item { SectionLabel("DATA") }
        item {
            GroveCard(modifier = Modifier.fillMaxWidth(), padding = PaddingValues(horizontal = GroveSpacing.LG)) {
                SettingRow(Icons.Outlined.Spa, "Replay onboarding", subtitle = "Walk through setup again", onClick = onReplayOnboarding)
            }
        }

        item { SectionLabel("DEBUG") }
        item {
            GroveCard(modifier = Modifier.fillMaxWidth(), padding = PaddingValues(horizontal = GroveSpacing.LG)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = GroveSpacing.SM),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(GroveSpacing.SM),
                ) {
                    Box(
                        modifier = Modifier.size(40.dp).clip(GroveShapes.Toggle).background(c.bgCard).border(1.dp, c.border, GroveShapes.Toggle).clickable {
                            onShiftDebugDate(-1)
                        },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("−", fontFamily = InterTight, fontSize = 18.sp, fontWeight = FontWeight.Medium, color = c.fg1)
                    }
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Debug date", style = GroveType.capLabel, color = c.fg3)
                        Text(if (debugDateOffset == 0) "Today ($debugDate)" else "$debugDate (${
                            if (debugDateOffset > 0) "+" else ""
                        }${debugDateOffset}d)", fontFamily = InterTight, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = c.fg1)
                    }
                    Box(
                        modifier = Modifier.size(40.dp).clip(GroveShapes.Toggle).background(c.bgCard).border(1.dp, c.border, GroveShapes.Toggle).clickable {
                            onShiftDebugDate(1)
                        },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("+", fontFamily = InterTight, fontSize = 18.sp, fontWeight = FontWeight.Medium, color = c.fg1)
                    }
                    Box(
                        modifier = Modifier.size(40.dp).clip(GroveShapes.Toggle).background(c.clayBg).border(1.dp, c.clay, GroveShapes.Toggle).clickable {
                            onShiftDebugDate(-debugDateOffset)
                        },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("Reset", fontFamily = InterTight, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = c.clay)
                    }
                }
            }
        }

        item {
            Box(modifier = Modifier.fillMaxWidth().padding(top = GroveSpacing.LG), contentAlignment = Alignment.Center) {
                Text("Grove v1.0 · made for quiet budgets", fontFamily = InterTight, fontSize = 12.sp, color = c.fg3)
            }
        }
    }

    if (showEditName) {
        EditNameSheet(
            current = state.user?.name ?: "Mae",
            onSave = { name ->
                onUpdateName(name)
                showEditName = false
            },
            onDismiss = { showEditName = false },
        )
    }

    if (showCurrencyPicker) {
        CurrencyPickerSheet(
            current = currency,
            onSelect = { code ->
                onUpdateCurrency(code)
                showCurrencyPicker = false
            },
            onDismiss = { showCurrencyPicker = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CurrencyPickerSheet(
    current: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val c = GroveTheme.colors
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = c.bgApp, shape = GroveShapes.SheetTop, sheetState = sheetState) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = GroveSpacing.XL).padding(bottom = GroveSpacing.XL),
        ) {
            Text("Currency", style = GroveType.sheetTitle, color = c.fg1)
            Spacer(Modifier.height(GroveSpacing.SM))
            Column(modifier = Modifier.height(320.dp)) {
                Currencies.list.forEachIndexed { i, currency ->
                    val selected = currency.code == current
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clip(GroveShapes.CatPicker)
                                .background(if (selected) c.accentSurface else androidx.compose.ui.graphics.Color.Transparent)
                                .clickable {
                                    scope.launch {
                                        sheetState.hide()
                                        onSelect(currency.code)
                                    }
                                }.padding(horizontal = GroveSpacing.SM + 4.dp, vertical = GroveSpacing.SM + 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier.size(40.dp).clip(GroveShapes.CatPicker).background(c.bone),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(currency.symbol, fontFamily = InterTight, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = c.fg1)
                        }
                        Spacer(Modifier.width(GroveSpacing.SM + 4.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(currency.code, fontFamily = InterTight, fontWeight = FontWeight.Medium, fontSize = 14.5.sp, color = c.fg1)
                            Text(currency.name, fontFamily = InterTight, fontSize = 12.5.sp, color = c.fg3)
                        }
                        Text(currency.example, fontFamily = InterTight, fontSize = 13.sp, color = c.fg3)
                        if (selected) {
                            Spacer(Modifier.width(GroveSpacing.SM))
                            Icon(Icons.Outlined.Check, contentDescription = null, tint = c.accent, modifier = Modifier.size(18.dp))
                        }
                    }
                    if (i < Currencies.list.size - 1) HorizontalDivider(color = c.border)
                }
            }
        }
    }
}

@Composable
private fun EditNameSheet(
    current: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val c = GroveTheme.colors
    var name by remember { mutableStateOf(current) }
    GroveBottomSheet(onDismiss = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = GroveSpacing.XL).padding(bottom = GroveSpacing.XL),
        ) {
            Text("Your name", style = com.grove.app.designsystem.theme.GroveType.sheetTitle, color = c.fg1)
            Spacer(Modifier.height(GroveSpacing.SM + 4.dp))
            FieldLabel("NAME")
            GroveTextField(value = name, onValueChange = { name = it }, placeholder = "e.g. Mae")
            Spacer(Modifier.height(GroveSpacing.SM + 6.dp))
            PrimaryButton(
                "Save",
                onClick = { onSave(name) },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.trim().isNotEmpty(),
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        style = GroveType.capLabel.copy(letterSpacing = 0.9.sp),
        color = GroveTheme.colors.fg3,
        modifier = Modifier.padding(top = GroveSpacing.XL, bottom = GroveSpacing.SM),
    )
}
