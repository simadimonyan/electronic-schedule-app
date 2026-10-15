package com.mycollege.schedule.shared.ui.design

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mycollege.schedule.shared.ui.theme.Ma
import com.mycollege.schedule.shared.ui.theme.MaShadow

/**
 * CSS box-shadow для скруглённого прямоугольника (размытие через setShadowLayer).
 * blur в макетах — радиус размытия CSS, сигма = blur / 2.
 */
fun Modifier.maShadow(shadow: MaShadow?, radius: Dp): Modifier =
    if (shadow == null || shadow.color.alpha == 0f) this else drawBehind {
        val blurPx = shadow.blur.dp.toPx()
        val spread = shadow.spread.dp.toPx()
        val r = radius.toPx()
        drawIntoCanvas { canvas ->
            val paint = Paint().asFrameworkPaint().apply {
                isAntiAlias = true
                color = Color.Transparent.toArgb()
                // радиус framework ≈ сигма / 0.57735, сигма CSS = blur / 2
                setShadowLayer((blurPx / 2f / 0.57735f).coerceAtLeast(0.1f), 0f, shadow.offsetY.dp.toPx(), shadow.color.toArgb())
            }
            canvas.nativeCanvas.drawRoundRect(-spread, -spread, size.width + spread, size.height + spread, r + spread, r + spread, paint)
        }
    }

fun Modifier.maShadows(shadows: List<MaShadow>, radius: Dp): Modifier =
    shadows.fold(this) { m, s -> m.maShadow(s, radius) }

/**
 * Карточка дизайна: поверхность, рамка 1dp цвета border и тень shXs/shSm
 */
@Composable
fun Modifier.maCard(
    radius: Dp = 16.dp,
    shadows: List<MaShadow> = listOfNotNull(Ma.colors.shXs),
    background: Color = Ma.colors.surface,
    borderColor: Color = Ma.colors.border,
): Modifier {
    val shape = RoundedCornerShape(radius)
    return this
        .maShadows(shadows, radius)
        .clip(shape)
        .background(background, shape)
        .border(1.dp, borderColor, shape)
}

/**
 * «Материал» Android из макетов (`mat`): плотная поверхность, кольцо 1dp и мягкая тень
 */
@Composable
fun Modifier.maMat(radius: Dp, nav: Boolean = false): Modifier {
    val c = Ma.colors
    val shape = RoundedCornerShape(radius)
    return this
        .maShadow(if (nav) c.navShadow else c.matShadow, radius)
        .clip(shape)
        .background(c.surface, shape)
        .border(1.dp, c.matRing, shape)
}
