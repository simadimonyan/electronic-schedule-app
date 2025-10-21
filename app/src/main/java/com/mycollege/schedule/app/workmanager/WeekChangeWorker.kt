package com.mycollege.schedule.app.workmanager

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Stable
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.my.tracker.MyTracker
import com.mycollege.schedule.core.cache.CacheManager
import com.mycollege.schedule.feature.settings.ui.state.SettingsState
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@Stable
@HiltWorker
class WeekChangeWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val cacheManager: CacheManager
) : CoroutineWorker(context, workerParams) {

    /**
     * Смена недели при выключенной синхронизации недели
     */
    override suspend fun doWork(): Result {
        return try {
            var settings = cacheManager.loadLastSettings()
            // если настройка синхронизации с сервером выключена
            if (!settings.synchronizeWeekParity) {
                settings = SettingsState(
                    settings.navigationInvisibility,
                    settings.notificationsEnabled,
                    settings.fullWeekVisibility,
                    settings.synchronizeWeekParity,
                    !settings.weekCount
                )
                cacheManager.saveActualSettings(settings)
                MyTracker.trackEvent("Локальная смена четности недели без синхронизации")
            }
            cacheManager.clearDismissedNotifications()
            Log.e("WeekChangerWorker", "Week auto-changing executed!")
            Result.success()
        } catch (e: Exception) {
            Log.e("WeekChangerWorker", "Error in worker", e)
            e.printStackTrace()
            Result.failure()
        }

    }
}