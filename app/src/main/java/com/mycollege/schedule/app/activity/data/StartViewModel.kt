package com.mycollege.schedule.app.activity.data

import androidx.lifecycle.ViewModel
import com.mycollege.schedule.core.cache.CacheManager
import com.mycollege.schedule.shared.state.SharedStateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StartViewModel @Inject constructor(
    private val cacheManager: CacheManager,
    val shared: SharedStateRepository
) : ViewModel() {

    fun init() {
        try {
            val settings = cacheManager.loadLastSettings()

            // UI Screen Index
            if (settings.isNavInvisible) {
                shared.updateIndex(1)
            }

            // Settings
            shared.updateFullWeek(settings.fullWeek)
            shared.updateNavInvisibility(settings.isNavInvisible)
            shared.updateWeekChangeMode(settings.changeWeekCount)
        }
        catch (_: Exception) {}
    }

}