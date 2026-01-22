package com.mycollege.schedule.feature.widgets.ui.components

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ExperimentalGlanceApi
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.mycollege.schedule.R
import com.mycollege.schedule.shared.utils.ResponsiveTextSize
import com.mycollege.schedule.shared.ui.theme.background
import com.mycollege.schedule.shared.ui.theme.backgroundDark
import com.mycollege.schedule.shared.ui.theme.buttons
import com.mycollege.schedule.shared.ui.theme.disabledWhite

@SuppressLint("RestrictedApi")
@OptIn(ExperimentalGlanceApi::class)
@Composable
fun Weekend(darkTheme: Boolean, context: Context) {
    val scaleFactor = ResponsiveTextSize.getScaleFactor(context)

    Row(
        modifier = GlanceModifier.fillMaxSize().background(if (darkTheme) backgroundDark else background),
        verticalAlignment = Alignment.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(GlanceModifier.width(5.dp))

        Box(GlanceModifier.fillMaxSize()) {

            Column(
                modifier = GlanceModifier.fillMaxHeight().width(25.dp),
                verticalAlignment = Alignment.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(GlanceModifier.fillMaxHeight().background(buttons).width(5.dp)) {
                    Text(text = " ", modifier = GlanceModifier)
                }
            }

            Column(GlanceModifier.fillMaxSize().padding(start = -(2).dp, top = 0.dp, end = 10.dp), verticalAlignment = Alignment.CenterVertically) {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        provider = ImageProvider(R.drawable.circle_glow),
                        contentDescription = null,
                        modifier = GlanceModifier.size(28.dp)
                    )

                    Spacer(GlanceModifier.width(10.dp))

                    Text(
                        text = "Выходной",
                        style = TextStyle(
                            fontSize = (11 * scaleFactor).toInt().sp,
                            color = ColorProvider(if (darkTheme) Color.White else Color.Black),
                            fontWeight = FontWeight.Bold,
                        ),
                        modifier = GlanceModifier,
                        maxLines = 2
                    )
                }

                Row(modifier = GlanceModifier.padding(start = 38.dp), horizontalAlignment = Alignment.Companion.CenterHorizontally, verticalAlignment = Alignment.CenterVertically) {

                    Text(
                        text = "Сегодня можно отдыхать 🏖️",
                        style = TextStyle(
                            color = ColorProvider(if (darkTheme) disabledWhite else Color.DarkGray),
                            fontSize = (10 * scaleFactor).toInt().sp,
                        ),
                        modifier = GlanceModifier
                    )

                }
            }

        }

    }

}