package com.mycollege.schedule.feature.groups.data.repository

import androidx.room.Dao
import androidx.room.Query
import com.mycollege.schedule.app.activity.data.models.Teacher

@Dao
interface TeacherRepository {

    @Query("SELECT name FROM teachers WHERE RTRIM(department) = RTRIM(:department) COLLATE NOCASE")
    fun findTeachersBy(department: String): List<String>

    @Query("SELECT * FROM teachers WHERE name = :name")
    fun getTeachersBy(name: String): List<Teacher>

    @Query("SELECT DISTINCT name FROM teachers")
    fun getTeachers(): List<String>

    @Query("SELECT DISTINCT department FROM teachers")
    fun getDepartments(): List<String>

    @Query("DELETE FROM `teachers`")
    fun clearTable()

}