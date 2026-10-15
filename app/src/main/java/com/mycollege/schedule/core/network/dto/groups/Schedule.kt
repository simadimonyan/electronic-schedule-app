package com.mycollege.schedule.core.network.dto.groups

import androidx.compose.runtime.Immutable
import com.google.gson.annotations.SerializedName
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
    val eiosLink: String,
    val auditory: String,
    /** календарная дата занятия (заочное обучение, сессия): yyyy-MM-dd или ISO-8601; приходит с `date=full` */
    @SerializedName("lessonDate", alternate = ["lesson_date"])
    val lessonDate: String? = null,
    /** запасной вариант даты: «дд.ММ» без года */
    @SerializedName("pinnedDate", alternate = ["pinned_date"])
    val pinnedDate: String? = null
)
