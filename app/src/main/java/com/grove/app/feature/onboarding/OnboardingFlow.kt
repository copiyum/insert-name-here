package com.grove.app.feature.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grove.app.designsystem.component.LeafGlyph
import com.grove.app.designsystem.component.PresetChipRow
import com.grove.app.designsystem.component.PrimaryButton
import com.grove.app.designsystem.component.Stepper
import com.grove.app.designsystem.format.Currencies
import com.grove.app.designsystem.format.ordinal
import com.grove.app.designsystem.theme.Fraunces
import com.grove.app.designsystem.theme.GroveTheme
import com.grove.app.designsystem.theme.InterTight

data class OnboardingResult(val monthBudget: Double, val resetDay: Int)

private const val STEPS = 4

@Composable
fun OnboardingFlow(userName: String = "there", currency: String = "USD", onDone: (OnboardingResult) -> Unit, onSkip: () -> Unit) {
    val c = GroveTheme.colors
    var step by rememberSaveable { mutableIntStateOf(0) }
    var budget by rememberSaveable { mutableIntStateOf(1500) }
    var resetDay by rememberSaveable { mutableIntStateOf(1) }

    Column(modifier = Modifier.fillMaxSize().background(c.bgApp).statusBarsPadding()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (step > 0) {
                Box(
                    modifier = Modifier.size(38.dp).clip(RoundedCornerShape(999.dp)).background(c.bgCard).border(1.dp, c.border, RoundedCornerShape(999.dp)).clickable { step-- },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = c.fg2, modifier = Modifier.size(18.dp))
                }
            } else {
                Spacer(Modifier.width(38.dp))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                repeat(STEPS) { i ->
                    val on = i == step
                    val width: Dp by animateDpAsState(if (on) 22.dp else 7.dp, label = "onbDot")
                    Box(modifier = Modifier.size(width = width, height = 7.dp).clip(RoundedCornerShape(999.dp)).background(if (on) c.accent else if (i < step) c.accentSoft else c.bone))
                }
            }

            if (step < STEPS - 1) {
                Text("Skip", fontFamily = InterTight, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = c.fg3, modifier = Modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onSkip() }.padding(8.dp))
            } else {
                Spacer(Modifier.width(38.dp))
            }
        }

        Box(
            modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 28.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            AnimatedContent(targetState = step, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "onbStep") { s ->
                when (s) {
                    0 -> StepWelcome()
                    1 -> StepBudget(budget, currency) { budget = it }
                    2 -> StepResetDay(resetDay) { resetDay = it }
                    else -> StepSummary(userName, budget, resetDay, currency)
                }
            }
        }

        Box(modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(start = 24.dp, end = 24.dp, top = 14.dp, bottom = 28.dp)) {
            if (step < STEPS - 1) {
                PrimaryButton(if (step == 0) "Get started" else "Continue", onClick = { step++ }, modifier = Modifier.fillMaxWidth())
            } else {
                PrimaryButton("Enter Grove", onClick = { onDone(OnboardingResult(budget.toDouble(), resetDay)) }, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun StepWelcome() {
    val c = GroveTheme.colors
    Column {
        OnbEmblem(success = false) { LeafGlyph(60, c.accent, 1f) }
        Spacer(Modifier.height(26.dp))
        OnbTitle("Welcome to Grove")
        Spacer(Modifier.height(12.dp))
        OnbSub("A calmer way to budget. Grove shows you one number — what's safe to spend today — so the rest can fade into the background.")
    }
}

@Composable
private fun StepBudget(budget: Int, currency: String, onBudget: (Int) -> Unit) {
    Column {
        OnbKicker("Step 1")
        OnbTitle("What's your monthly budget?")
        Spacer(Modifier.height(12.dp))
        OnbSub("The total you'd like to spend each month. You can fine-tune categories later.")
        Spacer(Modifier.height(28.dp))
        Stepper(value = budget, onChange = onBudget, step = 50, min = 100, currency = currency)
        Spacer(Modifier.height(6.dp))
        val symbol = Currencies.current(currency).symbol
        PresetChipRow(items = listOf(1000, 1500, 2000, 2500), label = { "$symbol${"%,d".format(it)}" }, onClick = onBudget, selected = { it == budget })
    }
}

@Composable
private fun StepResetDay(resetDay: Int, onResetDay: (Int) -> Unit) {
    val c = GroveTheme.colors
    Column {
        OnbKicker("Step 2")
        OnbTitle("When does your month reset?")
        Spacer(Modifier.height(12.dp))
        OnbSub("Usually payday or the 1st. Grove starts a fresh budget on this day.")
        Spacer(Modifier.height(24.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            listOf(1 to "Start of month", 15 to "Mid-month").forEach { (d, label) ->
                val on = resetDay == d
                Column(
                    modifier = Modifier
                        .fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(if (on) c.accent.copy(alpha = 0.08f) else c.bgCard)
                        .border(1.5.dp, if (on) c.accent else c.border, RoundedCornerShape(16.dp)).clickable { onResetDay(d) }.padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(ordinal(d), fontFamily = Fraunces, fontWeight = FontWeight.Medium, fontSize = 18.sp, color = c.fg1)
                    Text(label, fontFamily = InterTight, fontSize = 12.5.sp, color = c.fg3)
                }
            }
        }
    }
}

@Composable
private fun StepSummary(userName: String, budget: Int, resetDay: Int, currency: String) {
    val c = GroveTheme.colors
    Column {
        OnbEmblem(success = true) { Icon(Icons.Outlined.Check, contentDescription = null, tint = c.fgOnFern, modifier = Modifier.size(44.dp)) }
        Spacer(Modifier.height(26.dp))
        OnbTitle("You're all set, $userName")
        Spacer(Modifier.height(12.dp))
        OnbSub("Here's your plan. You can change anything from the Budget tab whenever you like.")
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
private fun OnbEmblem(success: Boolean, content: @Composable () -> Unit) {
    val c = GroveTheme.colors
    Box(
        modifier = Modifier.size(96.dp).clip(RoundedCornerShape(30.dp)).background(if (success) c.accent else c.accent.copy(alpha = 0.12f).compositeOver(c.bgCard)),
        contentAlignment = Alignment.Center,
    ) { content() }
}

@Composable
private fun OnbKicker(text: String) {
    val c = GroveTheme.colors
    Text(text.uppercase(), fontFamily = InterTight, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, letterSpacing = 1.2.sp, color = if (c.isDark) c.accentSoft else c.accentDeep, modifier = Modifier.padding(bottom = 10.dp))
}

@Composable
private fun OnbTitle(text: String) {
    Text(text, fontFamily = Fraunces, fontWeight = FontWeight.Normal, fontSize = 30.sp, lineHeight = 33.sp, color = GroveTheme.colors.fg1)
}

@Composable
private fun OnbSub(text: String) {
    Text(text, fontFamily = InterTight, fontSize = 15.sp, lineHeight = 22.5.sp, color = GroveTheme.colors.fg2, textAlign = TextAlign.Start, modifier = Modifier.widthIn(max = 320.dp))
}
