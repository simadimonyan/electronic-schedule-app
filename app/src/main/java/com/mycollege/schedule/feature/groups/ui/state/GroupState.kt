package com.mycollege.schedule.feature.groups.ui.state

import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data class for managing state of Group Screen
 */
@Immutable
data class GroupState(

    // choose configuration
    val course: String = "1 курс",
    val level: String = "Все специальности",
    val group: String = "Выбрать",

    // utils for interacting
    val showBottomSheet: Boolean = false,
    val selectedIndex: Int = 0,

    // данные для отображения в поиске расписания
    val coursesToDisplay: List<String> = ArrayList(),
    val levelsToDisplay: List<String> = ArrayList(),
    val groupsToDisplay: List<String> = ArrayList(),

    )

@Singleton
@Immutable
class GroupStateHolder @Inject constructor() {

    private val _groupState = MutableStateFlow(GroupState())
    val groupState: StateFlow<GroupState> = _groupState

    private val _scheduleCreateSignal = MutableSharedFlow<Boolean>()
    val scheduleCreateSignal = _scheduleCreateSignal.asSharedFlow()

    suspend fun sendCreateScheduleSignal() {
        _scheduleCreateSignal.emit(true)
    }

    fun updateCourse(course: String) {
        _groupState.update { it.copy(course = course) }
    }

    fun updateLevel(level: String) {
        _groupState.update { it.copy(level = level) }
    }

    fun updateGroup(group: String) {
        _groupState.update { it.copy(group = group) }
    }

    fun updateGroupsToDisplay(groups: List<String>) {
        _groupState.update { it.copy(groupsToDisplay = groups) }
    }

    fun updateLevelsToDisplay(levels: List<String>) {
        _groupState.update { it.copy(levelsToDisplay = levels) }
    }

    fun updateCoursesToDisplay(courses: List<String>) {
        _groupState.update { it.copy(coursesToDisplay = courses) }
    }

    fun updateSelectedIndex(index: Int) {
        _groupState.update { it.copy(selectedIndex = index) }
    }

    fun toggleBottomSheet(toggle: Boolean) {
        _groupState.update { it.copy(showBottomSheet = toggle) }
    }

}