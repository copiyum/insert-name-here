package com.grove.app.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grove.app.data.BudgetState
import com.grove.app.data.GroveDefaults
import com.grove.app.data.UserPreferences
import com.grove.app.data.UserPreferencesRepository
import com.grove.app.data.db.BudgetStateReactor
import com.grove.app.data.model.NotificationSettings
import com.grove.app.data.repository.NotificationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class ThemePrefs(val loaded: Boolean, val darkOverride: Boolean?)

/**
 * App-level read model: the cross-cutting state Compose chrome observes (theme, currency,
 * notification settings, the aggregated [BudgetState]). Writes live in per-feature ViewModels.
 */
class AppViewModel(
    private val notificationRepo: NotificationRepository,
    private val prefs: UserPreferencesRepository,
    reactor: BudgetStateReactor,
) : ViewModel() {
    val state: StateFlow<BudgetState> = reactor.state

    val preferences: StateFlow<UserPreferences> =
        prefs.preferences
            .stateIn(viewModelScope, SharingStarted.Eagerly, UserPreferences())

    val currency: StateFlow<String> =
        state
            .map { it.homeCurrency }
            .stateIn(viewModelScope, SharingStarted.Eagerly, GroveDefaults.DEFAULT_CURRENCY)

    val themePrefs: StateFlow<ThemePrefs> =
        prefs.preferences
            .map { ThemePrefs(loaded = true, darkOverride = it.darkModeOverride) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, ThemePrefs(loaded = false, darkOverride = null))

    val notificationSettings: StateFlow<NotificationSettings> =
        notificationRepo
            .observeSettings()
            .map { it ?: GroveDefaults.DEFAULT_NOTIFICATION_SETTINGS }
            .stateIn(viewModelScope, SharingStarted.Eagerly, GroveDefaults.DEFAULT_NOTIFICATION_SETTINGS)

    val soundsEnabled: StateFlow<Boolean> =
        preferences
            .map { it.soundsEnabled }
            .stateIn(viewModelScope, SharingStarted.Eagerly, true)
}
