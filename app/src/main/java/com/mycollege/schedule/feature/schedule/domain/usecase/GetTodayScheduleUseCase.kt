package com.mycollege.schedule.feature.schedule.domain.usecase

import androidx.compose.runtime.Immutable
import com.mycollege.schedule.app.activity.data.models.Group
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
) {

    suspend fun getTodaySchedule(group: Group, dayWeek: String, weekCount: Int): List<DataClasses.Lesson> =
        withContext(Dispatchers.IO) {
            val schedule = database.schedule().getDaySchedule(group.id.toString(), dayWeek, weekCount)
            val result = ArrayList<DataClasses.Lesson>()

            for (lesson in schedule) {
                val teacherName = getTeacherUseCase.getTeacherBy(lesson.teacher)?.name

                result.add(DataClasses.Lesson(
                    lesson.lessonCount,
                    lesson.time,
                    lesson.type,
                    lesson.name,
                    teacherName.toString(),
                    lesson.location
                ))
            }
        return@withContext result
    }

}