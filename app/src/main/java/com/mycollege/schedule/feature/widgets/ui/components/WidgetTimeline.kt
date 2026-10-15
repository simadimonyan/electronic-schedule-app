package com.mycollege.schedule.feature.widgets.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.background
import androidx.glance.appwidget.cornerRadius
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontFamily
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.mycollege.schedule.R

/*
 * Раскладки виджета по макету Widget.dc.html (platform = android):
 *  - малый (small 170×170): направляющая слева на 15dp, узлы на ней, надпись капслоком над названием;
 *  - средний (medium 364×170): направляющая на 17dp, название с подписью справа, строки «кто» и «время · аудитория».
 * Glance не поддерживает свои шрифты: заголовки — системный жирный, время и аудитории — моноширинный.
 */

private val mono = FontFamily.Monospace

@Composable
private fun Rail(palette: WidgetPalette, left: Dp) {
    Row(GlanceModifier.fillMaxHeight()) {
        Spacer(GlanceModifier.width(left))
        Box(GlanceModifier.width(3.dp).fillMaxHeight().background(ImageProvider(palette.rail))) {}
    }
}

/** Узел на направляющей: колонка шириной до начала текста, центр узла — на оси направляющей */
@Composable
private fun NodeColumn(palette: WidgetPalette, node: WidgetNode, columnWidth: Dp, railCenter: Float, centerY: Float) {
    val s = node.sizeDp
    Box(GlanceModifier.width(columnWidth).padding(start = (railCenter - s / 2).dp, top = (centerY - s / 2).coerceAtLeast(0f).dp)) {
        Image(ImageProvider(palette.nodeRes(node)), contentDescription = null, modifier = GlanceModifier.size(s.dp))
    }
}

/** Полоса прогресса с градиентом: ширина заполнения считается от доступной ширины */
@Composable
private fun ProgressLine(palette: WidgetPalette, progress: Float, label: String, trackWidth: Dp, labelSize: TextUnit, gap: Dp) {
    val fill = (trackWidth.value * progress.coerceIn(0f, 1f)).dp
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(GlanceModifier.width(trackWidth).height(4.dp).background(ImageProvider(palette.track))) {
            if (fill.value >= 1f) Box(GlanceModifier.width(fill).height(4.dp).background(ImageProvider(palette.bar))) {}
        }
        if (label.isNotEmpty()) {
            Spacer(GlanceModifier.width(gap))
            Text(label, style = TextStyle(color = palette.soft, fontSize = labelSize, fontFamily = mono), maxLines = 1)
        }
    }
}

/** ширина подписи моноширинным шрифтом (≈0.6em на символ) */
private fun monoWidth(text: String, size: TextUnit): Float = text.length * size.value * 0.62f

@Composable
private fun RoomTime(palette: WidgetPalette, room: String, time: String, size: TextUnit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (room.isNotEmpty()) {
            Text(room, style = TextStyle(color = palette.fg, fontSize = size, fontWeight = FontWeight.Bold, fontFamily = mono), maxLines = 1)
            Text(" · ", style = TextStyle(color = palette.soft, fontSize = size, fontFamily = mono), maxLines = 1)
        }
        Text(time, style = TextStyle(color = palette.soft, fontSize = size, fontFamily = mono), maxLines = 1)
    }
}

/** Малый виджет: два блока «сейчас → следующая» */
@Composable
fun SmallTimeline(palette: WidgetPalette, blocks: List<WidgetBlock>) {
    val size = LocalSize.current
    val tight = size.height < 160.dp
    val contentStart = 31f
    val endPad = 12f
    Box(GlanceModifier.fillMaxSize()) {
        Rail(palette, 15.dp)
        Column(GlanceModifier.fillMaxSize().padding(top = 13.dp, bottom = 12.dp, end = endPad.dp)) {
            blocks.forEachIndexed { i, b ->
                if (i > 0) Spacer(GlanceModifier.defaultWeight())
                Row(GlanceModifier.fillMaxWidth()) {
                    NodeColumn(palette, b.node, contentStart.dp, 16.5f, 21f)
                    Column(GlanceModifier.defaultWeight()) {
                        Text(
                            b.label.uppercase(),
                            style = TextStyle(color = if (b.labelAccent) palette.accent else palette.soft, fontSize = 9.5.sp, fontWeight = FontWeight.Bold),
                            maxLines = 1
                        )
                        Spacer(GlanceModifier.height(2.dp))
                        Text(
                            b.subject,
                            style = TextStyle(color = palette.fg, fontSize = 14.sp, fontWeight = FontWeight.Bold),
                            maxLines = if (i == 0 && !tight) 2 else 1
                        )
                        Spacer(GlanceModifier.height(1.dp))
                        if (b.meta != null) {
                            Text(b.meta, style = TextStyle(color = palette.soft, fontSize = 11.sp), maxLines = 1)
                        } else {
                            RoomTime(palette, b.room, b.time, 10.5.sp)
                        }
                        if (i == 0 && b.progress != null) {
                            Spacer(GlanceModifier.height(5.dp))
                            val labelW = monoWidth(b.progressLabel, 9.5.sp) + 6f
                            ProgressLine(palette, b.progress, b.progressLabel, (size.width.value - contentStart - endPad - labelW).coerceAtLeast(24f).dp, 9.5.sp, 6.dp)
                        }
                    }
                }
            }
        }
    }
}

/** Средний (большой в лаунчере) виджет: первый блок полный, второй компактный */
@Composable
fun MediumTimeline(palette: WidgetPalette, blocks: List<WidgetBlock>) {
    val size = LocalSize.current
    val contentStart = 36f
    val endPad = 16f
    Box(GlanceModifier.fillMaxSize()) {
        Rail(palette, 17.dp)
        Column(GlanceModifier.fillMaxSize().padding(top = 14.dp, bottom = 14.dp, end = endPad.dp)) {
            blocks.forEachIndexed { i, b ->
                if (i > 0) Spacer(GlanceModifier.defaultWeight())
                val full = i == 0 && b.meta == null
                Row(GlanceModifier.fillMaxWidth()) {
                    NodeColumn(palette, b.node, contentStart.dp, 18.5f, 10f)
                    Column(GlanceModifier.defaultWeight()) {
                        Row(GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                b.subject,
                                style = TextStyle(color = palette.fg, fontSize = 15.5.sp, fontWeight = FontWeight.Bold),
                                maxLines = 1,
                                modifier = GlanceModifier.defaultWeight()
                            )
                            Spacer(GlanceModifier.width(10.dp))
                            Text(
                                b.labelShort,
                                style = TextStyle(color = if (b.labelAccent) palette.accent else palette.soft, fontSize = 11.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.End),
                                maxLines = 1
                            )
                        }
                        Spacer(GlanceModifier.height(4.dp))
                        when {
                            b.meta != null -> Text(b.meta, style = TextStyle(color = palette.soft, fontSize = 12.sp), maxLines = 1)
                            full -> {
                                if (b.who.isNotEmpty()) {
                                    IconLine(palette, R.drawable.widget_ic_user) {
                                        Text(b.who, style = TextStyle(color = palette.soft, fontSize = 12.sp), maxLines = 1)
                                    }
                                    Spacer(GlanceModifier.height(3.dp))
                                }
                                IconLine(palette, R.drawable.widget_ic_clock) {
                                    Text(b.time, style = TextStyle(color = palette.soft, fontSize = 12.sp, fontFamily = mono), maxLines = 1)
                                    if (b.room.isNotEmpty()) {
                                        Spacer(GlanceModifier.width(8.dp))
                                        Image(ImageProvider(R.drawable.widget_ic_door), null, GlanceModifier.size(13.dp), colorFilter = ColorFilter.tint(palette.accent))
                                        Spacer(GlanceModifier.width(6.dp))
                                        Text(b.room, style = TextStyle(color = palette.fg, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = mono), maxLines = 1)
                                    }
                                }
                                if (b.progress != null) {
                                    Spacer(GlanceModifier.height(7.dp))
                                    val labelW = monoWidth(b.progressLabel, 10.sp) + 8f
                                    ProgressLine(palette, b.progress, b.progressLabel, (size.width.value - contentStart - endPad - labelW).coerceAtLeast(24f).dp, 10.sp, 8.dp)
                                }
                            }
                            else -> {
                                // компактная строка: кто · время · аудитория
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (b.who.isNotEmpty()) Text("${b.who} · ", style = TextStyle(color = palette.soft, fontSize = 12.sp), maxLines = 1)
                                    RoomTimeReversed(palette, b.time, b.room)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RoomTimeReversed(palette: WidgetPalette, time: String, room: String) {
    Text(time, style = TextStyle(color = palette.soft, fontSize = 12.sp, fontFamily = mono), maxLines = 1)
    if (room.isNotEmpty()) {
        Text(" · ", style = TextStyle(color = palette.soft, fontSize = 12.sp), maxLines = 1)
        Text(room, style = TextStyle(color = palette.fg, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = mono), maxLines = 1)
    }
}

@Composable
private fun IconLine(palette: WidgetPalette, icon: Int, content: @Composable () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(ImageProvider(icon), null, GlanceModifier.size(13.dp), colorFilter = ColorFilter.tint(palette.accent))
        Spacer(GlanceModifier.width(6.dp))
        content()
    }
}

/** Нет выбранной группы/преподавателя или расписания в кэше */
@Composable
fun ScheduleAlert(palette: WidgetPalette) {
    Column(
        GlanceModifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(ImageProvider(R.drawable.ma_logo), contentDescription = null, modifier = GlanceModifier.size(32.dp).cornerRadius(9.dp))
        Spacer(GlanceModifier.height(10.dp))
        Text("Нет расписания", style = TextStyle(color = palette.fg, fontSize = 14.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center), maxLines = 1)
        Spacer(GlanceModifier.height(2.dp))
        Text("Откройте приложение и выберите группу", style = TextStyle(color = palette.soft, fontSize = 11.5.sp, textAlign = TextAlign.Center), maxLines = 2)
    }
}
