package com.mycollege.schedule.feature.schedule.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mycollege.schedule.R
import com.mycollege.schedule.shared.ui.design.IconTile
import com.mycollege.schedule.shared.ui.design.MaIcons
import com.mycollege.schedule.shared.ui.design.PrimaryButton
import com.mycollege.schedule.shared.ui.design.maCard
import com.mycollege.schedule.shared.ui.design.maClick
import com.mycollege.schedule.shared.ui.theme.Ma
import com.mycollege.schedule.shared.ui.theme.MaType

/**
 * Ошибка загрузки расписания (Error.dc.html) — бывший экран «Упс, здесь ничего нет»
 */
@Composable
fun ScheduleErrorContent(onRetry: () -> Unit) {
    val c = Ma.colors
    val uri = LocalUriHandler.current
    Column(Modifier.fillMaxWidth().padding(horizontal = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Image(painterResource(R.drawable.ill_error), null, Modifier.offset(y = (-8).dp).size(176.dp))
        Text("Что-то пошло не так", style = MaType.display(28.sp, FontWeight.ExtraBold, 31.sp), color = c.fg, textAlign = TextAlign.Center)
        Text(
            "Не удалось получить расписание с сервера академии. Сохранённых данных для этой группы пока нет.",
            style = MaType.body(14.5.sp, lineHeight = 22.5.sp), color = c.soft, textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp).widthIn(max = 320.dp)
        )
        Column(Modifier.padding(top = 20.dp).fillMaxWidth().maCard(16.dp)) {
            Text("ПОЖАЛУЙСТА, ПРОВЕРЬТЕ", style = MaType.eyebrow(10.sp), color = c.faint, modifier = Modifier.padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 4.dp))
            listOf(
                MaIcons.wifi to "Стабильно ли интернет-соединение",
                MaIcons.trash to "Очистите кеш приложения в настройках телефона",
                MaIcons.refresh to "Перезапустите приложение",
                MaIcons.globe to "Возможны временные проблемы на imsit.ru",
            ).forEachIndexed { i, (icon, text) ->
                if (i > 0) Box(Modifier.fillMaxWidth().height(1.dp).background(c.border))
                Row(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    IconTile(icon, size = 30.dp, iconSize = 16.dp, stroke = 1.9f)
                    Text(text, style = MaType.body(13.5.sp, lineHeight = 19.sp), color = c.fg)
                }
            }
        }
        PrimaryButton("Повторить", onRetry, Modifier.padding(top = 18.dp).fillMaxWidth())
        Text(
            buildAnnotatedString {
                append("Не помогло? Напишите разработчику — ")
                withStyle(SpanStyle(color = c.accent, fontWeight = FontWeight.SemiBold)) { append("@dimitrisimonyan") }
            },
            style = MaType.body(13.sp), color = c.soft, textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 14.dp).maClick { uri.openUri("https://t.me/dimitrisimonyan") }.padding(4.dp)
        )
    }
}

/**
 * Расписание ещё не выбрано: приглашение выбрать группу или преподавателя
 */
@Composable
fun NoChoiceContent(student: Boolean, onOpenSelect: () -> Unit) {
    val c = Ma.colors
    Column(Modifier.fillMaxWidth().padding(horizontal = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Image(painterResource(R.drawable.ill_search_empty), null, Modifier.padding(top = 12.dp).size(168.dp))
        val title = if (student) "Выберите группу" else "Выберите преподавателя"
        Text(title, style = MaType.display(28.sp, FontWeight.ExtraBold, 31.sp), color = c.fg, textAlign = TextAlign.Center)
        Text(
            if (student) "Найдите свою группу — расписание сохранится на телефоне и будет доступно без сети."
            else "Найдите себя в списке преподавателей — расписание сохранится на телефоне и будет доступно без сети.",
            style = MaType.body(14.5.sp, lineHeight = 22.5.sp), color = c.soft, textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp).widthIn(max = 320.dp)
        )
        PrimaryButton(if (student) "Выбрать группу" else "Выбрать преподавателя", onOpenSelect, Modifier.padding(top = 22.dp).fillMaxWidth(), trailingIcon = MaIcons.arrowRight)
    }
}
