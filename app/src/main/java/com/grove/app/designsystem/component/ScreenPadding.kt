package com.grove.app.designsystem.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.grove.app.designsystem.theme.GroveSize

@Composable
fun groveScreenContentPadding(horizontal: Dp = 20.dp): PaddingValues {
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    return PaddingValues(
        start = horizontal,
        end = horizontal,
        bottom = GroveSize.NavClearance + bottomInset,
    )
}
