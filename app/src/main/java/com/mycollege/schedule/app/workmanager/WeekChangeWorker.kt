package com.mycollege.schedule.app.workmanager

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Stable
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mycollege.schedule.core.cache.CacheManager
import com.mycollege.schedule.feature.settings.ui.state.SettingsState
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@Stable
@HiltWorker
class WeekChangeWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private var cacheManager: CacheManager,
) : CoroutineWorker(context, workerParams) {

    /**
     * Смена недели
     */
    override suspend fun doWork(): Result {
        return try {
            val settings = cacheManager.loadLastSettings()
            cacheManager.saveActualSettings(
                SettingsState(
                    settings.navigationVisibility,
                    settings.fullWeekVisibility,
                    !settings.weekCount
                )
            )
            Log.e("WeekChangerWorker", "Week auto-changing executed!")
            Result.success()
        } catch (e: Exception) {
            Log.e("WeekChangerWorker", "Error in worker", e)
            e.printStackTrace()
            Result.failure()
        }

    }
}