package com.mycollege.schedule.feature.schedule.data.repository

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.mycollege.schedule.app.activity.data.models.Schedule

@Dao
interface ScheduleRepository {

    @Query("SELECT * FROM schedule WHERE group_id = :id AND day_week = :dayWeek AND week_count = :weekCount")
    fun getDaySchedule(id: String, dayWeek: String, weekCount: Int): List<Schedule>

    @Query("SELECT * FROM schedule WHERE group_id = :id AND week_count = :weekCount")
    fun getWeekSchedule(id: String, weekCount: Int): List<Schedule>

}