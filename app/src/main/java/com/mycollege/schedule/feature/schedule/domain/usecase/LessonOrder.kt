package com.mycollege.schedule.feature.schedule.domain.usecase

import com.mycollege.schedule.feature.schedule.data.models.DataClasses

/**
 * Порядок пар дня. У шаблонных пар номер приходит с сервера; у датированных (заочка) его может не быть
 * (null → 0) — тогда, как в iOS, сортируем по времени начала и нумеруем по порядку.
 */
object LessonOrder {

    private val timeRe = Regex("(\\d{1,2})[:.](\\d{2})")

    private fun startMinutes(l: DataClasses.Lesson): Int =
        timeRe.find(l.time)?.let { it.groupValues[1].toInt() * 60 + it.groupValues[2].toInt() } ?: Int.MAX_VALUE

    fun normalize(lessons: List<DataClasses.Lesson>): ArrayList<DataClasses.Lesson> {
        if (lessons.none { it.count <= 0 }) return lessons as? ArrayList<DataClasses.Lesson> ?: ArrayList(lessons)
        val sorted = lessons.sortedWith(compareBy({ startMinutes(it) }, { it.count }))
        return sorted.mapIndexedTo(ArrayList()) { i, l ->
            if (l.count > 0) l else when (l) {
                is DataClasses.GroupLesson -> l.copy(count = i + 1)
                is DataClasses.TeacherLesson -> l.copy(count = i + 1)
                else -> l
            }
        }
    }
}
