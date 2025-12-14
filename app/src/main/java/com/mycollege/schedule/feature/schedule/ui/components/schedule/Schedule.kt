package com.mycollege.schedule.feature.schedule.ui.components.schedule

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mycollege.schedule.R
import com.mycollege.schedule.feature.schedule.data.models.DataClasses
import com.mycollege.schedule.shared.ui.theme.buttons
import com.mycollege.schedule.shared.ui.theme.secondaryDark

@Preview
@Composable
fun ScheduleUnitPreview() {
    ScheduleUnit(DataClasses.GroupLesson(
        1,
        "08:00-09:30",
        "Практика",
        "Тестовая пара",
        "Тестовый преподаватель",
        "ссылка",
        "123"
    ))
}

@Composable
fun ScheduleUnit(lesson: DataClasses.Lesson) {

    val darkMode = isSystemInDarkTheme()

    Card(
        modifier = Modifier
            .padding(20.dp, 0.dp, 20.dp, 7.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = buttons)
    ) {
        Card(
            modifier = Modifier
                .padding(4.dp, 0.dp, 0.dp, 0.dp)
                .border(BorderStroke(2.dp, if (darkMode) secondaryDark else Color.White)),
            shape = RoundedCornerShape(0.dp),
            colors = CardDefaults.cardColors(containerColor = if (darkMode) secondaryDark else Color.White)
        ) {
            ScheduleUnitContent(lesson)
        }
    }

}

@Composable
private fun ScheduleUnitContent(lesson: DataClasses.Lesson) {

    val studentMode = lesson is DataClasses.GroupLesson
    val darkMode = isSystemInDarkTheme()

    Column(modifier = Modifier.padding(20.dp, 5.dp, 20.dp, 5.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "№${lesson.count}",
                color = if (darkMode) Color.White else Color.Black,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = lesson.type,
                color = if (darkMode) Color.White else Color.Black,
                fontSize = 15.sp,
                fontStyle = FontStyle.Italic
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = lesson.time,
                color = if (darkMode) Color.White else Color.Black,
                fontSize = 15.sp,
                textAlign = TextAlign.End
            )
        }
        Text(
            text =  (if (lesson.name.equals("null")) "Неизвестная дисциплина" else lesson.name).toString(),
            color = if (darkMode) Color.White else Color.Black,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text =  if (studentMode) (if (lesson.teacher.equals("null")) "" else lesson.teacher).toString() else (if ((lesson as DataClasses.TeacherLesson).group.equals("null")) "" else lesson.group).toString(),
            color = if (darkMode) Color.White else Color.Black,
            fontSize = 15.sp,
            fontStyle = FontStyle.Italic
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = (if (lesson.location.equals("null")) "Неизвестная аудитория" else lesson.location).toString(),
                modifier = Modifier.padding(top = 10.dp),
                color = if (darkMode) Color.White else Color.Black,
                fontSize = 15.sp,
                fontStyle = FontStyle.Italic
            )

            if (lesson.eios != null && lesson.eios!!.isNotBlank()) {

                val context = LocalContext.current
                val clipboard = LocalClipboardManager.current

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        clipboard.setText(AnnotatedString(lesson.eios.toString()))
                        Toast.makeText(context, "Ссылка скопирована", Toast.LENGTH_SHORT).show()
                    },
                    //modifier = Modifier.size(120.dp, 35.dp),
                    modifier = Modifier.height(32.dp),//.padding(horizontal = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = buttons),
                    contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {

                    Icon(
                        painter = painterResource(R.drawable.copy),
                        tint = Color.White,
                        contentDescription = "copy",
                        modifier = Modifier.size(14.dp)
                    )

                    Spacer(modifier = Modifier.width(5.dp))

                    Text(
                        text = "eios",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontStyle = FontStyle.Italic
                    )
                }

            }
        }
    }
}