package com.mycollege.schedule.app.workmanager

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Stable
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.mycollege.schedule.R
import com.mycollege.schedule.app.activity.domain.usecases.GetScheduleUseCase
import com.mycollege.schedule.app.notifications.NotificationsManager
import com.mycollege.schedule.core.cache.CacheManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Stable
@HiltWorker
@Suppress("SameParameterValue")
class GroupSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private var cacheManager: CacheManager,
    private var getScheduleUseCase: GetScheduleUseCase
) : CoroutineWorker(context, workerParams) {

    /**
     * Парсинг данных групп
     */
    override suspend fun doWork(): Result {
        return try {
            Log.d("GroupSyncWorker", "Last updated time: ${cacheManager.getLastUpdatedTime()}")
            if (cacheManager.getLastUpdatedTime() != 0L) {
                val notification = NotificationsManager().createNotification(applicationContext, R.string.get_data.toString())
                Log.d("GroupSyncWorker", "notification created: $notification visibility: ${notification.visibility}")
                setForeground(ForegroundInfo(1, notification))

                withContext(Dispatchers.IO) {
                    Log.d("GroupSyncWorker", "loading data")
                    getScheduleUseCase.getSchedule { newProgress ->
                        NotificationsManager().updateProgressNotification(1, applicationContext, newProgress) // Update progress
                    }
                }

                cacheManager.saveLastUpdatedTime(System.currentTimeMillis())
                Log.d("GroupSyncWorker", "data cached")

                NotificationsManager().cancelNotification(1, applicationContext)
            }
            Result.success()
        } catch (e: Exception) {
            Log.e("GroupSyncWorker", "Error in worker", e)
            e.printStackTrace()
            Result.failure()
        }
    }

}