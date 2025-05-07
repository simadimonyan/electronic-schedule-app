package com.mycollege.schedule.feature.schedule.ui.state

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mycollege.schedule.core.cache.CacheManager
import com.mycollege.schedule.feature.schedule.data.models.DataClasses
import com.mycollege.schedule.core.notifications.NotificationReceiver
import com.mycollege.schedule.feature.schedule.domain.usecase.GetWeekCountUseCase
import com.mycollege.schedule.shared.resources.ResourceManager
import com.mycollege.schedule.shared.state.SharedStateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
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
import kotlin.collections.iterator

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val cacheManager: CacheManager,
    private val resources: ResourceManager,
    val shared: SharedStateRepository
) : ViewModel() {

    private var _scheduleState = MutableStateFlow(ScheduleState())
    val scheduleState: StateFlow<ScheduleState> = _scheduleState

    fun init() {

        // restore cache and init dates
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                showDateToday()
                showTodayLessons()
            }
        }

        // schedule creation listener
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                shared.scheduleCreateSignal.collect { value ->
                    if (value) {
                        showTodayLessons()
                    }
                }
            }
        }

    }

    fun changeWeekCountEvent() {
        viewModelScope.launch { showTodayLessons() }
    }

    private fun updateWeekDates() {
        viewModelScope.launch {
            _scheduleState.update { it.copy(weekDates = getCurrentWeekDate()) }
        }
    }

    private fun showTodayLessons() {
        viewModelScope.launch {
            _scheduleState.update { it.copy(todayLessons = getTodayLessons()) }
        }
    }

    private fun showDateToday() {
        viewModelScope.launch {
            _scheduleState.update { it.copy(todayDate = getTodayDate()) }
        }
    }

    private fun showWeekLessons(week: HashMap<Int, ArrayList<DataClasses.Lesson>>) {
        _scheduleState.update { it.copy(weekLessons = week) }
    }

    private suspend fun getWeekLessonsByGroup(): HashMap<Int, ArrayList<DataClasses.Lesson>> {
        return withContext(Dispatchers.IO) {

            val groups: ArrayList<DataClasses.Group>? =
                cacheManager.loadGroupsFromCache()[shared.course.value]?.get(
                    when {
                        shared.group.value.contains("СПО") -> "СПО"
                        shared.group.value.contains("Мг") -> "Магистратура"
                        else -> "Бакалавриат"
                    }
                )

            var chosenGroup: DataClasses.Group? = null
            var count = GetWeekCountUseCase.Companion.calculateCount()

            if (shared.changeWeekCount.value) count =
                if (count == 1) 0 else 1 // if change week event is executed

            if (groups != null) {
                for (group in groups.iterator()) {
                    if (group.group == shared.group.value) {
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

    private suspend fun getTodayLessons(): ArrayList<DataClasses.Lesson> {
        return withContext(Dispatchers.IO) {
            val week: HashMap<Int, ArrayList<DataClasses.Lesson>> = getWeekLessonsByGroup()

            // update week
            showWeekLessons(week)
            updateWeekDates()

            val currentDate = LocalDate.now()
            val formatter = DateTimeFormatter.ofPattern("EEEE", Locale.ENGLISH)
            val dayWeek = currentDate.format(formatter).uppercase()

            for (day in week.keys) {
                if (DataClasses.DayWeek.findById(day)?.name == dayWeek) {
                    cacheManager.saveTodaySchedule(week[day] as ArrayList<DataClasses.Lesson>)

                    // clear all of the alarms
                    val alarmManager = resources.getContext()
                        .getSystemService(Context.ALARM_SERVICE) as AlarmManager

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

    private suspend fun getTodayDate(): String {
        return withContext(Dispatchers.IO) {
            val currentDate = LocalDate.now()
            val formatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM", Locale("RU"))
            return@withContext currentDate.format(formatter).replaceFirstChar { it.uppercase() }
        }
    }

}