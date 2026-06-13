package com.grove.app.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grove.app.data.GroveDefaults
import com.grove.app.data.UserPreferencesRepository
import com.grove.app.data.model.UserProfile
import com.grove.app.data.repository.NotificationRepository
import com.grove.app.data.repository.UserRepository
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val userRepo: UserRepository,
    private val notificationRepo: NotificationRepository,
    private val prefs: UserPreferencesRepository,
) : ViewModel() {
    fun toggleDark(current: Boolean) {
        viewModelScope.launch { prefs.updateDarkMode(if (current) "light" else "dark") }
    }

    fun toggleSounds(enabled: Boolean) {
        viewModelScope.launch { prefs.updateSounds(enabled) }
    }

    fun updateDailySafeSpend(enabled: Boolean) {
        viewModelScope.launch {
            val current = notificationRepo.getSettings() ?: GroveDefaults.DEFAULT_NOTIFICATION_SETTINGS
            notificationRepo.upsertSettings(current.copy(dailySafeSpendEnabled = enabled))
        }
    }

    fun updateBillAlerts(enabled: Boolean) {
        viewModelScope.launch {
            val current = notificationRepo.getSettings() ?: GroveDefaults.DEFAULT_NOTIFICATION_SETTINGS
            notificationRepo.upsertSettings(current.copy(billAlertsEnabled = enabled))
        }
    }

    fun updateCurrency(code: String) {
        viewModelScope.launch {
            val current =
                userRepo.get() ?: UserProfile(
                    name = GroveDefaults.DEFAULT_USER_NAME,
                    resetDay = 1,
                    currencyCode = GroveDefaults.DEFAULT_CURRENCY,
                    onboardingCompleted = false,
                )
            userRepo.upsert(current.copy(currencyCode = code))
        }
    }

    fun updateUserName(name: String) {
        viewModelScope.launch {
            val current = userRepo.get() ?: return@launch
            val trimmed = name.trim().take(GroveDefaults.MAX_USER_NAME_LENGTH)
            if (trimmed.isNotEmpty()) {
                userRepo.upsert(current.copy(name = trimmed))
            }
        }
    }

    fun updateResetDay(day: Int) {
        viewModelScope.launch {
            val current = userRepo.get() ?: return@launch
            userRepo.upsert(current.copy(resetDay = day.coerceIn(1, 28)))
        }
    }
}
