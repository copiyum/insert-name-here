package com.grove.app.feature.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grove.app.data.SpendPace
import com.grove.app.designsystem.component.GroveHaptic
import com.grove.app.designsystem.component.PlantMascot
import com.grove.app.designsystem.component.PresetChipRow
import com.grove.app.designsystem.component.PrimaryButton
import com.grove.app.designsystem.component.Stepper
import com.grove.app.designsystem.component.groveClick
import com.grove.app.core.format.Currencies
import com.grove.app.core.format.ordinal
import com.grove.app.designsystem.sound.GroveSound
import com.grove.app.designsystem.sound.LocalGroveSounds
import com.grove.app.designsystem.theme.GroveEase
import com.grove.app.designsystem.theme.GroveShapes
import com.grove.app.designsystem.theme.GroveSprings
import com.grove.app.designsystem.theme.GroveTheme
import com.grove.app.designsystem.theme.InterTight
import com.grove.app.designsystem.theme.JetBrainsMono
import com.grove.app.designsystem.theme.SpaceGrotesk

data class OnboardingResult(val monthBudget: Double, val resetDay: Int)

private const val STEPS = 3

@Composable
fun OnboardingFlow(userName: String = "there", currency: String = "INR", onDone: (OnboardingResult) -> Unit, onSkip: (OnboardingResult) -> Unit) {
    val c = GroveTheme.colors
    val sounds = LocalGroveSounds.current
    var step by rememberSaveable { mutableIntStateOf(0) }
    var budget by rememberSaveable { mutableIntStateOf(35000) }
    var resetDay by rememberSaveable { mutableIntStateOf(1) }

    Column(modifier = Modifier.fillMaxSize().background(c.bgApp).statusBarsPadding()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (step > 0) {
                Box(
                    modifier = Modifier.size(38.dp).clip(GroveShapes.Chip).background(c.bgCard).border(1.dp, c.border, GroveShapes.Chip).groveClick { step-- },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = c.fg2, modifier = Modifier.size(18.dp))
                }
            } else {
                Spacer(Modifier.width(38.dp))
            }

            // Seedling-stage progress: each dot is a step; the current one stretches.
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                repeat(STEPS) { i ->
                    val on = i == step
                    val width: Dp by animateDpAsState(if (on) 22.dp else 7.dp, animationSpec = GroveSprings.standard(), label = "onbDot")
                    Box(modifier = Modifier.size(width = width, height = 7.dp).clip(GroveShapes.Chip).background(if (on) c.accent else if (i < step) c.accentSoft else c.bone))
                }
            }

            if (step < STEPS - 1) {
                Text(
                    "Skip",
                    fontFamily = InterTight,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = c.fg3,
                    modifier =
                        Modifier
                            .groveClick(haptic = GroveHaptic.Light) {
                                onSkip(OnboardingResult(budget.toDouble(), resetDay))
                            }.padding(8.dp),
                )
            } else {
                Spacer(Modifier.width(38.dp))
            }
        }

        Box(
            modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 28.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    (fadeIn(GroveEase.normal()) + slideInVertically(GroveSprings.standard()) { it / 14 }) togetherWith fadeOut(GroveEase.fast())
                },
                label = "onbStep",
            ) { s ->
                when (s) {
                    0 -> StepIntro()
                    1 -> StepPlantNumber(budget, currency, resetDay, onBudget = { budget = it }, onResetDay = { resetDay = it })
                    else -> StepGrow(userName, budget, resetDay, currency)
                }
            }
        }

        Box(modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(start = 24.dp, end = 24.dp, top = 14.dp, bottom = 28.dp)) {
            if (step < STEPS - 1) {
                PrimaryButton(if (step == 0) "Begin" else "Continue", onClick = { step++ }, modifier = Modifier.fillMaxWidth())
            } else {
                PrimaryButton(
                    "Enter Grove",
                    onClick = {
                        sounds?.play(GroveSound.Chime)
                        onDone(OnboardingResult(budget.toDouble(), resetDay))
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun StepIntro() {
    Column {
        PlantMascot(growth = 0.35f, pace = SpendPace.Healthy, size = 120.dp)
        Spacer(Modifier.height(26.dp))
        OnbTitle("Meet Grove")
        Spacer(Modifier.height(12.dp))
        OnbSub("One number — what's safe to spend today. Grove keeps it honest as the month moves, quietly, day by day.")
    }
}

@Composable
private fun StepPlantNumber(budget: Int, currency: String, resetDay: Int, onBudget: (Int) -> Unit, onResetDay: (Int) -> Unit) {
    val c = GroveTheme.colors
    Column {
        OnbKicker("One number")
        OnbTitle("Plant one number")
        Spacer(Modifier.height(12.dp))
        OnbSub("The total you'd like to spend each month. Everything else grows from this — and you can fine-tune categories later.")
        Spacer(Modifier.height(28.dp))
        Stepper(value = budget, onChange = onBudget, step = 1000, min = 1000, currency = currency)
        Spacer(Modifier.height(6.dp))
        val symbol = Currencies.current(currency).symbol
        PresetChipRow(items = listOf(20000, 35000, 50000, 75000), label = { "$symbol${"%,d".format(it)}" }, onClick = onBudget, selected = { it == budget })

        Spacer(Modifier.height(26.dp))
        Text(
            "MONTH RESETS ON",
            fontFamily = JetBrainsMono,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            letterSpacing = 1.2.sp,
            color = c.fg3,
        )
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(1 to "Start of month", 15 to "Mid-month").forEach { (d, label) ->
                val on = resetDay == d
                Column(
                    modifier =
                        Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (on) c.accent.copy(alpha = 0.08f) else c.bgCard)
                            .border(1.5.dp, if (on) c.accent else c.border, RoundedCornerShape(14.dp))
                            .groveClick(haptic = GroveHaptic.Light) { onResetDay(d) }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(ordinal(d), fontFamily = SpaceGrotesk, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = c.fg1)
                    Text(label, fontFamily = InterTight, fontSize = 11.5.sp, color = c.fg3)
                }
            }
        }
    }
}

@Composable
private fun StepGrow(userName: String, budget: Int, resetDay: Int, currency: String) {
    val c = GroveTheme.colors
    Column {
        PlantMascot(growth = 0.9f, pace = SpendPace.Healthy, size = 120.dp)
        Spacer(Modifier.height(26.dp))
        OnbTitle("You're set, $userName")
        Spacer(Modifier.height(12.dp))
        OnbSub("Spend, log, glance. The number adjusts in real time, so you always know where today stands.")
        Spacer(Modifier.height(24.dp))
        Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(c.bgCard).border(1.dp, c.border, RoundedCornerShape(16.dp)).padding(horizontal = 18.dp)) {
            val symbol = Currencies.current(currency).symbol
            SummaryRow("Monthly budget", "$symbol${"%,d".format(budget)}", divider = false)
            SummaryRow("Resets", "${ordinal(resetDay)} of each month", divider = true)
            SummaryRow("Safe to spend / day", "~$symbol${Math.round(budget / 30.0)}", divider = true)
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, divider: Boolean) {
    val c = GroveTheme.colors
    if (divider) HorizontalDivider(color = c.border)
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontFamily = InterTight, fontSize = 13.5.sp, color = c.fg2)
        Text(value, fontFamily = InterTight, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = c.fg1)
    }
}

@Composable
private fun OnbKicker(text: String) {
    val c = GroveTheme.colors
    Text(text.uppercase(), fontFamily = JetBrainsMono, fontWeight = FontWeight.Medium, fontSize = 12.sp, letterSpacing = 1.2.sp, color = if (c.isDark) c.accentSoft else c.accentDeep, modifier = Modifier.padding(bottom = 10.dp))
}

@Composable
private fun OnbTitle(text: String) {
    Text(text, fontFamily = SpaceGrotesk, fontWeight = FontWeight.Medium, fontSize = 30.sp, lineHeight = 33.sp, color = GroveTheme.colors.fg1)
}

@Composable
private fun OnbSub(text: String) {
    Text(text, fontFamily = InterTight, fontSize = 15.sp, lineHeight = 22.5.sp, color = GroveTheme.colors.fg2, textAlign = TextAlign.Start, modifier = Modifier.widthIn(max = 320.dp))
}
