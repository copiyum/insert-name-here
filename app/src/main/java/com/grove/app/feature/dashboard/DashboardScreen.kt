package com.grove.app.feature.dashboard

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.graphicsLayer
import com.grove.app.data.BudgetState
import com.grove.app.data.SpendPace
import com.grove.app.designsystem.component.AmbientBackdrop
import com.grove.app.designsystem.component.AnimatedMoneyText
import com.grove.app.designsystem.component.AppTopBar
import com.grove.app.designsystem.component.BotanicalEmptyState
import com.grove.app.designsystem.component.ExpenseRow
import com.grove.app.designsystem.component.GroveCard
import com.grove.app.designsystem.component.GroveCardList
import com.grove.app.designsystem.component.GroveCardVariant
import com.grove.app.designsystem.component.GroveHaptic
import com.grove.app.designsystem.component.HeroStatusChip
import com.grove.app.designsystem.component.IconCircleButton
import com.grove.app.designsystem.component.MoneyTextSize
import com.grove.app.designsystem.component.SectionHeader
import com.grove.app.designsystem.component.TickerMoneyText
import com.grove.app.designsystem.component.rememberBounceOverscroll
import com.grove.app.designsystem.component.charts.ArcProgress
import com.grove.app.designsystem.component.charts.WavyProgress
import com.grove.app.designsystem.component.groveClick
import com.grove.app.designsystem.component.groveFadeSlide
import com.grove.app.designsystem.component.groveScreenContentPadding
import com.grove.app.designsystem.component.moneyTextStyle
import com.grove.app.core.format.Money
import com.grove.app.designsystem.theme.GroveBorder
import com.grove.app.designsystem.theme.GroveShapes
import com.grove.app.designsystem.theme.GroveSprings
import com.grove.app.designsystem.theme.GroveTheme
import com.grove.app.designsystem.theme.GroveType
import com.grove.app.designsystem.theme.InterTight
import com.grove.app.designsystem.theme.JetBrainsMono
import com.grove.app.designsystem.theme.SpaceGrotesk
import com.grove.app.designsystem.theme.SpendStatusCopy
import com.grove.app.designsystem.theme.SpendTone
import com.grove.app.designsystem.theme.heroWeightFor
import com.grove.app.designsystem.theme.spaceGroteskAtWeight
import java.time.format.DateTimeFormatter

/** Fraction of today's allowance already spent: spent / (spent + still-safe), clamped 0..1. */
private fun todaySpendFraction(spentTodayMinor: Long, safeTodayMinor: Long): Float {
    val day = spentTodayMinor + safeTodayMinor
    return when {
        day > 0L -> (spentTodayMinor.toFloat() / day).coerceIn(0f, 1f)
        spentTodayMinor > 0L -> 1f
        else -> 0f
    }
}

private fun responsiveHeroRingSize(maxWidth: Dp): Dp =
    (maxWidth * 0.64f).coerceIn(216.dp, 280.dp)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(
    state: BudgetState,
    currency: String,
    onNavigate: (String) -> Unit,
    onSafeSpendTargetBoundsChange: (Rect) -> Unit = {},
    onHeroRingBoundsChange: (Rect) -> Unit = {},
    onHeroRingVisualsChange: (Color, Color, Float) -> Unit = { _, _, _ -> },
    suppressInitialProgressAnimation: Boolean = false,
    safeSpendAnimationKey: Any? = null,
    safeSpendSettlementProgress: Float? = null,
    spendSnapshot: DashboardSpendSnapshot? = null,
) {
    val c = GroveTheme.colors
    val listState = rememberLazyListState()
    val bounce = rememberBounceOverscroll()
    var mode by rememberSaveable { mutableStateOf("Ring") }
    val monthName = remember(state.today) { state.today.format(DateTimeFormatter.ofPattern("MMMM")) }
    val settlementProgress = safeSpendSettlementProgress?.coerceIn(0f, 1f)
    val activeSnapshot = spendSnapshot.takeIf { settlementProgress != null }
    val ui = remember(state, currency, activeSnapshot, settlementProgress) {
        state.dashboardUiState(currency, activeSnapshot, settlementProgress)
    }
    val safeSpendFromMinor = activeSnapshot?.safeTodayMinor
    val pace =
        when {
            ui.budgetLeftMinor < 0L -> SpendPace.Over
            ui.safePerDayMinor > 0L && ui.dailyAvgMinor.toDouble() > ui.safePerDayMinor.toDouble() * 1.12 -> SpendPace.Tight
            else -> SpendPace.Healthy
        }
    val statusSeed = state.today.toLocalDate().toEpochDay().toInt()
    val statusLabel = SpendStatusCopy.label(pace, statusSeed)
    val tone =
        when (pace) {
            SpendPace.Over -> SpendTone(c.danger, c.danger, statusLabel, healthy = false)
            SpendPace.Tight -> SpendTone(c.warn, c.warn, statusLabel, healthy = false)
            SpendPace.Healthy -> SpendTone(c.success, c.success, statusLabel, healthy = true)
        }
    val budgetLeftFraction =
        if (state.monthBudgetMinor > 0L) (ui.budgetLeftMinor.toFloat() / state.monthBudgetMinor).coerceIn(0f, 1f) else 1f
    // The ring tracks how much of TODAY'S safe-to-spend has been used:
    // spent today / (spent today + still-safe today).
    val pctToday = todaySpendFraction(ui.spentTodayMinor, ui.safeTodayMinor)
    val fromPctToday = activeSnapshot?.let { todaySpendFraction(it.spentTodayMinor, it.safeTodayMinor) }
    SideEffect {
        onHeroRingVisualsChange(tone.color, tone.deep, pctToday)
    }
    AmbientBackdrop(modifier = Modifier.fillMaxSize()) {
    CompositionLocalProvider(LocalOverscrollConfiguration provides null) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize().nestedScroll(bounce.connection).then(bounce.modifier),
        contentPadding = groveScreenContentPadding(),
    ) {
        item {
            AppTopBar(
                title = "Hi, ${state.user?.name?.takeIf { it.isNotBlank() } ?: "there"}",
                subtitle = "$monthName ${state.today.dayOfMonth} · ${state.daysLeft} days left",
                actions = { IconCircleButton(Icons.Outlined.Settings, "Settings") { onNavigate("settings") } },
            )
        }

        item {
            HomeModeTabs(
                selected = mode,
                onSelect = { mode = it },
                modifier = Modifier.padding(bottom = 18.dp),
            )
        }

        item {
            Box(modifier = Modifier.fillMaxWidth().groveFadeSlide(key = mode, distance = 6.dp, durationMillis = 400)) {
                when (mode) {
                    "Focus" -> {
                        FocusHero(
                            safeTodayMinor = ui.safeTodayMinor,
                            currency = currency,
                            remaining = "${Money.currencyLong(ui.budgetLeftMinor, 0, currency)} left · ${state.daysLeft}d",
                            pctSpent = pctToday,
                            tone = tone,
                            leftFraction = budgetLeftFraction,
                            onTargetBoundsChange = onSafeSpendTargetBoundsChange,
                            animationKey = safeSpendAnimationKey,
                            fromMinor = safeSpendFromMinor,
                            settlementProgress = settlementProgress,
                        )
                    }
                    else -> {
                        RingHero(
                            safeTodayMinor = ui.safeTodayMinor,
                            currency = currency,
                            remaining = "${Money.currencyLong(ui.budgetLeftMinor, 0, currency)} left · ${state.daysLeft}d",
                            pctSpent = pctToday,
                            tone = tone,
                            leftFraction = budgetLeftFraction,
                            onTargetBoundsChange = onSafeSpendTargetBoundsChange,
                            onRingBoundsChange = onHeroRingBoundsChange,
                            animationKey = safeSpendAnimationKey,
                            fromMinor = safeSpendFromMinor,
                            fromPct = fromPctToday,
                            suppressInitialProgressAnimation = suppressInitialProgressAnimation,
                            settlementProgress = settlementProgress,
                        )
                    }
                }
            }
        }

        item {
            StatGrid(ui.stats, modifier = Modifier.padding(top = 18.dp))
        }

        item {
            SectionHeader(
                "RECENT",
                modifier = Modifier.padding(top = 26.dp, bottom = 12.dp),
                linkText = "See all",
                onLink = { onNavigate("history") },
            )
        }

        item {
            if (ui.recent.isEmpty()) {
                BotanicalEmptyState(
                    title = "Log your first expense",
                    subtitle = "Log what you spend and today's number stays honest.",
                )
            } else {
                GroveCardList(ui.recent, padding = PaddingValues(horizontal = 16.dp)) { expense ->
                    ExpenseRow(expense, state.today, currency = currency, sharedElementKey = "expense-${expense.id}")
                }
            }
        }
    }
    }
    }
}

@Composable
private fun RingHero(
    safeTodayMinor: Long,
    currency: String,
    remaining: String,
    pctSpent: Float,
    tone: SpendTone,
    leftFraction: Float,
    onTargetBoundsChange: (Rect) -> Unit,
    onRingBoundsChange: (Rect) -> Unit,
    animationKey: Any?,
    fromMinor: Long?,
    fromPct: Float?,
    suppressInitialProgressAnimation: Boolean,
    settlementProgress: Float?,
) {
    val c = GroveTheme.colors
    val pulse = rememberHeroPulse(safeTodayMinor, settlementProgress)
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val ringSize = responsiveHeroRingSize(maxWidth)
        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(ringSize)
                        .onGloballyPositioned { onRingBoundsChange(it.boundsInRoot()) }
                        .graphicsLayer {
                            scaleX = pulse
                            scaleY = pulse
                        },
                contentAlignment = Alignment.Center,
            ) {
                ArcProgress(
                    pct = pctSpent,
                    color = tone.color,
                    colorDeep = tone.deep,
                    modifier = Modifier.fillMaxSize(),
                    stroke = 18f,
                    animationKey = animationKey ?: pctSpent,
                    fromPct = fromPct,
                    progressOverride = when {
                        settlementProgress != null -> pctSpent
                        suppressInitialProgressAnimation -> 0f
                        else -> null
                    },
                    snapToTargetWhenOverrideClears = true,
                ) {}

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "SAFE TO SPEND TODAY",
                        fontFamily = JetBrainsMono,
                        fontSize = 11.sp,
                        letterSpacing = 1.4.sp,
                        color = c.fg3,
                    )
                    Spacer(Modifier.height(6.dp))
                    TickerMoneyText(
                        minor = safeTodayMinor,
                        currency = currency,
                        style = moneyTextStyle(MoneyTextSize.Hero).copy(
                            fontFamily = spaceGroteskAtWeight(heroWeightFor(leftFraction)),
                        ),
                        modifier = Modifier.onGloballyPositioned { onTargetBoundsChange(it.boundsInRoot()) },
                        color = c.fg1,
                        fromMinor = fromMinor,
                        progress = settlementProgress,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(remaining, fontFamily = InterTight, fontSize = 13.sp, color = c.fg3)
                }
            }
            Spacer(Modifier.height(14.dp))
            HeroStatusChip(tone)
        }
    }
}

@Composable
private fun FocusHero(
    safeTodayMinor: Long,
    currency: String,
    remaining: String,
    pctSpent: Float,
    tone: SpendTone,
    leftFraction: Float,
    onTargetBoundsChange: (Rect) -> Unit,
    animationKey: Any?,
    fromMinor: Long?,
    settlementProgress: Float?,
) {
    val c = GroveTheme.colors
    val pulse = rememberHeroPulse(safeTodayMinor, settlementProgress)
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, start = 4.dp, end = 4.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("SAFE TO SPEND TODAY", style = GroveType.capLabel, color = c.fg3)
            HeroStatusChip(tone)
        }
        Spacer(Modifier.height(16.dp))
        TickerMoneyText(
            minor = safeTodayMinor,
            currency = currency,
            style = moneyTextStyle(MoneyTextSize.Hero).copy(
                fontFamily = spaceGroteskAtWeight(heroWeightFor(leftFraction)),
            ),
            modifier = Modifier
                .onGloballyPositioned { onTargetBoundsChange(it.boundsInRoot()) }
                .graphicsLayer {
                    scaleX = pulse
                    scaleY = pulse
                },
            color = c.fg1,
            fromMinor = fromMinor,
            progress = settlementProgress,
        )
        Spacer(Modifier.height(8.dp))
        Text(remaining, fontFamily = InterTight, fontSize = 13.sp, color = c.fg3)
        Spacer(Modifier.height(18.dp))
        WavyProgress(
            progress = pctSpent,
            color = tone.color,
            trackColor = c.bgMuted,
            animateWave = settlementProgress == null,
        )
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("${(pctSpent * 100).toInt()}% of today used", style = GroveType.rowSub, color = c.fg3)
            Text("Today", style = GroveType.rowSub, color = c.fg3)
        }
    }
}

/**
 * 1.0 -> 1.02 -> 1.0 spring pulse whenever the hero value changes, paused while
 * the spend-transfer overlay drives its own settlement choreography.
 */
@Composable
private fun rememberHeroPulse(value: Long, settlementProgress: Float?): Float {
    val pulse = remember { Animatable(1f) }
    var first by remember { mutableStateOf(true) }
    LaunchedEffect(value) {
        if (first) {
            first = false
            return@LaunchedEffect
        }
        if (settlementProgress != null) return@LaunchedEffect
        pulse.animateTo(1.02f, GroveSprings.snappy())
        pulse.animateTo(1f, GroveSprings.expressive())
    }
    return pulse.value
}

@Composable
private fun StatGrid(
    stats: List<HomeStat>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        stats.chunked(2).forEachIndexed { rowIndex, row ->
            Row(horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                row.forEachIndexed { colIndex, stat ->
                    val index = rowIndex * 2 + colIndex
                    StatTile(
                        stat.label,
                        stat.value,
                        stat.subtitle,
                        Modifier
                            .weight(1f)
                            .groveFadeSlide(key = stat.label, delayMillis = 20 + index * 40),
                    )
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatTile(
    label: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    animatedMinor: Long? = null,
    currency: String? = null,
    onAnimatedBoundsChange: ((Rect) -> Unit)? = null,
    animationKey: Any? = null,
    fromMinor: Long? = null,
    settlementProgress: Float? = null,
) {
    val c = GroveTheme.colors
    GroveCard(
        modifier = modifier,
        padding = PaddingValues(horizontal = 16.dp, vertical = 15.dp),
        variant = GroveCardVariant.Default,
    ) {
        Text(label, style = GroveType.capLabel, color = c.fg3)
        Spacer(Modifier.height(8.dp))
        if (animatedMinor != null && currency != null) {
            AnimatedMoneyText(
                minor = animatedMinor,
                currency = currency,
                modifier =
                    if (onAnimatedBoundsChange != null) {
                        Modifier.onGloballyPositioned { onAnimatedBoundsChange(it.boundsInRoot()) }
                    } else {
                        Modifier
                    },
                size = MoneyTextSize.Title,
                color = c.fg1,
                animationKey = animationKey ?: animatedMinor,
                fromMinor = fromMinor,
                progress = settlementProgress,
            )
        } else {
            Text(
                value,
                fontFamily = SpaceGrotesk,
                fontSize = 23.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.6).sp,
                color = c.fg1,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.height(3.dp))
        Text(subtitle, fontFamily = InterTight, fontSize = 11.5.sp, color = c.fg3.copy(alpha = 0.78f))
    }
}

@Composable
private fun HomeModeTabs(
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = GroveTheme.colors
    Row(
        modifier =
            modifier
                .clip(GroveShapes.Chip)
                .background(c.bgCard)
                .border(GroveBorder.Thin, c.border, GroveShapes.Chip)
                .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        listOf("Ring", "Focus").forEach { label ->
            val active = label == selected
            Box(
                modifier =
                    Modifier
                        .clip(GroveShapes.Chip)
                        .background(if (active) c.fg1 else androidx.compose.ui.graphics.Color.Transparent)
                        .groveClick(haptic = GroveHaptic.Light) { onSelect(label) }
                        .padding(horizontal = 13.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label,
                    fontFamily = InterTight,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (active) c.bgApp else c.fg3,
                )
            }
        }
    }
}
