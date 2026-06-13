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
import androidx.compose.ui.graphics.Color
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
import com.grove.app.data.model.ExpenseInput
import com.grove.app.data.model.NotificationSettings
import com.grove.app.designsystem.catalog.CategoryVisuals
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import com.grove.app.designsystem.component.LocalNavAnimatedScope
import com.grove.app.designsystem.component.LocalSharedTransitionScope
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
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

private data class HeroRingVisuals(
    val color: Color,
    val colorDeep: Color,
    val pct: Float,
)

@Composable
fun GroveApp(startupDarkOverride: Boolean? = null) {
    val context = LocalContext.current
    val vm: MainViewModel = viewModel { MainViewModel(context.applicationContext as android.app.Application) }
    val themePrefs by vm.themePrefs.collectAsStateWithLifecycle()
    val dark = resolveDarkMode(themePrefs, startupDarkOverride)
    val currency by vm.currency.collectAsStateWithLifecycle()
    val notificationSettings by vm.notificationSettings.collectAsStateWithLifecycle()

    // Cold-start launch ceremony. rememberSaveable survives config changes (no replay on
    // rotation) but resets on process death — i.e. exactly once per cold start. Skipped
    // entirely under reduced motion so it can never read as a sluggish/broken wait.
    val motionEnabled = remember(context) { systemAnimationsEnabled(context) }
    var splashDone by rememberSaveable { mutableStateOf(false) }
    var heroRingBounds by remember { mutableStateOf<Rect?>(null) }
    var heroRingVisuals by remember { mutableStateOf<HeroRingVisuals?>(null) }
    var contentReady by remember { mutableStateOf(false) }
    var dashboardRevealProgress by remember { mutableStateOf(if (motionEnabled && !splashDone) 0f else 1f) }
    var dashboardRingProgressOverride by remember {
        mutableStateOf<Float?>(if (motionEnabled && !splashDone) 0f else null)
    }

    LaunchedEffect(splashDone, motionEnabled) {
        if (splashDone || !motionEnabled) {
            dashboardRevealProgress = 1f
            dashboardRingProgressOverride = null
        }
    }

    GroveTheme(dark = dark) {
        SystemBars(dark)
        Box(modifier = Modifier.fillMaxSize()) {
            HomeScaffold(
                vm = vm,
                dark = dark,
                currency = currency,
                notificationSettings = notificationSettings,
                onHeroRingBoundsChange = { heroRingBounds = it },
                onHeroRingVisualsChange = { color, colorDeep, pct ->
                    heroRingVisuals = HeroRingVisuals(color, colorDeep, pct)
                },
                onContentReady = { contentReady = it },
                suppressInitialProgressAnimation = dashboardRingProgressOverride != null,
                heroRingProgressOverride = dashboardRingProgressOverride,
                dashboardRevealProgress = dashboardRevealProgress,
            )
            if (!splashDone) {
                if (motionEnabled) {
                    SplashRing(
                        heroRingBounds = heroRingBounds,
                        heroRingColor = heroRingVisuals?.color,
                        heroRingColorDeep = heroRingVisuals?.colorDeep,
                        heroRingPct = heroRingVisuals?.pct,
                        contentReady = contentReady,
                        onRevealProgressChange = { dashboardRevealProgress = it },
                        onRingHandoffReady = { finalPct ->
                            dashboardRingProgressOverride = finalPct.coerceIn(0f, 1f)
                        },
                        onFinished = { splashDone = true },
                    )
                } else {
                    LaunchedEffect(Unit) {
                        splashDone = true
                    }
                }
            }
        }
    }
}

internal fun resolveDarkMode(
    themePrefs: ThemePrefs,
    startupDarkOverride: Boolean?,
): Boolean =
    themePrefs.darkOverride ?: startupDarkOverride ?: true

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun HomeScaffold(
    vm: MainViewModel,
    dark: Boolean,
    currency: String,
    notificationSettings: NotificationSettings,
    onHeroRingBoundsChange: (Rect) -> Unit = {},
    onHeroRingVisualsChange: (Color, Color, Float) -> Unit = { _, _, _ -> },
    onContentReady: (Boolean) -> Unit = {},
    suppressInitialProgressAnimation: Boolean = false,
    heroRingProgressOverride: Float? = null,
    dashboardRevealProgress: Float = 1f,
) {
    val c = GroveTheme.colors
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
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
    val hazeState = remember { HazeState() }
    val motionEnabled = remember(context) { systemAnimationsEnabled(context) }
    val safeSpendTargetAvailable = safeSpendTargetBounds != null
    val spendTargetReady = spendTransfer.event?.targetBounds != null && rootSize.width > 0 && rootSize.height > 0
    val spendSettlementProgress = spendTransfer.settlementProgress(motionEnabled)
    val contentReveal = dashboardRevealProgress.coerceIn(0f, 1f)
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
    // Signal the splash overlay once the budget data has actually loaded, so it only
    // hands off onto a fully-populated dashboard (no post-morph reload/pop-in).
    LaunchedEffect(state.user != null) { onContentReady(state.user != null) }
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .graphicsLayer {
                    alpha = contentReveal
                },
        ) {
            Box(modifier = Modifier.weight(1f).hazeSource(state = hazeState).nestedScroll(bottomNavScrollConnection)) {
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
                            onHeroRingBoundsChange = onHeroRingBoundsChange,
                            onHeroRingVisualsChange = onHeroRingVisualsChange,
                            suppressInitialProgressAnimation = suppressInitialProgressAnimation,
                            heroRingProgressOverride = heroRingProgressOverride,
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
                            onToggleBill = { bill ->
                                vm.toggleBill(bill)
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
            hazeState = hazeState,
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .offset { IntOffset(0, with(density) { navOffset.roundToPx() }) }
                    .graphicsLayer { alpha = navAlpha * contentReveal },
        )

        AddExpenseHost(
            visible = showAdd || editing != null,
            editing = editing,
            state = state,
            currency = currency,
            safeSpendTargetBounds = safeSpendTargetBounds,
            spendTransfer = spendTransfer,
            onSaveExpense = { input ->
                vm.saveExpense(input)
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
    onSaveExpense: (ExpenseInput) -> Unit,
    onNavigateHome: () -> Unit,
    onDismiss: () -> Unit,
) {
    if (!visible) return

    AddExpenseSheet(
        categories = state.categories,
        currency = currency,
        editing = editing,
        onSave = { input, originBounds ->
            val category = state.categories.firstOrNull { it.id == input.categoryId }
            val animatedCategory =
                category?.takeIf {
                    editing == null && it.kind != CategoryKind.income && input.amountMinor > 0L
                }
            val snapshot = if (animatedCategory != null) state.dashboardSpendSnapshot() else null
            if (animatedCategory != null) {
                spendTransfer.start(
                    amountMinor = input.amountMinor,
                    currency = currency,
                    categoryColor = CategoryVisuals.color(animatedCategory.iconKey),
                    origin = originBounds?.center,
                    targetBounds = safeSpendTargetBounds,
                    snapshot = snapshot,
                )
                onNavigateHome()
            }
            onSaveExpense(input)
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
