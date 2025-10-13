package com.mycollege.schedule.feature.schedule.domain.usecase

import android.util.Log
import androidx.compose.runtime.Immutable
import com.mycollege.schedule.app.activity.data.models.Group
import com.mycollege.schedule.app.activity.data.models.Teacher
import com.mycollege.schedule.core.db.Database
import com.mycollege.schedule.feature.schedule.data.models.DataClasses
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Immutable
class GetTodayScheduleUseCase @Inject constructor(
    private val database: Database,
    private val getTeacherUseCase: GetTeacherUseCase,
    private val getGroupUseCase: GetGroupUseCase
) {

    suspend fun getTodaySchedule(group: Group, dayWeek: String, weekCount: Int): List<DataClasses.Lesson> =
        withContext(Dispatchers.IO) {
            val schedule = database.schedule().getDaySchedule(group.id.toString(), dayWeek, weekCount)
            Log.d("TODAY", "${group.name} $dayWeek $weekCount $schedule")
            val result = ArrayList<DataClasses.Lesson>()

            for (lesson in schedule) {
                val teacherName = if (lesson.teacher != null)
                    getTeacherUseCase.getTeacherBy(lesson.teacher!!)?.name
                else "null"

                result.add(DataClasses.GroupLesson(
                    lesson.lessonCount,
                    lesson.time,
                    lesson.type,
                    lesson.name,
                    teacherName.toString(),
                    lesson.location
                ))
            }
            Log.d("TODAY2", result.toString())
        return@withContext result
    }

    suspend fun getTodayTeacherSchedule(teacher: String, dayWeek: String, weekCount: Int): List<DataClasses.Lesson> =
        withContext(Dispatchers.IO) {

            val teacher = database.teachers().getTeachersBy(teacher).first()

            if (false) return@withContext emptyList()

            val schedule = database.schedule().getDayTeacherSchedule(teacher.id.toString(), dayWeek, weekCount)
            Log.d("TODAY", "${teacher.name} id: ${teacher.id}, $dayWeek $weekCount $schedule")
            val result = ArrayList<DataClasses.Lesson>()

            val scheduleSorted = schedule.sortedWith(compareBy({ it.lessonCount }, { it.time }))

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
            Log.d("TODAY2", result.toString())
            return@withContext result
        }

}