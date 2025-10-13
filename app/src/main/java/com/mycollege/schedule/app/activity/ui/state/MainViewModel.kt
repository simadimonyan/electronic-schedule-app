package com.mycollege.schedule.app.activity.ui.state

import android.app.NotificationManager
import android.content.Context
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mycollege.schedule.app.activity.domain.models.LoadingStateHolder
import com.mycollege.schedule.app.notifications.NotificationsManager
import com.mycollege.schedule.core.cache.CacheManager
import com.mycollege.schedule.core.cache.CacheUpdater
import com.mycollege.schedule.feature.groups.ui.state.GroupStateHolder
import com.mycollege.schedule.feature.settings.ui.state.SettingsStateHolder
import com.mycollege.schedule.shared.resources.ResourceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ru.ok.tracer.crash.report.TracerCrashReport
import javax.inject.Inject

@Stable
@HiltViewModel
class MainViewModel @Inject constructor(
    private val resources: ResourceManager,
    private val cacheUpdater: CacheUpdater,
    val cacheManager: CacheManager,
    val appStateHolder: AppStateHolder,
    val groupParserStateHolder: LoadingStateHolder,
    val settingsStateHolder: SettingsStateHolder,
    val groupStateHolder: GroupStateHolder,
) : ViewModel() {

    private var fetchDataJob: Job? = null

    fun handleEvent(event: DataEvent) {
        when (event) {
            is DataEvent.FetchData -> fetchData()
            is DataEvent.SetupCacheUpdater -> setupCacheUpdater()
            is DataEvent.RestoreCache -> restoreCache()
        }
    }

    fun destroyNotifications() {
        val notificationManager = resources.getContext().getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager
        fetchDataJob?.cancel()
        notificationManager.cancel(2)
    }

    init {
        //appStateHolder.updatingFirstStartup(cacheManager.isFirstStartup())
    }

    private fun fetchData() {
        fetchDataJob = viewModelScope.launch {
            val context = resources.getContext()

            val notificationsManager = NotificationsManager()

            try {
                notificationsManager.createNotificationChannel(context)
                if (cacheManager.shouldUpdateCache()) {
//                    groupParserStateHolder.updateLoading(true)
//
//                    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//                    val notification = notificationsManager.createNotification(context, context.getString(
//                        R.string.get_data))
//                    notificationManager.notify(2, notification)
//
//                    withContext(Dispatchers.IO) {
//                        getScheduleUseCase.getSchedule { newProgress ->
//                            groupParserStateHolder.updateProgress(newProgress)
//                            notificationsManager.updateProgressNotification(
//                                2, context,
//                                newProgress
//                            )
//                        }
//                    }
//                    cacheManager.saveLastUpdatedTime(System.currentTimeMillis())
//
//
//                    notificationsManager.cancelNotification(2, context)
//                    groupParserStateHolder.updateLoading(false)

                }

            } catch (e: Exception) {
                groupParserStateHolder.updateChooseConfigurationLoading(true)
                e.printStackTrace()
                TracerCrashReport.report(e, issueKey = "NETWORK")
            }
        }
    }

    private fun setupCacheUpdater() {
        viewModelScope.launch {
            val context = resources.getContext()
            cacheUpdater.setupPeriodicWork(context)
            cacheUpdater.setupPeriodicScheduleWork(context)
            cacheUpdater.scheduleWeekChangeWorker(context)
        }
    }

    // global app restore
    // in main thread only | to avoid delay of loading
    private fun restoreCache() {
        try {
            val configuration = cacheManager.loadLastConfiguration()

            if (configuration.group.isNotEmpty()) {
                groupStateHolder.updateCourse(configuration.course)
                groupStateHolder.updateLevel(configuration.speciality)
                groupStateHolder.updateGroup(configuration.group)
            }

        } catch (e: Exception) {
            // first-time setup or empty cache case
        }
    }

}