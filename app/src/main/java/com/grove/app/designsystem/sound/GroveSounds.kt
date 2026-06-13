package com.grove.app.designsystem.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import com.grove.app.R

enum class GroveSound { Tick, Chime }

class GroveSoundPlayer(context: Context) {
    @Volatile var enabled: Boolean = true

    private val pool = SoundPool.Builder()
        .setMaxStreams(2)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        .build()

    private val ids = mapOf(
        GroveSound.Tick to pool.load(context, R.raw.grove_tick, 1),
        GroveSound.Chime to pool.load(context, R.raw.grove_chime, 1),
    )

    fun play(sound: GroveSound, volume: Float = 0.5f) {
        if (!enabled) return
        ids[sound]?.let { pool.play(it, volume, volume, 1, 0, 1f) }
    }

    fun release() = pool.release()
}

val LocalGroveSounds = staticCompositionLocalOf<GroveSoundPlayer?> { null }

@Composable
fun rememberGroveSoundPlayer(enabled: Boolean): GroveSoundPlayer {
    val context = LocalContext.current.applicationContext
    val player = remember { GroveSoundPlayer(context) }
    player.enabled = enabled
    DisposableEffect(Unit) {
        onDispose { player.release() }
    }
    return player
}
