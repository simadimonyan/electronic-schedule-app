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
     * Процесс обновления расписания
     */
    val loading: Boolean = false,

    /**
     * Прогресс статуса обновления
     */
    val progress: Int = 0

)

@Singleton
@Immutable
class LoadingStateHolder @Inject constructor() {

    private val _loadingState = MutableStateFlow(LoadingState())
    val loadingState: StateFlow<LoadingState> = _loadingState

    fun updateLoading(isLoading: Boolean) {
        _loadingState.update { it.copy(loading = isLoading) }
    }

    fun updateProgress(progress: Int) {
        _loadingState.update { it.copy(progress = progress) }
    }

}

