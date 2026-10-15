package com.mycollege.schedule.shared.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Цветовые токены дизайна «Моя Академия» (design-kit, объект `c`)
 */
@Immutable
data class MaColors(
    val isDark: Boolean,
    val bg: Color,
    val surface: Color,
    val soft2: Color,
    val sunk: Color,
    val border: Color,
    val borderStrong: Color,
    val fg: Color,
    val muted: Color,
    val soft: Color,
    val faint: Color,
    val accent: Color,
    val accentSoft: Color,
    val accentSoft2: Color,
    val accentBorder: Color,
    val sky: Color,
    /** фон выделенной вкладки нижней навигации */
    val navActive: Color,
    /** фон листов (опрос, выбор) */
    val sheet: Color,
    /** тёплый текст предупреждений и лабораторных */
    val amberFg: Color,
    val greenFg: Color,
    val redFg: Color,
    /** тени: shXs / shSm (в тёмной теме shXs нет) */
    val shXs: MaShadow?,
    val shSm: List<MaShadow>,
    /** «материал» Android: плотная поверхность с кольцом и тенью */
    val matRing: Color,
    val matShadow: MaShadow,
    val navShadow: MaShadow,
)

/** CSS-подобная тень: offsetY, blur, spread в dp */
@Immutable
data class MaShadow(val color: Color, val offsetY: Float, val blur: Float, val spread: Float = 0f)

object MaBrand {
    /** основной синий — кнопки, выбранные дни, тумблеры (одинаков в обеих темах) */
    val primary = Color(0xFF1A4FDB)
    val gradientMid = Color(0xFF1D6FE0)
    val sky = Color(0xFF0EA5E9)
    val green = Color(0xFF10B981)
    val amber = Color(0xFFF59E0B)
    val red = Color(0xFFEF4444)
    val toastBg = Color(0xFF0F172A)
    val toastFg = Color(0xFFF1F5F9)
    val primaryShadow = MaShadow(Color(0x4D1A4FDB), 6f, 18f)
    val heroShadow = MaShadow(Color(0x731A4FDB), 12f, 32f, -8f)
    val stripShadow = MaShadow(Color(0x521A4FDB), 4f, 14f)
}

val LightMaColors = MaColors(
    isDark = false,
    bg = Color(0xFFF8FAFC),
    surface = Color(0xFFFFFFFF),
    soft2 = Color(0xFFF8FAFC),
    sunk = Color(0xFFF1F5F9),
    border = Color(0x140F172A),
    borderStrong = Color(0x240F172A),
    fg = Color(0xFF0F172A),
    muted = Color(0xFF475569),
    soft = Color(0xFF64748B),
    faint = Color(0xFF94A3B8),
    accent = Color(0xFF1A4FDB),
    accentSoft = Color(0x141A4FDB),
    accentSoft2 = Color(0x1F1A4FDB),
    accentBorder = Color(0x401A4FDB),
    sky = Color(0x1A0EA5E9),
    navActive = Color(0x171A4FDB),
    sheet = Color(0xFFF8FAFC),
    amberFg = Color(0xFFB45309),
    greenFg = Color(0xFF047857),
    redFg = Color(0xFFDC2626),
    shXs = MaShadow(Color(0x0A0F172A), 1f, 1f),
    shSm = listOf(MaShadow(Color(0x0F0F172A), 1f, 3f), MaShadow(Color(0x0A0F172A), 1f, 2f)),
    matRing = Color(0x120F172A),
    matShadow = MaShadow(Color(0x140F172A), 6f, 18f),
    navShadow = MaShadow(Color(0x1F0F172A), 10f, 30f),
)

val DarkMaColors = MaColors(
    isDark = true,
    bg = Color(0xFF0A0F1E),
    surface = Color(0xFF131A2E),
    soft2 = Color(0xFF0F1628),
    sunk = Color(0xFF0A1020),
    border = Color(0x1F94A3B8),
    borderStrong = Color(0x3894A3B8),
    fg = Color(0xFFF1F5F9),
    muted = Color(0xFFCBD5E1),
    soft = Color(0xFF94A3B8),
    faint = Color(0xFF64748B),
    accent = Color(0xFF60A5FA),
    accentSoft = Color(0x2460A5FA),
    accentSoft2 = Color(0x3360A5FA),
    accentBorder = Color(0x5960A5FA),
    sky = Color(0x1F0EA5E9),
    navActive = Color(0x2960A5FA),
    sheet = Color(0xFF0F1628),
    amberFg = Color(0xFFFBBF24),
    greenFg = Color(0xFF34D399),
    redFg = Color(0xFFF87171),
    shXs = null,
    shSm = listOf(MaShadow(Color(0x80000000), 1f, 3f)),
    matRing = Color(0x2994A3B8),
    matShadow = MaShadow(Color(0x73000000), 6f, 18f),
    navShadow = MaShadow(Color(0x80000000), 10f, 30f),
)

val LocalMaColors = staticCompositionLocalOf { LightMaColors }

object Ma {
    val colors: MaColors
        @Composable @ReadOnlyComposable get() = LocalMaColors.current
}

/**
 * Вид пары по строке типа из расписания: цвет полоски и чипа
 */
enum class LessonKind { LECTURE, PRACTICE, LAB, EXAM, OTHER;

    companion object {
        fun of(type: String?): LessonKind {
            val t = type.orEmpty().lowercase()
            return when {
                t.contains("лек") -> LECTURE
                t.contains("лаб") -> LAB
                t.contains("экз") || t.contains("зач") || t.contains("консул") -> EXAM
                t.contains("прак") || t.contains("семин") -> PRACTICE
                else -> OTHER
            }
        }
    }
}

@Immutable
data class LessonPalette(val stripe: Color, val chipBg: Color, val chipFg: Color)

fun MaColors.lessonPalette(kind: LessonKind): LessonPalette = when (kind) {
    LessonKind.LECTURE -> LessonPalette(accent, accentSoft, accent)
    LessonKind.PRACTICE -> LessonPalette(MaBrand.green, Color(0x1F10B981), greenFg)
    LessonKind.LAB -> LessonPalette(MaBrand.amber, Color(0x24F59E0B), amberFg)
    LessonKind.EXAM -> LessonPalette(MaBrand.red, Color(0x1FEF4444), redFg)
    LessonKind.OTHER -> LessonPalette(faint, sunk, muted)
}
