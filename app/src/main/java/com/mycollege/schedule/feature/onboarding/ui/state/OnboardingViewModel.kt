package com.mycollege.schedule.feature.onboarding.ui.state

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mycollege.schedule.app.activity.ui.state.AppStateHolder
import com.mycollege.schedule.core.cache.CacheManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@Stable
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val cacheManager: CacheManager,
    val appStateHolder: AppStateHolder
) : ViewModel() {

    fun setFirstStartup() {
        viewModelScope.launch {
            appStateHolder.updatingFirstStartup(false)
            cacheManager.setFirstStartup(false)
        }
    }

}