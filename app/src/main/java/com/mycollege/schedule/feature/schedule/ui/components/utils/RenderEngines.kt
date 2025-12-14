package com.mycollege.schedule.feature.schedule.ui.components.utils

import android.annotation.SuppressLint
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mycollege.schedule.app.activity.ui.state.AppState
import com.mycollege.schedule.feature.schedule.ui.components.schedule.ScheduleUnit
import com.mycollege.schedule.feature.schedule.ui.components.schedule.WeekendUnit
import com.mycollege.schedule.feature.schedule.ui.state.ScheduleEvent
import com.mycollege.schedule.feature.schedule.ui.state.ScheduleState
import com.mycollege.schedule.feature.settings.ui.state.SettingsState

@SuppressLint("MutableCollectionMutableState")
@Composable
fun WeekScheduleRender(
    appState: AppState,
    scheduleState: ScheduleState,
    settingsState: SettingsState,
    handleEvent: (ScheduleEvent) -> Unit
) {

    LaunchedEffect(settingsState.weekCount, settingsState.fullWeekVisibility) {
        handleEvent(ScheduleEvent.WeekCountChanged)
    }

    val darkMode = isSystemInDarkTheme()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 5.dp),
    ) {

        item {
            Spacer(modifier = Modifier.height(25.dp))
        }

        item {
            if (scheduleState.weekLessons.isEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(25.dp, 15.dp, 80.dp, 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = scheduleState.todayDate,
                        color = if (darkMode) Color.White else Color.Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                WeekendUnit()
            }
            else {
                scheduleState.weekLessons.keys.forEach { dayIndex ->

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(25.dp, 20.dp, 80.dp, 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        scheduleState.weekDates[dayIndex]?.let {
                            Text(
                                text = it,
                                color = if (darkMode) Color.White else Color.Black,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    scheduleState.weekLessons[dayIndex]?.let { lessons ->

                        if (lessons.isEmpty()) {
                            WeekendUnit()
                        }

                        lessons.sortedBy { it.count }.forEach { lesson ->
                            ScheduleUnit(lesson)
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(120.dp))
        }
    }
}

@Composable
fun TodayScheduleRender(
    appState: AppState,
    scheduleState: ScheduleState,
    settingsState: SettingsState,
    handleEvent: (ScheduleEvent) -> Unit
) {

    val darkMode = isSystemInDarkTheme()

    LaunchedEffect(settingsState.weekCount) {
        handleEvent(ScheduleEvent.WeekCountChanged)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(25.dp, 45.dp, 80.dp, 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = scheduleState.todayDate,
            color = if (darkMode) Color.White else Color.Black,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }

    if (scheduleState.todayLessons.isEmpty()) {
        WeekendUnit()
    }
    else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(scheduleState.todayLessons.sortedBy { it.count }) { _, lesson ->
                ScheduleUnit(lesson)
            }

            if (scheduleState.todayLessons.size > 3) {
                item {
                    Spacer(modifier = Modifier.height(120.dp))
                }
            }
        }
    }
}
