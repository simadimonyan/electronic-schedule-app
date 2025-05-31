package com.mycollege.schedule.core.cache

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.Stable
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.mycollege.schedule.R
import com.mycollege.schedule.app.activity.domain.usecases.GetScheduleUseCase
import com.mycollege.schedule.app.notifications.NotificationReceiver
import com.mycollege.schedule.app.notifications.NotificationsManager
import com.mycollege.schedule.feature.schedule.data.models.DataClasses
import com.mycollege.schedule.feature.schedule.data.models.DataClasses.DayWeek
import com.mycollege.schedule.feature.schedule.domain.usecase.GetChosenGroupUseCase
import com.mycollege.schedule.feature.schedule.domain.usecase.GetTodayScheduleUseCase
import com.mycollege.schedule.feature.settings.ui.state.SettingsState
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
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
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "GroupSyncWorker",
            ExistingPeriodicWorkPolicy.UPDATE,
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
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "ScheduleWorker",
            ExistingPeriodicWorkPolicy.UPDATE,
            periodicScheduleWorkRequest
        )
    }

    /**
     * Установка WeekChangeWorker для смены недели раз в неделю в понедельник
     */
    fun scheduleWeekChangeWorker(context: Context) {
        val workRequest = PeriodicWorkRequestBuilder<WeekChangeWorker>(7, TimeUnit.DAYS)
            .setInitialDelay(getDelayUntilNextMonday(), TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "WeekChangeWorker",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

}

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

@Stable
@HiltWorker
class ScheduleWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private var cacheManager: CacheManager,
    private val getChosenGroupUseCase: GetChosenGroupUseCase,
    private val getTodayScheduleUseCase: GetTodayScheduleUseCase
) : CoroutineWorker(context, workerParams) {

    /**
     * Установка отложенных уведомлений
     */
    override suspend fun doWork(): Result {
        return try {
            Log.e("ScheduleWorker", "Starting")
            val appContext = applicationContext

            val todayLessons = getTodayLessons()

            // clear all of the alarms
            val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val alarms = cacheManager.loadAlarms()

            if (alarms.isNotEmpty()) {
                for (alarm in alarms) {

                    val pendingIntent = PendingIntent.getBroadcast(
                        appContext,
                        alarm.id,
                        alarm.intent,
                        PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    alarmManager.cancel(pendingIntent)
                }
            }

            // creating new alarms
            val intents = ArrayList<CacheManager.IntentConf>()
            for ((i, lesson) in todayLessons.withIndex()) {
                Log.e("ScheduleWorker", "setting alarms...")
                val intent = setNotificationForLesson(applicationContext, lesson, i)
                if (intent != null) intents.add(CacheManager.IntentConf(i, intent))
            }
            cacheManager.saveAlarms(intents)

            Result.success()
        } catch (e: Exception) {
            Log.e("ScheduleWorker", "Error in worker", e)
            e.printStackTrace()
            Result.failure()
        }
    }

    /**
     * Установить отложенное уведомление на пару
     */
    private fun setNotificationForLesson(context: Context, lesson: DataClasses.Lesson, id: Int): Intent? {
        val lessonName = lesson.name
        val lessonCount = lesson.count
        val lessonLocation = lesson.location
        val lessonTime = lesson.time

        val lessonStartTimeString = lessonTime.split("-")[0]
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        val lessonStartTime = LocalTime.parse(lessonStartTimeString, formatter)
        val currentDate = LocalDate.now(ZoneId.systemDefault()) // replaced by parameter

        val lessonTimeInMillis = currentDate.atTime(lessonStartTime).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        Log.d("ScheduleWorker", "scheduling lesson...")

        val notificationTime = lessonTimeInMillis - 5 * 60 * 1000

        if (lessonTimeInMillis < System.currentTimeMillis()) {
            Log.w("ScheduleWorker", "Уведомление пропущено: $lessonName, время уже прошло")
            return null
        }

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("lesson", "Пара $lessonCount: $lessonName в $lessonLocation")
            putExtra("timestamp", lessonTimeInMillis)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            notificationTime,
            pendingIntent
        )
        return intent
    }

    /**
     * Получить пары на сегодня
     */
    private suspend fun getTodayLessons(): ArrayList<DataClasses.Lesson> {
        return withContext(Dispatchers.IO) {
            val today = getTodayScheduleUseCase.getTodaySchedule(
                getChosenGroupUseCase.getByName(cacheManager.loadLastConfiguration().group)!!,
                DayWeek.findById(LocalDate.now().dayOfWeek.value)?.long ?: "Понедельник",
                if (cacheManager.loadLastSettings().weekCount) 0 else 1
            )
            return@withContext today as ArrayList<DataClasses.Lesson>
        }
    }

}
