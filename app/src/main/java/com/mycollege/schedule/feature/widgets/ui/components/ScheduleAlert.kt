package com.mycollege.schedule.feature.widgets.ui.components

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ExperimentalGlanceApi
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.mycollege.schedule.R
import com.mycollege.schedule.shared.utils.ResponsiveTextSize
import com.mycollege.schedule.shared.ui.theme.background
import com.mycollege.schedule.shared.ui.theme.backgroundDark

@SuppressLint("RestrictedApi")
@OptIn(ExperimentalGlanceApi::class)
@Composable
fun ScheduleAlert(darkTheme: Boolean, context: Context) {
    val scaleFactor = ResponsiveTextSize.getScaleFactor(context)
    Column(
        GlanceModifier.fillMaxSize().background(if (darkTheme) backgroundDark else background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            provider = ImageProvider(R.drawable.schedule_warning),
            contentDescription = null,
            modifier = GlanceModifier.size(50.dp)
        )
        Text(
            text = "Нет расписания",
            style = TextStyle(
                fontSize = (14 * scaleFactor).toInt().sp
            ),
            modifier = GlanceModifier.Companion.padding(12.dp)
        )
    }
}