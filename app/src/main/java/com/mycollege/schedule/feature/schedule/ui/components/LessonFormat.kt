package com.mycollege.schedule.feature.schedule.ui.components

import com.mycollege.schedule.feature.schedule.data.models.DataClasses
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

/**
 * Представление пары для экрана: разбор времени и подписи (только отображение, данные не меняются)
 */
data class LessonUi(
    val lesson: DataClasses.Lesson,
    val n: Int,
    val start: String,
    val end: String,
    val startMin: Int?,
    val endMin: Int?,
    val type: String,
    val subject: String,
    val who: String,
    val room: String,
    val eios: String?,
    val teacherMode: Boolean,
)

private val timeRe = Regex("(\\d{1,2})[:.](\\d{2})")

private fun clean(s: String?): String? = s?.trim()?.takeIf { it.isNotEmpty() && it != "null" }

fun DataClasses.Lesson.toUi(): LessonUi {
    val times = timeRe.findAll(time).map { it.groupValues[1].toInt() to it.groupValues[2].toInt() }.toList()
    val s = times.getOrNull(0)
    val e = times.getOrNull(1)
    fun fmt(p: Pair<Int, Int>?) = p?.let { "%02d:%02d".format(it.first, it.second) } ?: ""
    val teacherMode = this is DataClasses.TeacherLesson
    val who = when (this) {
        is DataClasses.GroupLesson -> clean(teacher)
        is DataClasses.TeacherLesson -> clean(group)
        else -> null
    }
    return LessonUi(
        lesson = this,
        n = count,
        start = fmt(s).ifEmpty { time },
        end = fmt(e),
        startMin = s?.let { it.first * 60 + it.second },
        endMin = e?.let { it.first * 60 + it.second },
        type = clean(type) ?: "Занятие",
        subject = clean(name) ?: "Неизвестная дисциплина",
        who = who ?: "",
        room = clean(location) ?: "—",
        eios = clean(eios),
        teacherMode = teacherMode,
    )
}

fun List<DataClasses.Lesson>.toUiSorted(): List<LessonUi> = sortedBy { it.count }.map { it.toUi() }

object RuDates {
    private val ru = Locale("ru")
    val weekShort = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
    val weekFull = listOf("Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота", "Воскресенье")
    private val monthGen = DateTimeFormatter.ofPattern("d MMMM", ru)
    private val monthShort = listOf("янв", "фев", "мар", "апр", "мая", "июн", "июл", "авг", "сен", "окт", "ноя", "дек")

    /** «15 октября» */
    fun dayMonth(d: LocalDate): String = d.format(monthGen)

    /** «15 окт» */
    fun dayMonthShort(d: LocalDate): String = "${d.dayOfMonth} ${monthShort[d.monthValue - 1]}"

    fun monday(today: LocalDate = LocalDate.now()): LocalDate = today.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))

    fun pairsWord(n: Int): String {
        val m10 = n % 10
        val m100 = n % 100
        return when {
            m10 == 1 && m100 != 11 -> "пара"
            m10 in 2..4 && m100 !in 12..14 -> "пары"
            else -> "пар"
        }
    }

    fun nowMinutes(): Int = LocalTime.now().let { it.hour * 60 + it.minute }

    /** «58 мин», «1 ч 20 мин» */
    fun duration(min: Int): String = when {
        min < 60 -> "$min мин"
        min % 60 == 0 -> "${min / 60} ч"
        else -> "${min / 60} ч ${min % 60} мин"
    }

    /** подпись перерыва между парами */
    fun breakLabel(gap: Int): String = when {
        gap >= 60 -> "окно " + (if (gap % 60 == 0) "${gap / 60}" else "%.1f".format(Locale("ru"), gap / 60f)) + " ч"
        gap >= 20 -> "$gap мин · большая перемена"
        else -> "$gap мин · перемена"
    }
}
