package com.grove.app.feature.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grove.app.designsystem.theme.GroveBorder
import com.grove.app.designsystem.theme.GroveTheme
import com.grove.app.designsystem.theme.GroveRadius
import com.grove.app.designsystem.theme.GroveShapes
import com.grove.app.designsystem.theme.GroveSize
import com.grove.app.designsystem.theme.GroveSpacing
import com.grove.app.designsystem.theme.InterTight

private const val CENTER_SLOT = 2
private const val SLOTS = 5

private fun slotOf(index: Int) = if (index < CENTER_SLOT) index else index + 1

@Composable
fun BottomNavBar(
    activeRoute: String,
    onChange: (String) -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = GroveTheme.colors
    val activeIndex = BottomTabs.indexOfFirst { it.route == activeRoute }.coerceAtLeast(0)
    val activeSlot = slotOf(activeIndex)
    val itemHeight = GroveSize.NavItemHeight

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 18.dp, end = 18.dp, bottom = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = GroveShapes.Nav,
            color = c.navBg,
            shadowElevation = 10.dp,
            border = BorderStroke(1.dp, if (c.isDark) Color(0x1FFFFFFF) else c.borderStrong),
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .clip(GroveShapes.Nav)
                    .padding(GroveSpacing.XS),
                contentAlignment = Alignment.CenterStart,
            ) {
                val slotWidth = maxWidth / SLOTS

                val pillOffset: Dp by animateDpAsState(
                    targetValue = slotWidth * activeSlot,
                    animationSpec = spring(dampingRatio = 0.9f, stiffness = Spring.StiffnessMediumLow),
                    label = "navPill",
                )

                val pillShape = remember(activeSlot) {
                    when (activeSlot) {
                        0 -> androidx.compose.foundation.shape.RoundedCornerShape(
                            topStart = GroveRadius.NavPill,
                            topEnd = GroveRadius.NavPillEnd,
                            bottomEnd = GroveRadius.NavPillEnd,
                            bottomStart = GroveRadius.NavPill,
                        )
                        SLOTS - 1 -> androidx.compose.foundation.shape.RoundedCornerShape(
                            topStart = GroveRadius.NavPillEnd,
                            topEnd = GroveRadius.NavPill,
                            bottomEnd = GroveRadius.NavPill,
                            bottomStart = GroveRadius.NavPillEnd,
                        )
                        else -> GroveShapes.NavPill
                    }
                }

                Box(
                    modifier = Modifier
                        .offset { IntOffset(pillOffset.roundToPx(), 0) }
                        .width(slotWidth)
                        .height(itemHeight)
                        .clip(pillShape)
                        .background(c.accent),
                )

                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    repeat(SLOTS) { slot ->
                        if (slot == CENTER_SLOT) {
                            Box(
                                modifier = Modifier.width(slotWidth).height(itemHeight),
                                contentAlignment = Alignment.Center,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(GroveSize.AddButton)
                                        .clip(GroveShapes.Chip)
                                        .background(c.accent)
                                        .border(GroveBorder.Thin, c.accentDeep, GroveShapes.Chip)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null,
                                            role = Role.Button,
                                            onClick = onAdd,
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(Icons.Filled.Add, contentDescription = "Add expense", tint = c.fgOnFern, modifier = Modifier.size(24.dp))
                                }
                            }
                        } else {
                            val tab = BottomTabs[if (slot < CENTER_SLOT) slot else slot - 1]
                            val isActive = tab.route == activeRoute
                            val textColor by animateColorAsState(
                                targetValue = if (isActive) c.navActiveText else c.navInactiveText,
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                label = "navText",
                            )
                            Box(
                                modifier = Modifier
                                    .width(slotWidth)
                                    .height(itemHeight)
                                    .clip(pillShape)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        role = Role.Tab,
                                    ) { onChange(tab.route) },
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    tab.label,
                                    fontFamily = InterTight,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = textColor,
                                )
                            }
                        }
                    }
                }

                if (c.isDark) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = (-5).dp)
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color(0x0DFFFFFF)),
                    )
                }
            }
        }
    }
}
