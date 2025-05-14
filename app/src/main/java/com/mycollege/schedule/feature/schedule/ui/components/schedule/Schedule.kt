package com.mycollege.schedule.feature.schedule.ui.components.schedule

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mycollege.schedule.feature.schedule.data.models.DataClasses
import com.mycollege.schedule.shared.ui.theme.buttons

@Composable
fun ScheduleUnit(lesson: DataClasses.Lesson) {
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
                .border(BorderStroke(2.dp, Color.White)),
            shape = RoundedCornerShape(0.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            ScheduleUnitContent(lesson)
        }
    }
}

@Composable
private fun ScheduleUnitContent(lesson: DataClasses.Lesson) {
    Column(modifier = Modifier.padding(20.dp, 5.dp, 20.dp, 5.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "№${lesson.count}",
                color = Color.Black,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = lesson.type,
                color = Color.Black,
                fontSize = 15.sp,
                fontStyle = FontStyle.Italic
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = lesson.time,
                color = Color.Black,
                fontSize = 15.sp,
                textAlign = TextAlign.End
            )
        }
        Text(
            text =  (if (lesson.name.equals("null")) "Неизвестная дисциплина" else lesson.name).toString(),
            color = Color.Black,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text =  (if (lesson.teacher.equals("null")) "" else lesson.teacher).toString(),
            color = Color.Black,
            fontSize = 15.sp,
            fontStyle = FontStyle.Italic
        )
        Text(
            text = (if (lesson.location.equals("null")) "Неизвестная аудитория" else lesson.location).toString(),
            modifier = Modifier.padding(top = 10.dp),
            color = Color.Black,
            fontSize = 15.sp,
            fontStyle = FontStyle.Italic
        )
    }
}