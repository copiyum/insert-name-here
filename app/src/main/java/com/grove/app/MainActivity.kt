package com.grove.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.grove.app.data.readDarkModeOverrideOnce
import com.grove.app.feature.home.GroveApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Manage the system splash (static dark frame) and dismiss it on first Compose
        // frame. The animated ceremony itself is the Compose SplashRing overlay.
        installSplashScreen()
        super.onCreate(savedInstanceState)
        val startupDarkOverride =
            runCatching {
                runBlocking(Dispatchers.IO) { applicationContext.readDarkModeOverrideOnce() }
            }.getOrNull()
        enableEdgeToEdge()
        setContent { GroveApp(startupDarkOverride = startupDarkOverride) }
    }
}
