package com.mycollege.schedule.feature.groups.domain.usecases.student

import androidx.compose.runtime.Immutable
import com.my.tracker.MyTracker
import com.mycollege.schedule.core.cache.CacheManager
import com.mycollege.schedule.core.db.Database
import com.mycollege.schedule.core.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Immutable
class GetCoursesUseCase @Inject constructor(
    private val database: Database,
    private val cacheManager: CacheManager
) {

    suspend fun getRoomCourses(): Set<String> {
        return withContext(Dispatchers.IO) {
            MyTracker.trackEvent("Получить кешированный список курсов")
            return@withContext database.groups().getCourses().toSortedSet()
        }
    }

    suspend fun getServerCourses(): Set<String> {
        return withContext(Dispatchers.IO) {
            MyTracker.trackEvent("Получить список курсов из сервера")
            val scheduleServerConfiguration = cacheManager.loadScheduleServerConfiguration()
            return@withContext RetrofitClient(scheduleServerConfiguration.serverUrl).groupsApi
                .courses(scheduleServerConfiguration.accessToken).courses
                .map { "$it" }.toSortedSet()
        }
    }

}