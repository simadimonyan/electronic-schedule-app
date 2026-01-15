package com.mycollege.schedule.feature.widgets.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
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
import com.mycollege.schedule.shared.ui.theme.buttons
import com.mycollege.schedule.shared.ui.theme.disabledWhite
import java.time.Duration
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun CurrentLesson(
    darkTheme: Boolean,
    studentMode: Boolean,
    currentLesson: DataClasses.Lesson
) {

    var currentLessonProgress by remember { mutableIntStateOf(0) }

    Column(GlanceModifier.fillMaxWidth().padding(start = -(2).dp, top = 0.dp, end = 20.dp)) {

        Row(GlanceModifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
            Text(
                text = "СЕЙЧАС ИДЕТ",
                style = TextStyle(
                    textAlign = TextAlign.End,
                    color = ColorProvider(if (darkTheme) disabledWhite else Color.DarkGray),
                    fontSize = 12.sp
                ),
                modifier = GlanceModifier
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                provider = ImageProvider(R.drawable.circle_glow),
                contentDescription = null,
                modifier = GlanceModifier.size(28.dp)
            )

            Spacer(GlanceModifier.width(10.dp))

            Text(
                text = "${currentLesson.name}",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorProvider(if (darkTheme) Color.White else Color.Black)
                ),
                modifier = GlanceModifier,
                maxLines = 2
            )
        }

        Spacer(GlanceModifier.height(2.dp))

        Row(
            modifier = GlanceModifier.padding(start = 38.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val firstLabel = if (studentMode) (currentLesson as DataClasses.GroupLesson).teacher else (currentLesson as DataClasses.TeacherLesson).group

            if (!firstLabel.equals("null")) {

                Image(
                    provider = ImageProvider(R.drawable.person),
                    contentDescription = null,
                    modifier = GlanceModifier.size(15.dp),
                    colorFilter = ColorFilter.tint(ColorProvider(buttons))
                )

                Spacer(GlanceModifier.width(5.dp))

                Text(
                    text = "$firstLabel",
                    style = TextStyle(
                        color = ColorProvider(if (darkTheme) disabledWhite else Color.DarkGray),
                        fontSize = 13.sp
                    ),
                    maxLines = 1,
                    modifier = GlanceModifier
                )

            }
        }

        Row(
            modifier = GlanceModifier.padding(start = 38.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Image(
                provider = ImageProvider(R.drawable.time),
                contentDescription = null,
                modifier = GlanceModifier.size(15.dp),
                colorFilter = ColorFilter.tint(ColorProvider(buttons))
            )

            Spacer(GlanceModifier.width(5.dp))

            Text(
                text = currentLesson.time,
                style = TextStyle(
                    color = ColorProvider(if (darkTheme) disabledWhite else Color.DarkGray),
                    fontSize = 13.sp
                ),
                modifier = GlanceModifier
            )

            Spacer(GlanceModifier.width(25.dp))

            Image(
                provider = ImageProvider(R.drawable.auditory_label),
                contentDescription = null,
                modifier = GlanceModifier.size(15.dp),
                colorFilter = ColorFilter.tint(ColorProvider(buttons))
            )

            Spacer(GlanceModifier.width(5.dp))

            Text(
                text = currentLesson.location.toString(),
                style = TextStyle(
                    color = ColorProvider(if (darkTheme) disabledWhite else Color.DarkGray),
                    fontSize = 13.sp
                ),
                modifier = GlanceModifier
            )
        }

        Box(GlanceModifier.fillMaxWidth().padding(start = 38.dp)) {

            val formatter = DateTimeFormatter.ofPattern("HH:mm")
            val time = currentLesson.time.split("-")
            val startTime = LocalTime.parse(time[0], formatter)
            val endTime = LocalTime.parse(time[1], formatter)

            val totalMinutes = Duration.between(startTime, endTime).toMinutes()
            val passedMinutes = Duration.between(startTime, LocalTime.now()).toMinutes()
            currentLessonProgress = (passedMinutes * 100 / totalMinutes).toInt()

            Column {
                Spacer(GlanceModifier.height(4.dp))
                GlanceProgressBar(currentLessonProgress)
                Spacer(GlanceModifier.height(2.dp))
                Text(
                    text = "$currentLessonProgress%",
                    style = TextStyle(
                        color = ColorProvider(if (darkTheme) disabledWhite else Color.DarkGray),
                        fontSize = 14.sp
                    ),
                    modifier = GlanceModifier
                )
            }
        }
    }

}