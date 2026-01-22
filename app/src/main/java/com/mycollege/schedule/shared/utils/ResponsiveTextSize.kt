package com.mycollege.schedule.shared.utils

import android.content.Context
import android.util.DisplayMetrics
import androidx.compose.ui.unit.sp
import kotlin.math.cbrt

/**
 * Вспомогательный класс для расчёта адаптивных размеров шрифта
 * на основе плотности пикселей устройства (DPI)
 */
object ResponsiveTextSize {
    
    /**
     * Получает плотность пикселей устройства (DPI)
     * @param context контекст приложения
     * @return плотность в dpi (ldpi=120, mdpi=160, hdpi=240, xhdpi=320, xxhdpi=480, xxxhdpi=640)
     */
    fun getDeviceDensity(context: Context): Float {
        return context.resources.displayMetrics.densityDpi.toFloat()
    }
    
    /**
     * Расчитывает адаптивный размер шрифта
     * @param baseSizeSp базовый размер в SP (для mdpi/160 dpi)
     * @param context контекст приложения
     * @return адаптивный размер в SP
     */
    fun calculateAdaptiveTextSize(baseSizeSp: Int, context: Context): Int {
        val density = getDeviceDensity(context)
        // mdpi имеет densityDpi = 160
        // Рассчитываем коэффициент: реальная плотность / базовая плотность
        val scaleFactor = density / DisplayMetrics.DENSITY_DEFAULT.toFloat()
        return (baseSizeSp * scaleFactor).toInt()
    }
    
    /**
     * Коэффициент масштабирования для текущего устройства
     * Использует кубический корень для очень мягкого масштабирования
     * @param context контекст приложения
     * @return коэффициент (< 1 для низкой плотности, > 1 для высокой)
     */
    fun getScaleFactor(context: Context): Float {
        val density = getDeviceDensity(context)
        // Используем cbrt (кубический корень) для минимального масштабирования
        // Линейное: 480 DPI -> 3.0 (слишком большой текст)
        // С sqrt: 480 DPI -> 1.73 (все еще большой)
        // С cbrt: 480 DPI -> 1.29 (оптимально)
        val baseScaleFactor = density / DisplayMetrics.DENSITY_DEFAULT.toFloat()
        return cbrt(baseScaleFactor)
    }

}
