package com.mycollege.schedule.feature.schedule.ui.state

sealed class ScheduleEvent {

    /**
     * При изменении порядка недели
     */
    object WeekCountChanged : ScheduleEvent()

    /**
     * Показать данные расписания на сегодня
     */
    object ShowTodaySchedule : ScheduleEvent()

    /**
     * Показать данные расписания на неделю
     */
    object ShowWeekSchedule : ScheduleEvent()

}