package com.mycollege.schedule.core.db.repository

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.mycollege.schedule.core.db.models.Group
import com.mycollege.schedule.core.db.models.Schedule

@Dao
interface ScheduleRepository {

    // выбор группы

    @Query("SELECT course FROM `groups`")
    fun getCourses(): LiveData<List<Int>>

    @Query("SELECT level FROM `groups` WHERE course = :course")
    fun getLevelsBy(course: Int): LiveData<List<String>>

    @Query("SELECT * FROM `groups` WHERE level = :level AND course = :course")
    fun getGroupsBy(level: String, course: Int): LiveData<List<Group>>

    // расписание

    @Query("SELECT * FROM schedule WHERE group_id = :id AND day_week = :dayWeek AND week_count = :weekCount")
    fun getDaySchedule(id: String, dayWeek: String, weekCount: Int): LiveData<List<Schedule>>

    @Query("SELECT * FROM schedule WHERE group_id = :id AND week_count = :weekCount")
    fun getWeekSchedule(id: String, weekCount: Int): LiveData<List<Schedule>>

}