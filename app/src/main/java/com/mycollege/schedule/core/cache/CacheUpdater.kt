package com.mycollege.schedule.core.cache

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.Stable
import androidx.preference.PreferenceManager
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.mycollege.schedule.R
import com.mycollege.schedule.core.notifications.NotificationReceiver
import com.mycollege.schedule.core.notifications.NotificationsManager
import com.mycollege.schedule.feature.schedule.data.models.DataClasses
import com.mycollege.schedule.feature.schedule.domain.usecase.GetScheduleUseCase.Companion.getSchedule
import com.mycollege.schedule.feature.schedule.domain.usecase.GetWeekCountUseCase
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
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Stable
@Singleton
class CacheUpdater @Inject constructor(
    private var cacheManager: CacheManager
) {

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

    private fun calculateDelayUntilMidnight(): Long {
        val currentTime = System.currentTimeMillis()
        val currentDate = LocalDate.now()

        val midnight = currentDate.plusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        return midnight - currentTime
    }

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

    private fun getDelayUntilNextMonday(): Long {
        val now = LocalDateTime.now()
        val nextMonday = now.with(TemporalAdjusters.next(DayOfWeek.MONDAY)).withHour(0).withMinute(0).withSecond(0)
        return Duration.between(now, nextMonday).toMillis()
    }

}

@Stable
class WeekChangeWorker(
    context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {

    private var cacheManager: CacheManager = CacheManager(PreferenceManager.getDefaultSharedPreferences(applicationContext))

    override suspend fun doWork(): Result {
        return try {
            val settings = cacheManager.loadLastSettings()
            cacheManager.saveActualSettings(CacheManager.Settings(settings.fullWeek, settings.isNavInvisible, !settings.changeWeekCount))
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
@Suppress("SameParameterValue")
class GroupSyncWorker(
    context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {

    private var cacheManager: CacheManager = CacheManager(PreferenceManager.getDefaultSharedPreferences(applicationContext))

    override suspend fun doWork(): Result {
        return try {
            if (cacheManager.getLastUpdatedTime() != 0L) {
                val notification = NotificationsManager().createNotification(applicationContext, R.string.get_data.toString())
                Log.d("GroupSyncWorker", "notification created: $notification visibility: ${notification.visibility}")
                setForeground(ForegroundInfo(1, notification))

                val loadedGroups = withContext(Dispatchers.IO) {
                    Log.d("GroupSyncWorker", "loading data")
                    getSchedule { newProgress ->
                        NotificationsManager().updateProgressNotification(1, applicationContext, newProgress) // Update progress
                    }
                }

                cacheManager.saveGroupsToCache(loadedGroups)
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
class ScheduleWorker(
    context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {

    private var cacheManager: CacheManager = CacheManager(PreferenceManager.getDefaultSharedPreferences(applicationContext))

    override suspend fun doWork(): Result {
        return try {
            Log.e("ScheduleWorker", "Starting")
            val appContext = applicationContext

            val currentDate = LocalDate.now(ZoneId.systemDefault())

            val todayLessons = getTodayLessons(currentDate)

            updateCache(todayLessons, appContext)

            // clear all of the alarms
            val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val alarms = cacheManager.loadAlarms()

            if (alarms != null && alarms.isNotEmpty()) {
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
                val intent = setNotificationForLesson(applicationContext, lesson, i, currentDate)
                intents.add(CacheManager.IntentConf(i, intent))
            }
            cacheManager.saveAlarms(intents)

            Result.success()
        } catch (e: Exception) {
            Log.e("ScheduleWorker", "Error in worker", e)
            e.printStackTrace()
            Result.failure()
        }
    }

    private fun setNotificationForLesson(context: Context, lesson: DataClasses.Lesson, id: Int, currentDate: LocalDate): Intent {
        val lessonName = lesson.name
        val lessonCount = lesson.count
        val lessonLocation = lesson.location
        val lessonTime = lesson.time

        val lessonStartTimeString = lessonTime.split("-")[0]
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        val lessonStartTime = LocalTime.parse(lessonStartTimeString, formatter)
        // val currentDate = LocalDate.now(ZoneId.systemDefault()) // replaced by parameter

        val lessonTimeInMillis = currentDate.atTime(lessonStartTime).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        Log.d("ScheduleWorker", "scheduling lesson...")

        val notificationTime = lessonTimeInMillis - 5 * 60 * 1000

        if (notificationTime < System.currentTimeMillis()) {
            Log.w("ScheduleWorker", "Уведомление пропущено: $lessonName, время уже прошло")
            val intent = Intent(context, NotificationReceiver::class.java).apply {
                putExtra("lesson", "Пара $lessonCount: $lessonName в $lessonLocation")
            }
            return intent
        }

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("lesson", "Пара $lessonCount: $lessonName в $lessonLocation")
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

    private suspend fun getWeekLessonsByGroup(currentDate: LocalDate): HashMap<Int, ArrayList<DataClasses.Lesson>> {
        return withContext(Dispatchers.IO) {

            val groupsConfiguration = cacheManager.loadLastConfiguration()

            val groups: ArrayList<DataClasses.Group>? =
                cacheManager.loadGroupsFromCache()[groupsConfiguration.course]?.get(
                    when {
                        groupsConfiguration.group.contains("СПО") -> "СПО"
                        groupsConfiguration.group.contains("Мг") -> "Магистратура"
                        else -> "Бакалавриат"
                    }
                )

            var chosenGroup: DataClasses.Group? = null
            val count = GetWeekCountUseCase.calculateCount()

            if (groups != null) {
                for (group in groups.iterator()) {
                    if (group.group == groupsConfiguration.group) {
                        chosenGroup = group
                        break
                    }
                }
            }

            try {
                if (chosenGroup != null) {
                    return@withContext if (count == 0) chosenGroup.lessons?.weekEven!! else chosenGroup.lessons?.weekOdd!!
                }
            } catch (e: NullPointerException) {
                return@withContext HashMap()
            }
            return@withContext HashMap()
        }
    }

    private suspend fun getTodayLessons(currentDate: LocalDate): ArrayList<DataClasses.Lesson> {
        return withContext(Dispatchers.IO) {
            val week: HashMap<Int, ArrayList<DataClasses.Lesson>> = getWeekLessonsByGroup(currentDate)

            // val currentDate = LocalDate.now(ZoneId.systemDefault()) // replaced by parameter
            val formatter = DateTimeFormatter.ofPattern("EEEE", Locale.ENGLISH)
            val dayWeek = currentDate.format(formatter).uppercase()

            for (day in week.keys) {
                if (DataClasses.DayWeek.findById(day)?.name == dayWeek) {
                    return@withContext week[day] as ArrayList<DataClasses.Lesson>
                }
            }
            return@withContext ArrayList()
        }
    }

    private fun updateCache(todayLessons: ArrayList<DataClasses.Lesson>, context: Context) {
        cacheManager.saveTodaySchedule(todayLessons)
    }

}
