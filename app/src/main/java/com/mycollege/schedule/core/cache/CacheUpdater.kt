package com.mycollege.schedule.core.cache

import android.content.Context
import androidx.compose.runtime.Stable
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.mycollege.schedule.app.workmanager.GroupSyncWorker
import com.mycollege.schedule.app.workmanager.ScheduleWorker
import com.mycollege.schedule.app.workmanager.WeekChangeWorker
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Stable
@Singleton
class CacheUpdater @Inject constructor(
    private var cacheManager: CacheManager
) {

    /**
     * Просчет сколько осталось до следующего обновления (период 1 день) с последнего
     */
    private fun calculateDelayUntilNextUpdate(lastUpdateTime: Long): Long {
        val currentTime = System.currentTimeMillis()
        val oneDayInMillis = 24 * 60 * 60 * 1000L

        if (lastUpdateTime == 0L) {
            return oneDayInMillis
        }

        val timeSinceLastUpdate = currentTime - lastUpdateTime

        return if (timeSinceLastUpdate < oneDayInMillis) {
            oneDayInMillis - timeSinceLastUpdate
        } else {
            0
        }
    }

    /**
     * Просчет сколько осталось до полуночи
     */
    private fun calculateDelayUntilMidnight(): Long {
        val currentTime = System.currentTimeMillis()
        val currentDate = LocalDate.now()

        val midnight = currentDate.plusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        return midnight - currentTime
    }

    /**
     * Просчет сколько осталось до понедельника
     */
    private fun getDelayUntilNextMonday(): Long {
        val now = LocalDateTime.now()
        val nextMonday = now.with(TemporalAdjusters.next(DayOfWeek.MONDAY)).withHour(0).withMinute(0).withSecond(0)
        return Duration.between(now, nextMonday).toMillis()
    }

    /**
     * Установка GroupSyncWorker для обновления парсинга данных раз в день
     */
    fun setupPeriodicWork(context: Context) {

        val last = cacheManager.getLastUpdatedTime()

        val delay = calculateDelayUntilNextUpdate(last)

        val periodicWorkRequest = PeriodicWorkRequestBuilder<GroupSyncWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiresCharging(false)
                    .setRequiresBatteryNotLow(false)
                    .setRequiresStorageNotLow(false)
                    .setRequiresDeviceIdle(false)
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "GroupSyncWorker",
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            periodicWorkRequest
        )
    }

    /**
     * Установка ScheduleWorker для обновления отложенных уведомлений раз в день в полночь
     */
    fun setupPeriodicScheduleWork(context: Context) {
        val delayUntilMidnight = calculateDelayUntilMidnight()

        val periodicScheduleWorkRequest = PeriodicWorkRequestBuilder<ScheduleWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(delayUntilMidnight, TimeUnit.MILLISECONDS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiresCharging(false)
                    .setRequiresBatteryNotLow(false)
                    .setRequiresStorageNotLow(false)
                    .setRequiresDeviceIdle(false)
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "ScheduleWorker",
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            periodicScheduleWorkRequest
        )
    }

    /**
     * Установка WeekChangeWorker для смены недели раз в неделю в понедельник
     */
    fun scheduleWeekChangeWorker(context: Context) {
        val workRequest = PeriodicWorkRequestBuilder<WeekChangeWorker>(7, TimeUnit.DAYS)
            .setInitialDelay(getDelayUntilNextMonday(), TimeUnit.MILLISECONDS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiresCharging(false)
                    .setRequiresBatteryNotLow(false)
                    .setRequiresStorageNotLow(false)
                    .setRequiresDeviceIdle(false)
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "WeekChangeWorker",
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            workRequest
        )
    }

}