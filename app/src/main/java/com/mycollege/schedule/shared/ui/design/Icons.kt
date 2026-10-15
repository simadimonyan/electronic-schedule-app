package com.mycollege.schedule.shared.ui.design

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
import androidx.compose.ui.graphics.ColorFilter

/**
 * Контурные значки из макетов (viewBox 24, stroke-linecap/join round)
 */
object MaIcons {
    const val today = "M7 3v3M17 3v3M4 9h16M5.5 5h13A1.5 1.5 0 0 1 20 6.5v12a1.5 1.5 0 0 1-1.5 1.5h-13A1.5 1.5 0 0 1 4 18.5v-12A1.5 1.5 0 0 1 5.5 5z"
    const val week = "M4 5h16v14H4zM4 10h16M4 15h16M9.5 5v14M15 5v14"
    const val settings = "M4 7h9M17 7h3M4 17h3M11 17h9M15 5v4M9 15v4"
    const val search = "M11 18a7 7 0 1 0 0-14 7 7 0 0 0 0 14zM20 20l-4-4"
    const val chevDown = "M6 9l6 6 6-6"
    const val chevRight = "M9 6l6 6-6 6"
    const val back = "M19 12H5M11 6l-6 6 6 6"
    const val close = "M6 6l12 12M18 6 6 18"
    const val link = "M10 14a4 4 0 0 0 5.66 0l3-3a4 4 0 0 0-5.66-5.66l-1 1M14 10a4 4 0 0 0-5.66 0l-3 3a4 4 0 0 0 5.66 5.66l1-1"
    const val user = "M20 21a8 8 0 0 0-16 0M12 13a5 5 0 1 0 0-10 5 5 0 0 0 0 10z"
    const val users = "M16 20v-1.5a3.5 3.5 0 0 0-3.5-3.5h-5A3.5 3.5 0 0 0 4 18.5V20M10 11a3.5 3.5 0 1 0 0-7 3.5 3.5 0 0 0 0 7zM20 20v-1.5a3.5 3.5 0 0 0-2.5-3.35M15.5 4.15a3.5 3.5 0 0 1 0 6.7"
    const val cloudOff = "M3 3l18 18M8.5 6.3A6 6 0 0 1 16.1 10.2 4.5 4.5 0 0 1 20.3 16.6M17.5 19H7a3.5 3.5 0 0 1-2.5-6A6 6 0 0 1 5.8 9.9"
    const val moon = "M20 14.5A8 8 0 0 1 9.5 4 8 8 0 1 0 20 14.5z"
    const val cap = "M22 10 12 5 2 10l10 5 10-5zM6 12v5c3 2 9 2 12 0v-5"
    const val check = "M5 12.5l4.5 4.5L19 7"
    const val refresh = "M20 11a8 8 0 0 0-14.9-4M4 4v3h3M4 13a8 8 0 0 0 14.9 4M20 20v-3h-3"
    const val calendar = today
    const val grid = week
    const val bell = "M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9M10.3 21a1.94 1.94 0 0 0 3.4 0"
    const val clock = "M12 21a9 9 0 1 0 0-18 9 9 0 0 0 0 18zM12 7v5l3 2"
    const val widget = "M4 4h6v6H4zM14 4h6v6h-6zM4 14h16v6H4z"
    const val info = "M12 21a9 9 0 1 0 0-18 9 9 0 0 0 0 18zM12 11v5M12 8h.01"
    const val send = "M22 2 11 13M22 2l-7 20-4-9-9-4z"
    const val nav = "M4 15h16v5H4zM8 17.5h.01M12 17.5h.01M16 17.5h.01M4 4h16v7H4z"
    const val message = "M21 15a2 2 0 0 1-2 2H8l-5 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"
    const val shield = "M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"
    const val doc = "M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8zM14 2v6h6M8 13h8M8 17h5"
    const val copyright = "M12 21a9 9 0 1 0 0-18 9 9 0 0 0 0 18zM14.8 9.6a3.5 3.5 0 1 0 0 4.8"
    const val external = "M15 3h6v6M10 14 21 3M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6"
    const val mail = "M4 6h16v12H4zM4 7l8 6 8-6"
    const val star = "M12 3l2.5 5 5.5.8-4 3.9.9 5.5L12 15.6 7.1 18.2 8 12.7 4 8.8 9.5 8z"
    const val cloudCheck = "M17.5 19a4.5 4.5 0 1 0-1.4-8.78A6 6 0 0 0 4.5 13 3.5 3.5 0 0 0 7 19zM9.5 14.5l2 2 3.5-4"
    const val arrowRight = "M5 12h14M13 6l6 6-6 6"
    const val wifi = "M5 12.5a10 10 0 0 1 14 0M8.5 16a5 5 0 0 1 7 0M12 19.5h.01M2 9a15 15 0 0 1 20 0"
    const val trash = "M3 6h18M8 6V4h8v2M6 6l1 14h10l1-14"
    const val globe = "M12 21a9 9 0 1 0 0-18 9 9 0 0 0 0 18zM3 12h18M12 3a14 14 0 0 1 0 18M12 3a14 14 0 0 0 0 18"
}

@Composable
fun rememberMaIcon(path: String, strokeWidth: Float = 2f): ImageVector = remember(path, strokeWidth) {
    ImageVector.Builder(defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f)
        .addPath(
            pathData = addPathNodes(path),
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = strokeWidth,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        )
        .build()
}

/** Контурный значок; цвет задаётся tint */
@Composable
fun MaIcon(path: String, tint: Color, size: Dp = 20.dp, strokeWidth: Float = 2f, modifier: Modifier = Modifier) {
    Image(
        painter = rememberVectorPainter(rememberMaIcon(path, strokeWidth)),
        contentDescription = null,
        colorFilter = ColorFilter.tint(tint),
        modifier = modifier.size(size)
    )
}
