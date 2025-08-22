package com.mycollege.schedule.feature.schedule.ui.state

import androidx.compose.runtime.Immutable
import com.mycollege.schedule.feature.schedule.data.models.DataClasses
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data class for managing state of Schedule Screen
 */
@Immutable
data class ScheduleState(

    // system local dates
    val todayDate: String = "",
    val weekDates: HashMap<Int, String> = HashMap(),

    // schedule params
    val todayLessons: ArrayList<DataClasses.Lesson> = ArrayList(),
    val weekLessons: HashMap<Int, ArrayList<DataClasses.Lesson>> = HashMap()

)

@Immutable
@Singleton
class ScheduleStateHolder @Inject constructor() {

    private var _scheduleState = MutableStateFlow(ScheduleState())
    val scheduleState: StateFlow<ScheduleState> = _scheduleState

    fun updateWeekDates(week: HashMap<Int, String>) {
        _scheduleState.update { it.copy(weekDates = week) }
    }

    fun showTodayLessons(todayLessons: ArrayList<DataClasses.Lesson>) {
        _scheduleState.update { it.copy(todayLessons = todayLessons) }
    }

    fun showDateToday(today: String) {
        _scheduleState.update { it.copy(todayDate = today) }
    }

    fun showWeekLessons(week: HashMap<Int, ArrayList<DataClasses.Lesson>>) {
        _scheduleState.update { it.copy(weekLessons = week) }
    }

}