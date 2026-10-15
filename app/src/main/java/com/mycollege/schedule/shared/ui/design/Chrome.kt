package com.mycollege.schedule.shared.ui.design

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mycollege.schedule.shared.ui.theme.Ma
import com.mycollege.schedule.shared.ui.theme.MaBrand
import com.mycollege.schedule.shared.ui.theme.MaShadow
import com.mycollege.schedule.shared.ui.theme.MaType
import kotlinx.coroutines.delay

/** Отступы Android-макетов от верха: контент и шапка (с учётом статус-бара 36dp в макете) */
object MaInsets {
    /** верх контента под шапкой: top 104 − статус-бар макета 36 */
    val contentTop = 68.dp
    /** верх шапки: headTop 46 − 36 */
    val headTop = 10.dp
    /** нижний отступ прокрутки под плавающую навигацию (132 в макете) */
    val navScrollBottom = 132.dp
}

@Composable
fun statusBarTop(): Dp = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

@Composable
fun navBarBottom(): Dp = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

/** Эллиптическое радиальное свечение: центр и радиусы — доли размера блока */
data class Glow(val cx: Float, val cy: Float, val rx: Float, val ry: Float, val color: Color, val stop: Float = 0.62f)

private fun DrawScope.ellipse(g: Glow, solid: Boolean = false) {
    val center = Offset(size.width * g.cx, size.height * g.cy)
    val rx = size.width * g.rx
    val ry = size.height * g.ry
    withTransform({ scale(1f, ry / rx, pivot = center) }) {
        val brush = Brush.radialGradient(0f to g.color, g.stop to Color.Transparent, center = center, radius = rx)
        if (solid) {
            // маска должна закрыть всю область, а не только круг
            val k = rx / ry
            drawRect(brush, topLeft = Offset(-size.width, center.y - size.height * k * 2), size = androidx.compose.ui.geometry.Size(size.width * 3, size.height * k * 4), blendMode = BlendMode.DstIn)
        } else drawCircle(brush = brush, radius = rx, center = center)
    }
}

/**
 * Фон верха экрана: мягкие свечения (accentSoft2 и sky) с затуханием вниз и сетка 28dp под радиальной маской
 */
@Composable
fun ScreenBackdrop(
    modifier: Modifier = Modifier,
    height: Dp = 380.dp,
    glows: List<Glow>? = null,
    fadeFrom: Float = 0.6f,
    gridHeight: Dp = 340.dp,
    grid: Boolean = true,
    gridMask: Glow = Glow(0.6f, 0.25f, 0.7f, 0.7f, Color.Black, 0.72f),
) {
    val c = Ma.colors
    val g = glows ?: listOf(
        Glow(1f, 0f, 0.7f, 0.6f, c.accentSoft2),
        Glow(0f, 0.7f, 0.55f, 0.45f, c.sky),
    )
    val border = c.border
    Box(modifier.fillMaxWidth()) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(height)
                .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                .drawBehind {
                    g.forEach { ellipse(it) }
                    if (fadeFrom < 1f) drawRect(
                        Brush.verticalGradient(0f to Color.Black, fadeFrom to Color.Black, 1f to Color.Transparent),
                        blendMode = BlendMode.DstIn
                    )
                }
        )
        if (grid) Box(
            Modifier
                .fillMaxWidth()
                .height(gridHeight)
                .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                .drawBehind {
                    val step = 28.dp.toPx()
                    val w = 1.dp.toPx()
                    var y = 0f
                    while (y < size.height) { drawRect(border, Offset(0f, y), androidx.compose.ui.geometry.Size(size.width, w)); y += step }
                    var x = 0f
                    while (x < size.width) { drawRect(border, Offset(x, 0f), androidx.compose.ui.geometry.Size(w, size.height)); x += step }
                    ellipse(gridMask, solid = true)
                }
        )
    }
}

enum class MainTab { Today, Week, Settings }

/**
 * Плавающая нижняя навигация (Chrome.dc.html): три раздела в капсуле и круглая кнопка поиска
 */
@Composable
fun FloatingNavBar(
    current: MainTab?,
    onSelect: (MainTab) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = Ma.colors
    val tabs = listOf(
        Triple(MainTab.Today, "Сегодня", MaIcons.today),
        Triple(MainTab.Week, "Неделя", MaIcons.week),
        Triple(MainTab.Settings, "Настройки", MaIcons.settings),
    )
    Row(
        modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 22.dp + navBarBottom()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            Modifier
                .weight(1f)
                .height(62.dp)
                .maMat(31.dp, nav = true)
                .padding(5.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            tabs.forEach { (tab, label, icon) ->
                val on = tab == current
                Column(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(26.dp))
                        .background(if (on) c.navActive else Color.Transparent)
                        .maClick { onSelect(tab) }
                        .semantics { contentDescription = label },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically)
                ) {
                    val color = if (on) c.accent else c.fg
                    MaIcon(icon, color, 22.dp, if (on) 2.1f else 1.8f)
                    Text(label, style = MaType.body(10.5.sp, if (on) FontWeight.Bold else FontWeight.SemiBold), color = color, maxLines = 1)
                }
            }
        }
        Box(
            Modifier
                .size(62.dp)
                .maMat(31.dp, nav = true)
                .maClick(onClick = onSearch)
                .semantics { contentDescription = "Найти группу или преподавателя" },
            contentAlignment = Alignment.Center
        ) {
            MaIcon(MaIcons.search, c.fg, 22.dp, 2f)
        }
    }
}

/** Всплывающее сообщение в стиле макета (тёмная плашка с зелёной точкой) */
class MaToastState {
    var message by mutableStateOf<Pair<String, String?>?>(null)
        private set
    var version by mutableStateOf(0)
        private set

    fun show(title: String, hint: String? = null) {
        message = title to hint
        version++
    }

    fun hide() { message = null }
}

@Composable
fun rememberMaToast() = remember { MaToastState() }

@Composable
fun BoxScope.MaToastHost(state: MaToastState, bottom: Dp = 104.dp) {
    val msg = state.message
    LaunchedEffect(state.version) {
        if (msg != null) {
            delay(1800)
            state.hide()
        }
    }
    AnimatedVisibility(
        visible = msg != null,
        enter = fadeIn() + slideInVertically { it / 2 },
        exit = fadeOut() + slideOutVertically { it / 2 },
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(start = 16.dp, end = 16.dp, bottom = bottom + navBarBottom())
    ) {
        val (title, hint) = msg ?: ("" to null)
        Row(
            Modifier
                .fillMaxWidth()
                .maShadow(MaShadow(Color(0x4D0F172A), 12f, 32f), 14.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MaBrand.toastBg)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(MaBrand.green))
            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) { append(title) }
                    if (hint != null) withStyle(SpanStyle(color = MaBrand.toastFg.copy(alpha = 0.7f))) { append(" · $hint") }
                },
                style = MaType.body(13.5.sp),
                color = MaBrand.toastFg
            )
        }
    }
}

/** Экранный контейнер: фон темы во весь экран */
@Composable
fun MaScreen(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(modifier.fillMaxSize().background(Ma.colors.bg)) {
        content()
        // подложка под статус-бар: прокручиваемый контент не наезжает на системные значки
        Box(
            Modifier
                .fillMaxWidth()
                .height(statusBarTop())
                .background(Brush.verticalGradient(listOf(Ma.colors.bg, Ma.colors.bg.copy(alpha = 0.85f))))
        )
    }
}
