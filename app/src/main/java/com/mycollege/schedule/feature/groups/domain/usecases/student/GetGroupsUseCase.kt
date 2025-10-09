package com.mycollege.schedule.feature.groups.domain.usecases.student

import androidx.compose.runtime.Immutable
import androidx.room.Transaction
import com.mycollege.schedule.core.cache.CacheManager
import com.mycollege.schedule.core.db.Database
import com.mycollege.schedule.core.network.RetrofitClient
import com.mycollege.schedule.core.network.dto.groups.Groups
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Immutable
class GetGroupsUseCase @Inject constructor(
    private val database: Database,
    private val cacheManager: CacheManager
) {

    suspend fun getRoomGroups(course: String, level: String): Set<String> {
        return withContext(Dispatchers.IO) {
            if (level == "Все уровни")
                return@withContext database.groups().getAllGroupNamesBy(course).toSortedSet()
            else
                return@withContext database.groups().getGroupNamesBy(level, course).toSortedSet()
        }
    }

    @Transaction
    suspend fun getServerGroups(actualCourse: String, maxCourse: String, progress: (Int) -> Unit): Set<String> {
        return withContext(Dispatchers.IO) {
            val scheduleServerConfiguration = cacheManager.loadScheduleServerConfiguration()

            var response = Groups(emptyList())

            // очистка групп
            database.groups().clearTable()

            progress(10)

            val maxCourses = Integer.parseInt(maxCourse)
            var progressValue = 10
            val progressRatio = maxCourses / 90

            // собрать все группы
            for (course in 1..maxCourses) {

                // запрос
                var cacheDriver: Groups = RetrofitClient(scheduleServerConfiguration.serverUrl).groupsApi
                    .search(scheduleServerConfiguration.accessToken, course)

                // отдать в response выбранный курс
                if ("$course" == actualCourse) response = cacheDriver

                if (cacheDriver.groups.isNotEmpty()) {

                    // кеширование времени последнего запроса
                    val lastRequest = cacheManager.loadServerNetworkLastRequest()
                    if (lastRequest != null) {
                        cacheManager.saveServerNetworkLastRequest(CacheManager.ServerNetworkLastRequest(
                            lastRequest.groupChooseConfiguration,
                            System.currentTimeMillis(),
                            lastRequest.teacherChooseConfiguration,
                            lastRequest.groupScheduleSynchronization,
                            lastRequest.teacherScheduleSynchronization
                        ))
                    }
                    else
                        cacheManager.saveServerNetworkLastRequest(CacheManager.ServerNetworkLastRequest(
                            groupChooseConfiguration = System.currentTimeMillis()))

                    // сохранение в бд
                    for (group in cacheDriver.groups) {
                        database.persistence().save(
                            com.mycollege.schedule.app.activity.data.models.Group(
                                group.name,
                                "${group.course}",
                                group.level
                            )
                        )
                    }

                    progressValue = progressRatio + progressValue
                    progress(progressValue)
                }
            }

            progress(100)

            return@withContext response.groups.map { it.name }.toSortedSet()
        }
    }

}