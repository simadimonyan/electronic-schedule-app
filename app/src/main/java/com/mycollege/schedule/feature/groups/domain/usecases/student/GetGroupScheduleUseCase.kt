package com.mycollege.schedule.feature.groups.domain.usecases.student

import android.util.Log
import androidx.room.Transaction
import com.my.tracker.MyTracker
import com.mycollege.schedule.app.activity.data.models.Schedule
import com.mycollege.schedule.app.activity.data.models.Teacher
import com.mycollege.schedule.core.cache.CacheManager
import com.mycollege.schedule.core.db.Database
import com.mycollege.schedule.core.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetGroupScheduleUseCase @Inject constructor(
    private val cacheManager: CacheManager,
    private val database: Database
) {

    @Transaction
    suspend fun getServerGroupSchedule(name: String) {
        return withContext(Dispatchers.IO) {
            MyTracker.trackEvent("Получить расписание группы из сервера")

            val scheduleServerConfiguration = cacheManager.loadScheduleServerConfiguration()
            val group = database.groups().getGroupByName(name).first()

            val newSchedules = mutableListOf<Schedule>()

            // подгрузка данных
            for (i in 1..2) {
                val week = RetrofitClient(scheduleServerConfiguration.serverUrl).groupsApi
                    .schedule(scheduleServerConfiguration.accessToken, name, i).schedule

                week.forEach { schedule ->

                    Log.i("GetGroupScheduleUseCase", "$schedule")

                    // добавить преподавателя, если его нет в базе (при запросе списка преподавателей они перезапишутся)
                    val teacher: Teacher? = if (schedule.teacher != null) {
                        val entry = database.teachers().getTeachersBy(schedule.teacher.label)
                        if (entry.isEmpty()) {
                            database.persistence().save(Teacher(
                                schedule.teacher.label,
                                schedule.teacher.department.toString()
                            ))
                            database.teachers().getTeachersBy(schedule.teacher.label).first()
                        }
                        else
                            entry.first()
                    }
                    else null

                    newSchedules.add(Schedule(
                        teacher?.id,
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
            database.schedule().deleteScheduleByGroup(group.id.toString())

            newSchedules.forEach { schedule ->
                database.persistence().save(schedule)
            }

            val lastRequest = cacheManager.loadServerNetworkLastRequest()

            val map: MutableMap<String, Long> = if (lastRequest.groupScheduleSynchronization.isEmpty()) {
                val map = mutableMapOf<String, Long>()
                map.put(name, System.currentTimeMillis())
                map
            }
            else {
                val map = lastRequest.groupScheduleSynchronization
                map.put(name, System.currentTimeMillis())
                map
            }

            // запись времени загрузки расписания группы
            cacheManager.saveServerNetworkLastRequest(
                CacheManager.ServerNetworkLastRequest(
                    lastRequest.weekParitySynchronization,
                    lastRequest.groupChooseConfiguration,
                    lastRequest.teacherChooseConfiguration,
                    map,
                    lastRequest.teacherScheduleSynchronization
                )
            )

        }

    }

}