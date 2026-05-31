package com.grove.app.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.grove.app.designsystem.theme.GroveTheme
import com.grove.app.designsystem.theme.GroveShapes
import com.grove.app.designsystem.theme.GroveSpacing

@Composable
fun GroveCard(
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues(GroveSpacing.LG),
    content: @Composable ColumnScope.() -> Unit,
) {
    val c = GroveTheme.colors
    Column(modifier = modifier.clip(GroveShapes.Container).background(c.bgCard)) {
        Column(modifier = Modifier.padding(padding), content = content)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroveBottomSheet(onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    val c = GroveTheme.colors
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = c.bgApp,
        shape = GroveShapes.SheetTop,
    ) {
        Column(modifier = Modifier.fillMaxWidth(), content = content)
    }
}
