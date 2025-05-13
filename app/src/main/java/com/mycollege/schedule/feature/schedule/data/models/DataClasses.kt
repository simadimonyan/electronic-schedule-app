package com.mycollege.schedule.feature.schedule.data.models

import androidx.compose.runtime.Immutable

@Immutable
class DataClasses {

    @Immutable
    data class Lesson(
        val count: Int,
        val time: String,
        val type: String,
        val name: String?,
        val teacher: String?,
        val location: String?
    )

    @Immutable
    enum class DayWeek(val id: Int, val short: String, val long: String) {
        MONDAY(1, "Пнд", "Понедельник"),
        TUESDAY(2, "Втр",  "Вторник"),
        WEDNESDAY(3, "Срд",  "Среда"),
        THURSDAY(4, "Чтв",  "Четверг"),
        FRIDAY(5, "Птн",  "Пятница"),
        SATURDAY(6, "Сбт",  "Суббота"),
        SUNDAY(7, "Вск",  "Воскресенье");

        companion object {
            fun findByShort(shortName: String): DayWeek? {
                return entries.find { it.short.equals(shortName, ignoreCase = true) }
            }
            fun findById(id: Int): DayWeek? {
                return entries.find { it.id == id }
            }
        }

    }

}