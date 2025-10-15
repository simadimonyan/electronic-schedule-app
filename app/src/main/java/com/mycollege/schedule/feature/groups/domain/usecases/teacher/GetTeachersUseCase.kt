package com.mycollege.schedule.feature.groups.domain.usecases.teacher

import androidx.compose.runtime.Immutable
import androidx.room.Transaction
import com.mycollege.schedule.app.activity.data.models.Teacher
import com.mycollege.schedule.core.cache.CacheManager
import com.mycollege.schedule.core.db.Database
import com.mycollege.schedule.core.network.RetrofitClient
import com.mycollege.schedule.feature.groups.ui.state.GroupStateHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Immutable
@Singleton
class GetTeachersUseCase @Inject constructor(
    private val database: Database,
    private val cacheManager: CacheManager,
    private val groupStateHolder: GroupStateHolder
) {

    suspend fun getRoomTeachers(department: String): Set<String> {
        return withContext(Dispatchers.IO) {
            val result = mutableSetOf<String>()
            if (!department.equals("Все кафедры")) {
                database.teachers().findTeachersBy(department).map {
                    if (!(it.contains("Вакансия") || it.contains("null") || it.contains("ВАК"))) {
                        result.add(it)
                    }
                }
                result.toSortedSet()
            }
            else {
                database.teachers().getTeachers().map {
                    if (!(it.contains("Вакансия") || it.contains("null") || it.contains("ВАК"))) {
                        result.add(it)
                    }
                }
                result.toSortedSet()
            }
        }
    }

    @Transaction
    suspend fun getServerTeachers(progress: (Int) -> Unit): Set<String> {
        return withContext(Dispatchers.IO) {
            val scheduleServerConfiguration = cacheManager.loadScheduleServerConfiguration()

            progress(10)

            // запрос преподавателей
            val response = RetrofitClient(scheduleServerConfiguration.serverUrl).teachersApi
                .search(scheduleServerConfiguration.accessToken).teachers

            val dbTeachers = database.teachers().getTeachers()

            if (response.isNotEmpty()) {

                progress(50)

                // кеширование времени последнего запроса
                val lastRequest = cacheManager.loadServerNetworkLastRequest()
                if (lastRequest != null) {
                    cacheManager.saveServerNetworkLastRequest(CacheManager.ServerNetworkLastRequest(
                        lastRequest.weekParitySynchronization,
                        lastRequest.groupChooseConfiguration,
                        System.currentTimeMillis(),
                        lastRequest.groupScheduleSynchronization,
                        lastRequest.teacherScheduleSynchronization
                    ))
                }
                else
                    cacheManager.saveServerNetworkLastRequest(CacheManager.ServerNetworkLastRequest(
                        groupChooseConfiguration = System.currentTimeMillis()))

                progress(75)

                // добавление в бд
                for (teacher in response) {
                    database.persistence().save(Teacher(
                        teacher.label,
                        teacher.department.toString()
                    ))
                }

                // разница удаленных преподавателей из сервера
                val teachersToDelete = dbTeachers - response.map { it.label }
                database.schedule().setNullTeachers(teachersToDelete) // обнуляет преподавателей в расписании групп (в группах такого нет тк без группы нет расписания)
                database.teachers().clearTable(teachersToDelete)
                if (teachersToDelete.contains(groupStateHolder.groupState.value.teacher))
                    groupStateHolder.updateTeacher("Выбрать")

            }
            progress(100)

            return@withContext response.map { it.label }.toSortedSet()
        }
    }

}