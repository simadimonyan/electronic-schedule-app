package com.mycollege.schedule.app.workmanager

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.Stable
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mycollege.schedule.app.notifications.NotificationReceiver
import com.mycollege.schedule.core.cache.CacheManager
import com.mycollege.schedule.feature.schedule.data.models.DataClasses
import com.mycollege.schedule.feature.schedule.data.models.DataClasses.DayWeek
import com.mycollege.schedule.feature.schedule.domain.usecase.GetChosenGroupUseCase
import com.mycollege.schedule.feature.schedule.domain.usecase.GetTodayScheduleUseCase
import com.mycollege.schedule.feature.settings.domain.usecase.GetWeekParityUseCase
import com.mycollege.schedule.feature.settings.ui.state.SettingsState
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Stable
@HiltWorker
class ScheduleWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private var cacheManager: CacheManager,
    private val getChosenGroupUseCase: GetChosenGroupUseCase,
    private val getTodayScheduleUseCase: GetTodayScheduleUseCase,
    private val getWeekParityUseCase: GetWeekParityUseCase
) : CoroutineWorker(context, workerParams) {

    private val monitor = Mutex()

    /**
     * Установка отложенных уведомлений
     */
    override suspend fun doWork(): Result {
        return try {
            monitor.withLock {
                Log.i("ScheduleWorker", "Starting")
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
                    Log.i("ScheduleWorker", "setting alarms...")
                    val intent = setNotificationForLesson(applicationContext, lesson, i)
                    if (intent != null) intents.add(CacheManager.IntentConf(i, intent))
                }
                cacheManager.saveAlarms(intents)


                // СИНХРОНИЗАЦИЯ НЕДЕЛИ (если включена)

                var settings = cacheManager.loadLastSettings()

                // если настройка синхронизации с сервером выключена
                if (settings.synchronizeWeekParity) {
                    val parity = getWeekParityUseCase.getWeekParity()

                    settings = SettingsState(
                        settings.navigationVisibility,
                        settings.notificationsEnabled,
                        settings.fullWeekVisibility,
                        settings.synchronizeWeekParity,
                        settings.weekCount,
                        parity == 2 // false - нечетная
                    )
                    cacheManager.saveServerNetworkLastRequest(CacheManager.ServerNetworkLastRequest(
                        System.currentTimeMillis()))
                    cacheManager.saveActualSettings(settings)
                }

            }
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
    @SuppressLint("ScheduleExactAlarm")
    private fun setNotificationForLesson(context: Context, lesson: DataClasses.Lesson, id: Int): Intent? {
        val lessonName = lesson.name
        val lessonCount = lesson.count
        val lessonLocation = lesson.location
        val lessonTime = lesson.time

        val lessonStartTimeString = lessonTime.split("-")[0]
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        val lessonStartTime = LocalTime.parse(lessonStartTimeString, formatter)
        val currentDate = LocalDate.now(ZoneId.systemDefault())

        val lessonTimeInMillis = currentDate.atTime(lessonStartTime).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        if (cacheManager.isNotificationDismissed(currentDate.dayOfWeek.toString(), "Пара $lessonCount: $lessonName в $lessonLocation")) {
            Log.d("ScheduleWorker", "Уведомление для пары $lessonName (lesson: Пара $lessonCount: $lessonName в $lessonLocation, date: $currentDate) уже смахнуто, пропускаем")
            return null
        }

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
            val settings = cacheManager.loadLastSettings()
            val today = getTodayScheduleUseCase.getTodaySchedule(
                getChosenGroupUseCase.getByName(cacheManager.loadLastConfiguration().group)!!,
                DayWeek.findById(LocalDate.now().dayOfWeek.value)?.long ?: "Понедельник",
                if (settings == null) 0 else { if (settings.weekCount) 0 else 1 }
            )
            return@withContext today as ArrayList<DataClasses.Lesson>
        }
    }

}
