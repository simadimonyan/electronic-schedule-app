package com.mycollege.schedule.shared.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.mycollege.schedule.R

/**
 * Шрифты дизайна (OFL): Inter — текст, Plus Jakarta Sans — заголовки, JetBrains Mono — время/аудитории.
 * В Plus Jakarta Sans нет кириллицы (в макетах русский текст заголовков падает на системный шрифт),
 * поэтому заголовки с кириллицей набираются Inter тех же начертаний — см. [MaType.displayFor].
 */
object MaFonts {
    val inter = FontFamily(
        Font(R.font.inter_regular, FontWeight.Normal),
        Font(R.font.inter_medium, FontWeight.Medium),
        Font(R.font.inter_semibold, FontWeight.SemiBold),
        Font(R.font.inter_bold, FontWeight.Bold),
        Font(R.font.inter_extrabold, FontWeight.ExtraBold),
    )
    val jakarta = FontFamily(
        Font(R.font.plus_jakarta_sans_medium, FontWeight.Medium),
        Font(R.font.plus_jakarta_sans_semibold, FontWeight.SemiBold),
        Font(R.font.plus_jakarta_sans_bold, FontWeight.Bold),
        Font(R.font.plus_jakarta_sans_extrabold, FontWeight.ExtraBold),
    )
    val mono = FontFamily(
        Font(R.font.jetbrains_mono_regular, FontWeight.Normal),
        Font(R.font.jetbrains_mono_medium, FontWeight.Medium),
        Font(R.font.jetbrains_mono_semibold, FontWeight.SemiBold),
        Font(R.font.jetbrains_mono_bold, FontWeight.Bold),
    )
}

object MaType {

    private val cyrillic = Regex("[\\u0400-\\u04FF]")

    /** класс .d: заголовочный шрифт, трекинг -0.025em */
    fun display(size: TextUnit, weight: FontWeight = FontWeight.ExtraBold, lineHeight: TextUnit = TextUnit.Unspecified, tracking: Float = -0.025f) =
        TextStyle(fontFamily = MaFonts.inter, fontSize = size, fontWeight = weight, lineHeight = lineHeight, letterSpacing = tracking.em)

    /** заголовок с подбором шрифта: латиница и цифры — Plus Jakarta Sans, кириллица — Inter */
    fun displayFor(text: String, size: TextUnit, weight: FontWeight = FontWeight.ExtraBold, lineHeight: TextUnit = TextUnit.Unspecified, tracking: Float = -0.025f) =
        display(size, weight, lineHeight, tracking).copy(fontFamily = if (cyrillic.containsMatchIn(text)) MaFonts.inter else MaFonts.jakarta)

    /** класс .m: моноширинный, трекинг 0 */
    fun mono(size: TextUnit, weight: FontWeight = FontWeight.Medium) =
        TextStyle(fontFamily = MaFonts.mono, fontSize = size, fontWeight = weight, letterSpacing = 0.sp)

    fun body(size: TextUnit, weight: FontWeight = FontWeight.Normal, lineHeight: TextUnit = TextUnit.Unspecified) =
        TextStyle(fontFamily = MaFonts.inter, fontSize = size, fontWeight = weight, lineHeight = lineHeight)

    /** класс .eb: надзаголовок капслоком, трекинг .09em */
    fun eyebrow(size: TextUnit = 11.sp, tracking: Float = 0.09f) =
        TextStyle(fontFamily = MaFonts.inter, fontSize = size, fontWeight = FontWeight.Bold, letterSpacing = tracking.em)
}

val Typography = Typography(
    bodyLarge = MaType.body(15.sp, lineHeight = 22.sp),
    bodyMedium = MaType.body(14.sp, lineHeight = 20.sp),
    bodySmall = MaType.body(12.sp, lineHeight = 16.sp),
    titleLarge = MaType.display(22.sp),
    titleMedium = MaType.body(16.sp, FontWeight.SemiBold),
    labelLarge = MaType.body(14.sp, FontWeight.SemiBold),
)
