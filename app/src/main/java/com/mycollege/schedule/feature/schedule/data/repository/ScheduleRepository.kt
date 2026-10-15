package com.mycollege.schedule.feature.schedule.data.repository

import androidx.room.Dao
import androidx.room.Query
import com.mycollege.schedule.app.activity.data.models.Schedule

@Dao
interface ScheduleRepository {

    /**
     * Пары на конкретный день: шаблон по дню недели и чётности (строки без даты)
     * плюс датированные занятия на эту дату (заочное обучение). date — yyyy-MM-dd или null.
     */
    @Query("SELECT * FROM schedule WHERE group_id = :id AND ((pinned_date = '' AND day_week = :dayWeek AND week_count = :weekCount) OR pinned_date = :date)")
    fun getDaySchedule(id: String, dayWeek: String, weekCount: Int, date: String?): List<Schedule>

    @Query("SELECT * FROM schedule WHERE teacher_id = :id AND ((pinned_date = '' AND day_week = :dayWeek AND week_count = :weekCount) OR pinned_date = :date)")
    fun getDayTeacherSchedule(id: String, dayWeek: String, weekCount: Int, date: String?): List<Schedule>

    /** всё расписание группы (шаблон обеих недель и датированные занятия) — для календаря */
    @Query("SELECT * FROM schedule WHERE group_id = :id")
    fun getAllSchedule(id: String): List<Schedule>

    @Query("SELECT * FROM schedule WHERE teacher_id = :id")
    fun getAllTeacherSchedule(id: String): List<Schedule>

    @Query("SELECT * FROM schedule WHERE group_id = :id AND week_count = :weekCount")
    fun getWeekSchedule(id: String, weekCount: Int): List<Schedule>

    @Query("SELECT * FROM schedule WHERE teacher_id = :id AND week_count = :weekCount")
    fun getWeekTeacherSchedule(id: String, weekCount: Int): List<Schedule>

    @Query("UPDATE schedule SET teacher_id = NULL WHERE teacher_id IN (SELECT teacher_id FROM teachers WHERE name IN (:teachersToNull))")
    fun setNullTeachers(teachersToNull: List<String>)

    @Query("DELETE FROM `schedule` WHERE group_id = :id")
    fun deleteScheduleByGroup(id: String)

    @Query("DELETE FROM `schedule` WHERE teacher_id = :id")
    fun deleteScheduleByTeacher(id: String)

}