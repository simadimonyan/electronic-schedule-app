package com.mycollege.schedule.feature.settings.ui.state

import androidx.lifecycle.ViewModel
import com.mycollege.schedule.core.cache.CacheManager
import com.mycollege.schedule.shared.state.SharedStateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val cacheManager: CacheManager,
    val shared: SharedStateRepository
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
        shared.updateWeekChangeMode(toChange)
    }

    private fun saveSettings() {
        cacheManager.saveActualSettings(
            CacheManager.Settings(
            shared.scheduleFullWeek.value,
            shared.navigationInvisibility.value,
            shared.changeWeekCount.value)
        )
    }

    private fun makeScheduleFullWeek(isFull: Boolean) {
        shared.updateFullWeek(isFull)
    }

    private fun makeNavInvisible(isVisible: Boolean) {
        shared.updateNavInvisibility(isVisible)
    }

}