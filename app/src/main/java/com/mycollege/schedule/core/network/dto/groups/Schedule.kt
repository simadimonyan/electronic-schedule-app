package com.mycollege.schedule.core.network.dto.groups

import androidx.compose.runtime.Immutable
import com.mycollege.schedule.core.network.dto.teachers.Teacher

@Immutable
data class Schedule(val schedule: List<ScheduleUnit>)

@Immutable
data class ScheduleUnit(
    val id: Long,
    val dayWeek: String,
    val timePeriod: String,
    val weekCount: Int,
    val group: Group,
    val lessonCount: Int,
    val lessonType: String,
    val lessonName: String,
    val teacher: Teacher,
    val auditory: String
)
