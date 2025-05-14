package com.mycollege.schedule.feature.settings.ui.state

import android.util.Log
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import com.mycollege.schedule.core.cache.CacheManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@Stable
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val cacheManager: CacheManager,
    val settingsStateHolder: SettingsStateHolder
) : ViewModel() {

    fun handleEvent(event: SettingsEvent) {
        when(event) {
            is SettingsEvent.MakeNavigationInvisible -> makeNavInvisible(event.isVisible)
            is SettingsEvent.SaveSettings -> saveSettings()
            is SettingsEvent.MakeScheduleWeekFull -> makeScheduleFullWeek(event.isFull)
            is SettingsEvent.MakeWeekCountDifferent -> changeWeekCountMode(event.toChange)
        }
    }

    private fun changeWeekCountMode(toChange: Boolean) {
        settingsStateHolder.updateWeekChangeMode(toChange)
        saveSettings()
    }

    private fun saveSettings() {
        cacheManager.saveActualSettings(
            settingsStateHolder.settingsState.value
        )
    }

    private fun makeScheduleFullWeek(isFull: Boolean) {
        settingsStateHolder.updateFullWeek(isFull)
        saveSettings()
    }

    private fun makeNavInvisible(isVisible: Boolean) {
        settingsStateHolder.updateNavInvisibility(isVisible)
        saveSettings()
    }

}