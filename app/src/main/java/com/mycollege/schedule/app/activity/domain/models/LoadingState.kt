package com.mycollege.schedule.app.activity.domain.models

import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Immutable
data class LoadingState(

    /**
     * Процесс обновления конфигурации для выбора групп / преподавателя
     */
    val chooseConfigurationLoading: Boolean = false,

    /**
     * Прогресс статуса обновления конфигурации для выбора групп / преподавателя
     */
    val chooseConfigurationProgress: Int = 0,

    /**
     * Процесс обновления расписания
     */
    val scheduleLoading: Boolean = false,

    /**
     * Прогресс статуса обновления расписания
     */
    val scheduleProgress: Int = 0

)

@Singleton
@Immutable
class LoadingStateHolder @Inject constructor() {

    private val _loadingState = MutableStateFlow(LoadingState())
    val loadingState: StateFlow<LoadingState> = _loadingState

    fun updateChooseConfigurationLoading(isLoading: Boolean) {
        _loadingState.update { it.copy(chooseConfigurationLoading = isLoading) }
    }

    fun updateChooseConfigurationProgress(progress: Int) {
        _loadingState.update { it.copy(chooseConfigurationProgress = progress) }
    }

    fun updateScheduleLoading(isLoading: Boolean) {
        _loadingState.update { it.copy(scheduleLoading = isLoading) }
    }

    fun updateScheduleProgress(progress: Int) {
        _loadingState.update { it.copy(scheduleProgress = progress) }
    }

}

