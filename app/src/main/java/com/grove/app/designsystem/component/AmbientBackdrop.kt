package com.grove.app.designsystem.component

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.toArgb
import com.grove.app.designsystem.theme.GroveTheme
import java.time.LocalTime
import java.time.MonthDay

@Composable
fun AmbientBackdrop(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val c = GroveTheme.colors
    val (top, glow) = remember(c.isDark) { ambientColors(LocalTime.now(), MonthDay.now(), c.isDark) }
    val motionEnabled = rememberMotionEnabled()
    val drift by if (motionEnabled) {
        rememberInfiniteTransition(label = "ambientDrift").animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(24_000, easing = LinearEasing), RepeatMode.Reverse),
            label = "ambientDrift",
        )
    } else {
        remember { androidx.compose.runtime.mutableFloatStateOf(0.5f) }
    }

    val backdrop = if (Build.VERSION.SDK_INT >= 33) {
        val shader = remember { RuntimeShader(AMBIENT_SHADER) }
        Modifier.drawBehind {
            shader.setFloatUniform("uSize", size.width, size.height)
            shader.setFloatUniform("uTime", drift)
            shader.setColorUniform("uGlow", glow.toArgb())
            shader.setColorUniform("uBase", top.toArgb())
            drawRect(brush = ShaderBrush(shader))
        }
    } else {
        Modifier.background(
            Brush.verticalGradient(0f to glow, 0.55f to top, 1f to Color.Transparent),
        )
    }

    Box(modifier = modifier.then(backdrop)) {
        Box(modifier = Modifier.fillMaxSize()) { content() }
    }
}

private fun ambientColors(time: LocalTime, day: MonthDay, dark: Boolean): Pair<Color, Color> {
    val transparent = Color.Transparent
    val hour = time.hour + time.minute / 60f
    val anchors = listOf(
        5f to Color(0xFFE8AA4E),
        9f to Color(0xFF62C37A),
        15f to Color(0xFF4CB0E5),
        19f to Color(0xFFECA851),
        23f to Color(0xFF328759),
    )
    var lower = anchors.last()
    var upper = anchors.first()
    for (i in anchors.indices) {
        if (hour >= anchors[i].first) lower = anchors[i]
        if (hour < anchors[i].first) { upper = anchors[i]; break }
    }
    val span = ((upper.first - lower.first + 24f) % 24f).coerceAtLeast(0.01f)
    val f = (((hour - lower.first + 24f) % 24f) / span).coerceIn(0f, 1f)
    var glow = lerp(lower.second, upper.second, f)
    val month = day.monthValue
    if (month in 9..11) glow = lerp(glow, Color(0xFFECA851), 0.25f)
    if (month in 3..5) glow = lerp(glow, Color(0xFF62C37A), 0.25f)
    val alpha = if (dark) 0.10f else 0.07f
    return transparent to glow.copy(alpha = alpha)
}

private val AMBIENT_SHADER = """
    uniform float2 uSize;
    uniform float uTime;
    layout(color) uniform half4 uGlow;
    layout(color) uniform half4 uBase;

    half4 main(float2 frag) {
        float2 uv = frag / uSize;
        float2 blob1 = float2(0.25 + 0.18 * uTime, 0.12 + 0.10 * uTime);
        float2 blob2 = float2(0.78 - 0.16 * uTime, 0.22 - 0.06 * uTime);
        float d1 = distance(uv, blob1);
        float d2 = distance(uv, blob2);
        float glow = smoothstep(0.55, 0.0, d1) * 0.8 + smoothstep(0.6, 0.0, d2) * 0.6;
        float fade = smoothstep(0.85, 0.0, uv.y);
        return uGlow * glow * fade + uBase * (1.0 - glow) * fade * 0.4;
    }
""".trimIndent()
