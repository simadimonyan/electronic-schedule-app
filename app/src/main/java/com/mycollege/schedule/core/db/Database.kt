package com.mycollege.schedule.core.db

import android.util.Log
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mycollege.schedule.app.activity.data.models.Group
import com.mycollege.schedule.app.activity.data.models.Schedule
import com.mycollege.schedule.app.activity.data.models.Teacher
import com.mycollege.schedule.app.activity.data.repository.PersistenceRepository
import com.mycollege.schedule.feature.groups.data.repository.GroupRepository
import com.mycollege.schedule.feature.groups.data.repository.TeacherRepository
import com.mycollege.schedule.feature.schedule.data.repository.ScheduleRepository
import ru.ok.tracer.crash.report.TracerCrashReport

@Database(
    entities = [Group::class, Schedule::class, Teacher::class],
    version = 2, exportSchema = true
)
abstract class Database : RoomDatabase() {
    abstract fun persistence(): PersistenceRepository
    abstract fun groups(): GroupRepository
    abstract fun teachers(): TeacherRepository
    abstract fun schedule(): ScheduleRepository

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                try {
                    database.execSQL("ALTER TABLE schedule ADD COLUMN eios TEXT NOT NULL DEFAULT ''")
                } catch (e: Exception) {
                    TracerCrashReport.report(e, issueKey = "DATABASE_MIGRATION")
                    Log.e("Migration", "Error adding column: ${e.message}")
                }
            }
        }
    }
}