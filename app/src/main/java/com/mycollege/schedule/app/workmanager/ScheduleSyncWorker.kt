package com.mycollege.schedule.app.workmanager

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Stable
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.my.tracker.MyTracker
import com.mycollege.schedule.app.activity.ui.state.AppStateHolder
import com.mycollege.schedule.core.cache.CacheManager
import com.mycollege.schedule.feature.groups.domain.usecases.student.GetGroupScheduleUseCase
import com.mycollege.schedule.feature.groups.domain.usecases.teacher.GetTeacherScheduleUseCase
import com.mycollege.schedule.feature.groups.ui.state.GroupStateHolder
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@Stable
@HiltWorker
@Suppress("SameParameterValue")
class ScheduleSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val cacheManager: CacheManager,
    private val appStateHolder: AppStateHolder,
    private val groupStateHolder: GroupStateHolder,
    private val getGroupScheduleUseCase: GetGroupScheduleUseCase,
    private val getTeacherScheduleUseCase: GetTeacherScheduleUseCase,
    //private var getScheduleUseCase: GetScheduleUseCase

) : CoroutineWorker(context, workerParams) {

    /**
     * Парсинг данных групп
     */
    override suspend fun doWork(): Result {
        return try {

//            Log.d("GroupSyncWorker", "Last updated time: ${cacheManager.getLastUpdatedTime()}")
//            if (cacheManager.getLastUpdatedTime() != 0L) {
//                val notification = NotificationsManager().createNotification(applicationContext, R.string.get_data.toString())
//                Log.d("GroupSyncWorker", "notification created: $notification visibility: ${notification.visibility}")
//                setForeground(ForegroundInfo(1, notification))
//
//                withContext(Dispatchers.IO) {
//                    Log.d("GroupSyncWorker", "loading data")
//                    getScheduleUseCase.getSchedule { newProgress ->
//                        NotificationsManager().updateProgressNotification(1, applicationContext, newProgress) // Update progress
//                    }
//                }
//
//                cacheManager.saveLastUpdatedTime(System.currentTimeMillis())
//                Log.d("GroupSyncWorker", "data cached")
//
//                NotificationsManager().cancelNotification(1, applicationContext)
//            }

            // отправить запрос на обновление расписания

            val settings = cacheManager.loadLastSettings()

            if (settings != null) {

                if (appStateHolder.appState.value.studentMode) {
                    if (groupStateHolder.groupState.value.group != "Выбрать группу") {
                        MyTracker.trackEvent("Фоновый процесс загрузки расписания группы из сервера")
                        getGroupScheduleUseCase.getServerGroupSchedule(groupStateHolder.groupState.value.group)
                    }
                }
                else {
                    if (groupStateHolder.groupState.value.teacher != "Выбрать преподавателя") {
                        MyTracker.trackEvent("Фоновый процесс загрузки расписания преподавателя из сервера")
                        getTeacherScheduleUseCase.getServerTeacherSchedule(groupStateHolder.groupState.value.teacher)
                    }
                }

            }

            Result.success()
        } catch (e: Exception) {
            Log.e("GroupSyncWorker", "Error in worker", e)
            e.printStackTrace()
            Result.failure()
        }
    }

}