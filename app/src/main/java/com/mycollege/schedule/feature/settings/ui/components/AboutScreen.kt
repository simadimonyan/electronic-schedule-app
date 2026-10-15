package com.mycollege.schedule.feature.settings.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.my.tracker.MyTracker
import com.mycollege.schedule.BuildConfig
import com.mycollege.schedule.R
import com.mycollege.schedule.feature.schedule.ui.components.dashedBorder
import com.mycollege.schedule.shared.ui.design.Glow
import com.mycollege.schedule.shared.ui.design.IconTile
import com.mycollege.schedule.shared.ui.design.MaIcon
import com.mycollege.schedule.shared.ui.design.MaIcons
import com.mycollege.schedule.shared.ui.design.MaInsets
import com.mycollege.schedule.shared.ui.design.MaScreen
import com.mycollege.schedule.shared.ui.design.MatIconButton
import com.mycollege.schedule.shared.ui.design.ScreenBackdrop
import com.mycollege.schedule.shared.ui.design.SectionLabel
import com.mycollege.schedule.shared.ui.design.maCard
import com.mycollege.schedule.shared.ui.design.maClick
import com.mycollege.schedule.shared.ui.design.maShadow
import com.mycollege.schedule.shared.ui.design.navBarBottom
import com.mycollege.schedule.shared.ui.design.statusBarTop
import com.mycollege.schedule.shared.ui.theme.Ma
import com.mycollege.schedule.shared.ui.theme.MaShadow
import com.mycollege.schedule.shared.ui.theme.MaType
import java.time.LocalDate

/**
 * «О приложении» (About.dc.html) — заменяет прежний нижний лист: версия, что нового, связь и документы
 */
@Composable
fun AboutScreen(
    onBack: () -> Unit,
    openPrivacyPolicy: () -> Unit,
    openUserAgreement: () -> Unit,
    openCopyrights: () -> Unit,
) {
    val c = Ma.colors
    val uri = LocalUriHandler.current
    BackHandler(onBack = onBack)

    MaScreen {
        ScreenBackdrop(
            height = 420.dp,
            glows = listOf(Glow(0.5f, 0.18f, 0.6f, 0.5f, c.accentSoft2, 0.7f)),
            fadeFrom = 1f,
            gridHeight = 380.dp,
            gridMask = Glow(0.5f, 0.25f, 0.55f, 0.55f, Color.Black, 0.72f)
        )
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = statusBarTop() + 72.dp, bottom = 48.dp + navBarBottom())
        ) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painterResource(R.drawable.ma_logo), "Логотип «Моя Академия»",
                    Modifier.size(92.dp).maShadow(MaShadow(Color(0x8C1A4FDB), 16f, 40f, -10f), 24.dp).clip(RoundedCornerShape(24.dp))
                )
                Text("Моя Академия", style = MaType.display(28.sp), color = c.fg, modifier = Modifier.padding(top = 16.dp))
                Text("v${BuildConfig.VERSION_NAME} · build ${BuildConfig.VERSION_CODE}", style = MaType.mono(12.sp, FontWeight.Normal), color = c.soft, modifier = Modifier.padding(top = 8.dp))
            }

            SectionLabel("Что нового", Modifier.padding(top = 28.dp, bottom = 8.dp))
            Column(Modifier.fillMaxWidth().maCard(16.dp)) {
                listOf(
                    Triple(MaIcons.star, "Новый дизайн", "В стиле сайта Академии, светлая и тёмная тема."),
                    Triple(MaIcons.today, "Сегодня и неделя", "Текущая пара с таймером, перемены и вся неделя на одном экране."),
                    Triple(MaIcons.cloudCheck, "Работа без сети", "Расписание хранится на телефоне."),
                    Triple(MaIcons.message, "Ежемесячный опрос", "Расскажите, что улучшить, — читаем каждый ответ."),
                ).forEachIndexed { i, (icon, title, text) ->
                    if (i > 0) Box(Modifier.fillMaxWidth().height(1.dp).background(c.border))
                    Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        IconTile(icon)
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(title, style = MaType.body(14.5.sp, FontWeight.SemiBold), color = c.fg)
                            Text(text, style = MaType.body(12.5.sp, lineHeight = 18.sp), color = c.soft)
                        }
                    }
                }
            }

            SectionLabel("Связь и документы", Modifier.padding(top = 24.dp, bottom = 8.dp))
            Column(Modifier.fillMaxWidth().maCard(16.dp)) {
                listOf(
                    Triple(MaIcons.send, "Telegram", "") to { uri.openUri("https://t.me/+KJ4GaYqruzJjOTJi") },
                    Triple(MaIcons.message, "Написать разработчику", "") to { uri.openUri("https://t.me/dimitrisimonyan") },
                    Triple(MaIcons.shield, "Политика конфиденциальности", "") to {
                        MyTracker.trackEvent("Открыть вкладку политика конфиденциальности"); openPrivacyPolicy()
                    },
                    Triple(MaIcons.doc, "Пользовательское соглашение", "") to {
                        MyTracker.trackEvent("Открыть вкладку пользовательское соглашение"); openUserAgreement()
                    },
                    Triple(MaIcons.copyright, "Авторское право", "") to {
                        MyTracker.trackEvent("Открыть вкладку авторское право"); openCopyrights()
                    },
                ).forEachIndexed { i, (row, action) ->
                    val (icon, label, value) = row
                    if (i > 0) Box(Modifier.fillMaxWidth().height(1.dp).background(c.border))
                    Row(
                        Modifier.fillMaxWidth().heightIn(min = 52.dp).maClick(onClick = action).padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        IconTile(icon)
                        Text(label, style = MaType.body(14.5.sp), color = c.fg, modifier = Modifier.weight(1f))
                        if (value.isNotEmpty()) Text(value, style = MaType.mono(11.5.sp, FontWeight.Normal), color = c.faint)
                        MaIcon(if (i < 2) MaIcons.external else MaIcons.chevRight, c.faint, 14.dp, 2f)
                    }
                }
            }

            Column(
                Modifier.padding(top = 24.dp).fillMaxWidth().dashedBorder(c.borderStrong, 16.dp).padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Text(
                    "Расписание загружается с сервера Академии ИМСИТ и хранится на устройстве — его можно смотреть без интернета.",
                    style = MaType.body(13.5.sp, lineHeight = 20.sp), color = c.muted
                )
            }
            Text(
                "© 2024–${LocalDate.now().year} · Моя Академия",
                style = MaType.body(12.sp), color = c.faint, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 20.dp)
            )
        }

        Row(Modifier.padding(start = 16.dp, top = statusBarTop() + MaInsets.headTop)) {
            MatIconButton(MaIcons.back, "Назад", onBack, iconSize = 20.dp, stroke = 2.2f)
        }
    }
}
