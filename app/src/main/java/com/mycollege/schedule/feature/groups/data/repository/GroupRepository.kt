package com.mycollege.schedule.feature.groups.data.repository

import androidx.room.Dao
import androidx.room.Query
import com.mycollege.schedule.app.activity.data.models.Group

@Dao
interface GroupRepository {

    @Query("SELECT course FROM `groups`")
    fun getCourses(): List<String>

    @Query("SELECT level FROM `groups` WHERE course = :course")
    fun getLevelsBy(course: String): List<String>

    @Query("SELECT name FROM `groups` WHERE level = :level AND course = :course")
    fun getGroupNamesBy(level: String, course: String): List<String>

    @Query("SELECT name FROM `groups` WHERE course = :course")
    fun getAllGroupNamesBy(course: String): List<String>

    @Query("SELECT * FROM `groups` WHERE level = :level AND course = :course")
    fun getGroupsBy(level: String, course: String): List<Group>

    @Query("SELECT name FROM teachers WHERE RTRIM(department) = RTRIM(:department) COLLATE NOCASE")
    fun findTeachersBy(department: String): List<String>

    @Query("SELECT DISTINCT name FROM teachers")
    fun getTeachers(): List<String>

    @Query("SELECT DISTINCT department FROM teachers")
    fun getDepartments(): List<String>

}