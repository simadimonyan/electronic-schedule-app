package com.mycollege.schedule.feature.schedule.domain.calendar

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

/**
 * Чётность недели для любой даты.
 *
 * У очников сервер отдаёт шаблон по чётности (неделя 1 / неделя 2), а не даты. Текущая чётность
 * хранится в настройках (`SettingsState.weekCount`: false — неделя 1, true — неделя 2): её либо
 * присылает сервер (`api/v1/configuration/week`, настройка «Определять неделю»), либо задаёт
 * пользователь («Текущая неделя») и раз в неделю переворачивает WeekChangeWorker.
 * Отсюда правило: недели чередуются, поэтому чётность даты = текущая чётность,
 * сдвинутая на число календарных недель (с понедельника) между сегодняшней неделей и неделей даты.
 */
object WeekParity {

    fun monday(date: LocalDate): LocalDate = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

    /** @param currentParity чётность недели, в которую входит [today] (1 или 2) */
    fun of(date: LocalDate, today: LocalDate, currentParity: Int): Int {
        val weeks = ChronoUnit.WEEKS.between(monday(today), monday(date))
        val shift = Math.floorMod(weeks, 2L).toInt()
        val base = if (currentParity == 2) 1 else 0
        return (base + shift) % 2 + 1
    }
}

/**
 * Дата занятия из ответа API (как в iOS-приложении): `lessonDate` (yyyy-MM-dd или ISO-8601 — берём
 * первые 10 символов) либо `pinnedDate` «дд.ММ», год для которого подбирается ближайший к сегодняшнему дню.
 */
object LessonDateParser {

    fun resolve(lessonDate: String?, pinnedDate: String?, today: LocalDate = LocalDate.now()): String? {
        normalize(lessonDate)?.let { return it }
        return fromPinned(pinnedDate, today)
    }

    private fun normalize(raw: String?): String? {
        val t = raw?.trim().orEmpty()
        if (t.length < 10) return null
        return runCatching { LocalDate.parse(t.substring(0, 10)).toString() }.getOrNull()
    }

    private fun fromPinned(raw: String?, today: LocalDate): String? {
        val parts = raw?.trim()?.split(".") ?: return null
        if (parts.size != 2) return null
        val d = parts[0].toIntOrNull() ?: return null
        val m = parts[1].toIntOrNull() ?: return null
        return (today.year - 1..today.year + 1)
            .mapNotNull { y -> runCatching { LocalDate.of(y, m, d) }.getOrNull() }
            .minByOrNull { Math.abs(ChronoUnit.DAYS.between(today, it)) }
            ?.toString()
    }
}
