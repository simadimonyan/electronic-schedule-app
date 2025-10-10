package com.mycollege.schedule.app.activity.ui.state

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
import java.util.concurrent.TimeUnit
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

        Log.i("StartViewModel", "Инициализация настроек")

        try {
            val settings = cacheManager.loadLastSettings()

            Log.i("StartViewModel", "Load: ${settings}")

            if (settings != null) {
                // UI Screen Index
                if (settings.navigationInvisibility) {
                    appStateHolder.updateIndex(1)
                }

                // Settings
                settingsStateHolder.updateFullWeek(settings.fullWeekVisibility)
                settingsStateHolder.updateNavInvisibility(settings.navigationInvisibility)
                settingsStateHolder.updateWeekChangeMode(settings.weekCount)
                settingsStateHolder.updateNotificationsEnabled(settings.notificationsEnabled)
                settingsStateHolder.updateSynchronizationWeekParity(settings.synchronizeWeekParity)
                settingsStateHolder.updateSynchronizedWeekCount(settings.synchronizedWeekCount)

                Log.i("StartViewModel", "Данные настроек восстановлены")
                Log.i("StartViewModel", "Cache: ${settings}")
                Log.i("StartViewModel", "State: ${settingsStateHolder.settingsState.value}")
            }

            viewModelScope.launch {

                try {

                    val lastRequest = cacheManager.loadServerNetworkLastRequest()

                    Log.i("StartViewModel", "LastRequests: $lastRequest")

                    // первый запуск или нет
                    if (settings != null && lastRequest != null) {

                        // если синхронизация включена
                        if (settings.synchronizeWeekParity) {

                            // проверка последнего запроса синхронизации недели (в случае если )
                            if ((System.currentTimeMillis() - lastRequest.weekParitySynchronization) >= TimeUnit.DAYS.toMillis(1)) {
                                // week parity setting
                                val parity = getWeekParityUseCase.getWeekParity()
                                settingsStateHolder.updateWeekChangeMode(parity == 2) // false - нечетная
                                settingsStateHolder.updateSynchronizedWeekCount(parity == 2) // false нечетная

                                cacheManager.saveActualSettings(SettingsState(
                                    settings.navigationInvisibility,
                                    settings.notificationsEnabled,
                                    settings.fullWeekVisibility,
                                    settings.synchronizeWeekParity,
                                    parity == 2,
                                    parity == 2
                                ))
                                cacheManager.saveServerNetworkLastRequest(CacheManager.ServerNetworkLastRequest(
                                    System.currentTimeMillis(),
                                    lastRequest.groupChooseConfiguration,
                                    lastRequest.teacherChooseConfiguration,
                                    lastRequest.groupScheduleSynchronization,
                                    lastRequest.teacherScheduleSynchronization
                                ))
                                Log.i("StartViewModel", "Cинхронизация недели c сервером: weekCount - $parity")
                            }
                            else { // берет последную синхронизованную четность недели
                                Log.i("StartViewModel", "Синхронизация включена - используем кешированную серверную четность недели ")
                                val parity = cacheManager.loadLastSettings()
                                settingsStateHolder.updateWeekChangeMode(parity.synchronizedWeekCount)
                            }

                        }
                        else { // берет локальную четность недели (тк выключена синхронизация)
                            Log.i("StartViewModel", "Синхронизация выключена - используем локальную четность недели")
                            val parity = cacheManager.loadLastSettings()
                            settingsStateHolder.updateWeekChangeMode(parity.weekCount)
                        }

                    }
                    else {
                        // week parity setting
                        val parity = getWeekParityUseCase.getWeekParity()
                        settingsStateHolder.updateWeekChangeMode(parity == 2) // false - нечетная
                        settingsStateHolder.updateSynchronizedWeekCount(parity == 2) // false нечетная

                        cacheManager.saveActualSettings(SettingsState(
                            synchronizeWeekParity = true,
                            weekCount = parity == 2,
                            synchronizedWeekCount = parity == 2)
                        )
                        cacheManager.saveServerNetworkLastRequest(CacheManager.ServerNetworkLastRequest(
                                weekParitySynchronization = System.currentTimeMillis()))
                        Log.i("StartViewModel", "Первая синхронизация недели c сервером: weekCount - $parity")
                    }

                    // сохранение состояния инициализации
                    cacheManager.saveActualSettings(settingsStateHolder.settingsState.value)

                }
                catch (e: Exception) { // проблемы с интернетом
                    Log.e("StartViewModel", "В корутине: $e")

                    // отключение функции синхронизации
                    settingsStateHolder.updateSynchronizationWeekParity(false)
                    if (settings != null) {
                        cacheManager.saveActualSettings(SettingsState(
                            settings.navigationInvisibility,
                            settings.notificationsEnabled,
                            settings.fullWeekVisibility,
                            false,
                            settings.weekCount,
                            settings.synchronizedWeekCount
                        ))
                    }
                    else
                        cacheManager.saveActualSettings(SettingsState(synchronizeWeekParity = false))
                }

            }
        }
        catch (e: Exception) {
            Log.e("StartViewModel", e.toString())
        }

    }

}