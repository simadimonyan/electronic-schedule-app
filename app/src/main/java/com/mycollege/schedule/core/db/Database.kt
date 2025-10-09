package com.mycollege.schedule.core.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mycollege.schedule.app.activity.data.models.Group
import com.mycollege.schedule.app.activity.data.models.Schedule
import com.mycollege.schedule.app.activity.data.models.Teacher
import com.mycollege.schedule.app.activity.data.repository.PersistenceRepository
import com.mycollege.schedule.feature.groups.data.repository.GroupRepository
import com.mycollege.schedule.feature.groups.data.repository.TeacherRepository
import com.mycollege.schedule.feature.schedule.data.repository.ScheduleRepository

@Database(entities = [Group::class, Schedule::class, Teacher::class], version = 1, exportSchema = false)
abstract class Database : RoomDatabase() {
    abstract fun persistence(): PersistenceRepository
    abstract fun groups(): GroupRepository
    abstract fun teachers(): TeacherRepository
    abstract fun schedule(): ScheduleRepository
}