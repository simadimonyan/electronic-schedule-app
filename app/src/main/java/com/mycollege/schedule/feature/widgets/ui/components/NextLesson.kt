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
fun NextLesson(
    darkTheme: Boolean,
    studentMode: Boolean,
    nextLesson: DataClasses.Lesson?,
    tomorrowLessons: List<DataClasses.Lesson>?,
    previous: Boolean,
    context: Context
) {
    val homeMode = nextLesson == null
    val tomorrowLessonFlag = !previous && nextLesson == null
    val tomorrowLesson: DataClasses.Lesson? = if (tomorrowLessons?.isEmpty() == true) null else tomorrowLessons?.get(0)
    val scaleFactor = ResponsiveTextSize.getScaleFactor(context)

    if (tomorrowLessonFlag)
        TomorrowLesson(darkTheme, studentMode, tomorrowLesson, context)
    else {
        Column(GlanceModifier.fillMaxWidth().padding(start = -(2).dp, end = 10.dp)) {

            if (!homeMode) {
                Row(GlanceModifier.fillMaxWidth().padding(0), horizontalAlignment = Alignment.End) {
                    Text(
                        text = "СЛЕДУЮЩАЯ ПАРА",
                        style = TextStyle(
                            textAlign = TextAlign.End,
                            color = ColorProvider(if (darkTheme) disabledWhite else Color.DarkGray),
                            fontSize = (8 * scaleFactor).toInt().sp
                        ),
                        modifier = GlanceModifier.padding(0)
                    )
                }

                if (!previous) Spacer(GlanceModifier.height(5.dp))
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    provider = ImageProvider(R.drawable.circle_glow),
                    contentDescription = null,
                    modifier = GlanceModifier.size(28.dp)
                )

                Spacer(GlanceModifier.width(10.dp))

                Text(
                    text = nextLesson?.name ?:  "Пары закончились",
                    style = TextStyle(
                        fontSize = ((if (previous) 10 else 11) * scaleFactor).toInt().sp,
                        color = ColorProvider(if (darkTheme) Color.White else Color.Black),
                        fontWeight = FontWeight.Bold,
                    ),
                    modifier = GlanceModifier,
                    maxLines = 2
                )
            }

            //Spacer(GlanceModifier.height(2.dp))

            if (!homeMode) {
                Row(modifier = GlanceModifier.padding(start = 38.dp), verticalAlignment = Alignment.CenterVertically) {
                    val nextLabel = if (studentMode) (nextLesson as DataClasses.GroupLesson).teacher else (nextLesson as DataClasses.TeacherLesson).group

                    if (!nextLabel.equals("null")) {

                        Image(
                            provider = ImageProvider(R.drawable.person),
                            contentDescription = null,
                            modifier = GlanceModifier.size(if (previous) 12.dp else 13.dp),
                            colorFilter = ColorFilter.tint(ColorProvider(buttons))
                        )

                        Spacer(GlanceModifier.width(5.dp))

                        Text(
                            text = "$nextLabel",
                            style = TextStyle(
                                color = ColorProvider(if (darkTheme) disabledWhite else Color.DarkGray),
                                fontSize = ((if (previous) 9 else 10) * scaleFactor).toInt().sp
                            ),
                            maxLines = 1,
                            modifier = GlanceModifier
                        )

                    }
                }
            }

            Row(modifier = GlanceModifier.padding(start = 38.dp), horizontalAlignment = Alignment.Companion.CenterHorizontally, verticalAlignment = Alignment.CenterVertically) {

                if (!homeMode) {
                    Image(
                        provider = ImageProvider(R.drawable.time),
                        contentDescription = null,
                        modifier = GlanceModifier.size(if (previous) 12.dp else 13.dp),
                        colorFilter = ColorFilter.tint(ColorProvider(buttons))
                    )

                    Spacer(GlanceModifier.width(5.dp))
                }

                Text(
                    text = nextLesson?.time ?: "Можно идти домой 🏡",
                    style = TextStyle(
                        color = ColorProvider(if (darkTheme) disabledWhite else Color.DarkGray),
                        fontSize = ((if (previous) 9 else 10) * scaleFactor).toInt().sp
                    ),
                    modifier = GlanceModifier
                )

                if (!homeMode) {
                    Spacer(GlanceModifier.width(25.dp))

                    Image(
                        provider = ImageProvider(R.drawable.auditory_label),
                        contentDescription = null,
                        modifier = GlanceModifier.size(if (previous) 12.dp else 13.dp),
                        colorFilter = ColorFilter.tint(ColorProvider(buttons))
                    )

                    Spacer(GlanceModifier.width(5.dp))

                    Text(
                        text = nextLesson.location.toString(),
                        style = TextStyle(
                            color = ColorProvider(if (darkTheme) disabledWhite else Color.DarkGray),
                            fontSize = ((if (previous) 9 else 10) * scaleFactor).toInt().sp
                        ),
                        modifier = GlanceModifier
                    )
                }
            }
        }
    }

}