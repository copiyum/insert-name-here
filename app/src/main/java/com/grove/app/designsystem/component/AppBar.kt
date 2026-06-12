package com.grove.app.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grove.app.designsystem.theme.GroveTheme
import com.grove.app.designsystem.theme.GroveType

@Composable
fun AppTopBar(title: String, subtitle: String? = null, actions: @Composable RowScope.() -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 12.dp, top = 18.dp, bottom = 22.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(title, style = GroveType.appBarTitle)
            if (subtitle != null) Text(subtitle, style = GroveType.rowSub.copy(fontSize = 13.5.sp), color = GroveTheme.colors.fg3)
        }
        Row(verticalAlignment = Alignment.CenterVertically, content = actions)
    }
}
