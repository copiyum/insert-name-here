package com.grove.app.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grove.app.designsystem.theme.GroveShapes
import com.grove.app.designsystem.theme.GroveSize
import com.grove.app.designsystem.theme.GroveTheme
import com.grove.app.designsystem.theme.InterTight
import com.grove.app.designsystem.component.GroveHaptic
import com.grove.app.designsystem.component.groveClick

@Composable
fun BottomNavBar(
    activeRoute: String,
    onChange: (String) -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = GroveTheme.colors
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = c.navBg,
        shadowElevation = 0.dp,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(c.border))
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(start = 6.dp, end = 6.dp, top = 5.dp, bottom = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BottomTabs.take(2).forEach { tab ->
                    NavItem(tab, tab.icon(), activeRoute, onChange, Modifier.weight(1f))
                }
                AddTabItem(onAdd, Modifier.weight(1f))
                BottomTabs.drop(2).forEach { tab ->
                    NavItem(tab, tab.icon(), activeRoute, onChange, Modifier.weight(1f))
                }
            }
        }
    }
}

private fun Dest.icon(): ImageVector =
    when (this) {
        Dest.Home -> Icons.Outlined.Home
        Dest.History -> Icons.Outlined.History
        Dest.Reports -> Icons.Outlined.BarChart
        Dest.Bills -> Icons.Outlined.Receipt
        Dest.Settings, Dest.Budget -> Icons.Outlined.Home
    }

@Composable
private fun AddTabItem(
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = GroveTheme.colors
    Box(
        modifier =
            modifier
                .height(GroveSize.NavItemHeight)
                .groveClick(role = Role.Button, onClick = onAdd),
        contentAlignment = Alignment.Center,
    ) {
        androidx.compose.foundation.layout.Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier =
                    Modifier
                        .size(GroveSize.AddButton)
                        .clip(GroveShapes.Chip)
                        .background(c.accent.copy(alpha = if (c.isDark) 0.18f else 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add expense", tint = c.accent, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(2.dp))
            Text(
                "Add",
                fontFamily = InterTight,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = c.accent,
            )
        }
    }
}

@Composable
private fun NavItem(
    tab: Dest,
    icon: ImageVector,
    activeRoute: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = GroveTheme.colors
    val active = tab.route == activeRoute
    val color = if (active) c.navActiveText else c.navInactiveText
    Box(
        modifier =
            modifier
                .height(GroveSize.NavItemHeight)
                .groveClick(role = Role.Tab, haptic = GroveHaptic.Light) { onChange(tab.route) },
        contentAlignment = Alignment.Center,
    ) {
        androidx.compose.foundation.layout.Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(icon, contentDescription = tab.label, tint = color, modifier = Modifier.size(23.dp))
            Spacer(Modifier.height(2.dp))
            Text(
                tab.label,
                fontFamily = InterTight,
                fontSize = 10.sp,
                fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
                color = color,
            )
        }
    }
}
