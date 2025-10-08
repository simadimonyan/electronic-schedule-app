package com.mycollege.schedule.feature.settings.domain.usecase

import android.util.Log
import androidx.compose.runtime.Immutable
import com.mycollege.schedule.core.cache.CacheManager
import com.mycollege.schedule.core.network.RetrofitClient
import ru.ok.tracer.crash.report.TracerCrashReport
import javax.inject.Inject
import javax.inject.Singleton

@Immutable
@Singleton
class GetWeekParityUseCase @Inject constructor(
    private val cacheManager: CacheManager
){

    suspend fun getWeekParity(): Int {
        var response = 1
        try {
            val scheduleServerConfiguration = cacheManager.loadScheduleServerConfiguration()
            response = RetrofitClient(scheduleServerConfiguration.serverUrl)
                .configsApi.getWeek(scheduleServerConfiguration.accessToken).weekCount
        }
        catch (e: Exception) {
            TracerCrashReport.report(e, issueKey = "GetWeekParityUseCase")
            Log.e("GetWeekParityUseCase", e.toString())
        }
        return response
    }

}