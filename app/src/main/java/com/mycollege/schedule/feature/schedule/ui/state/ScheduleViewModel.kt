package com.mycollege.schedule.feature.schedule.ui.state

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mycollege.schedule.app.activity.domain.models.GroupParserStateHolder
import com.mycollege.schedule.app.notifications.NotificationReceiver
import com.mycollege.schedule.core.cache.CacheManager
import com.mycollege.schedule.core.db.Database
import com.mycollege.schedule.feature.groups.ui.state.GroupStateHolder
import com.mycollege.schedule.feature.schedule.data.models.DataClasses
import com.mycollege.schedule.feature.schedule.data.models.DataClasses.DayWeek
import com.mycollege.schedule.feature.schedule.domain.usecase.GetChosenGroupUseCase
import com.mycollege.schedule.feature.schedule.domain.usecase.GetTodayScheduleUseCase
import com.mycollege.schedule.feature.schedule.domain.usecase.GetWeekScheduleUseCase
import com.mycollege.schedule.feature.settings.ui.state.SettingsStateHolder
import com.mycollege.schedule.shared.resources.ResourceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import javax.inject.Inject

@Stable
@HiltViewModel
class ScheduleViewModel @Inject constructor(

    // cache & resources
    private val cacheManager: CacheManager,
    private val resources: ResourceManager,

    // state
    val groupStateHolder: GroupStateHolder,
    val scheduleStateHolder: ScheduleStateHolder,
    val settingsStateHolder: SettingsStateHolder,
    val parserStateHolder: GroupParserStateHolder,

    // use cases
    private val getChosenGroupUseCase: GetChosenGroupUseCase,
    private val getWeekScheduleUseCase: GetWeekScheduleUseCase,
    private val getTodayScheduleUseCase: GetTodayScheduleUseCase,

    // database
    private val database: Database

) : ViewModel() {

    fun handleEvent(event: ScheduleEvent) {
        when(event) {
            is ScheduleEvent.WeekCountChanged -> changedWeekCountEvent()
            is ScheduleEvent.ShowTodaySchedule -> getTodayLessons()
            is ScheduleEvent.ShowWeekSchedule -> getWeekLessons()
        }
    }

    /**
     * Обновить расписание по четности недели
     */
    private fun changedWeekCountEvent() {
        viewModelScope.launch { // при условии, что группа выбрана
            scheduleStateHolder.showDateToday(getTodayDate())
            if (cacheManager.loadLastConfiguration() != null && cacheManager.loadLastConfiguration().group != "Выбрать") {
                getTodayLessons()
                if (settingsStateHolder.settingsState.value.fullWeekVisibility) getWeekLessons()
            }
        }
    }

    /**
     * Обновить расписание за неделю
     */
    private fun getWeekLessons() {
        viewModelScope.launch {
            var chosenGroup = getChosenGroupUseCase.getByName(groupStateHolder.groupState.value.group)
            var count = if (settingsStateHolder.settingsState.value.weekCount) 0 else 1

            if (settingsStateHolder.settingsState.value.weekCount)
                count = if (count == 1) 0 else 1 // if change week event is executed

            if (chosenGroup != null) {
                scheduleStateHolder.showWeekLessons(getWeekScheduleUseCase.getWeekSchedule(chosenGroup, count))
                scheduleStateHolder.updateWeekDates(getCurrentWeekDate())
            }
        }
    }

    /**
     * Обновить расписание на день
     */
    private fun getTodayLessons() {
        viewModelScope.launch {

            val today = getTodayScheduleUseCase.getTodaySchedule(
                getChosenGroupUseCase.getByName(groupStateHolder.groupState.value.group)!!,
                DayWeek.findById(LocalDate.now().dayOfWeek.value)?.long ?: "Понедельник",
                if (settingsStateHolder.settingsState.value.weekCount) 0 else 1
            )

            // clear all of the alarms
            val alarmManager = resources.getContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val alarms = cacheManager.loadAlarms()

            if (alarms != null && alarms.isNotEmpty()) {
                for (alarm in alarms) {

                    val pendingIntent = PendingIntent.getBroadcast(
                        resources.getContext(),
                        alarm.id,
                        alarm.intent,
                        PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    alarmManager.cancel(pendingIntent)
                }
            }

            // set new alarms
            val intents = ArrayList<CacheManager.IntentConf>()
            for ((i, lesson) in (today as ArrayList<DataClasses.Lesson>).withIndex()) {
                val intent = setNotificationForLesson(resources.getContext(), lesson, i)
                if (intent != null) intents.add(CacheManager.IntentConf(i, intent))
            }
            cacheManager.saveAlarms(intents)

            scheduleStateHolder.showTodayLessons(today)
        }
    }

    /**
     * Определение дат по неделе отдельным массивом
     */
    private suspend fun getCurrentWeekDate(): HashMap<Int, String> {
        return withContext(Dispatchers.IO) {
            val week = HashMap<Int, String>()

            val today = LocalDate.now()
            val startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            val formatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM", Locale("RU"))

            for (i in 0..7) {
                val dayOfWeek = startOfWeek.plusDays(i.toLong())
                week[i + 1] = dayOfWeek.format(formatter).replaceFirstChar { it.uppercase() }
            }

            return@withContext week
        }
    }

    /**
     * Установка отложенного уведомления для одной пары
     */
    private suspend fun setNotificationForLesson(context: Context, lesson: DataClasses.Lesson, id: Int): Intent? {
        return withContext(Dispatchers.IO) {
            val lessonName = lesson.name
            val lessonCount = lesson.count
            val lessonLocation = lesson.location
            val lessonTime = lesson.time

            val lessonStartTimeString = lessonTime.split("-")[0]
            val formatter = DateTimeFormatter.ofPattern("HH:mm")
            val lessonStartTime = LocalTime.parse(lessonStartTimeString, formatter)
            val currentDate = LocalDate.now()
            val lessonTimeInMillis =
                currentDate.atTime(lessonStartTime).atZone(ZoneId.systemDefault()).toInstant()
                    .toEpochMilli()

            if (cacheManager.isNotificationDismissed(currentDate.dayOfWeek.toString(), "Пара $lessonCount: $lessonName в $lessonLocation")) {
                Log.d("ScheduleWorker", "Уведомление для пары $lessonName (lesson: Пара $lessonCount: $lessonName в $lessonLocation, date: $currentDate) уже смахнуто, пропускаем")
                return@withContext null
            }

            if (lessonTimeInMillis >= System.currentTimeMillis()) {
                val notificationTime = lessonTimeInMillis - 5 * 60 * 1000

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

                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    notificationTime,
                    pendingIntent
                )
                return@withContext intent
            }
            return@withContext null
        }
    }

    /**
     * Получить дату на сегодня - Пятница, 09 мая
     */
    private suspend fun getTodayDate(): String {
        return withContext(Dispatchers.IO) {
            val currentDate = LocalDate.now()
            val formatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM", Locale("RU"))
            return@withContext currentDate.format(formatter).replaceFirstChar { it.uppercase() }
        }
    }

}