package com.mycollege.schedule.app.activity.ui.state

import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Immutable
data class AppState(

    /**
     * Индекс Pager навигации
     */
    val pagerIndex: Int = 0,

    /**
     * Приложение было первый раз запущено
     */
    val firstStartUp: Boolean = false,

    /**
     * Режим студента / преподавателя
     */
    val studentMode: Boolean = true

)

@Singleton
@Immutable
class AppStateHolder @Inject constructor() {

    private val _appState = MutableStateFlow(AppState())
    val appState: StateFlow<AppState> = _appState

    fun updateIndex(index: Int) {
        _appState.update { it.copy(pagerIndex = index) }
    }

    fun updatingFirstStartup(isFirst: Boolean) {
        _appState.update { it.copy(firstStartUp = isFirst) }
    }

    fun updateStudentMode(studentMode: Boolean) {
        _appState.update { it.copy(studentMode = studentMode) }
    }

}