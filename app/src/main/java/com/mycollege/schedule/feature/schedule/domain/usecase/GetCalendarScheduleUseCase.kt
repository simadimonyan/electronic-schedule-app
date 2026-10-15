package com.mycollege.schedule.feature.schedule.domain.usecase

import androidx.compose.runtime.Immutable
import com.mycollege.schedule.app.activity.data.models.Group
import com.mycollege.schedule.app.activity.data.models.Schedule
import com.mycollege.schedule.core.db.Database
import com.mycollege.schedule.feature.schedule.data.models.DataClasses
import com.mycollege.schedule.feature.schedule.domain.calendar.ScheduleCalendar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Всё сохранённое расписание группы или преподавателя для календаря — из базы, без сети:
 * шаблон обеих недель и датированные занятия.
 */
@Singleton
@Immutable
class GetCalendarScheduleUseCase @Inject constructor(
    private val database: Database,
    private val getTeacherUseCase: GetTeacherUseCase,
    private val getGroupUseCase: GetGroupUseCase,
) {

    suspend fun forGroup(group: Group): ScheduleCalendar = withContext(Dispatchers.IO) {
        val rows = database.schedule().getAllSchedule(group.id.toString())
        val teachers = HashMap<Long, String?>()
        build(rows) { day ->
            day.sortedBy { it.lessonCount }.map { r ->
                val teacher = r.teacher?.let { id -> teachers.getOrPut(id) { getTeacherUseCase.getTeacherBy(id)?.name } }
                DataClasses.GroupLesson(r.lessonCount, r.time, r.type, r.name, teacher.toString(), r.eios.ifBlank { null }, r.location)
            }
        }
    }

    suspend fun forTeacher(name: String): ScheduleCalendar = withContext(Dispatchers.IO) {
        val teacher = database.teachers().getTeachersBy(name).firstOrNull() ?: return@withContext ScheduleCalendar()
        val rows = database.schedule().getAllTeacherSchedule(teacher.id.toString())
        val groups = HashMap<Long, String>()
        build(rows) { day ->
            // одна пара у нескольких групп — одной строкой, как на экранах «Сегодня» и «Неделя»
            val result = ArrayList<DataClasses.TeacherLesson>()
            for (r in day.sortedWith(compareBy({ it.lessonCount }, { it.time }))) {
                val group = groups.getOrPut(r.group) { getGroupUseCase.getGroupById(r.group.toString()).name }
                val last = result.lastOrNull()
                if (last != null && last.name == r.name && last.count == r.lessonCount && last.time == r.time)
                    result[result.size - 1] = last.copy(group = "${last.group}, $group")
                else
                    result.add(DataClasses.TeacherLesson(r.lessonCount, r.time, r.type, r.name, group, r.eios.ifBlank { null }, r.location))
            }
            result
        }
    }

    private inline fun build(rows: List<Schedule>, map: (List<Schedule>) -> List<DataClasses.Lesson>): ScheduleCalendar {
        val template = rows.filter { it.pinnedDate.isEmpty() }
        val templates = (1..2).associateWith { parity ->
            (1..7).associateWith { day ->
                val name = DataClasses.DayWeek.findById(day)!!.long
                LessonOrder.normalize(map(template.filter { it.weekCount == parity && it.dayWeek == name }))
            }
        }
        val dated = rows.filter { it.pinnedDate.isNotEmpty() }
            .groupBy { runCatching { LocalDate.parse(it.pinnedDate) }.getOrNull() }
            .filterKeys { it != null }
            .map { (date, day) -> date!! to LessonOrder.normalize(map(day)) as List<DataClasses.Lesson> }
            .toMap()
        return ScheduleCalendar(templates, dated)
    }
}
