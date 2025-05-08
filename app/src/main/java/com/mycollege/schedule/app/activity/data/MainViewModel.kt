package com.mycollege.schedule.app.activity.data

import android.app.NotificationManager
import android.content.Context
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mycollege.schedule.R
import com.mycollege.schedule.core.cache.CacheManager
import com.mycollege.schedule.core.cache.CacheUpdater
import com.mycollege.schedule.core.notifications.NotificationsManager
import com.mycollege.schedule.feature.schedule.domain.usecase.GetScheduleUseCase.Companion.getSchedule
import com.mycollege.schedule.shared.resources.ResourceManager
import com.mycollege.schedule.shared.state.SharedStateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.ok.tracer.crash.report.TracerCrashReport
import javax.inject.Inject

@Stable
@HiltViewModel
class MainViewModel @Inject constructor(
    private val resources: ResourceManager,
    private val cacheUpdater: CacheUpdater,
    val cacheManager: CacheManager,
    val shared: SharedStateRepository
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
        shared.updatingFirstStartup(cacheManager.isFirstStartup())
    }

    private fun fetchData() {
        fetchDataJob = viewModelScope.launch {
            val context = resources.getContext()

            val notificationsManager = NotificationsManager()

            try {
                notificationsManager.createNotificationChannel(context)
                if (cacheManager.shouldUpdateCache()) {
                    shared.updateLoading(true)

                    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    val notification = notificationsManager.createNotification(context, context.getString(R.string.get_data))
                    notificationManager.notify(2, notification)

                    val loadedGroups = withContext(Dispatchers.IO) {
                        getSchedule { newProgress ->
                            shared.updateProgress(newProgress)
                            notificationsManager.updateProgressNotification(2, context,
                                newProgress
                            )
                        }
                    }

                    cacheManager.saveGroupsToCache(loadedGroups)
                    cacheManager.saveLastUpdatedTime(System.currentTimeMillis())
                    shared.loadGroups(loadedGroups)

                    notificationsManager.cancelNotification(2, context)
                    shared.updateLoading(false)
                }
                else {
                    shared.loadGroups(cacheManager.loadGroupsFromCache())
                }
            } catch (e: Exception) {
                shared.updateLoading(true)
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
                shared.updateCourse(configuration.course)
                shared.updateSpeciality(configuration.speciality)
                shared.updateGroup(configuration.group)
            }

        } catch (e: Exception) {
            // first-time setup or empty cache case
        }
    }

}