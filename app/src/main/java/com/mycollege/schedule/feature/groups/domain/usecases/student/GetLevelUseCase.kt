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
class GetLevelUseCase @Inject constructor(
    private val database: Database,
    private val cacheManager: CacheManager
) {

    suspend fun getRoomLevels(course: String): Set<String> {
        return withContext(Dispatchers.IO) {
            MyTracker.trackEvent("Получить кешированный список уровней")
            return@withContext database.groups().getLevelsBy(course).toSortedSet(compareBy({ it.length }, { it }))
        }
    }

    suspend fun getServerLevels(course: String): Set<String> {
        return withContext(Dispatchers.IO) {
            MyTracker.trackEvent("Получить список уровней из сервера")
            val scheduleServerConfiguration = cacheManager.loadScheduleServerConfiguration()
            return@withContext RetrofitClient(scheduleServerConfiguration.serverUrl).groupsApi
                .levels(scheduleServerConfiguration.accessToken, Integer.parseInt(course)).levels
                .toSortedSet(compareBy({ it.length }, { it }))
        }
    }

}