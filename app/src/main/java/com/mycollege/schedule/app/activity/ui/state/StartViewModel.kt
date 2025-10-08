package com.mycollege.schedule.app.activity.ui.state

import java.util.concurrent.TimeUnit
import android.util.Log
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mycollege.schedule.core.cache.CacheManager
import com.mycollege.schedule.feature.settings.domain.usecase.GetWeekParityUseCase
import com.mycollege.schedule.feature.settings.ui.state.SettingsState
import com.mycollege.schedule.feature.settings.ui.state.SettingsStateHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.ok.tracer.crash.report.TracerCrashReport
import javax.inject.Inject

@Stable
@HiltViewModel
class StartViewModel @Inject constructor(
    private val cacheManager: CacheManager,
    val settingsStateHolder: SettingsStateHolder,
    val appStateHolder: AppStateHolder,
    private val getWeekParityUseCase: GetWeekParityUseCase
) : ViewModel() {

    fun settingsInit() {

        try {
            val settings = cacheManager.loadLastSettings()

            if (settings != null) {
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

            viewModelScope.launch {

                val lastRequest = cacheManager.loadServerNetworkLastRequest()

                // первый запуск или нет
                if (settings != null) {

                    // если синхронизация включена
                    if (settings.synchronizeWeekParity) {

                        // проверка последнего запроса синхронизации недели (в случае если )
                        if (lastRequest == null
                            || lastRequest.weekParitySynchronization == -1L
                            || (System.currentTimeMillis() - lastRequest.weekParitySynchronization) >= TimeUnit.DAYS.toMillis(1)
                        ) {
                            // week parity setting
                            val parity = getWeekParityUseCase.getWeekParity()
                            settingsStateHolder.updateWeekChangeMode(parity == 2) // false - нечетная
                            settingsStateHolder.updateSynchronizedWeekCount(parity == 2) // false нечетная

                            cacheManager.saveActualSettings(SettingsState(
                                settings.navigationVisibility,
                                settings.notificationsEnabled,
                                settings.fullWeekVisibility,
                                settings.synchronizeWeekParity,
                                parity == 2
                            ))
                            cacheManager.saveServerNetworkLastRequest(CacheManager.ServerNetworkLastRequest(
                                System.currentTimeMillis()))
                            Log.i("StartViewModel", "Cинхронизация недели c сервером: weekCount - $parity")
                        }
                        else { // берет последную синхронизованную четность недели
                            Log.i("StartViewModel", "Week parity daily request")
                            val parity = cacheManager.loadLastSettings()
                            settingsStateHolder.updateWeekChangeMode(parity.synchronizedWeekCount)
                        }

                    }
                    else { // берет локальную четность недели (тк выключена синхронизация)
                        val parity = cacheManager.loadLastSettings()
                        settingsStateHolder.updateWeekChangeMode(parity.weekCount)
                    }

                }
                else {
                    // week parity setting
                    val parity = getWeekParityUseCase.getWeekParity()
                    settingsStateHolder.updateWeekChangeMode(parity == 2) // false - нечетная
                    settingsStateHolder.updateSynchronizedWeekCount(parity == 2) // false нечетная

                    cacheManager.saveActualSettings(SettingsState(weekCount = parity == 2))
                    cacheManager.saveServerNetworkLastRequest(CacheManager.ServerNetworkLastRequest(
                        System.currentTimeMillis()))
                    Log.i("StartViewModel", "Первая синхронизация недели c сервером: weekCount - $parity")
                }

            }
        }
        catch (e: Exception) {
            TracerCrashReport.report(e, issueKey = "StartViewModel")
            Log.e("StartViewModel", e.toString())
        }

    }

}