package com.mycollege.schedule.feature.schedule.domain.calendar

import androidx.compose.runtime.Immutable
import com.mycollege.schedule.feature.schedule.data.models.DataClasses
import java.time.LocalDate

/**
 * Расписание для календаря: шаблон по чётности недель (очники, преподаватели)
 * и занятия с настоящими датами (заочники). Пары любой даты:
 * дата → чётность ([WeekParity]) → день недели → пары шаблона, плюс датированные занятия этой даты.
 */
@Immutable
data class ScheduleCalendar(
    /** чётность (1, 2) → день недели (1 = пн … 7 = вс) → пары */
    val templates: Map<Int, Map<Int, List<DataClasses.Lesson>>> = emptyMap(),
    /** дата → пары (заочное обучение) */
    val dated: Map<LocalDate, List<DataClasses.Lesson>> = emptyMap(),
) {

    /** расписание построено на настоящих датах (сессии заочников), шаблона нет */
    val datedOnly: Boolean get() = dated.isNotEmpty() && templates.values.all { w -> w.values.all { it.isEmpty() } }

    val isEmpty: Boolean get() = dated.isEmpty() && templates.values.all { w -> w.values.all { it.isEmpty() } }

    fun lessonsOn(date: LocalDate, today: LocalDate, currentParity: Int): List<DataClasses.Lesson> {
        val parity = WeekParity.of(date, today, currentParity)
        val template = templates[parity]?.get(date.dayOfWeek.value).orEmpty()
        val exact = dated[date].orEmpty()
        return if (exact.isEmpty()) template else if (template.isEmpty()) exact else template + exact
    }

    /** первая и последняя даты датированных занятий (период сессии) */
    val datedRange: ClosedRange<LocalDate>? get() = if (dated.isEmpty()) null else dated.keys.min()..dated.keys.max()
}
