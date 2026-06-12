package com.grove.app.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object GroveType {
    val body: TextStyle
        @Composable get() = TextStyle(fontFamily = InterTight, fontSize = GroveTokens.TextBody.sp, lineHeight = 22.sp, color = GroveTheme.colors.fg2)

    val sheetTitle: TextStyle
        @Composable get() = TextStyle(fontFamily = SpaceGrotesk, fontSize = GroveTokens.TextSheetTitle.sp, fontWeight = FontWeight.SemiBold, color = GroveTheme.colors.fg1)

    val rowTitle: TextStyle
        @Composable get() = TextStyle(fontFamily = SpaceGrotesk, fontSize = GroveTokens.TextRowTitle.sp, fontWeight = FontWeight.Medium, color = GroveTheme.colors.fg1)

    val rowSub: TextStyle
        @Composable get() = TextStyle(fontFamily = InterTight, fontSize = GroveTokens.TextRowSub.sp, color = GroveTheme.colors.fg3)

    val sectionLabel: TextStyle
        @Composable get() = TextStyle(fontFamily = JetBrainsMono, fontSize = GroveTokens.TextSectionLabel.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.2.sp, color = GroveTheme.colors.fg3)

    val capLabel: TextStyle
        @Composable get() = TextStyle(fontFamily = JetBrainsMono, fontSize = GroveTokens.TextCapLabel.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.8.sp, color = GroveTheme.colors.fg3)

    val link: TextStyle
        @Composable get() = TextStyle(fontFamily = InterTight, fontSize = GroveTokens.TextLink.sp, fontWeight = FontWeight.SemiBold, color = GroveTheme.colors.accent)

    val fieldLabel: TextStyle
        @Composable get() = TextStyle(fontFamily = JetBrainsMono, fontSize = GroveTokens.TextFieldLabel.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.8.sp, color = GroveTheme.colors.fg3)

    val amount: TextStyle
        @Composable get() = TextStyle(fontFamily = SpaceGrotesk, fontSize = GroveTokens.TextAmount.sp, fontWeight = FontWeight.SemiBold, fontFeatureSettings = "tnum", color = GroveTheme.colors.fg1)

    val appBarTitle: TextStyle
        @Composable get() = TextStyle(fontFamily = SpaceGrotesk, fontSize = 26.sp, fontWeight = FontWeight.Medium, letterSpacing = (-0.6).sp, color = GroveTheme.colors.fg1)
}
