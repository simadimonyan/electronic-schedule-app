package com.mycollege.schedule.feature.widgets.ui.components

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ExperimentalGlanceApi
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.mycollege.schedule.R
import com.mycollege.schedule.app.activity.ui.MainActivity
import com.mycollege.schedule.shared.ui.theme.background
import com.mycollege.schedule.shared.ui.theme.backgroundDark

@OptIn(ExperimentalGlanceApi::class)
@Composable
fun ScheduleAlert(darkTheme: Boolean) {
    Column(
        GlanceModifier.fillMaxSize().background(if (darkTheme) backgroundDark else background).clickable(onClick = actionStartActivity<MainActivity>(
            activityOptions = Bundle().apply {
                putInt("android.activity.splashScreenStyle", 1)
            })
        ),
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
                fontSize = 18.sp,
                color = ColorProvider(if (darkTheme) Color.White else Color.Black)
            ),
            modifier = GlanceModifier.Companion.padding(12.dp)
        )
    }
}