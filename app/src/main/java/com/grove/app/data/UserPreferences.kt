package com.grove.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

data class UserPreferences(
    val darkModeOverride: Boolean? = null,
    val soundsEnabled: Boolean = true,
)

object UserPreferencesKeys {
    val DARK_MODE = stringPreferencesKey("dark_mode")
    val SOUNDS = booleanPreferencesKey("sounds_enabled")
}

fun darkModeOverrideFromPreference(value: String?): Boolean? =
    when (value) {
        "light" -> false
        "dark" -> true
        else -> null
    }

suspend fun Context.readDarkModeOverrideOnce(): Boolean? =
    userPreferencesDataStore.data
        .map { prefs -> darkModeOverrideFromPreference(prefs[UserPreferencesKeys.DARK_MODE]) }
        .first()

class UserPreferencesRepository(
    private val dataStore: DataStore<Preferences>,
) {
    val preferences: Flow<UserPreferences> =
        dataStore.data.map { prefs ->
            UserPreferences(
                darkModeOverride = darkModeOverrideFromPreference(prefs[UserPreferencesKeys.DARK_MODE]),
                soundsEnabled = prefs[UserPreferencesKeys.SOUNDS] ?: true,
            )
        }

    suspend fun updateDarkMode(mode: String) {
        dataStore.edit { prefs -> prefs[UserPreferencesKeys.DARK_MODE] = mode }
    }

    suspend fun updateSounds(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[UserPreferencesKeys.SOUNDS] = enabled }
    }
}
