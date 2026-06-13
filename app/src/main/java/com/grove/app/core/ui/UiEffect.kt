package com.grove.app.core.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * One-shot, transient events a screen reacts to once (toast, navigation, haptic) — never
 * replayed on recomposition or config change. The à-la-carte MVI "effects channel" applied
 * to an otherwise plain MVVM/UDF codebase.
 */
sealed interface UiEffect {
    data class ShowToast(val message: String) : UiEffect
}

/**
 * Base for ViewModels that emit one-shot [UiEffect]s. The transient display state (e.g. how
 * long a toast stays visible) belongs to the UI layer that collects [effects].
 */
abstract class EffectViewModel : ViewModel() {
    private val _effects = Channel<UiEffect>(Channel.BUFFERED)
    val effects: Flow<UiEffect> = _effects.receiveAsFlow()

    protected fun emitEffect(effect: UiEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }

    protected fun toast(message: String) = emitEffect(UiEffect.ShowToast(message))
}
