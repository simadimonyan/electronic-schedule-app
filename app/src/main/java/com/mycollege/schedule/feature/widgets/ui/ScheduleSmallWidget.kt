package com.mycollege.schedule.feature.widgets.ui

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.ExperimentalGlanceApi
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import com.mycollege.schedule.app.activity.data.models.Group
import com.mycollege.schedule.app.activity.data.models.Teacher
import com.mycollege.schedule.app.activity.ui.MainActivity
import com.mycollege.schedule.core.di.widget.WidgetEntryPoint
import com.mycollege.schedule.feature.schedule.data.models.DataClasses
import com.mycollege.schedule.feature.schedule.data.models.DataClasses.DayWeek
import com.mycollege.schedule.feature.widgets.ui.components.ScheduleAlert
import com.mycollege.schedule.feature.widgets.ui.components.SmallTimeline
import com.mycollege.schedule.feature.widgets.ui.components.WidgetPalette
import com.mycollege.schedule.feature.widgets.ui.components.buildWidgetBlocks
import dagger.hilt.android.EntryPointAccessors
import java.time.LocalDate

class ScheduleSmallWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Exact

    @OptIn(ExperimentalGlanceApi::class)
    @SuppressLint("RestrictedApi")
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

        val settings = storage.getCacheManager().loadLastSettings()
        val parity = if (settings != null && settings.weekCount) 2 else 1

        val group: Group?
        val teacher: Teacher?

        var groupSchedule: List<DataClasses.Lesson>? = null
        var tomorrowGroupSchedule: List<DataClasses.Lesson>? = null
        var teacherSchedule: List<DataClasses.Lesson>? = null
        var tomorrowTeacherSchedule: List<DataClasses.Lesson>? = null

        val configuration = storage.getCacheManager().loadLastConfiguration()

        if (configuration != null && settings != null) {
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
            val palette = WidgetPalette.of(darkTheme)
            Box(
                GlanceModifier
                    .fillMaxSize()
                    .appWidgetBackground()
                    .cornerRadius(28.dp)
                    .background(ImageProvider(palette.background))
                    .clickable(onClick = actionStartActivity<MainActivity>(
                        activityOptions = Bundle().apply {
                            putInt("android.activity.splashScreenStyle", 1)
                        })
                    )
            ) {
                TodayWidget(
                    palette,
                    scheduleError,
                    studentMode,
                    if (studentMode) groupSchedule else teacherSchedule,
                    if (studentMode) tomorrowGroupSchedule else tomorrowTeacherSchedule
                )
            }
        }

    }

    /**
     * Отрисовка по макету Widget.dc.html: ошибка — заглушка, иначе таймлайн из двух блоков
     * (текущая/следующая пара, перемена, конец дня, выходной)
     */
    @Composable
    fun TodayWidget(
        palette: WidgetPalette,
        scheduleError: Boolean,
        mode: Boolean,
        lessons: List<DataClasses.Lesson>?,
        tomorrowLessons: List<DataClasses.Lesson>?
    ) {
        if (lessons.isNullOrEmpty() && scheduleError) {
            ScheduleAlert(palette)
            return
        }
        SmallTimeline(palette, buildWidgetBlocks(mode, lessons, tomorrowLessons))
    }

}
