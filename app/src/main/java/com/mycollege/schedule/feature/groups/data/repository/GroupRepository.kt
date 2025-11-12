package com.mycollege.schedule.feature.groups.data.repository

import androidx.room.Dao
import androidx.room.Query
import com.mycollege.schedule.app.activity.data.models.Group
import com.mycollege.schedule.app.activity.data.models.Teacher
import com.mycollege.schedule.core.network.dto.groups.Groups

@Dao
interface GroupRepository {

    @Query("SELECT * FROM `groups` WHERE group_id = :id")
    fun getGroupById(id: String): List<Group>

    @Query("SELECT * FROM `groups` WHERE name = :name")
    fun getGroupByName(name: String): List<Group>

    @Query("SELECT course FROM `groups`")
    fun getCourses(): List<String>

    @Query("SELECT DISTINCT level FROM `groups` WHERE course = :course")
    fun getLevelsBy(course: String): List<String>

    @Query("SELECT name FROM `groups` WHERE level = :level AND course = :course")
    fun getGroupNamesBy(level: String, course: String): List<String>

    @Query("SELECT name FROM `groups` WHERE course = :course")
    fun getAllGroupNamesBy(course: String): List<String>

    @Query("SELECT * FROM `groups` WHERE level = :level AND course = :course")
    fun getGroupsBy(level: String, course: String): List<Group>

    @Query("SELECT * FROM `groups` WHERE course = :course")
    fun getGroupsBy(course: String): List<Group>

    @Query("DELETE FROM `groups` WHERE name IN (:groupsToRemove)")
    fun clearTable(groupsToRemove: List<String>)

}