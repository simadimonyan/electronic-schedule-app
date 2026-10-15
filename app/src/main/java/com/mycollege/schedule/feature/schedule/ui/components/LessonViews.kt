package com.mycollege.schedule.feature.schedule.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mycollege.schedule.R
import com.mycollege.schedule.shared.ui.design.MaIcon
import com.mycollege.schedule.shared.ui.design.MaIcons
import com.mycollege.schedule.shared.ui.design.maCard
import com.mycollege.schedule.shared.ui.design.maClick
import com.mycollege.schedule.shared.ui.design.maMat
import com.mycollege.schedule.shared.ui.design.maShadow
import com.mycollege.schedule.shared.ui.theme.LessonKind
import com.mycollege.schedule.shared.ui.theme.Ma
import com.mycollege.schedule.shared.ui.theme.MaBrand
import com.mycollege.schedule.shared.ui.theme.MaType
import com.mycollege.schedule.shared.ui.theme.lessonPalette

/** Чип типа занятия */
@Composable
fun TypeChip(type: String, modifier: Modifier = Modifier, small: Boolean = false) {
    val p = Ma.colors.lessonPalette(LessonKind.of(type))
    Text(
        type,
        style = MaType.body(10.5.sp, FontWeight.SemiBold),
        color = p.chipFg,
        maxLines = 1,
        modifier = modifier
            .clip(CircleShape)
            .background(p.chipBg)
            .padding(horizontal = if (small) 7.dp else 8.dp, vertical = if (small) 1.dp else 2.dp)
    )
}

/** Пунктирная линия перерыва между парами */
@Composable
fun BreakRow(text: String) {
    val c = Ma.colors
    Row(Modifier.fillMaxWidth().height(20.dp).padding(horizontal = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        DashedLine(Modifier.weight(1f), c.borderStrong)
        Text(text, style = MaType.mono(10.5.sp, FontWeight.Normal), color = c.faint)
        DashedLine(Modifier.weight(1f), c.borderStrong)
    }
}

@Composable
private fun DashedLine(modifier: Modifier, color: Color) {
    Canvas(modifier.height(1.dp)) {
        drawLine(color, Offset(0f, 0f), Offset(size.width, 0f), 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 3.dp.toPx())))
    }
}

/** Пунктирная рамка (border: 1px dashed) */
fun Modifier.dashedBorder(color: Color, radius: Dp): Modifier = drawBehind {
    val r = radius.toPx()
    val w = 1.dp.toPx()
    drawRoundRect(
        color = color,
        topLeft = Offset(w / 2, w / 2),
        size = androidx.compose.ui.geometry.Size(size.width - w, size.height - w),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(r, r),
        style = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 3.dp.toPx())))
    )
}

/**
 * Карточка пары (Today: полоса типа, номер, время, чип, предмет, кто, колонка аудитории)
 */
@Composable
fun LessonCard(l: LessonUi, modifier: Modifier = Modifier, alpha: Float = 1f, onCopyEios: ((String) -> Unit)? = null) {
    val c = Ma.colors
    val p = c.lessonPalette(LessonKind.of(l.type))
    Row(
        modifier
            .fillMaxWidth()
            .alpha(alpha)
            .maCard(16.dp)
            .then(if (l.eios != null && onCopyEios != null) Modifier.maClick { onCopyEios(l.eios) } else Modifier)
            .height(IntrinsicSize.Min)
    ) {
        Box(Modifier.weight(1f)) {
            Box(Modifier.padding(start = 6.dp, top = 12.dp, bottom = 12.dp).width(4.dp).fillMaxHeight().clip(RoundedCornerShape(4.dp)).background(p.stripe))
            Column(Modifier.padding(start = 20.dp, top = 12.dp, end = 12.dp, bottom = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        Modifier.size(22.dp).clip(CircleShape).background(c.soft2).border(1.dp, c.borderStrong, CircleShape),
                        contentAlignment = Alignment.Center
                    ) { Text("${l.n}", style = MaType.mono(10.5.sp, FontWeight.Bold), color = c.fg) }
                    Text(if (l.end.isNotEmpty()) "${l.start}–${l.end}" else l.start, style = MaType.mono(11.5.sp), color = c.muted)
                    Spacer(Modifier.weight(1f))
                    TypeChip(l.type)
                }
                Text(l.subject, style = MaType.displayFor(l.subject, 15.sp, FontWeight.Bold, 19.5.sp, -0.01f), color = c.fg, maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (l.who.isNotEmpty() || l.eios != null) Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (l.who.isNotEmpty()) {
                        MaIcon(if (l.teacherMode) MaIcons.users else MaIcons.user, c.soft, 13.dp, 2f)
                        Text(l.who, style = MaType.body(12.5.sp), color = c.soft, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                    }
                    if (l.eios != null) {
                        Spacer(Modifier.weight(1f))
                        MaIcon(MaIcons.link, c.accent, 13.dp, 2f)
                        Text("eios", style = MaType.body(11.5.sp, FontWeight.SemiBold), color = c.accent)
                    }
                }
            }
        }
        RoomColumn(l.room, Modifier.width(72.dp).fillMaxHeight())
    }
}

@Composable
private fun RoomColumn(room: String, modifier: Modifier) {
    val c = Ma.colors
    Row(modifier) {
        Box(Modifier.width(1.dp).fillMaxHeight().background(c.border))
        Column(
            Modifier.fillMaxSize().background(c.sunk).padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterVertically)
        ) {
            Text("АУД.", style = MaType.eyebrow(9.sp), color = c.faint)
            Text(room, style = MaType.mono(13.5.sp, FontWeight.Bold), color = c.fg, textAlign = TextAlign.Center, lineHeight = 15.5.sp)
        }
    }
}

/** Пульсирующая точка «сейчас» */
@Composable
fun PulseDot(color: Color = Color.White, size: Dp = 6.dp) {
    val t = rememberInfiniteTransition(label = "pulse")
    val k by t.animateFloat(0f, 1f, infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Restart), label = "k")
    Box(Modifier.size(size), contentAlignment = Alignment.Center) {
        val ring = (k / 0.7f).coerceAtMost(1f)
        Canvas(Modifier.size(size)) {
            val r = this.size.minDimension / 2
            drawCircle(color.copy(alpha = 0.6f * (1f - ring)), radius = r + 7.dp.toPx() * ring)
            drawCircle(color, radius = r)
        }
    }
}

/**
 * Карточка «Сейчас идёт» / «Далее» — синий градиент, прогресс, кнопка eios, колонка аудитории
 */
@Composable
fun HeroLessonCard(
    l: LessonUi,
    live: Boolean,
    progress: Float,
    timeNote: String,
    onCopyEios: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier
            .fillMaxWidth()
            .maShadow(MaBrand.heroShadow, 20.dp)
            .clip(shape)
            .background(Brush.linearGradient(0f to MaBrand.primary, 0.55f to MaBrand.gradientMid, 1f to MaBrand.sky, start = Offset(0f, 0f), end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)))
            .height(IntrinsicSize.Min)
    ) {
        Row(Modifier.fillMaxWidth().drawBehind {
            // декоративный круг: top −60, right 40, Ø190
            val r = 95.dp.toPx()
            drawCircle(Color.White.copy(alpha = 0.12f), r, Offset(size.width - 40.dp.toPx() - r, -60.dp.toPx() + r))
        }) {
            Column(Modifier.weight(1f).padding(start = 18.dp, top = 16.dp, end = 14.dp, bottom = 16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (live) PulseDot() else Box(Modifier.size(6.dp).clip(CircleShape).background(Color.White))
                        Text(if (live) "СЕЙЧАС" else "ДАЛЕЕ", style = MaType.eyebrow(10.sp), color = Color.White)
                    }
                    Text("№${l.n} · ${l.start}–${l.end}", style = MaType.mono(11.sp, FontWeight.Normal), color = Color.White.copy(alpha = 0.85f))
                }
                Text(l.subject, style = MaType.displayFor(l.subject, 19.sp, FontWeight.ExtraBold, 23.sp), color = Color.White, modifier = Modifier.padding(top = 8.dp))
                Text(
                    listOf(l.type, l.who).filter { it.isNotEmpty() }.joinToString(" · "),
                    style = MaType.body(13.sp), color = Color.White.copy(alpha = 0.88f), modifier = Modifier.padding(top = 4.dp),
                    maxLines = 2, overflow = TextOverflow.Ellipsis
                )
                if (live) Box(Modifier.padding(top = 14.dp).fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)).background(Color.White.copy(alpha = 0.28f))) {
                    Box(Modifier.fillMaxWidth(progress.coerceIn(0.02f, 1f)).fillMaxHeight().clip(RoundedCornerShape(2.dp)).background(Color.White))
                }
                Row(Modifier.fillMaxWidth().padding(top = 12.dp).heightIn(min = 32.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(timeNote, style = MaType.mono(12.sp, FontWeight.SemiBold), color = Color.White, modifier = Modifier.weight(1f))
                    if (l.eios != null) Row(
                        Modifier
                            .height(32.dp)
                            .maShadow(com.mycollege.schedule.shared.ui.theme.MaShadow(Color(0x260F172A), 1f, 2f), 16.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .maClick { onCopyEios(l.eios) }
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        MaIcon(MaIcons.link, MaBrand.primary, 15.dp, 2f)
                        Text("eios", style = MaType.body(12.5.sp, FontWeight.SemiBold), color = MaBrand.primary)
                    }
                }
            }
            Row(Modifier.width(84.dp).fillMaxHeight()) {
                Box(Modifier.width(1.dp).fillMaxHeight().background(Color.White.copy(alpha = 0.22f)))
                Column(
                    Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.14f)).padding(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically)
                ) {
                    Text("АУД.", style = MaType.eyebrow(9.sp), color = Color.White.copy(alpha = 0.8f))
                    Text(l.room, style = MaType.mono(17.sp, FontWeight.Bold), color = Color.White, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

/** Полоса дней недели (7 кнопок) */
@Composable
fun DayStrip(
    dates: List<java.time.LocalDate>,
    selected: Int,
    todayIndex: Int,
    hasLessons: (Int) -> Boolean,
    onPick: (Int) -> Unit,
) {
    val c = Ma.colors
    Row(
        Modifier
            .fillMaxWidth()
            .maCard(20.dp, c.shSm)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        dates.forEachIndexed { i, d ->
            val sel = i == selected
            val fg = when {
                sel -> Color.White
                i == 6 -> MaBrand.red
                else -> c.fg
            }
            Column(
                Modifier
                    .weight(1f)
                    .height(56.dp)
                    .then(if (sel) Modifier.maShadow(MaBrand.stripShadow, 16.dp) else Modifier)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (sel) MaBrand.primary else Color.Transparent)
                    .maClick { onPick(i) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically)
            ) {
                Text(RuDates.weekShort[i].uppercase(), style = MaType.eyebrow(10.sp, 0.06f), color = fg.copy(alpha = if (sel) 0.85f else 0.55f))
                Text("${d.dayOfMonth}", style = MaType.displayFor("1", 17.sp, FontWeight.ExtraBold, tracking = -0.01f), color = fg)
                val dot = when {
                    i == todayIndex -> if (sel) Color.White else c.accent
                    hasLessons(i) -> if (sel) Color.White.copy(alpha = 0.6f) else c.borderStrong
                    else -> Color.Transparent
                }
                Box(Modifier.size(4.dp).clip(CircleShape).background(dot))
            }
        }
    }
}

/** Скелетон загрузки расписания */
@Composable
fun ScheduleSkeleton() {
    val c = Ma.colors
    val t = rememberInfiniteTransition(label = "sk")
    val a by t.animateFloat(0.55f, 1f, infiniteRepeatable(tween(700), RepeatMode.Reverse), label = "a")
    Column(Modifier.fillMaxWidth().padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.fillMaxWidth().height(176.dp).alpha(a).clip(RoundedCornerShape(20.dp)).background(c.sunk))
        Box(Modifier.padding(start = 2.dp, top = 18.dp, bottom = 6.dp).width(120.dp).height(14.dp).alpha(a).clip(RoundedCornerShape(6.dp)).background(c.sunk))
        repeat(3) { Box(Modifier.fillMaxWidth().height(92.dp).alpha(a).clip(RoundedCornerShape(16.dp)).background(c.sunk)) }
        Text("Загружаем расписание…", style = MaType.body(12.5.sp), color = c.faint, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
    }
}

/** Плашка «Нет подключения» */
@Composable
fun OfflineBanner(onRetry: () -> Unit) {
    val c = Ma.colors
    Column(Modifier.fillMaxWidth().padding(top = 16.dp).maCard(16.dp, c.shSm)) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(32.dp).clip(RoundedCornerShape(9.dp)).background(Color(0x24F59E0B)), contentAlignment = Alignment.Center) {
                MaIcon(MaIcons.cloudOff, c.amberFg, 18.dp, 1.9f)
            }
            Column(Modifier.weight(1f)) {
                Text("Нет подключения", style = MaType.body(14.sp, FontWeight.SemiBold), color = c.fg)
                Text("Показываем сохранённое расписание. Изменения подтянем, когда появится сеть.", style = MaType.body(13.sp, lineHeight = 18.8.sp), color = c.soft)
            }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(c.border))
        Row(Modifier.fillMaxWidth().background(c.soft2).padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Данные с устройства", style = MaType.mono(11.sp, FontWeight.Normal), color = c.faint, modifier = Modifier.weight(1f))
            com.mycollege.schedule.shared.ui.design.AccentChipButton("Повторить", onRetry)
        }
    }
}

/** Логотип 32×32 (радиус 9) */
@Composable
fun LogoBadge(size: Dp = 32.dp, radius: Dp = 9.dp) {
    Image(painterResource(R.drawable.ma_logo), null, Modifier.size(size).clip(RoundedCornerShape(radius)))
}

/** Кружок с инициалами преподавателя */
@Composable
fun InitialsBadge(name: String, size: Dp = 32.dp, bg: Color = MaBrand.primary, fg: Color = Color.White, fontSize: androidx.compose.ui.unit.TextUnit = 12.sp) {
    val initials = name.split(' ', '.').filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }
    Box(Modifier.size(size).clip(CircleShape).background(bg), contentAlignment = Alignment.Center) {
        Text(initials, style = MaType.display(fontSize, FontWeight.ExtraBold, tracking = 0f), color = fg)
    }
}

/**
 * Шапка расписания: чип «моя группа» слева и действия справа
 */
@Composable
fun ScheduleHeader(
    title: String,
    teacher: Boolean,
    onOpenSelect: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: @Composable () -> Unit = {},
) {
    val c = Ma.colors
    Row(modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        Row(
            Modifier
                .weight(1f, fill = false)
                .height(44.dp)
                .maMat(22.dp)
                .maClick(onClick = onOpenSelect)
                .padding(start = 6.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (teacher) InitialsBadge(title) else LogoBadge()
            Text(title, style = MaType.displayFor(title, 14.sp, FontWeight.Bold, tracking = -0.01f), color = c.fg, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
            MaIcon(MaIcons.chevDown, c.fg, 14.dp, 2.2f)
        }
        Spacer(Modifier.weight(0.001f).width(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) { trailing() }
    }
}

/** Чип чётности недели «● Нед. 1» */
@Composable
fun ParityChip(parity: Int, onClick: () -> Unit) {
    val c = Ma.colors
    Row(
        Modifier
            .height(44.dp)
            .maMat(22.dp)
            .maClick(onClick = onClick)
            .padding(horizontal = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Box(Modifier.size(7.dp).clip(CircleShape).background(MaBrand.green))
        Text("Нед. $parity", style = MaType.mono(12.5.sp, FontWeight.SemiBold), color = c.fg)
    }
}

/** Блок статистики преподавателя */
@Composable
fun StatTile(value: String, label: String, modifier: Modifier = Modifier) {
    val c = Ma.colors
    Column(modifier.maCard(14.dp).padding(horizontal = 12.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(value, style = MaType.mono(20.sp, FontWeight.Bold), color = c.fg, maxLines = 1)
        Text(label, style = MaType.body(11.5.sp), color = c.soft, maxLines = 1)
    }
}
