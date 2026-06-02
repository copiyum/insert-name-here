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
    val currency: String = "USD",
    val name: String = "Mae",
    val resetDay: Int = 1,
    val darkModeOverride: Boolean? = null, // null = follow system
)

object UserPreferencesKeys {
    val CURRENCY = stringPreferencesKey("currency")
    val NAME = stringPreferencesKey("name")
    val RESET_DAY = stringPreferencesKey("reset_day")
    val DARK_MODE = stringPreferencesKey("dark_mode") // "system" | "light" | "dark"
}

class UserPreferencesRepository(private val dataStore: DataStore<Preferences>) {

    val preferences: Flow<UserPreferences> = dataStore.data.map { prefs ->
        UserPreferences(
            currency = prefs[UserPreferencesKeys.CURRENCY] ?: "USD",
            name = prefs[UserPreferencesKeys.NAME] ?: "Mae",
            resetDay = prefs[UserPreferencesKeys.RESET_DAY]?.toIntOrNull() ?: 1,
            darkModeOverride = when (prefs[UserPreferencesKeys.DARK_MODE]) {
                "light" -> false
                "dark" -> true
                else -> null
            },
        )
    }

    suspend fun updateCurrency(code: String) {
        dataStore.edit { prefs -> prefs[UserPreferencesKeys.CURRENCY] = code }
    }

    suspend fun updateDarkMode(mode: String) { // "system" | "light" | "dark"
        dataStore.edit { prefs -> prefs[UserPreferencesKeys.DARK_MODE] = mode }
    }

    suspend fun updateAll(name: String, resetDay: Int, currency: String) {
        dataStore.edit { prefs ->
            prefs[UserPreferencesKeys.NAME] = name
            prefs[UserPreferencesKeys.RESET_DAY] = resetDay.toString()
            prefs[UserPreferencesKeys.CURRENCY] = currency
        }
    }
}
