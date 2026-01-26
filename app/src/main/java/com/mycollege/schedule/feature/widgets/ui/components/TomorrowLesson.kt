package com.mycollege.schedule.feature.widgets.ui.components

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.mycollege.schedule.R
import com.mycollege.schedule.feature.schedule.data.models.DataClasses
import com.mycollege.schedule.shared.utils.ResponsiveTextSize
import com.mycollege.schedule.shared.ui.theme.buttons
import com.mycollege.schedule.shared.ui.theme.disabledWhite

@SuppressLint("RestrictedApi")
@Composable
fun TomorrowLesson(
    darkTheme: Boolean,
    studentMode: Boolean,
    tomorrowLesson: DataClasses.Lesson?,
    context: Context
) {

    val weekendFlag = tomorrowLesson == null
    val scaleFactor = ResponsiveTextSize.getScaleFactor(context)

    Column(GlanceModifier.fillMaxSize().padding(start = -(2).dp, end = 10.dp), verticalAlignment = Alignment.CenterVertically) {

        if (!weekendFlag) {
            Row(GlanceModifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
                Text(
                    text = "СЛЕДУЮЩАЯ ПАРА",
                    style = TextStyle(
                        textAlign = TextAlign.End,
                        color = ColorProvider(if (darkTheme) disabledWhite else Color.DarkGray),
                        fontSize = (8 * scaleFactor).toInt().sp),
                    modifier = GlanceModifier
                )
            }

            Row(GlanceModifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
                Text(
                    text = "ЗАВТРА",
                    style = TextStyle(
                        textAlign = TextAlign.End,
                        color = ColorProvider(buttons),
                        fontSize = (8 * scaleFactor).toInt().sp,
                    ),
                    modifier = GlanceModifier
                )
            }

            Spacer(GlanceModifier.height(5.dp))
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                provider = ImageProvider(R.drawable.circle_glow),
                contentDescription = null,
                modifier = GlanceModifier.size(28.dp)
            )

            Spacer(GlanceModifier.width(10.dp))

            Text(
                text = tomorrowLesson?.name ?: "Завтра нет пар",
                style = TextStyle(
                    fontSize = (10 * scaleFactor).toInt().sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorProvider(if (darkTheme) Color.White else Color.Black)
                ),
                modifier = GlanceModifier,
                maxLines = 2
            )
        }

        if (!weekendFlag) {
            Row(modifier = GlanceModifier.padding(start = 38.dp), verticalAlignment = Alignment.CenterVertically) {
                val nextLabel = if (studentMode) (tomorrowLesson as DataClasses.GroupLesson).teacher else (tomorrowLesson as DataClasses.TeacherLesson).group

                if (!nextLabel.equals("null")) {

                    Image(
                        provider = ImageProvider(R.drawable.person),
                        contentDescription = null,
                        modifier = GlanceModifier.size(14.dp),
                        colorFilter = ColorFilter.tint(ColorProvider(buttons))
                    )

                    Spacer(GlanceModifier.width(5.dp))

                    Text(
                        text = "$nextLabel",
                        style = TextStyle(
                            color = ColorProvider(if (darkTheme) disabledWhite else Color.DarkGray),
                            fontSize = (9 * scaleFactor).toInt().sp
                        ),
                        maxLines = 1,
                        modifier = GlanceModifier
                    )

                }
            }
        }

        Row(modifier = GlanceModifier.padding(start = 38.dp), horizontalAlignment = Alignment.Companion.CenterHorizontally, verticalAlignment = Alignment.CenterVertically) {

            if (!weekendFlag) {
                Image(
                    provider = ImageProvider(R.drawable.time),
                    contentDescription = null,
                    modifier = GlanceModifier.size(14.dp),
                    colorFilter = ColorFilter.tint(ColorProvider(buttons))
                )

                Spacer(GlanceModifier.width(5.dp))
            }

            Text(
                text = tomorrowLesson?.time ?: "Можно выспаться и отдохнуть 🏖️",
                style = TextStyle(
                    color = ColorProvider(if (darkTheme) disabledWhite else Color.DarkGray),
                    fontSize = (9 * scaleFactor).toInt().sp
                ),
                modifier = GlanceModifier
            )

            if (!weekendFlag) {
                Spacer(GlanceModifier.width(25.dp))

                Image(
                    provider = ImageProvider(R.drawable.auditory_label),
                    contentDescription = null,
                    modifier = GlanceModifier.size(14.dp),
                    colorFilter = ColorFilter.tint(ColorProvider(buttons))
                )

                Spacer(GlanceModifier.width(5.dp))

                Text(
                    text = tomorrowLesson.location.toString(),
                    style = TextStyle(
                        color = ColorProvider(if (darkTheme) disabledWhite else Color.DarkGray),
                        fontSize = (9 * scaleFactor).toInt().sp
                    ),
                    modifier = GlanceModifier
                )
            }
        }
    }

}