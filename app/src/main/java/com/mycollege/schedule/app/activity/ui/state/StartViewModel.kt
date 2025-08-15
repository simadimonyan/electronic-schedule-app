package com.mycollege.schedule.app.activity.ui.state

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import com.mycollege.schedule.core.cache.CacheManager
import com.mycollege.schedule.feature.settings.ui.state.SettingsStateHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@Stable
@HiltViewModel
class StartViewModel @Inject constructor(
    private val cacheManager: CacheManager,
    val settingsStateHolder: SettingsStateHolder,
    val appStateHolder: AppStateHolder
) : ViewModel() {

    fun init() {
        try {
            val settings = cacheManager.loadLastSettings()

            // UI Screen Index
            if (settings.navigationVisibility) {
                appStateHolder.updateIndex(1)
            }

            // Settings
            //settingsStateHolder.updateSettingsState(settings)
            settingsStateHolder.updateFullWeek(settings.fullWeekVisibility)
            settingsStateHolder.updateNavInvisibility(settings.navigationVisibility)
            settingsStateHolder.updateWeekChangeMode(settings.weekCount)
            settingsStateHolder.updateNotificationsEnabled(settings.notificationsEnabled)
        }
        catch (_: Exception) {}
    }

}