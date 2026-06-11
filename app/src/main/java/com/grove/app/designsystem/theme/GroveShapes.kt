package com.grove.app.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp


object GroveShapes {
    val Container = RoundedCornerShape(GroveRadius.Container)
    val Nav = RoundedCornerShape(GroveRadius.Nav)
    val NavPill = RoundedCornerShape(GroveRadius.NavPill)
    val Button = RoundedCornerShape(GroveRadius.Button)
    val Chip = RoundedCornerShape(GroveRadius.Chip)
    val Tile = RoundedCornerShape(GroveRadius.Tile)
    val InputOutlined = RoundedCornerShape(GroveRadius.InputOutlined)
    val InputFilled = RoundedCornerShape(GroveRadius.InputFilled)
    val SheetTop = RoundedCornerShape(topStart = GroveRadius.Sheet, topEnd = GroveRadius.Sheet)
    val Keypad = RoundedCornerShape(GroveRadius.Keypad)
    val CatPicker = RoundedCornerShape(GroveRadius.CatPicker)
    val DateRow = RoundedCornerShape(GroveRadius.DateRow)
    val SmallTile = RoundedCornerShape(GroveRadius.SmallTile)
    val Toggle = RoundedCornerShape(GroveRadius.Toggle)
    val Stepper = RoundedCornerShape(GroveRadius.Stepper)
}
