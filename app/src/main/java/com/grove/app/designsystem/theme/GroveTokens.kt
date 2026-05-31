package com.grove.app.designsystem.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ─── Spacing scale ───────────────────────────────────────────────────────────
object GroveSpacing {
    val XS: Dp = 4.dp
    val SM: Dp = 8.dp
    val MD: Dp = 12.dp
    val LG: Dp = 16.dp
    val XL: Dp = 24.dp
    val XXL: Dp = 32.dp
}

// ─── Corner radii ────────────────────────────────────────────────────────────

object GroveRadius {
    val Container = 16.dp    // GroveCard, most containers
    val Nav = 22.dp         // Bottom nav bar
    val NavPill = 18.dp     // Active nav pill
    val NavPillEnd = 8.dp   // First/last pill end cap
    val Button = 16.dp
    val Chip = 999.dp        // Full circle
    val InputOutlined = 10.dp
    val InputFilled = 14.dp
    val Sheet = 28.dp
    val Tile = 14.dp        // IconTileRow background
    val Toggle = 999.dp     // Switch / progress bar
    val Stepper = 16.dp
    val Keypad = 14.dp
    val CatPicker = 14.dp   // Category grid item
    val DateRow = 14.dp
    val SmallTile = 8.dp
}

// ─── Sizes ───────────────────────────────────────────────────────────────────

object GroveSize {
    val NavItemHeight = 44.dp
    val AddButton = 44.dp
    val IconCircle = 38.dp
    val IconTile = 40.dp
    val CategoryIcon = 40.dp
    val CategoryIconSmall = 36.dp
    val SwipeAction = 44.dp
    val SwitchTrackW = 44.dp
    val SwitchTrackH = 26.dp
    val SwitchKnob = 20.dp
    val KeypadButtonH = 58.dp
    val Dot = 4.dp
    val StatusDot = 7.dp
}

// ─── Elevation ───────────────────────────────────────────────────────────────

object GroveElevation {
    val Card = 14.dp
    val Nav = 14.dp
}

// ─── Animation ───────────────────────────────────────────────────────────────

object GroveMotion {
    val DrawInMs = 1100
    val CountUpMs = 1000
    val BarMs = 600
    val ScreenEnterMs = 140
    val ScreenExitMs = 90
    val SwipeDismissMs = 280
}

// ─── Border widths ───────────────────────────────────────────────────────────

object GroveBorder {
    val Thin = 1.dp
    val Strong = 1.5.dp
    val AddButton = 2.dp
}

// ─── Backward-compatible alias ─────────────────────────────────────────────────

@Suppress("MemberVisibilityCanBePrivate")
object GroveTokens {
    val Spacing get() = GroveSpacing
    val Radius get() = GroveRadius
    val Size get() = GroveSize
    val Elevation get() = GroveElevation
    val Motion get() = GroveMotion
    val Border get() = GroveBorder

    val MotionEnterFast = GroveMotion.ScreenEnterMs
    val MotionEnterMid = GroveMotion.ScreenEnterMs
    val MotionEnterSlow = GroveMotion.ScreenEnterMs
    val MotionExitFast = GroveMotion.ScreenExitMs
    val MotionExitMid = GroveMotion.ScreenExitMs
    val MotionExitSlow = GroveMotion.ScreenExitMs
    val MotionNav = GroveMotion.ScreenExitMs
    val SpringDefault = 300
    val DrawInMs = GroveMotion.DrawInMs
    val CountUpMs = GroveMotion.CountUpMs
    val BarMs = GroveMotion.BarMs
    val SwipeDismissMs = GroveMotion.SwipeDismissMs

    val TextBody = 15
    val TextSheetTitle = 24
    val TextRowTitle = 16
    val TextRowSub = 13
    val TextSectionLabel = 12
    val TextCapLabel = 10
    val TextLink = 14
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
    val NavBg = androidx.compose.ui.graphics.Color.Unspecified
    val NavShadow = 14.dp
    val NavActiveText = androidx.compose.ui.graphics.Color.Unspecified
    val NavInactiveText = androidx.compose.ui.graphics.Color.Unspecified
}
