package com.mycollege.schedule.feature.settings.ui.state

import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Immutable
data class SettingsState(

    /**
     * Невидимость панели навигации
     */
    val navigationInvisibility: Boolean = false,

    /**
     * Уведомления включены
     */
    val notificationsEnabled: Boolean = true,

    /**
     * Видимость расписания на неделю
     */
    val fullWeekVisibility: Boolean = false,

    /**
     * Синхронизация четности недели с сервером
     */
    val synchronizeWeekParity: Boolean = true,

    /**
     * Четность недели локальная (false - нечетная)
     */
    val weekCount: Boolean = false,

    /**
     * Четность недели с сервера (false - нечетная)
     */
    val synchronizedWeekCount: Boolean = false

)

@Singleton
@Immutable
class SettingsStateHolder @Inject constructor() {

    private val _settingsState = MutableStateFlow(SettingsState())
    val settingsState: StateFlow<SettingsState> = _settingsState

    private val _networkSynchronizationIssues = MutableSharedFlow<Boolean>()
    val networkSynchronizationIssues = _networkSynchronizationIssues.asSharedFlow()

    suspend fun sendNetworkIssue() {
        _networkSynchronizationIssues.emit(true)
    }

    fun updateSettingsState(state: SettingsState) {
        _settingsState.update { state }
    }

    fun updateWeekChangeMode(toChange: Boolean) {
        _settingsState.update { it.copy(weekCount = toChange) }
    }

    fun updateNotificationsEnabled(isEnabled: Boolean) {
        _settingsState.update { it.copy(notificationsEnabled = isEnabled) }
    }

    fun updateFullWeek(isFull: Boolean) {
        _settingsState.update { it.copy(fullWeekVisibility = isFull) }
    }

    fun updateNavInvisibility(isVisible: Boolean) {
        _settingsState.update { it.copy(navigationInvisibility = isVisible) }
    }

    fun updateSynchronizationWeekParity(isSynchronized: Boolean) {
        _settingsState.update { it.copy(synchronizeWeekParity = isSynchronized) }
    }

    fun updateSynchronizedWeekCount(weekCount: Boolean) {
        _settingsState.update { it.copy(synchronizedWeekCount = weekCount) }
    }

}