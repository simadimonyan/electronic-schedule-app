package com.mycollege.schedule.feature.widgets.ui.components

import androidx.compose.ui.graphics.Color
import androidx.glance.unit.ColorProvider
import com.mycollege.schedule.R

/**
 * Палитра виджета из макета Widget.dc.html (LOOK light / dark).
 * Glance не видит LocalMaColors приложения, поэтому цвета и drawable заданы здесь.
 */
data class WidgetPalette(
    val fg: ColorProvider,
    val soft: ColorProvider,
    val faint: ColorProvider,
    val accent: ColorProvider,
    val background: Int,
    val rail: Int,
    val bar: Int,
    val track: Int,
    val nodeLive: Int,
    val nodeNext: Int,
    val nodePast: Int,
) {
    companion object {
        val Light = WidgetPalette(
            fg = ColorProvider(Color(0xFF0F172A)),
            soft = ColorProvider(Color(0xFF64748B)),
            faint = ColorProvider(Color(0xFF94A3B8)),
            accent = ColorProvider(Color(0xFF1A4FDB)),
            background = R.drawable.widget_bg_light,
            rail = R.drawable.widget_rail_light,
            bar = R.drawable.widget_bar_light,
            track = R.drawable.widget_track_light,
            nodeLive = R.drawable.widget_node_live_light,
            nodeNext = R.drawable.widget_node_next_light,
            nodePast = R.drawable.widget_node_past_light,
        )
        val Dark = WidgetPalette(
            fg = ColorProvider(Color(0xFFF1F5F9)),
            soft = ColorProvider(Color(0xFFA3AEC2)),
            faint = ColorProvider(Color(0xFF6B7891)),
            accent = ColorProvider(Color(0xFF60A5FA)),
            background = R.drawable.widget_bg_dark,
            rail = R.drawable.widget_rail_dark,
            bar = R.drawable.widget_bar_dark,
            track = R.drawable.widget_track_dark,
            nodeLive = R.drawable.widget_node_live_dark,
            nodeNext = R.drawable.widget_node_next_dark,
            nodePast = R.drawable.widget_node_past_dark,
        )

        fun of(darkTheme: Boolean) = if (darkTheme) Dark else Light
    }
}

/** Узел на направляющей: идёт сейчас / следующая / прошло (нет пар) */
enum class WidgetNode(val sizeDp: Float) { LIVE(19f), NEXT(11f), PAST(7f) }

fun WidgetPalette.nodeRes(node: WidgetNode) = when (node) {
    WidgetNode.LIVE -> nodeLive
    WidgetNode.NEXT -> nodeNext
    WidgetNode.PAST -> nodePast
}
