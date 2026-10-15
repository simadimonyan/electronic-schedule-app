package com.mycollege.schedule.feature.widgets.ui.components

import com.mycollege.schedule.feature.schedule.data.models.DataClasses
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * Блок таймлайна виджета (макет Widget.dc.html: «сейчас → следующая»)
 *
 * label — надпись над названием (малый виджет), labelShort — справа от названия (средний)
 */
data class WidgetBlock(
    val label: String,
    val labelShort: String,
    val subject: String,
    val who: String = "",
    val time: String = "",
    val room: String = "",
    val node: WidgetNode,
    val labelAccent: Boolean = false,
    /** 0..1 — прогресс пары или перемены; null — без полосы */
    val progress: Float? = null,
    val progressLabel: String = "",
    /** строка вместо «аудитория · время» (нет пар, выходной) */
    val meta: String? = null,
)

private val hhmm = DateTimeFormatter.ofPattern("HH:mm")

private fun DataClasses.Lesson.start(): LocalTime = LocalTime.parse(time.split("-")[0].trim(), hhmm)
private fun DataClasses.Lesson.end(): LocalTime = LocalTime.parse(time.split("-")[1].trim(), hhmm)

private fun clean(s: String?) = if (s == null || s == "null") "" else s

private fun DataClasses.Lesson.who(studentMode: Boolean): String =
    clean(if (studentMode) (this as? DataClasses.GroupLesson)?.teacher else (this as? DataClasses.TeacherLesson)?.group)

private fun DataClasses.Lesson.subject() = clean(name).ifBlank { "Неизвестная дисциплина" }

private fun DataClasses.Lesson.timeLabel() = time.replace("-", "–")

private fun pairsWord(n: Int) = when {
    n % 10 == 1 && n % 100 != 11 -> "пара"
    n % 10 in 2..4 && n % 100 !in 12..14 -> "пары"
    else -> "пар"
}

private fun untilLabel(minutes: Long): String {
    val h = minutes / 60
    val m = minutes % 60
    return when {
        h == 0L -> "Через $m мин"
        m == 0L -> "Через $h ч"
        else -> "Через $h ч $m мин"
    }
}

private fun lessonBlock(lesson: DataClasses.Lesson, studentMode: Boolean, label: String, labelShort: String = label, node: WidgetNode, accent: Boolean = false, progress: Float? = null, progressLabel: String = "") =
    WidgetBlock(
        label = label,
        labelShort = labelShort,
        subject = lesson.subject(),
        who = lesson.who(studentMode),
        time = lesson.timeLabel(),
        room = clean(lesson.location),
        node = node,
        labelAccent = accent,
        progress = progress,
        progressLabel = progressLabel,
    )

/**
 * Собирает два блока таймлайна из уже загруженных виджетом пар (текущая/следующая/завтра).
 * Источник данных не меняется — только раскладка по состояниям макета (now, break, morning, done, weekend).
 */
fun buildWidgetBlocks(
    studentMode: Boolean,
    lessons: List<DataClasses.Lesson>?,
    tomorrowLessons: List<DataClasses.Lesson>?,
    now: LocalTime = LocalTime.now(),
): List<WidgetBlock> {
    val tomorrowFirst = tomorrowLessons?.sortedBy { it.start() }?.firstOrNull()
    val tomorrowBlock = tomorrowFirst?.let { lessonBlock(it, studentMode, "Завтра", node = WidgetNode.NEXT) }

    if (lessons.isNullOrEmpty()) {
        val today = LocalDate.now().dayOfWeek.getDisplayName(TextStyle.FULL_STANDALONE, Locale("ru")).replaceFirstChar { it.uppercase() }
        return listOfNotNull(
            WidgetBlock(label = today, labelShort = "Сегодня", subject = "Выходной", node = WidgetNode.PAST, meta = "Пар нет — отдыхайте"),
            tomorrowBlock
        )
    }

    val sorted = lessons.sortedBy { it.start() }
    val current = sorted.firstOrNull { now >= it.start() && now <= it.end() }

    if (current != null) {
        val total = Duration.between(current.start(), current.end()).toMinutes().coerceAtLeast(1)
        val passed = Duration.between(current.start(), now).toMinutes().coerceIn(0, total)
        val pct = passed.toFloat() / total
        val next = sorted.getOrNull(sorted.indexOf(current) + 1)
        return listOf(
            lessonBlock(current, studentMode, "Сейчас идёт", node = WidgetNode.LIVE, accent = true, progress = pct, progressLabel = "${(pct * 100).toInt()}%"),
            next?.let { lessonBlock(it, studentMode, "Следующая пара", node = WidgetNode.NEXT) }
                ?: WidgetBlock(label = "Затем", labelShort = "Затем", subject = "Пар больше нет", node = WidgetNode.PAST, meta = "Можно идти домой")
        )
    }

    val nextIndex = sorted.indexOfFirst { it.start() > now }
    if (nextIndex >= 0) {
        val next = sorted[nextIndex]
        val minutes = Duration.between(now, next.start()).toMinutes() + 1
        val previous = sorted.getOrNull(nextIndex - 1)
        // перемена между парами: живой узел и полоса до начала следующей
        val first = if (previous != null) {
            val gap = Duration.between(previous.end(), next.start()).toMinutes().coerceAtLeast(1)
            val gone = Duration.between(previous.end(), now).toMinutes().coerceIn(0, gap)
            lessonBlock(next, studentMode, untilLabel(minutes), node = WidgetNode.LIVE, accent = true, progress = gone.toFloat() / gap, progressLabel = "перемена")
        } else {
            lessonBlock(next, studentMode, untilLabel(minutes), node = WidgetNode.NEXT)
        }
        val then = sorted.getOrNull(nextIndex + 1)?.let { lessonBlock(it, studentMode, "Затем", node = WidgetNode.NEXT) }
        return listOfNotNull(first, then)
    }

    // пары на сегодня закончились
    return listOf(
        WidgetBlock(label = "Сегодня", labelShort = "Сегодня", subject = "Пары закончились", node = WidgetNode.PAST, meta = if (sorted.size == 1) "Пара позади" else "Все ${sorted.size} ${pairsWord(sorted.size)} позади"),
        tomorrowBlock ?: WidgetBlock(label = "Завтра", labelShort = "Завтра", subject = "Пар нет", node = WidgetNode.PAST, meta = "Можно выспаться")
    )
}
