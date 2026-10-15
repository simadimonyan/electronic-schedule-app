package com.mycollege.schedule.feature.schedule.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mycollege.schedule.feature.schedule.data.models.DataClasses
import com.mycollege.schedule.shared.ui.design.EyebrowPill
import com.mycollege.schedule.shared.ui.design.MaIcon
import com.mycollege.schedule.shared.ui.design.MaIcons
import com.mycollege.schedule.shared.ui.design.maCard
import com.mycollege.schedule.shared.ui.design.maClick
import com.mycollege.schedule.shared.ui.design.maShadow
import com.mycollege.schedule.shared.ui.theme.LessonKind
import com.mycollege.schedule.shared.ui.theme.Ma
import com.mycollege.schedule.shared.ui.theme.MaBrand
import com.mycollege.schedule.shared.ui.theme.MaShadow
import com.mycollege.schedule.shared.ui.theme.MaType
import com.mycollege.schedule.shared.ui.theme.lessonPalette
import java.time.LocalDate

/**
 * Экран «Неделя» (Week.dc.html): сводка по типам занятий и дни списком
 */
@Composable
fun WeekContent(
    weekLessons: Map<Int, List<DataClasses.Lesson>>,
    todayLessons: List<DataClasses.Lesson>,
    parity: Int,
    loading: Boolean,
    onCopyEios: (String) -> Unit,
) {
    val c = Ma.colors
    val today = LocalDate.now()
    val todayIndex = today.dayOfWeek.value - 1
    val monday = RuDates.monday(today)
    val nowMin = RuDates.nowMinutes()

    // дни пн–сб; сегодняшний день берём из актуального расписания на сегодня
    val days = (0..5).map { i -> if (i == todayIndex && todayLessons.isNotEmpty()) todayLessons.toUiSorted() else weekLessons[i + 1].orEmpty().toUiSorted() }
    val all = days.flatten()
    val studyDays = days.count { it.isNotEmpty() }

    EyebrowPill("${monday.dayOfMonth} – ${RuDates.dayMonth(monday.plusDays(5))}")
    Text("Неделя $parity", style = MaType.display(36.sp, FontWeight.ExtraBold, 38.sp), color = c.fg, modifier = Modifier.padding(top = 10.dp))
    Text(
        "${all.size} ${RuDates.pairsWord(all.size)} · $studyDays ${if (studyDays == 1) "учебный день" else if (studyDays in 2..4) "учебных дня" else "учебных дней"}",
        style = MaType.body(14.sp), color = c.soft, modifier = Modifier.padding(top = 4.dp)
    )

    if (loading) {
        ScheduleSkeleton()
        return
    }

    // распределение по типам
    val mix = listOf(
        Triple("Лекции", LessonKind.LECTURE, c.accent),
        Triple("Практики", LessonKind.PRACTICE, MaBrand.green),
        Triple("Лабораторные", LessonKind.LAB, MaBrand.amber),
        Triple("Экзамены и зачёты", LessonKind.EXAM, MaBrand.red),
        Triple("Другое", LessonKind.OTHER, c.faint),
    ).map { (label, kind, color) -> Triple(label, all.count { LessonKind.of(it.type) == kind }, color) }.filter { it.second > 0 }

    if (mix.isNotEmpty()) Column(Modifier.padding(top = 16.dp).fillMaxWidth().maCard(16.dp, c.shSm).padding(horizontal = 14.dp, vertical = 12.dp)) {
        Row(Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            mix.forEach { (_, n, color) -> Box(Modifier.weight(n.toFloat()).fillMaxHeight().background(color)) }
        }
        mix.chunked(2).forEach { pair ->
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                pair.forEach { (label, n, color) ->
                    Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        Box(Modifier.size(8.dp).clip(CircleShape).background(color))
                        Text(label, style = MaType.body(12.5.sp), color = c.muted, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("$n", style = MaType.mono(11.5.sp, FontWeight.Normal), color = c.faint)
                    }
                }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }

    days.forEachIndexed { i, lessons ->
        val isToday = i == todayIndex
        val date = monday.plusDays(i.toLong())
        Column(Modifier.alpha(if (i < todayIndex) 0.6f else 1f)) {
            Row(Modifier.fillMaxWidth().padding(start = 2.dp, end = 2.dp, top = 24.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(RuDates.weekFull[i], style = MaType.display(17.sp, FontWeight.Bold), color = c.fg)
                Text(RuDates.dayMonthShort(date), style = MaType.mono(11.5.sp, FontWeight.Normal), color = c.faint)
                Spacer(Modifier.weight(1f))
                if (isToday) Text(
                    "СЕГОДНЯ", style = MaType.eyebrow(10.sp), color = Color.White,
                    modifier = Modifier.clip(CircleShape).background(MaBrand.primary).padding(horizontal = 9.dp, vertical = 3.dp)
                ) else if (lessons.isNotEmpty()) Text("${lessons.size} ${RuDates.pairsWord(lessons.size)}", style = MaType.body(12.sp), color = c.faint)
            }
            if (lessons.isEmpty()) {
                Row(
                    Modifier.fillMaxWidth().dashedBorder(c.borderStrong, 16.dp).padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MaIcon(MaIcons.moon, c.soft, 18.dp, 1.8f)
                    Text("Выходной", style = MaType.body(13.5.sp), color = c.soft)
                }
            } else {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .then(if (isToday) Modifier.maShadow(MaShadow(c.accentSoft, 0f, 0f, 3f), 16.dp) else Modifier)
                        .maCard(16.dp, borderColor = if (isToday) c.accentBorder else c.border)
                ) {
                    lessons.forEachIndexed { j, l ->
                        val now = isToday && l.startMin != null && l.endMin != null && nowMin >= l.startMin && nowMin < l.endMin
                        WeekLessonRow(l, divider = j > 0, now = now, onCopyEios = onCopyEios)
                    }
                }
            }
        }
    }
}

@Composable
private fun WeekLessonRow(l: LessonUi, divider: Boolean, now: Boolean, onCopyEios: (String) -> Unit) {
    val c = Ma.colors
    val p = c.lessonPalette(LessonKind.of(l.type))
    Column(Modifier.fillMaxWidth().background(if (now) c.accentSoft else Color.Transparent)) {
        if (divider) Box(Modifier.fillMaxWidth().height(1.dp).background(c.border))
        Box(Modifier.fillMaxWidth().then(if (l.eios != null) Modifier.maClick { onCopyEios(l.eios) } else Modifier)) {
            Box(Modifier.matchParentSize().padding(vertical = 10.dp)) {
                Box(Modifier.width(3.dp).fillMaxHeight().clip(RoundedCornerShape(topEnd = 3.dp, bottomEnd = 3.dp)).background(p.stripe))
            }
            Row(
                Modifier.fillMaxWidth().padding(start = 16.dp, end = 14.dp, top = 11.dp, bottom = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(Modifier.width(50.dp)) {
                    Text(l.start, style = MaType.mono(12.sp, FontWeight.SemiBold), color = c.fg)
                    Text(l.end, style = MaType.mono(12.sp, FontWeight.Normal), color = c.faint)
                }
                Column(Modifier.weight(1f)) {
                    Text(l.subject, style = MaType.body(14.sp, FontWeight.SemiBold), color = c.fg, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Row(Modifier.padding(top = 2.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        TypeChip(l.type, small = true)
                        Text(l.who, style = MaType.body(12.sp), color = c.soft, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        l.room, style = MaType.mono(12.sp, FontWeight.Bold), color = c.fg, maxLines = 1,
                        modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(c.sunk).border(1.dp, c.border, RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                    if (now) Text("СЕЙЧАС", style = MaType.eyebrow(9.sp), color = c.accent)
                    else if (l.eios != null) Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        MaIcon(MaIcons.link, c.accent, 11.dp, 2f)
                        Text("eios", style = MaType.body(10.5.sp, FontWeight.SemiBold), color = c.accent)
                    }
                }
            }
        }
    }
}
