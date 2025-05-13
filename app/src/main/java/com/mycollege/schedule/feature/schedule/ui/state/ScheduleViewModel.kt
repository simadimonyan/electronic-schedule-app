package com.mycollege.schedule.feature.schedule.ui.state

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
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
import com.mycollege.schedule.feature.schedule.domain.usecase.GetTeacherUseCase
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
import java.time.temporal.ChronoUnit
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
    val getTeacherUseCase: GetTeacherUseCase,

    // database
    private val database: Database

) : ViewModel() {

    fun init() {

        // restore cache and init dates
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                scheduleStateHolder.showDateToday(getTodayDate())
                scheduleStateHolder.showTodayLessons(getTodayLessons())
            }
        }

        // schedule creation listener
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                groupStateHolder.scheduleCreateSignal.collect { value ->
                    if (value) {
                        scheduleStateHolder.showTodayLessons(getTodayLessons())
                    }
                }
            }
        }

    }

    fun changeWeekCountEvent() {
        viewModelScope.launch { scheduleStateHolder.showTodayLessons(getTodayLessons()) }
    }

    /**
     * Получить расписание за неделю
     */
    private suspend fun getWeekLessonsByGroup(): HashMap<Int, ArrayList<DataClasses.Lesson>> {
        return withContext(Dispatchers.IO) {

            var chosenGroup = getChosenGroupUseCase.getByName(groupStateHolder.groupState.value.group)
            var count = calculateCount()

            if (settingsStateHolder.settingsState.value.weekCount)
                count = if (count == 1) 0 else 1 // if change week event is executed

            if (chosenGroup != null) {
                return@withContext getWeekScheduleUseCase.getWeekSchedule(chosenGroup, count)
            }
            return@withContext HashMap()
        }
    }

    private suspend fun getTodayLessons(): ArrayList<DataClasses.Lesson> {
        return withContext(Dispatchers.IO) {
            val week: HashMap<Int, ArrayList<DataClasses.Lesson>> = getWeekLessonsByGroup()

            // update week
            scheduleStateHolder.showWeekLessons(week)
            scheduleStateHolder.updateWeekDates(getCurrentWeekDate())

            val currentDate = LocalDate.now()
            val formatter = DateTimeFormatter.ofPattern("EEEE", Locale.ENGLISH)
            val dayWeek = currentDate.format(formatter).uppercase()

            for (day in week.keys) {
                if (DayWeek.findById(day)?.name == dayWeek) {

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
                    for ((i, lesson) in (week[day] as ArrayList<DataClasses.Lesson>).withIndex()) {
                        val intent = setNotificationForLesson(resources.getContext(), lesson, i)
                        if (intent != null) intents.add(CacheManager.IntentConf(i, intent))
                    }
                    cacheManager.saveAlarms(intents)

                    return@withContext week[day] as ArrayList<DataClasses.Lesson>
                }
            }
            return@withContext ArrayList()
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

            if (lessonTimeInMillis > System.currentTimeMillis()) {
                val notificationTime = lessonTimeInMillis - 5 * 60 * 1000

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

    /**
     * Посчитать номер недели
     */
    private fun calculateCount(): Int {
        val currentDate = LocalDate.now()

        val firstSeptember = LocalDate.of(currentDate.year, 9, 1)

        val startDate = if (currentDate.isBefore(firstSeptember)) {
            LocalDate.of(currentDate.year - 1, 9, 1)
        } else {
            firstSeptember
        }

        val weeksBetween = ChronoUnit.WEEKS.between(startDate, currentDate).toInt()

        // Count: 0 - even, 1 - odd
        return weeksBetween % 2
    }

}