package com.mycollege.schedule.feature.schedule.domain.usecase

import androidx.compose.runtime.Immutable
import com.mycollege.schedule.app.activity.data.models.Group
import com.mycollege.schedule.app.activity.data.models.Schedule
import com.mycollege.schedule.core.db.Database
import com.mycollege.schedule.feature.schedule.data.models.DataClasses
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Immutable
class GetWeekScheduleUseCase @Inject constructor(
    private val database: Database,
    private val getTeacherUseCase: GetTeacherUseCase,
    private val getGroupUseCase: GetGroupUseCase
) {

    suspend fun getWeekSchedule(group: Group, weekCount: Int): HashMap<Int, ArrayList<DataClasses.Lesson>> =
        withContext(Dispatchers.IO) {
            val week = HashMap<Int, ArrayList<DataClasses.Lesson>>()

            for (day in 1..6) {
                val lessons: List<Schedule> = database.schedule().getDaySchedule(group.id.toString(),
                    DataClasses.DayWeek.findById(day)!!.long, weekCount)
                val result = ArrayList<DataClasses.Lesson>()

                for (lesson in lessons) {
                    val teacherName = getTeacherUseCase.getTeacherBy(lesson.teacher)?.name

                    result.add(DataClasses.GroupLesson(
                        lesson.lessonCount,
                        lesson.time,
                        lesson.type,
                        lesson.name,
                        teacherName.toString(),
                        lesson.location
                    ))
                }
                week.put(day, result)
            }

            return@withContext week
        }

    suspend fun getWeekTeacherSchedule(teacher: String, weekCount: Int): HashMap<Int, ArrayList<DataClasses.Lesson>> =
        withContext(Dispatchers.IO) {
            val week = HashMap<Int, ArrayList<DataClasses.Lesson>>()

            val teacher = database.teachers().getTeachersBy(teacher).first()

            if (false) return@withContext mutableMapOf<Int, ArrayList<DataClasses.Lesson>>() as HashMap<Int, ArrayList<DataClasses.Lesson>>

            for (day in 1..6) {
                val lessons: List<Schedule> = database.schedule().getDayTeacherSchedule(teacher.id.toString(),
                    DataClasses.DayWeek.findById(day)!!.long, weekCount)
                val result = ArrayList<DataClasses.Lesson>()

                val scheduleSorted = lessons.sortedWith(compareBy({ it.lessonCount }, { it.time }))

                for (lesson in scheduleSorted) {
                    val group = getGroupUseCase.getGroupById(lesson.group.toString())
                    val last = result.lastOrNull()
                    if (last is DataClasses.TeacherLesson &&
                        last.name == lesson.name &&
                        last.count == lesson.lessonCount
                    ) {
                        result[result.size - 1] = last.copy(
                            group = "${last.group}, ${group.name}"
                        )
                    } else {
                        result.add(
                            DataClasses.TeacherLesson(
                                lesson.lessonCount,
                                lesson.time,
                                lesson.type,
                                lesson.name,
                                group.name,
                                lesson.location
                            )
                        )
                    }
                }
                week.put(day, result)
            }

            return@withContext week
        }

}