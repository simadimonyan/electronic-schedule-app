package com.mycollege.schedule.core.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mycollege.schedule.core.db.models.Group
import com.mycollege.schedule.core.db.models.Schedule
import com.mycollege.schedule.core.db.models.Teacher
import com.mycollege.schedule.core.db.repository.ScheduleRepository

@Database(entities = [Group::class, Schedule::class, Teacher::class], version = 1, exportSchema = false)
abstract class Database : RoomDatabase() {
    abstract fun schedule(): ScheduleRepository
}