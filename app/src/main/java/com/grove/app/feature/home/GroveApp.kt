package com.grove.app.feature.home

import android.app.Activity
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.grove.app.data.model.Expense
import com.grove.app.designsystem.theme.GroveTheme
import com.grove.app.designsystem.theme.GroveTokens
import com.grove.app.designsystem.theme.InterTight
import com.grove.app.feature.addexpense.AddExpenseSheet
import com.grove.app.feature.bills.BillsScreen
import com.grove.app.feature.budget.BudgetScreen
import com.grove.app.feature.dashboard.DashboardScreen
import com.grove.app.feature.history.HistoryScreen
import com.grove.app.feature.onboarding.OnboardingFlow
import com.grove.app.feature.reports.ReportsScreen
import com.grove.app.feature.settings.SettingsScreen

@Composable
fun GroveApp() {
    val context = LocalContext.current
    val vm: MainViewModel = viewModel { MainViewModel(context.applicationContext as android.app.Application) }
    val darkOverride by vm.darkOverride.collectAsStateWithLifecycle()
    val dark = darkOverride ?: isSystemInDarkTheme()
    val currency by vm.currency.collectAsStateWithLifecycle()

    GroveTheme(dark = dark) {
        SystemBars(dark)
        HomeScaffold(vm = vm, dark = dark, currency = currency)
    }
}

@Composable
private fun HomeScaffold(
    vm: MainViewModel,
    dark: Boolean,
    currency: String,
) {
    val c = GroveTheme.colors
    val state by vm.state.collectAsStateWithLifecycle()
    val toast by vm.toast.collectAsStateWithLifecycle()
    val nav = rememberNavController()
    val currentRoute =
        nav
            .currentBackStackEntryAsState()
            .value
            ?.destination
            ?.route
    val activeTab = if (BottomTabs.any { it.route == currentRoute }) currentRoute!! else Dest.Home.route

    var showAdd by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Expense?>(null) }
    var onboarding by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(c.bgApp)) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            Box(modifier = Modifier.weight(1f)) {
                NavHost(
                    navController = nav,
                    startDestination = Dest.Home.route,
                    enterTransition = { fadeIn(tween(GroveTokens.MotionEnterSlow)) },
                    exitTransition = { fadeOut(tween(GroveTokens.MotionExitSlow)) },
                ) {
                    composable(Dest.Home.route) {
                        DashboardScreen(state = state, currency = currency, onNavigate = { nav.switchTab(it) }, onAddExpense = {
                            showAdd =
                                true
                        })
                    }
                    composable(Dest.History.route) {
                        HistoryScreen(
                            state = state,
                            currency = currency,
                            onDelete = vm::deleteExpense,
                            onEdit = { lite -> vm.findExpenseForEdit(lite.id) { editing = it } },
                        )
                    }
                    composable(Dest.Bills.route) {
                        BillsScreen(state = state, currency = currency, onToggleBill = vm::toggleBill, onAddBill = vm::addBill)
                    }
                    composable(Dest.Reports.route) {
                        ReportsScreen(state = state, currency = currency)
                    }
                    composable(Dest.Budget.route) {
                        BudgetScreen(
                            state = state,
                            currency = currency,
                            onUpdateBudget = vm::updateMonthBudget,
                            onUpdateCatBudget = vm::updateCategoryBudget,
                        )
                    }
                    composable(Dest.Settings.route) {
                        SettingsScreen(
                            state = state,
                            currency = currency,
                            dark = dark,
                            onToggleDark = { vm.toggleDark(dark) },
                            onReplayOnboarding = { onboarding = true },
                            onOpenBudget = { nav.navigate(Dest.Budget.route) },
                            onUpdateCurrency = vm::updateCurrency,
                        )
                    }
                }
            }
        }

        BottomNavBar(
            activeRoute = activeTab,
            onChange = { nav.switchTab(it) },
            onAdd = { showAdd = true },
            modifier = Modifier.align(Alignment.BottomCenter),
        )

        if (showAdd || editing != null) {
            AddExpenseSheet(
                categories = state.categories,
                currency = currency,
                editing = editing,
                onSave = vm::saveExpense,
                onDismiss = {
                    showAdd = false
                    editing = null
                },
            )
        }

        if (onboarding) {
            OnboardingFlow(
                userName = state.user?.name ?: "Mae",
                onDone = { result ->
                    vm.applyOnboarding(result.monthBudget, result.resetDay)
                    onboarding = false
                    nav.switchTab(Dest.Home.route)
                },
                onSkip = { onboarding = false },
            )
        }

        toast?.let { msg ->
            Row(
                modifier =
                    Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(bottom = 92.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(c.fg1)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = c.bgCard, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(8.dp))
                Text(msg, fontFamily = InterTight, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = c.bgCard)
            }
        }
    }
}

private fun NavController.switchTab(route: String) =
    navigate(route) {
        launchSingleTop = true
        restoreState = true
        popUpTo(graph.findStartDestination().id) { saveState = true }
    }

@Composable
private fun SystemBars(dark: Boolean) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !dark
        }
    }
}
