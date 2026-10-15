package com.mycollege.schedule.feature.schedule.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mycollege.schedule.R
import com.mycollege.schedule.feature.schedule.data.models.DataClasses
import com.mycollege.schedule.shared.ui.design.EyebrowPill
import com.mycollege.schedule.shared.ui.design.MaIcon
import com.mycollege.schedule.shared.ui.design.MaIcons
import com.mycollege.schedule.shared.ui.design.OutlineButton
import com.mycollege.schedule.shared.ui.design.maCard
import com.mycollege.schedule.shared.ui.design.maClick
import com.mycollege.schedule.shared.ui.theme.Ma
import com.mycollege.schedule.shared.ui.theme.MaFonts
import com.mycollege.schedule.shared.ui.theme.MaType
import kotlinx.coroutines.delay
import java.time.LocalDate

/**
 * Экран «Сегодня» (Today.dc.html / Weekend.dc.html): заголовок дня, полоса недели,
 * текущая пара, список пар, «завтра», выходной
 */
@Composable
fun TodayContent(
    todayLessons: List<DataClasses.Lesson>,
    weekLessons: Map<Int, List<DataClasses.Lesson>>,
    parity: Int,
    teacherMode: Boolean,
    loading: Boolean,
    offline: Boolean,
    selectedDay: Int,
    onSelectDay: (Int) -> Unit,
    onOpenWeek: () -> Unit,
    onRetry: () -> Unit,
    onCopyEios: (String) -> Unit,
) {
    val c = Ma.colors
    val today = LocalDate.now()
    val todayIndex = today.dayOfWeek.value - 1
    val monday = RuDates.monday(today)
    val dates = (0..6).map { monday.plusDays(it.toLong()) }

    // минутные «часы» для текущей пары
    var nowMin by remember { mutableIntStateOf(RuDates.nowMinutes()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000)
            nowMin = RuDates.nowMinutes()
        }
    }

    fun lessonsOf(i: Int): List<LessonUi> =
        if (i == todayIndex) todayLessons.toUiSorted() else weekLessons[i + 1].orEmpty().toUiSorted()

    val isToday = selectedDay == todayIndex
    val lessons = lessonsOf(selectedDay)
    val date = dates[selectedDay]
    val sunday = selectedDay == 6

    // заголовок
    EyebrowPill("${RuDates.weekFull[selectedDay]} · Неделя $parity")
    val title = when {
        isToday -> "Сегодня"
        sunday && lessons.isEmpty() -> "Выходной"
        else -> RuDates.weekFull[selectedDay]
    }
    Text(title, style = MaType.displayFor(title, 36.sp, FontWeight.ExtraBold, 38.sp), color = c.fg, modifier = Modifier.padding(top = 10.dp))
    val sub = buildString {
        append(RuDates.dayMonth(date))
        if (lessons.isNotEmpty()) {
            append(" · ${lessons.size} ${RuDates.pairsWord(lessons.size)}")
            val first = lessons.first().start
            val last = lessons.last().end
            if (first.isNotEmpty() && last.isNotEmpty()) append(", $first–$last")
        } else if (!loading) append(if (sunday) " · можно выдохнуть" else " · пар нет")
    }
    Text(sub, style = MaType.body(14.sp), color = c.soft, modifier = Modifier.padding(top = 4.dp))

    if (teacherMode && lessons.isNotEmpty()) {
        val groups = lessons.flatMap { it.who.split(",").map(String::trim) }.filter { it.isNotEmpty() }.toSet().size
        val room = lessons.groupingBy { it.room }.eachCount().maxByOrNull { it.value }?.key ?: "—"
        Row(Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatTile("${lessons.size}", "${RuDates.pairsWord(lessons.size)} ${if (isToday) "сегодня" else "за день"}", Modifier.weight(1f))
            StatTile("$groups", if (groups == 1) "группа" else if (groups in 2..4) "группы" else "групп", Modifier.weight(1f))
            StatTile(room, "чаще всего", Modifier.weight(1f))
        }
    }

    if (offline) OfflineBanner(onRetry)

    Spacer(Modifier.padding(top = 16.dp))
    DayStrip(dates, selectedDay, todayIndex, { i -> lessonsOf(i).isNotEmpty() }, onSelectDay)

    if (loading) {
        ScheduleSkeleton()
        return
    }

    // текущая или ближайшая пара сегодня
    var hero: LessonUi? = null
    var heroLive = false
    if (isToday) {
        hero = lessons.firstOrNull { it.startMin != null && it.endMin != null && nowMin >= it.startMin && nowMin < it.endMin }
        heroLive = hero != null
        if (hero == null) hero = lessons.firstOrNull { it.startMin != null && it.endMin != null && it.startMin > nowMin }
    }
    hero?.let { h ->
        val progress = if (heroLive) (nowMin - h.startMin!!).toFloat() / (h.endMin!! - h.startMin).coerceAtLeast(1) else 0f
        val note = if (heroLive) "осталось ${RuDates.duration(h.endMin!! - nowMin)}" else "через ${RuDates.duration(h.startMin!! - nowMin)}"
        HeroLessonCard(h, heroLive, progress, note, onCopyEios, Modifier.padding(top = 16.dp))
    }

    val rows = lessons.filter { it !== hero }
    if (rows.isNotEmpty()) {
        Row(Modifier.fillMaxWidth().padding(start = 2.dp, end = 2.dp, top = 26.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            val listTitle = if (isToday) (if (hero != null) "Дальше сегодня" else "Пары сегодня") else "Пары"
            Text(listTitle, style = MaType.displayFor(listTitle, 18.sp, FontWeight.Bold), color = c.fg, modifier = Modifier.weight(1f))
            if (isToday && lessons.last().end.isNotEmpty()) Text("до ${lessons.last().end}", style = MaType.mono(11.5.sp, FontWeight.Normal), color = c.faint)
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            rows.forEachIndexed { idx, l ->
                if (idx > 0) {
                    val prev = lessons[lessons.indexOf(l) - 1]
                    val gap = if (l.startMin != null && prev.endMin != null) l.startMin - prev.endMin else null
                    if (gap != null && gap > 0) BreakRow(RuDates.breakLabel(gap))
                }
                val past = isToday && l.endMin != null && l.endMin <= nowMin
                LessonCard(l, alpha = if (past) 0.5f else 1f, onCopyEios = onCopyEios)
            }
        }
    }

    // «Завтра» — первая пара следующего дня этой недели
    if (isToday && lessons.isNotEmpty() && todayIndex < 5) {
        val tomorrow = weekLessons[todayIndex + 2].orEmpty().toUiSorted().firstOrNull()
        if (tomorrow != null) {
            val tDate = dates[todayIndex + 1]
            Row(
                Modifier
                    .padding(top = 18.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .dashedBorder(c.borderStrong, 16.dp)
                    .maClick { onSelectDay(todayIndex + 1) }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("ЗАВТРА · ${RuDates.weekShort[todayIndex + 1].uppercase()}, ${RuDates.dayMonthShort(tDate).uppercase()}", style = MaType.eyebrow(10.sp), color = c.faint)
                    Text("${tomorrow.type} · ${tomorrow.subject}", style = MaType.body(14.sp, FontWeight.SemiBold), color = c.fg, maxLines = 1)
                    Text("${tomorrow.start} · ауд. ${tomorrow.room}", style = MaType.mono(11.5.sp, FontWeight.Normal), color = c.soft)
                }
                MaIcon(MaIcons.chevRight, c.faint, 18.dp, 2f)
            }
        }
    }

    if (lessons.isEmpty()) {
        if (sunday) WeekendCard(onOpenWeek) else {
            // ближайшая пара на этой неделе после выбранного дня
            val next = ((selectedDay + 1)..5).firstNotNullOfOrNull { d -> lessonsOf(d).firstOrNull()?.let { d to it } }
            DayOffCard(next, onOpenWeek)
        }
    }
}

@Composable
private fun DayOffCard(next: Pair<Int, LessonUi>?, onOpenWeek: () -> Unit) {
    val c = Ma.colors
    Column(
        Modifier.padding(top = 16.dp).fillMaxWidth().maCard(20.dp, c.shSm).padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(painterResource(R.drawable.ill_day_off), null, Modifier.padding(top = 0.dp).size(150.dp))
        Text("Пар нет — отдыхайте", style = MaType.display(20.sp), color = c.fg)
        val text = buildAnnotatedString {
            if (next != null) {
                val (d, l) = next
                append("Ближайшая пара — ${RuDates.weekFull[d].lowercase()}, ")
                withStyle(SpanStyle(fontFamily = MaFonts.mono, fontSize = 12.5.sp)) { append(l.start) }
                append(", ${l.subject}, ауд. ")
                withStyle(SpanStyle(fontFamily = MaFonts.mono, fontSize = 12.5.sp)) { append(l.room) }
            } else append("На этой неделе пар больше нет")
        }
        Text(text, style = MaType.body(13.5.sp, lineHeight = 20.sp), color = c.soft, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 6.dp))
        OutlineButton("Открыть неделю", onOpenWeek, Modifier.padding(top = 14.dp))
    }
}

@Composable
private fun WeekendCard(onOpenWeek: () -> Unit) {
    val c = Ma.colors
    Column(
        Modifier.padding(top = 16.dp).fillMaxWidth().maCard(20.dp, c.shSm)
            .padding(start = 18.dp, end = 18.dp, top = 22.dp, bottom = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(painterResource(R.drawable.ill_weekend), null, Modifier.size(128.dp))
        Text("ВОСКРЕСЕНЬЕ", style = MaType.eyebrow(10.5.sp), color = c.faint, modifier = Modifier.padding(top = 10.dp))
        Text("Пар нет", style = MaType.display(28.sp), color = c.fg, modifier = Modifier.padding(top = 4.dp))
        Text("Расписание на неделю — во вкладке «Неделя»", style = MaType.body(13.5.sp), color = c.soft, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 4.dp))
        OutlineButton("Открыть неделю", onOpenWeek, Modifier.padding(top = 14.dp).fillMaxWidth(), height = 50.dp, fontSize = 15.sp, background = c.surface, trailingIcon = MaIcons.chevRight)
    }
}
