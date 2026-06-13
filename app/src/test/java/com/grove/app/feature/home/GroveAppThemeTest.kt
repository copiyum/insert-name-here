package com.grove.app.feature.home

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GroveAppThemeTest {
    @Test
    fun unloadedPrefsDefaultToDarkMode() {
        assertTrue(resolveDarkMode(ThemePrefs(loaded = false, darkOverride = null), startupDarkOverride = null))
    }

    @Test
    fun unloadedPrefsUseStartupLightOverride() {
        assertFalse(resolveDarkMode(ThemePrefs(loaded = false, darkOverride = null), startupDarkOverride = false))
    }

    @Test
    fun loadedLightOverrideWinsOverStartupDark() {
        assertFalse(resolveDarkMode(ThemePrefs(loaded = true, darkOverride = false), startupDarkOverride = true))
    }

    @Test
    fun loadedDarkOverrideWinsOverStartupLight() {
        assertTrue(resolveDarkMode(ThemePrefs(loaded = true, darkOverride = true), startupDarkOverride = false))
    }

    @Test
    fun loadedPrefsWithoutOverrideDefaultToDark() {
        assertTrue(resolveDarkMode(ThemePrefs(loaded = true, darkOverride = null), startupDarkOverride = null))
    }
}
