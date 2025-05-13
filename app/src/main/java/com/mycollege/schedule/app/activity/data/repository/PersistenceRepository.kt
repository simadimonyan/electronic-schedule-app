package com.mycollege.schedule.app.activity.data.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.mycollege.schedule.app.activity.data.models.Group
import com.mycollege.schedule.app.activity.data.models.Schedule
import com.mycollege.schedule.app.activity.data.models.Teacher

@Dao
interface PersistenceRepository {

    @Query("SELECT group_id FROM `groups` WHERE RTRIM(name) = RTRIM(:name) COLLATE NOCASE")
    fun findGroupBy(name: String): Long

    @Query("SELECT * FROM `groups` WHERE name = :name")
    fun getGroupBy(name: String): Group?

    @Query("SELECT teacher_id FROM teachers WHERE RTRIM(name) = RTRIM(:name) COLLATE NOCASE")
    fun findTeacherBy(name: String): Long

    @Query("SELECT * FROM teachers WHERE teacher_id = :id")
    fun getTeacherBy(id: Long): Teacher?

    @Insert
    fun save(group: Group): Long

    @Insert
    fun save(teacher: Teacher): Long

    @Insert
    fun save(schedule: Schedule): Long

}