package com.mycollege.schedule.feature.onboarding.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mycollege.schedule.R
import com.mycollege.schedule.feature.onboarding.ui.state.OnboardingViewModel
import com.mycollege.schedule.feature.schedule.ui.components.PulseDot
import com.mycollege.schedule.shared.ui.design.Glow
import com.mycollege.schedule.shared.ui.design.MaIcon
import com.mycollege.schedule.shared.ui.design.MaIcons
import com.mycollege.schedule.shared.ui.design.MaScreen
import com.mycollege.schedule.shared.ui.design.PrimaryButton
import com.mycollege.schedule.shared.ui.design.ScreenBackdrop
import com.mycollege.schedule.shared.ui.design.maCard
import com.mycollege.schedule.shared.ui.design.maClick
import com.mycollege.schedule.shared.ui.design.maMat
import com.mycollege.schedule.shared.ui.design.maShadow
import com.mycollege.schedule.shared.ui.design.navBarBottom
import com.mycollege.schedule.shared.ui.design.statusBarTop
import com.mycollege.schedule.shared.ui.theme.Ma
import com.mycollege.schedule.shared.ui.theme.MaBrand
import com.mycollege.schedule.shared.ui.theme.MaShadow
import com.mycollege.schedule.shared.ui.theme.MaType

/**
 * Приветствие (Onboarding.dc.html). Экран по-прежнему не подключён к навигации
 * (стартовый маршрут — Start), обе кнопки, как и раньше, отмечают первый запуск.
 * [onChoose] — необязательная реакция на выбор «студент / преподаватель».
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = hiltViewModel(),
    onChoose: (studentMode: Boolean) -> Unit = {}
) {
    val c = Ma.colors
    val uri = LocalUriHandler.current
    val choose: (Boolean) -> Unit = {
        viewModel.setFirstStartup()
        onChoose(it)
    }

    MaScreen {
        ScreenBackdrop(
            height = 844.dp,
            glows = listOf(Glow(1f, 0f, 0.8f, 0.5f, c.accentSoft2), Glow(0f, 0.45f, 0.6f, 0.4f, c.sky)),
            fadeFrom = 1f,
            gridHeight = 520.dp,
            gridMask = Glow(0.6f, 0.3f, 0.7f, 0.6f, Color.Black, 0.72f)
        )
        Column(
            Modifier
                .fillMaxSize()
                .padding(start = 24.dp, end = 24.dp, top = statusBarTop() + 20.dp, bottom = 44.dp + navBarBottom())
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Image(
                    painterResource(R.drawable.ma_logo), "Логотип «Моя Академия»",
                    Modifier.size(48.dp).maShadow(MaShadow(Color(0x471A4FDB), 4f, 14f), 13.dp).clip(RoundedCornerShape(13.dp))
                )
                Column {
                    Text("Моя Академия", style = MaType.display(16.sp), color = c.fg)
                    Text("Академия ИМСИТ · Краснодар", style = MaType.body(12.5.sp), color = c.soft)
                }
            }

            ProductPreview(Modifier.padding(top = 26.dp))

            Text(
                buildAnnotatedString {
                    append("Расписание ")
                    withStyle(SpanStyle(brush = Brush.linearGradient(listOf(MaBrand.primary, MaBrand.sky)))) { append("ИМСИТ") }
                    append(" всегда под рукой")
                },
                style = MaType.display(38.sp, FontWeight.ExtraBold, 40.sp, tracking = -0.035f),
                color = c.fg,
                modifier = Modifier.padding(top = 26.dp)
            )
            Text(
                "Текущая пара, аудитория и напоминания. Работает без интернета.",
                style = MaType.body(15.5.sp, lineHeight = 24.8.sp), color = c.soft, modifier = Modifier.padding(top = 12.dp)
            )
            FlowRow(Modifier.padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(MaIcons.clock to "Сейчас и дальше", MaIcons.bell to "Напоминания", MaIcons.cloudCheck to "Без сети", MaIcons.widget to "Виджеты").forEach { (icon, t) ->
                    Row(
                        Modifier.height(30.dp).clip(CircleShape).background(c.surface).border(1.dp, c.border, CircleShape).padding(start = 9.dp, end = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        MaIcon(icon, c.accent, 14.dp, 2f)
                        Text(t, style = MaType.body(12.5.sp, FontWeight.SemiBold), color = c.muted)
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PrimaryButton("Я студент", { choose(true) }, Modifier.fillMaxWidth(), height = 54.dp, fontSize = 16.sp, trailingIcon = MaIcons.arrowRight)
                Box(
                    Modifier.fillMaxWidth().height(54.dp).maMat(27.dp).maClick { choose(false) },
                    contentAlignment = Alignment.Center
                ) { Text("Я преподаватель", style = MaType.body(16.sp, FontWeight.SemiBold), color = c.fg) }
                Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.Center) {
                    Text("Продолжая, вы принимаете ", style = MaType.body(11.5.sp), color = c.faint)
                    Text("условия", style = MaType.body(11.5.sp), color = c.accent, modifier = Modifier.maClick { uri.openUri("https://myimsit.ru") })
                    Text(" и ", style = MaType.body(11.5.sp), color = c.faint)
                    Text("политику", style = MaType.body(11.5.sp), color = c.accent, modifier = Modifier.maClick { uri.openUri("https://myimsit.ru") })
                }
            }
        }
    }
}

/** Иллюстрация продукта: карточка «Сейчас» и следующая пара */
@Composable
private fun ProductPreview(modifier: Modifier) {
    val c = Ma.colors
    Box(modifier.fillMaxWidth().height(228.dp)) {
        Box(
            Modifier.padding(horizontal = 22.dp).fillMaxWidth().height(74.dp).rotate(-2.5f).alpha(0.7f).maCard(16.dp, c.shSm)
        )
        Row(
            Modifier
                .offset(y = 22.dp)
                .fillMaxWidth()
                .maShadow(MaShadow(Color(0x8C1A4FDB), 18f, 40f, -12f), 20.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Brush.linearGradient(0f to MaBrand.primary, 0.55f to MaBrand.gradientMid, 1f to MaBrand.sky, start = Offset.Zero, end = Offset.Infinite))
                .drawBehind {
                    val r = 90.dp.toPx()
                    drawCircle(Color.White.copy(alpha = 0.12f), r, Offset(size.width - 40.dp.toPx() - r, -60.dp.toPx() + r))
                }
                .height(IntrinsicSize.Min)
        ) {
            Column(Modifier.weight(1f).padding(start = 18.dp, top = 16.dp, end = 14.dp, bottom = 16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        PulseDot()
                        Text("СЕЙЧАС", style = MaType.eyebrow(10.sp), color = Color.White)
                    }
                    Text("№4 · 13:10–14:40", style = MaType.mono(11.sp, FontWeight.Normal), color = Color.White.copy(alpha = 0.85f))
                }
                Text("Этические вопросы", style = MaType.display(18.sp, FontWeight.ExtraBold, 22.sp), color = Color.White, modifier = Modifier.padding(top = 8.dp))
                Text("Практика · Чавыкина У.Г.", style = MaType.body(12.5.sp), color = Color.White.copy(alpha = 0.88f), modifier = Modifier.padding(top = 3.dp))
                Box(Modifier.padding(top = 12.dp).fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)).background(Color.White.copy(alpha = 0.28f))) {
                    Box(Modifier.fillMaxWidth(0.36f).fillMaxHeight().background(Color.White))
                }
            }
            Row(Modifier.width(80.dp).fillMaxHeight()) {
                Box(Modifier.width(1.dp).fillMaxHeight().background(Color.White.copy(alpha = 0.22f)))
                Column(
                    Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.14f)),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically)
                ) {
                    Text("АУД.", style = MaType.eyebrow(9.sp), color = Color.White.copy(alpha = 0.8f))
                    Text("1-102", style = MaType.mono(16.sp, FontWeight.Bold), color = Color.White)
                }
            }
        }
        Row(
            Modifier
                .padding(horizontal = 14.dp)
                .offset(y = 162.dp)
                .fillMaxWidth()
                .maCard(16.dp, listOf(MaShadow(Color(0x140F172A), 4f, 12f), MaShadow(Color(0x0A0F172A), 2f, 4f)))
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.padding(start = 7.dp, top = 11.dp, bottom = 11.dp).width(4.dp).fillMaxHeight().clip(RoundedCornerShape(4.dp)).background(c.accent))
            Column(Modifier.weight(1f).padding(start = 11.dp, top = 11.dp, bottom = 11.dp)) {
                Text("14:50–16:20 · Лекция", style = MaType.mono(11.sp, FontWeight.Normal), color = c.muted)
                Text("Этические вопросы", style = MaType.display(14.sp, FontWeight.Bold), color = c.fg, modifier = Modifier.padding(top = 2.dp))
            }
            Text(
                "1-301", style = MaType.mono(12.sp, FontWeight.Bold), color = c.fg,
                modifier = Modifier.padding(end = 14.dp).clip(RoundedCornerShape(6.dp)).background(c.sunk).border(1.dp, c.border, RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
    }
}
