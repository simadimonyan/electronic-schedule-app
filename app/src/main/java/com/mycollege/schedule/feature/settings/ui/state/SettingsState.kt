package com.mycollege.schedule.feature.settings.ui.state

import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Immutable
data class SettingsState(

    /**
     * Видимость панели навигации
     */
    val navigationVisibility: Boolean = false,

    /**
     * Видимость расписания на неделю
     */
    val fullWeekVisibility: Boolean = false,

    /**
     * Четность недели (false - нечетная)
     */
    val weekCount: Boolean = false

)

@Singleton
@Immutable
class SettingsStateHolder @Inject constructor() {

    private val _settingsState = MutableStateFlow(SettingsState())
    val settingsState: StateFlow<SettingsState> = _settingsState

    fun updateWeekChangeMode(toChange: Boolean) {
        _settingsState.update { it.copy(weekCount = toChange) }
    }

    fun updateFullWeek(isFull: Boolean) {
        _settingsState.update { it.copy(fullWeekVisibility = isFull) }
    }

    fun updateNavInvisibility(isVisible: Boolean) {
        _settingsState.update { it.copy(navigationVisibility = isVisible) }
    }

}