package com.grove.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

data class UserPreferences(
    val darkModeOverride: Boolean? = null, // null = follow system
)

object UserPreferencesKeys {
    val DARK_MODE = stringPreferencesKey("dark_mode") // "system" | "light" | "dark"
}

class UserPreferencesRepository(
    private val dataStore: DataStore<Preferences>,
) {
    val preferences: Flow<UserPreferences> =
        dataStore.data.map { prefs ->
            UserPreferences(
                darkModeOverride =
                    when (prefs[UserPreferencesKeys.DARK_MODE]) {
                        "light" -> false
                        "dark" -> true
                        else -> null
                    },
            )
        }

    suspend fun updateDarkMode(mode: String) { // "system" | "light" | "dark"
        dataStore.edit { prefs -> prefs[UserPreferencesKeys.DARK_MODE] = mode }
    }
}
