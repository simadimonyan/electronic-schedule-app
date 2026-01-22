package com.mycollege.schedule.feature.widgets.ui.components

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.LocalSize
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.background
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.width
import androidx.glance.text.Text
import androidx.glance.unit.ColorProvider
import com.mycollege.schedule.shared.ui.theme.buttons
import com.mycollege.schedule.shared.ui.theme.disabledBlue

@SuppressLint("RestrictedApi")
@Composable
fun GlanceProgressBar(percent: Float) {

//    val size = LocalSize.current
//    Log.i("PROGRESS", size.width.toString())
//
//    val baseSize = if (size.width > 300.dp) 300 else 200
//
//    val fill by remember { mutableIntStateOf(percent * baseSize / 100) }
//
//    Box {
//        Box(GlanceModifier.fillMaxWidth().background(disabledBlue).height(5.dp)) {
//            Text("")
//        }
//
//        Box(GlanceModifier.background(buttons).width(fill).height(5.dp)) {
//            Text("")
//        }
//    }

    LinearProgressIndicator(
        progress = percent,
        modifier = GlanceModifier.fillMaxWidth().height(4.dp),
        color = ColorProvider(buttons),
        backgroundColor = ColorProvider(disabledBlue)
    )

}