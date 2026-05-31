package com.grove.app.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object GroveType {
    val body: TextStyle
        @Composable get() = TextStyle(fontFamily = InterTight, fontSize = GroveTokens.TextBody.sp, lineHeight = 22.sp, color = GroveTheme.colors.fg2)

    val sheetTitle: TextStyle
        @Composable get() = TextStyle(fontFamily = Fraunces, fontSize = GroveTokens.TextSheetTitle.sp, fontWeight = FontWeight.Medium, color = GroveTheme.colors.fg1)

    val rowTitle: TextStyle
        @Composable get() = TextStyle(fontFamily = InterTight, fontSize = GroveTokens.TextRowTitle.sp, fontWeight = FontWeight.Medium, color = GroveTheme.colors.fg1)

    val rowSub: TextStyle
        @Composable get() = TextStyle(fontFamily = InterTight, fontSize = GroveTokens.TextRowSub.sp, color = GroveTheme.colors.fg3)

    val sectionLabel: TextStyle
        @Composable get() = TextStyle(fontFamily = InterTight, fontSize = GroveTokens.TextSectionLabel.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.6.sp, color = GroveTheme.colors.fg2)

    val capLabel: TextStyle
        @Composable get() = TextStyle(fontFamily = InterTight, fontSize = GroveTokens.TextCapLabel.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.0.sp, color = GroveTheme.colors.fg3)

    val link: TextStyle
        @Composable get() = TextStyle(fontFamily = InterTight, fontSize = GroveTokens.TextLink.sp, fontWeight = FontWeight.SemiBold, color = GroveTheme.colors.accent)

    val fieldLabel: TextStyle
        @Composable get() = TextStyle(fontFamily = InterTight, fontSize = GroveTokens.TextFieldLabel.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.8.sp, color = GroveTheme.colors.fg3)

    val amount: TextStyle
        @Composable get() = TextStyle(fontFamily = InterTight, fontSize = GroveTokens.TextAmount.sp, fontWeight = FontWeight.SemiBold, color = GroveTheme.colors.fg1)
}

object Motion {
    val EnterFast = GroveTokens.MotionEnterFast
    val EnterMid = GroveTokens.MotionEnterMid
    val EnterSlow = GroveTokens.MotionEnterSlow
    val ExitFast = GroveTokens.MotionExitFast
    val ExitMid = GroveTokens.MotionExitMid
    val ExitSlow = GroveTokens.MotionExitSlow
    val Nav = GroveTokens.MotionNav
    val Spring = GroveTokens.SpringDefault
}
