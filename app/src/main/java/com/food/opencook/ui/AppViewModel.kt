package com.food.opencook.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.food.opencook.data.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Whether onboarding is done, which gates the whole app. Satisfied either by joining a
 * household (server-backed) or by explicitly choosing local-only mode (offline-first).
 */
sealed interface OnboardState {
    /** DataStore not read yet — show a splash to avoid flashing onboarding. */
    data object Loading : OnboardState
    data object NotOnboarded : OnboardState
    data object Onboarded : OnboardState
}

@HiltViewModel
class AppViewModel @Inject constructor(
    settings: SettingsRepository,
) : ViewModel() {

    /**
     * Drives the top-level branch in [OpenCookApp]. Reacting to the DataStore flows
     * makes the transition automatic and bidirectional: joining/creating a household —
     * or choosing local-only mode — flips this to [OnboardState.Onboarded]; leaving a
     * household (without local-only) flips back to onboarding — no imperative navigation.
     */
    val onboardState: StateFlow<OnboardState> =
        combine(settings.householdId, settings.householdCode, settings.localOnly) { id, code, localOnly ->
            val joined = !id.isNullOrBlank() && !code.isNullOrBlank()
            if (joined || localOnly) OnboardState.Onboarded
            else OnboardState.NotOnboarded
        }.stateIn(viewModelScope, SharingStarted.Eagerly, OnboardState.Loading)
}
