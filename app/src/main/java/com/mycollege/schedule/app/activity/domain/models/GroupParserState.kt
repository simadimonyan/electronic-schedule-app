package com.mycollege.schedule.app.activity.domain.models

import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Immutable
data class GroupParserState(

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
class GroupParserStateHolder @Inject constructor() {

    private val _groupParserState = MutableStateFlow(GroupParserState())
    val groupParserState: StateFlow<GroupParserState> = _groupParserState

    fun updateLoading(isLoading: Boolean) {
        _groupParserState.update { it.copy(loading = isLoading) }
    }

    fun updateProgress(progress: Int) {
        _groupParserState.update { it.copy(progress = progress) }
    }

}

