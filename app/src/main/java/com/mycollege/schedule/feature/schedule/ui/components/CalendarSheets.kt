package com.mycollege.schedule.feature.schedule.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mycollege.schedule.feature.schedule.data.models.DataClasses
import com.mycollege.schedule.shared.ui.design.MaIcon
import com.mycollege.schedule.shared.ui.design.MaIcons
import com.mycollege.schedule.shared.ui.design.OutlineButton
import com.mycollege.schedule.shared.ui.design.PrimaryButton
import com.mycollege.schedule.shared.ui.design.maClick
import com.mycollege.schedule.shared.ui.theme.LessonKind
import com.mycollege.schedule.shared.ui.theme.Ma
import com.mycollege.schedule.shared.ui.theme.MaBrand
import com.mycollege.schedule.shared.ui.theme.MaType
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.abs

/*
 * Календарь расписания (Session.dc.html, вариант calendar, ветка android) и выбор даты
 * (Main.dc.html, overlay datepicker) в токенах редизайна.
 */

private val monthNom = listOf("Январь", "Февраль", "Март", "Апрель", "Май", "Июнь", "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь")
private val monthShortNom = listOf("Янв", "Фев", "Мар", "Апр", "Май", "Июн", "Июл", "Авг", "Сен", "Окт", "Ноя", "Дек")

fun monthTitle(m: YearMonth): String = "${monthNom[m.monthValue - 1]} ${m.year}"

/** «15 октября» или «15 октября 2027», если год не текущий */
fun dayMonthMaybeYear(d: LocalDate, today: LocalDate = LocalDate.now()): String =
    RuDates.dayMonth(d) + if (d.year != today.year) " ${d.year}" else ""

/** цвет точки под днём по типу пары */
@Composable
private fun kindColor(type: String): Color = when (LessonKind.of(type)) {
    LessonKind.LECTURE -> Ma.colors.accent
    LessonKind.PRACTICE -> MaBrand.green
    LessonKind.LAB -> MaBrand.amber
    LessonKind.EXAM -> MaBrand.red
    LessonKind.OTHER -> Ma.colors.faint
}

/** подпись дня для TalkBack: «Четверг, 15 октября, сегодня, 3 пары: лекция, практика» */
fun dayDescription(date: LocalDate, today: LocalDate, lessons: List<DataClasses.Lesson>?, selected: Boolean): String = buildString {
    append(RuDates.weekFull[date.dayOfWeek.value - 1]).append(", ").append(dayMonthMaybeYear(date, today))
    if (date == today) append(", сегодня")
    if (selected) append(", выбрано")
    if (lessons != null) {
        if (lessons.isEmpty()) append(", пар нет")
        else {
            append(", ${lessons.size} ${RuDates.pairsWord(lessons.size)}: ")
            append(lessons.map { it.type.trim().lowercase() }.filter { it.isNotEmpty() }.distinct().joinToString(", "))
        }
    }
}

/** Круглая кнопка листания: видимый круг 36dp, зона нажатия 44dp */
@Composable
private fun NavCircle(path: String, description: String, onClick: () -> Unit) {
    val c = Ma.colors
    Box(
        Modifier
            .size(44.dp)
            .clip(CircleShape)
            .maClick(onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        Box(Modifier.size(36.dp).clip(CircleShape).background(c.sunk), contentAlignment = Alignment.Center) {
            MaIcon(path, c.fg, 16.dp, 2.2f)
        }
    }
}

private const val chevLeft = "M15 6l-6 6 6 6"

/** Шапка месяца: название и стрелки */
@Composable
private fun MonthHeader(
    title: String,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    prevLabel: String = "Предыдущий месяц",
    nextLabel: String = "Следующий месяц",
    onTitleClick: (() -> Unit)? = null,
    titleExpanded: Boolean = false,
) {
    val c = Ma.colors
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            Modifier
                .weight(1f)
                .height(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .then(if (onTitleClick != null) Modifier.maClick(onClick = onTitleClick).semantics { contentDescription = "$title, выбрать месяц и год" } else Modifier.semantics { heading() })
                .padding(horizontal = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(title, style = MaType.display(20.sp, FontWeight.ExtraBold), color = c.fg, maxLines = 1)
            if (onTitleClick != null) MaIcon(if (titleExpanded) "M6 15l6-6 6 6" else MaIcons.chevDown, c.soft, 16.dp, 2.2f)
        }
        NavCircle(chevLeft, prevLabel, onPrev)
        NavCircle(MaIcons.chevRight, nextLabel, onNext)
    }
}

/**
 * Ячейка дня: круг 40dp в зоне 44dp, число и точки под ним.
 * Выбранный день — синий круг, сегодня — кольцо, дни с парами (в режиме сессии) — мягкая заливка, выходные приглушены.
 */
@Composable
private fun DayCell(
    date: LocalDate,
    today: LocalDate,
    selected: Boolean,
    dots: List<Color>,
    highlight: Boolean,
    bold: Boolean,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = Ma.colors
    val isToday = date == today
    val weekend = date.dayOfWeek.value >= 6
    val fg = when {
        selected -> Color.White
        isToday -> c.accent
        weekend -> c.faint
        bold -> c.fg
        else -> c.soft
    }
    Box(
        modifier
            .height(48.dp)
            .clickable(description, selected, onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(
                    when {
                        selected -> MaBrand.primary
                        highlight -> c.accentSoft
                        else -> Color.Transparent
                    }
                )
                .then(if (isToday && !selected) Modifier.border(1.5.dp, c.accent, CircleShape) else Modifier),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("${date.dayOfMonth}", style = MaType.body(14.5.sp, if (bold || isToday || selected) FontWeight.Bold else FontWeight.Medium), color = fg)
            Row(Modifier.padding(top = 2.dp).height(4.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                dots.take(4).forEach { dot ->
                    Box(Modifier.size(4.dp).clip(CircleShape).background(if (selected) Color.White.copy(alpha = 0.9f) else dot))
                }
            }
        }
    }
}

/** нажатие + одна понятная подпись для TalkBack вместо числа и точек */
@Composable
private fun Modifier.clickable(description: String, selected: Boolean, onClick: () -> Unit): Modifier =
    this
        .clip(RoundedCornerShape(12.dp))
        .maClick(onClick = onClick)
        .clearAndSetSemantics {
            contentDescription = description
            this.selected = selected
            role = Role.Button
            onClick { onClick(); true }
        }

/** Заголовок дней недели Пн…Вс */
@Composable
private fun WeekHead() {
    val c = Ma.colors
    Row(Modifier.fillMaxWidth().padding(top = 8.dp).clearAndSetSemantics { }) {
        RuDates.weekShort.forEachIndexed { i, w ->
            Text(
                w, style = MaType.eyebrow(11.sp, 0.05f), color = if (i >= 5) c.faint.copy(alpha = 0.7f) else c.faint,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.weight(1f).padding(bottom = 4.dp)
            )
        }
    }
}

/** Сетка месяца с понедельника; дни соседних месяцев не показываются */
@Composable
private fun MonthGrid(
    month: YearMonth,
    cell: @Composable (LocalDate, Modifier) -> Unit,
) {
    val first = month.atDay(1)
    val lead = first.dayOfWeek.value - 1
    val total = lead + month.lengthOfMonth()
    val rows = (total + 6) / 7
    Column(Modifier.fillMaxWidth()) {
        for (r in 0 until rows) {
            Row(Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val idx = r * 7 + col - lead
                    if (idx < 0 || idx >= month.lengthOfMonth()) Box(Modifier.weight(1f).height(48.dp))
                    else cell(first.plusDays(idx.toLong()), Modifier.weight(1f))
                }
            }
        }
    }
}

/** Листание месяцев свайпом и стрелками с горизонтальной анимацией */
@Composable
private fun SwipeMonths(
    month: YearMonth,
    onMonth: (YearMonth) -> Unit,
    content: @Composable (YearMonth) -> Unit,
) {
    var drag by remember { mutableStateOf(0f) }
    Box(
        Modifier.pointerInput(month) {
            detectHorizontalDragGestures(
                onDragStart = { drag = 0f },
                onDragEnd = {
                    if (abs(drag) > 64.dp.toPx()) onMonth(if (drag < 0) month.plusMonths(1) else month.minusMonths(1))
                    drag = 0f
                },
                onHorizontalDrag = { _, d -> drag += d }
            )
        }
    ) {
        AnimatedContent(
            targetState = month,
            transitionSpec = {
                val dir = if (targetState > initialState) 1 else -1
                (slideInHorizontally(tween(260)) { it / 3 * dir } + fadeIn(tween(200))) togetherWith
                    (slideOutHorizontally(tween(200)) { -it / 3 * dir } + fadeOut(tween(150)))
            },
            label = "month"
        ) { m -> content(m) }
    }
}

@Composable
private fun LegendDot(color: Color, label: String, ring: Boolean = false) {
    val c = Ma.colors
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            Modifier.size(8.dp).clip(CircleShape)
                .then(if (ring) Modifier.border(1.5.dp, color, CircleShape) else Modifier.background(color))
        )
        Text(label, style = MaType.body(12.sp), color = c.soft)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MaSheet(onDismiss: () -> Unit, content: @Composable (hide: (() -> Unit) -> Unit) -> Unit) {
    val c = Ma.colors
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val hide: (() -> Unit) -> Unit = { then ->
        scope.launch { state.hide() }.invokeOnCompletion { then(); onDismiss() }
    }
    ModalBottomSheet(
        modifier = Modifier.statusBarsPadding(),
        sheetState = state,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = c.sheet,
        contentColor = c.fg,
        scrimColor = Color(0x570F172A),
        dragHandle = {
            Box(Modifier.padding(top = 8.dp, bottom = 10.dp).size(36.dp, 5.dp).clip(RoundedCornerShape(3.dp)).background(c.borderStrong))
        },
        onDismissRequest = onDismiss
    ) {
        content(hide)
    }
}

/**
 * Календарь месяца: точки по числу и типу пар, сегодня, выбранный день, легенда.
 * «Показать …» открывает расписание выбранного дня тем же списком, что на «Сегодня».
 *
 * @param lessonsOn пары на дату (шаблон по чётности или даты заочников)
 * @param sessionMode расписание на настоящих датах (заочка): дни с занятиями подсвечены, как в макете
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CalendarSheet(
    initial: LocalDate,
    today: LocalDate,
    lessonsOn: (LocalDate) -> List<DataClasses.Lesson>,
    sessionMode: Boolean,
    onShow: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    val c = Ma.colors
    var selectedDay by rememberSaveable { mutableStateOf(initial.toEpochDay()) }
    val selected = LocalDate.ofEpochDay(selectedDay)
    var month by remember { mutableStateOf(YearMonth.from(initial)) }

    MaSheet(onDismiss) { hide ->
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 18.dp, end = 18.dp, bottom = 24.dp)
                .semantics { contentDescription = "Календарь расписания" }
        ) {
            MonthHeader(monthTitle(month), onPrev = { month = month.minusMonths(1) }, onNext = { month = month.plusMonths(1) })
            WeekHead()
            SwipeMonths(month, { month = it }) { m ->
                MonthGrid(m) { date, mod ->
                    val lessons = lessonsOn(date)
                    val colors = lessons.map { kindColor(it.type) }
                    DayCell(
                        date = date,
                        today = today,
                        selected = date == selected,
                        dots = colors,
                        highlight = sessionMode && lessons.isNotEmpty(),
                        bold = lessons.isNotEmpty(),
                        description = dayDescription(date, today, lessons, date == selected),
                        onClick = { selectedDay = date.toEpochDay() },
                        modifier = mod
                    )
                }
            }
            FlowRow(
                Modifier.fillMaxWidth().padding(start = 2.dp, end = 2.dp, top = 10.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (sessionMode) LegendDot(c.accent, "День сессии")
                LegendDot(c.accent, "Лекция")
                LegendDot(MaBrand.green, "Практика")
                LegendDot(MaBrand.amber, "Лабораторная")
                LegendDot(MaBrand.red, "Экзамен, зачёт")
                LegendDot(c.accent, "Сегодня", ring = true)
            }
            val pairs = lessonsOn(selected).size
            PrimaryButton(
                text = "Показать ${dayMonthMaybeYear(selected, today)}" + if (pairs == 0) " · пар нет" else "",
                onClick = { hide { onShow(selected) } },
                modifier = Modifier.fillMaxWidth(),
                height = 50.dp,
                fontSize = 15.sp
            )
        }
    }
}

/**
 * Быстрый переход к любой дате: месяц листается стрелками или свайпом, по названию месяца открывается
 * выбор месяца и года. Нажатие на день сразу открывает его расписание.
 */
@Composable
fun DatePickerSheet(
    initial: LocalDate,
    today: LocalDate,
    hasLessons: (LocalDate) -> Boolean,
    onPick: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    val c = Ma.colors
    var month by remember { mutableStateOf(YearMonth.from(initial)) }
    var monthsMode by rememberSaveable { mutableStateOf(false) }

    MaSheet(onDismiss) { hide ->
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 18.dp, end = 18.dp, bottom = 24.dp)
        ) {
            Row(Modifier.fillMaxWidth().padding(bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Перейти к дате", style = MaType.display(17.sp, FontWeight.Bold), color = c.fg, modifier = Modifier.weight(1f).semantics { heading() })
                Box(
                    Modifier.size(44.dp).clip(CircleShape).maClick { hide {} }.semantics { contentDescription = "Закрыть" },
                    contentAlignment = Alignment.Center
                ) {
                    Box(Modifier.size(30.dp).clip(CircleShape).background(c.sunk), contentAlignment = Alignment.Center) {
                        MaIcon(MaIcons.close, c.soft, 14.dp, 2.2f)
                    }
                }
            }
            if (monthsMode) {
                MonthHeader(
                    "${month.year}",
                    onPrev = { month = month.minusYears(1) },
                    onNext = { month = month.plusYears(1) },
                    prevLabel = "Предыдущий год",
                    nextLabel = "Следующий год",
                    onTitleClick = { monthsMode = false },
                    titleExpanded = true
                )
                Column(Modifier.fillMaxWidth().padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    (0 until 12).chunked(3).forEach { row ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { i ->
                                val m = YearMonth.of(month.year, i + 1)
                                val sel = m == month
                                val current = m == YearMonth.from(today)
                                Box(
                                    Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(if (sel) MaBrand.primary else c.surface)
                                        .border(1.dp, if (sel) Color.Transparent else if (current) c.accent else c.border, RoundedCornerShape(14.dp))
                                        .clickable("${monthNom[i]} ${month.year}", sel) { month = m; monthsMode = false },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(monthShortNom[i], style = MaType.body(14.sp, FontWeight.SemiBold), color = if (sel) Color.White else if (current) c.accent else c.fg)
                                }
                            }
                        }
                    }
                }
            } else {
                MonthHeader(
                    monthTitle(month),
                    onPrev = { month = month.minusMonths(1) },
                    onNext = { month = month.plusMonths(1) },
                    onTitleClick = { monthsMode = true }
                )
                WeekHead()
                SwipeMonths(month, { month = it }) { m ->
                    MonthGrid(m) { date, mod ->
                        val has = hasLessons(date)
                        DayCell(
                            date = date,
                            today = today,
                            selected = date == initial,
                            dots = if (has) listOf(c.borderStrong) else emptyList(),
                            highlight = false,
                            bold = has,
                            description = dayDescription(date, today, null, date == initial) + if (has) ", есть пары" else "",
                            onClick = { hide { onPick(date) } },
                            modifier = mod
                        )
                    }
                }
            }
            Row(Modifier.fillMaxWidth().padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlineButton("Сегодня", { hide { onPick(today) } }, Modifier.weight(1f), height = 48.dp, background = c.surface)
                OutlineButton("Завтра", { hide { onPick(today.plusDays(1)) } }, Modifier.weight(1f), height = 48.dp, background = c.surface)
            }
        }
    }
}
