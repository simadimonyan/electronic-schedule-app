package com.mycollege.schedule.feature.schedule.ui.state

import androidx.compose.runtime.Immutable
import com.mycollege.schedule.feature.schedule.data.models.DataClasses

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