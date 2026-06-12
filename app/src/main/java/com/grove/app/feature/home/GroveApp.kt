package com.grove.app.feature.home

import android.app.Activity
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
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
import com.grove.app.data.GroveDefaults
import com.grove.app.data.model.CategoryKind
import com.grove.app.data.model.Expense
import com.grove.app.data.model.NotificationSettings
import com.grove.app.designsystem.catalog.CategoryVisuals
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import com.grove.app.designsystem.component.LocalNavAnimatedScope
import com.grove.app.designsystem.component.LocalSharedTransitionScope
import com.grove.app.designsystem.component.MorphLoader
import com.grove.app.designsystem.component.rememberBottomNavVisibilityConnection
import com.grove.app.designsystem.sound.GroveSound
import com.grove.app.designsystem.sound.LocalGroveSounds
import com.grove.app.designsystem.sound.rememberGroveSoundPlayer
import com.grove.app.designsystem.theme.GroveEase
import com.grove.app.designsystem.theme.GroveShapes
import com.grove.app.designsystem.theme.GroveSprings
import com.grove.app.designsystem.theme.GroveTheme
import com.grove.app.designsystem.theme.GroveTokens
import com.grove.app.designsystem.theme.InterTight
import com.grove.app.feature.addexpense.AddExpenseSheet
import com.grove.app.feature.bills.BillsScreen
import com.grove.app.feature.budget.BudgetScreen
import com.grove.app.feature.dashboard.DashboardScreen
import com.grove.app.feature.dashboard.dashboardSpendSnapshot
import com.grove.app.feature.history.HistoryScreen
import com.grove.app.feature.onboarding.OnboardingFlow
import com.grove.app.feature.reports.ReportsScreen
import com.grove.app.feature.settings.SettingsScreen

@Composable
fun GroveApp() {
    val context = LocalContext.current
    val vm: MainViewModel = viewModel { MainViewModel(context.applicationContext as android.app.Application) }
    val darkOverride by vm.darkOverride.collectAsStateWithLifecycle()
    val dark = darkOverride ?: true
    val currency by vm.currency.collectAsStateWithLifecycle()
    val notificationSettings by vm.notificationSettings.collectAsStateWithLifecycle()

    GroveTheme(dark = dark) {
        SystemBars(dark)
        HomeScaffold(vm = vm, dark = dark, currency = currency, notificationSettings = notificationSettings)
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun HomeScaffold(
    vm: MainViewModel,
    dark: Boolean,
    currency: String,
    notificationSettings: NotificationSettings,
) {
    val c = GroveTheme.colors
    val context = LocalContext.current
    val state by vm.state.collectAsStateWithLifecycle()
    val toast by vm.toast.collectAsStateWithLifecycle()
    val soundsEnabled by vm.soundsEnabled.collectAsStateWithLifecycle()
    val sounds = rememberGroveSoundPlayer(soundsEnabled)
    val view = LocalView.current
    val density = LocalDensity.current
    val nav = rememberNavController()
    val currentRoute =
        nav
            .currentBackStackEntryAsState()
            .value
            ?.destination
            ?.route
    val activeTab = currentRoute?.takeIf { route -> BottomTabs.any { it.route == route } }.orEmpty()

    var showAdd by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Expense?>(null) }
    var onboarding by rememberSaveable { mutableStateOf(false) }
    var navVisible by remember { mutableStateOf(true) }
    var lastToast by remember { mutableStateOf<String?>(null) }
    var rootSize by remember { mutableStateOf(IntSize.Zero) }
    var safeSpendTargetBounds by remember { mutableStateOf<Rect?>(null) }
    val spendTransfer = rememberSpendTransferController()
    val motionEnabled = remember(context) { systemAnimationsEnabled(context) }
    val safeSpendTargetAvailable = safeSpendTargetBounds != null
    val spendTargetReady = spendTransfer.event?.targetBounds != null && rootSize.width > 0 && rootSize.height > 0
    val spendSettlementProgress = spendTransfer.settlementProgress(motionEnabled)
    val navOffset by animateDpAsState(
        targetValue = if (navVisible) 0.dp else 96.dp,
        animationSpec = GroveSprings.standard(),
        label = "navHideOffset",
    )
    val navAlpha by animateFloatAsState(
        targetValue = if (navVisible) 1f else 0f,
        animationSpec = GroveEase.normal(),
        label = "navHideAlpha",
    )
    val bottomNavScrollConnection = rememberBottomNavVisibilityConnection { navVisible = it }

    LaunchedEffect(state.user?.onboardingCompleted) {
        if (state.user?.onboardingCompleted == false) onboarding = true
    }
    LaunchedEffect(currentRoute) {
        navVisible = true
    }
    LaunchedEffect(toast) {
        if (toast != null) lastToast = toast
    }
    LaunchedEffect(spendTransfer.event?.id, safeSpendTargetAvailable) {
        spendTransfer.attachTargetBounds(safeSpendTargetBounds)
    }
    LaunchedEffect(spendTransfer.event?.id, spendTargetReady, motionEnabled) {
        spendTransfer.event ?: return@LaunchedEffect
        if (!spendTargetReady) return@LaunchedEffect
        spendTransfer.playCurrent(view, motionEnabled)
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .onSizeChanged { rootSize = it }
                .background(c.bgApp),
    ) {
        CompositionLocalProvider(LocalGroveSounds provides sounds) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            Box(modifier = Modifier.weight(1f).nestedScroll(bottomNavScrollConnection)) {
                SharedTransitionLayout {
                CompositionLocalProvider(LocalSharedTransitionScope provides this@SharedTransitionLayout) {
                NavHost(
                    navController = nav,
                    startDestination = Dest.Home.route,
                    enterTransition = {
                        fadeIn(tween(GroveTokens.MotionEnterSlow, easing = GroveEase.Out)) +
                            slideInVertically(tween(GroveTokens.MotionEnterSlow, easing = GroveEase.Out)) { 6 }
                    },
                    exitTransition = {
                        fadeOut(tween(GroveTokens.MotionExitSlow, easing = GroveEase.Out)) +
                            slideOutVertically(tween(GroveTokens.MotionExitSlow, easing = GroveEase.Out)) { -6 }
                    },
                ) {
                    groveDestination(Dest.Home.route) {
                        DashboardScreen(
                            state = state,
                            currency = currency,
                            onNavigate = { nav.switchTab(it) },
                            onSafeSpendTargetBoundsChange = { safeSpendTargetBounds = it },
                            safeSpendAnimationKey = spendTransfer.event?.id,
                            safeSpendSettlementProgress = spendSettlementProgress,
                            spendSnapshot = spendTransfer.snapshot,
                        )
                    }
                    groveDestination(Dest.History.route) {
                        HistoryScreen(
                            state = state,
                            currency = currency,
                            onDelete = vm::deleteExpense,
                            onEdit = { lite -> vm.findExpenseForEdit(lite.id) { editing = it } },
                        )
                    }
                    groveDestination(Dest.Bills.route) {
                        BillsScreen(
                            state = state,
                            currency = currency,
                            onToggleBill = { id ->
                                vm.toggleBill(id)
                                sounds.play(GroveSound.Chime, volume = 0.35f)
                            },
                            onAddBill = vm::addBill,
                            onDeleteBill = vm::deleteBill,
                        )
                    }
                    groveDestination(Dest.Reports.route) {
                        ReportsScreen(state = state, currency = currency)
                    }
                    groveDestination(Dest.Budget.route) {
                        BudgetScreen(
                            state = state,
                            currency = currency,
                            onUpdateBudget = vm::updateMonthBudget,
                            onUpdateCatBudget = vm::updateCategoryBudget,
                        )
                    }
                    groveDestination(Dest.Settings.route) {
                        SettingsScreen(
                            state = state,
                            currency = currency,
                            dark = dark,
                            notificationSettings = notificationSettings,
                            soundsEnabled = soundsEnabled,
                            onToggleDark = { vm.toggleDark(dark) },
                            onToggleSounds = vm::toggleSounds,
                            onReplayOnboarding = { onboarding = true },
                            onOpenBudget = { nav.navigate(Dest.Budget.route) },
                            onUpdateCurrency = vm::updateCurrency,
                            onUpdateName = vm::updateUserName,
                            onUpdateResetDay = vm::updateResetDay,
                            onUpdateDailySafeSpend = vm::updateDailySafeSpend,
                            onUpdateBillAlerts = vm::updateBillAlerts,
                        )
                    }
                }
                }
                }
            }
        }

        BottomNavBar(
            activeRoute = activeTab,
            onChange = { nav.switchTab(it) },
            onAdd = { showAdd = true },
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .offset { IntOffset(0, with(density) { navOffset.roundToPx() }) }
                    .graphicsLayer { alpha = navAlpha },
        )

        AddExpenseHost(
            visible = showAdd || editing != null,
            editing = editing,
            state = state,
            currency = currency,
            safeSpendTargetBounds = safeSpendTargetBounds,
            spendTransfer = spendTransfer,
            onSaveExpense = { expense ->
                vm.saveExpense(expense)
                sounds.play(GroveSound.Tick)
            },
            onNavigateHome = {
                navVisible = true
                nav.switchTab(Dest.Home.route)
            },
            onDismiss = {
                showAdd = false
                editing = null
            },
        )

        OnboardingHost(
            visible = onboarding,
            userName = state.user?.name ?: GroveDefaults.DEFAULT_USER_NAME,
            currency = currency,
            onApply = { monthBudget, resetDay ->
                vm.applyOnboarding(monthBudget, resetDay)
                onboarding = false
                nav.switchTab(Dest.Home.route)
            },
        )

        SpendOverlayHost(spendTransfer = spendTransfer, rootSize = rootSize)

        BootVeil(visible = state.user == null)

        ToastHost(
            visible = toast != null,
            message = lastToast.orEmpty(),
            modifier = Modifier.align(Alignment.BottomCenter),
        )
        }
    }
}

@Composable
private fun AddExpenseHost(
    visible: Boolean,
    editing: Expense?,
    state: com.grove.app.data.BudgetState,
    currency: String,
    safeSpendTargetBounds: Rect?,
    spendTransfer: SpendTransferController,
    onSaveExpense: (Expense) -> Unit,
    onNavigateHome: () -> Unit,
    onDismiss: () -> Unit,
) {
    if (!visible) return

    AddExpenseSheet(
        categories = state.categories,
        currency = currency,
        editing = editing,
        onSave = { expense, originBounds ->
            val category = state.categories.firstOrNull { it.id == expense.categoryId }
            val animatedCategory =
                category?.takeIf {
                    editing == null && it.kind != CategoryKind.income && expense.amountMinor > 0L
                }
            val snapshot = if (animatedCategory != null) state.dashboardSpendSnapshot() else null
            onSaveExpense(expense)
            if (animatedCategory != null) {
                spendTransfer.start(
                    amountMinor = expense.amountMinor,
                    currency = currency,
                    categoryColor = CategoryVisuals.color(animatedCategory.iconKey),
                    origin = originBounds?.center,
                    targetBounds = safeSpendTargetBounds,
                    snapshot = snapshot,
                )
                onNavigateHome()
            }
        },
        onDismiss = onDismiss,
    )
}

@Composable
private fun OnboardingHost(
    visible: Boolean,
    userName: String,
    currency: String,
    onApply: (monthBudget: Double, resetDay: Int) -> Unit,
) {
    if (!visible) return

    OnboardingFlow(
        userName = userName,
        currency = currency,
        onDone = { result -> onApply(result.monthBudget, result.resetDay) },
        onSkip = { result -> onApply(result.monthBudget, result.resetDay) },
    )
}

@Composable
private fun SpendOverlayHost(
    spendTransfer: SpendTransferController,
    rootSize: IntSize,
) {
    spendTransfer.event?.let { event ->
        SpendMotionOverlay(
            event = event,
            targetBounds = event.targetBounds,
            rootSize = rootSize,
            travelProgress = spendTransfer.travelProgress,
            impactProgress = spendTransfer.impactProgress,
        )
    }
}

/**
 * Covers the first frames while Room delivers the initial state, so the user
 * never sees empty placeholders flash. Local-first means this is gone in well
 * under a second; the morphing loader keeps even that moment alive.
 */
@Composable
private fun BootVeil(visible: Boolean) {
    val c = GroveTheme.colors
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(GroveEase.fast()),
        exit = fadeOut(GroveEase.normal()),
    ) {
        Box(
            modifier = Modifier.fillMaxSize().background(c.bgApp),
            contentAlignment = Alignment.Center,
        ) {
            MorphLoader(size = 44.dp)
        }
    }
}

@Composable
private fun ToastHost(
    visible: Boolean,
    message: String,
    modifier: Modifier = Modifier,
) {
    val c = GroveTheme.colors
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(GroveEase.normal()) + slideInVertically(GroveSprings.standard()) { it / 2 },
        exit = fadeOut(GroveEase.fast()) + slideOutVertically(GroveEase.fast()) { it / 2 },
        modifier = modifier,
    ) {
        Row(
            modifier =
                Modifier
                    .navigationBarsPadding()
                    .padding(bottom = 92.dp)
                    .clip(GroveShapes.Chip)
                    .background(c.fg1)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.Check, contentDescription = null, tint = c.bgCard, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(8.dp))
            Text(message, fontFamily = InterTight, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = c.bgCard)
        }
    }
}

private fun androidx.navigation.NavGraphBuilder.groveDestination(
    route: String,
    content: @Composable () -> Unit,
) {
    composable(route) {
        CompositionLocalProvider(LocalNavAnimatedScope provides this) {
            content()
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
