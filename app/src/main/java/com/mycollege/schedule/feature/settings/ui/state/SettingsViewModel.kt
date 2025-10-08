package com.mycollege.schedule.feature.settings.ui.state

import android.util.Log
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mycollege.schedule.core.cache.CacheManager
import com.mycollege.schedule.feature.settings.domain.usecase.GetWeekParityUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@Stable
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val cacheManager: CacheManager,
    val settingsStateHolder: SettingsStateHolder,
    private val getWeekParityUseCase: GetWeekParityUseCase
) : ViewModel() {

    fun handleEvent(event: SettingsEvent) {
        when(event) {
            is SettingsEvent.MakeNavigationInvisible -> makeNavInvisible(event.isVisible)
            is SettingsEvent.SaveSettings -> saveSettings()
            is SettingsEvent.MakeScheduleWeekFull -> makeScheduleFullWeek(event.isFull)
            is SettingsEvent.MakeWeekCountDifferent -> changeWeekCountMode(event.toChange)
            is SettingsEvent.MakeNotificationsEnabled -> makeNotificationsEnabled(event.isEnabled)
            is SettingsEvent.SynchronizeWeekParity -> makeWeekParitySynchronization(event.synchronizedWeekParity)
        }
    }

    private fun changeWeekCountMode(toChange: Boolean) {
        settingsStateHolder.updateWeekChangeMode(toChange)
        //saveSettings()
    }

    private fun saveSettings() {
        Log.d("Settings", "before save: " + settingsStateHolder.settingsState.value.toString())
        cacheManager.saveActualSettings(
            settingsStateHolder.settingsState.value
        )
        Log.d("Settings", "after save: " + cacheManager.loadLastSettings().toString())
    }

    private fun makeNotificationsEnabled(isEnabled: Boolean) {
        settingsStateHolder.updateNotificationsEnabled(isEnabled)
        //saveSettings()
    }

    private fun makeScheduleFullWeek(isFull: Boolean) {
        settingsStateHolder.updateFullWeek(isFull)
        //saveSettings()
    }

    private fun makeNavInvisible(isVisible: Boolean) {
        settingsStateHolder.updateNavInvisibility(isVisible)
        //saveSettings()
    }

    private fun makeWeekParitySynchronization(isSynchronized: Boolean) {
        viewModelScope.launch {
            settingsStateHolder.updateSynchronizationWeekParity(isSynchronized)

            if (isSynchronized) {

                val lastRequest = cacheManager.loadServerNetworkLastRequest()

                try {

                    Log.i("SYSTEM", System.currentTimeMillis().toString())
                    Log.i("LAST", lastRequest.weekParitySynchronization.toString())
                    Log.i("DIFF", (System.currentTimeMillis() - lastRequest.weekParitySynchronization).toString())
                    Log.i("MINUTE", TimeUnit.MINUTES.toMillis(1).toString())

                    // debounce
                    if ((System.currentTimeMillis() - lastRequest.weekParitySynchronization) >= TimeUnit.MINUTES.toMillis(1)) {
                        val parity = getWeekParityUseCase.getWeekParity()
                        changeWeekCountMode(parity == 2) // локальная четность - false нечетная
                        settingsStateHolder.updateSynchronizedWeekCount(parity == 2) // серверная четность - false нечетная

                        // последний запрос был отправлен ...
                        cacheManager.saveServerNetworkLastRequest(CacheManager.ServerNetworkLastRequest(
                            System.currentTimeMillis()))
                        saveSettings()
                    }
                    else {
                        Log.i("SettingsViewModel", "Debounce режим для синхронизации недели: частота запросов превышает порог 1 минуты")
                        changeWeekCountMode(cacheManager.loadLastSettings().synchronizedWeekCount)
                    }

                } // в случае если приложение запускается без интернета
                catch (e: Exception) {
                    Log.e("SettingsViewModel", e.toString())
                }

            }

        }
    }

}