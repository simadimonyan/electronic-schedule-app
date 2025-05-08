package com.mycollege.schedule.feature.onboarding.ui.state

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mycollege.schedule.core.cache.CacheManager
import com.mycollege.schedule.shared.state.SharedStateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@Stable
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val cacheManager: CacheManager,
    val shared: SharedStateRepository
) : ViewModel() {

    fun setFirstStartup() {
        viewModelScope.launch {
            shared.updatingFirstStartup(false)
            cacheManager.setFirstStartup(false)
        }
    }

}