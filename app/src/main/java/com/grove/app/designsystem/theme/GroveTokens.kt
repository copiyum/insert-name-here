package com.grove.app.designsystem.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object GroveSpacing {
    val XS: Dp = 4.dp
    val SM: Dp = 8.dp
    val MD: Dp = 12.dp
    val LG: Dp = 16.dp
    val XL: Dp = 24.dp
    val XXL: Dp = 32.dp
}

object GroveRadius {
    val Container = 16.dp
    val Button = 12.dp
    val Chip = 999.dp
    val InputOutlined = 16.dp
    val Sheet = 24.dp
    val Tile = 16.dp
    val Toggle = 999.dp
    val Stepper = 14.dp
    val CatPicker = 16.dp
    val SmallTile = 8.dp
}


object GroveSize {
    val NavItemHeight = 49.dp
    val AddButton = 30.dp
    val IconCircle = 42.dp
    val IconTile = 40.dp
    val CategoryIcon = 40.dp
    val SwipeAction = 44.dp
    val SwitchTrackW = 44.dp
    val SwitchTrackH = 26.dp
    val SwitchKnob = 20.dp
    val StatusDot = 7.dp

    val NavClearance = 108.dp
}

object GroveMotion {
    val ScreenEnterMs = 140
    val ScreenExitMs = 90
}

object GroveBorder {
    val Thin = 1.dp
    val Strong = 1.5.dp
    val AddButton = 2.dp
}

@Suppress("MemberVisibilityCanBePrivate")
object GroveTokens {
    val Spacing get() = GroveSpacing
    val Radius get() = GroveRadius
    val Size get() = GroveSize
    val Border get() = GroveBorder

    val MotionEnterSlow = GroveMotion.ScreenEnterMs
    val MotionExitSlow = GroveMotion.ScreenExitMs

    val TextBody = 14
    val TextSheetTitle = 24
    val TextRowTitle = 15
    val TextRowSub = 13
    val TextSectionLabel = 11
    val TextCapLabel = 10
    val TextLink = 13
    val TextFieldLabel = 11
    val TextAmount = 16

    val RowPaddingV = GroveSpacing.MD
    val IconTextGap = GroveSpacing.SM
    val IconTileSize = GroveSize.IconTile
    val CardPadding = GroveSpacing.LG
    val SheetPaddingH = GroveSpacing.XL
    val SheetPaddingB = GroveSpacing.XL
    val SectionTopPadding = GroveSpacing.XL
    val SectionBottomPadding = GroveSpacing.SM
    val SwipeActionGap = GroveSpacing.SM
    val SwipeActionSize = GroveSize.SwipeAction
    val SwipeRevealWidth = GroveSize.SwipeAction * 2 + GroveSpacing.SM * 2
    val ChipGap = GroveSpacing.SM
    val PresetChipGap = GroveSpacing.SM
}
