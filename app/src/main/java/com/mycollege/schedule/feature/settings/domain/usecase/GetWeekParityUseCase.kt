package com.mycollege.schedule.feature.settings.domain.usecase

import androidx.compose.runtime.Immutable
import com.mycollege.schedule.core.cache.CacheManager
import com.mycollege.schedule.core.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Immutable
@Singleton
class GetWeekParityUseCase @Inject constructor(
    private val cacheManager: CacheManager
){

    suspend fun getWeekParity(): Int {
        return withContext(Dispatchers.IO) {
            val scheduleServerConfiguration = cacheManager.loadScheduleServerConfiguration()
            return@withContext RetrofitClient(scheduleServerConfiguration.serverUrl)
                .configsApi.getWeek(scheduleServerConfiguration.accessToken).weekCount
        }
    }

}