package com.mycollege.schedule.feature.groups.domain.usecases.teacher

import android.util.Log
import androidx.room.Transaction
import com.my.tracker.MyTracker
import com.mycollege.schedule.app.activity.data.models.Group
import com.mycollege.schedule.app.activity.data.models.Schedule
import com.mycollege.schedule.core.cache.CacheManager
import com.mycollege.schedule.core.db.Database
import com.mycollege.schedule.core.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetTeacherScheduleUseCase @Inject constructor(
    private val cacheManager: CacheManager,
    private val database: Database
) {

    @Transaction
    suspend fun getServerTeacherSchedule(name: String) {
        return withContext(Dispatchers.IO) {
            MyTracker.trackEvent("ServerGetTeacherScheduleUseCaseEvent")

            val scheduleServerConfiguration = cacheManager.loadScheduleServerConfiguration()
            val teacher = database.teachers().getTeachersBy(name).first()

            val newSchedules = mutableListOf<Schedule>()

            // подгрузка данных
            for (i in 1..2) {
                val week = RetrofitClient(scheduleServerConfiguration.serverUrl).teachersApi
                    .schedule(scheduleServerConfiguration.accessToken, name, i).schedule

                week.forEach { schedule ->

                    Log.i("GetTeacherScheduleUseCase", "$schedule")

                    var dbResponse = database.groups().getGroupByName(schedule.group.name)

                    // добавить группу, если его нет в базе (при запросе списка групп они перезапишутся)
                    val group = if (dbResponse.isEmpty()) {
                        database.persistence().save(Group(
                            schedule.group.name,
                            schedule.group.course.toString(),
                            schedule.group.level
                        ))
                        database.groups().getGroupByName(schedule.group.name).first()
                    } else dbResponse.first()

                    newSchedules.add(Schedule(
                        teacher.id,
                        group.id,
                        schedule.dayWeek,
                        schedule.weekCount,
                        schedule.lessonCount,
                        schedule.timePeriod.replace(".", ":"),
                        schedule.lessonName,
                        schedule.lessonType,
                        schedule.auditory
                    ))
                }
            }

            // очистить старое расписание
            database.schedule().deleteScheduleByTeacher(teacher.id.toString())

            newSchedules.forEach { schedule ->
                database.persistence().save(schedule)
            }

            val lastRequest = cacheManager.loadServerNetworkLastRequest()

            val map: MutableMap<String, Long> = if (lastRequest.teacherScheduleSynchronization.isEmpty()) {
                val map = mutableMapOf<String, Long>()
                map.put(name, System.currentTimeMillis())
                map
            }
            else {
                val map = lastRequest.teacherScheduleSynchronization
                map.put(name, System.currentTimeMillis())
                map
            }

            // запись времени загрузки расписания преподавателя
            cacheManager.saveServerNetworkLastRequest(
                CacheManager.ServerNetworkLastRequest(
                    lastRequest.weekParitySynchronization,
                    lastRequest.groupChooseConfiguration,
                    lastRequest.teacherChooseConfiguration,
                    lastRequest.groupScheduleSynchronization,
                    map
                )
            )

        }

    }

}