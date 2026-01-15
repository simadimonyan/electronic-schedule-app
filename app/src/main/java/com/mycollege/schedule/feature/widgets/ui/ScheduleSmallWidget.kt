package com.mycollege.schedule.feature.widgets.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.ExperimentalGlanceApi
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.width
import androidx.glance.text.Text
import com.mycollege.schedule.app.activity.data.models.Group
import com.mycollege.schedule.app.activity.data.models.Teacher
import com.mycollege.schedule.app.activity.ui.MainActivity
import com.mycollege.schedule.core.di.widget.WidgetEntryPoint
import com.mycollege.schedule.feature.schedule.data.models.DataClasses
import com.mycollege.schedule.feature.schedule.data.models.DataClasses.DayWeek
import com.mycollege.schedule.feature.widgets.ui.components.CurrentLesson
import com.mycollege.schedule.feature.widgets.ui.components.NextLesson
import com.mycollege.schedule.feature.widgets.ui.components.ScheduleAlert
import com.mycollege.schedule.feature.widgets.ui.components.Weekend
import com.mycollege.schedule.shared.ui.theme.background
import com.mycollege.schedule.shared.ui.theme.backgroundDark
import com.mycollege.schedule.shared.ui.theme.buttons
import dagger.hilt.android.EntryPointAccessors
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class ScheduleSmallWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId
    ) {

        val storage = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WidgetEntryPoint::class.java
        )

        val studentMode = storage.getCacheManager().loadStudentMode()
        val darkTheme = storage.getCacheManager().loadAppTheme()
        val dayWeek: String = DayWeek.findById(LocalDate.now().dayOfWeek.value)?.long ?: "Понедельник"
        val tomorrowDayWeek: String = DayWeek.findById(LocalDate.now().plusDays(1).dayOfWeek.value)?.long ?: "Понедельник"

        var scheduleError = false
        val parity = if (storage.getCacheManager().loadLastSettings().weekCount) 2 else 1

        val group: Group?
        val teacher: Teacher?

        var groupSchedule: List<DataClasses.Lesson>? = null
        var tomorrowGroupSchedule: List<DataClasses.Lesson>? = null
        var teacherSchedule: List<DataClasses.Lesson>? = null
        var tomorrowTeacherSchedule: List<DataClasses.Lesson>? = null

        val configuration = storage.getCacheManager().loadLastConfiguration()

        if (configuration != null) {
            if (studentMode) {
                group = storage.getChosenGroupUseCase().getByName(configuration.group)

                if (group != null) {
                    if (dayWeek != "Воскресенье") groupSchedule = storage.getTodayScheduleUseCase().getTodaySchedule(group, dayWeek, parity)
                    if (tomorrowDayWeek != "Воскресенье") tomorrowGroupSchedule = storage.getTodayScheduleUseCase().getTodaySchedule(group, tomorrowDayWeek, parity)
                    if (tomorrowDayWeek == "Воскресенье") tomorrowGroupSchedule = storage.getTodayScheduleUseCase().getTodaySchedule(group, tomorrowDayWeek, if (parity == 1) 2 else 1)
                }
                else
                    scheduleError = true
            }
            else {
                teacher = storage.getChosenTeacherUseCase().getByName(configuration.teacher)

                if (teacher != null) {
                    if (dayWeek != "Воскресенье") teacherSchedule = storage.getTodayScheduleUseCase().getTodayTeacherSchedule(teacher.name, dayWeek, parity)
                    if (tomorrowDayWeek != "Воскресенье") tomorrowTeacherSchedule = storage.getTodayScheduleUseCase().getTodayTeacherSchedule(teacher.name, tomorrowDayWeek, parity)
                    if (tomorrowDayWeek == "Воскресенье") tomorrowTeacherSchedule = storage.getTodayScheduleUseCase().getTodayTeacherSchedule(teacher.name, tomorrowDayWeek,if (parity == 1) 2 else 1)
                }
                else
                    scheduleError = true
            }

        }
        else
            scheduleError = true

        provideContent {
            TodayWidget(darkTheme, scheduleError, studentMode, if (studentMode) groupSchedule else teacherSchedule, if (studentMode) tomorrowGroupSchedule else tomorrowTeacherSchedule)
        }

    }

    @OptIn(ExperimentalGlanceApi::class)
    @Composable
    fun TodayWidget(
        darkTheme: Boolean,
        scheduleError: Boolean,
        mode: Boolean,
        lessons: List<DataClasses.Lesson>?,
        tomorrowLessons: List<DataClasses.Lesson>?
    ) {

        if (lessons?.isNotEmpty() == true) {

            val formatter = DateTimeFormatter.ofPattern("HH:mm")
            val now = LocalTime.now()

            val sortedLessons = lessons.sortedBy { lesson ->
                val startTimeStr = lesson.time.split("-")[0]
                LocalTime.parse(startTimeStr, formatter)
            }

            var currentLesson: DataClasses.Lesson? = null
            var nextLesson: DataClasses.Lesson? = null

            for (lesson in sortedLessons) {
                val timeParts = lesson.time.split("-")
                val lessonStartTime = LocalTime.parse(timeParts[0], formatter)
                val lessonEndTime = LocalTime.parse(timeParts[1], formatter)

                if (now in lessonStartTime..lessonEndTime) {
                    currentLesson = lesson
                    break
                }
            }

            if (currentLesson != null) {
                val currentIndex = sortedLessons.indexOf(currentLesson)
                if (currentIndex + 1 < sortedLessons.size) {
                    nextLesson = sortedLessons[currentIndex + 1]
                }
            } else {
                for (lesson in sortedLessons) {
                    val lessonStartTime = LocalTime.parse(lesson.time.split("-")[0], formatter)
                    if (lessonStartTime > now) {
                        nextLesson = lesson
                        break
                    }
                }
            }

            Row(
                modifier = GlanceModifier.fillMaxSize().background(if (darkTheme) backgroundDark else background)
                    .clickable(onClick = actionStartActivity<MainActivity>(
                        activityOptions = Bundle().apply {
                            putInt("android.activity.splashScreenStyle", 1)
                        })
                    ),
                verticalAlignment = Alignment.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(GlanceModifier.width(15.dp))

                Box(GlanceModifier.fillMaxSize()) {

                    Column(
                        modifier = GlanceModifier.fillMaxHeight().width(25.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Column(GlanceModifier.fillMaxHeight().background(buttons).width(5.dp)) {
                            Text(text = " ", modifier = GlanceModifier)
                        }
                    }

                    Column(GlanceModifier.fillMaxHeight(), verticalAlignment = Alignment.CenterVertically) {

                        if (currentLesson != null) {
                            CurrentLesson(darkTheme, mode, currentLesson)

                            Spacer(GlanceModifier.height(5.dp))
                        }

                        Log.i("LESSON", nextLesson.toString())

                        NextLesson(darkTheme, mode, nextLesson, tomorrowLessons, currentLesson != null)

                    }

                }

            }

        }
        else if (scheduleError)
            ScheduleAlert(darkTheme)
        else
            Weekend(darkTheme)

    }

}