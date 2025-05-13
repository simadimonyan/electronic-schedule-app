package com.mycollege.schedule.feature.schedule.ui

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.mycollege.schedule.R
import com.mycollege.schedule.app.activity.data.models.Schedule
import com.mycollege.schedule.app.activity.data.models.Teacher
import com.mycollege.schedule.app.navigation.Settings
import com.mycollege.schedule.feature.schedule.data.models.DataClasses
import com.mycollege.schedule.shared.ui.theme.ScheduleTheme
import com.mycollege.schedule.shared.ui.theme.background
import com.mycollege.schedule.shared.ui.theme.buttons
import com.mycollege.schedule.feature.schedule.ui.state.ScheduleViewModel

@Composable
fun ScheduleScreen(
    viewModel: ScheduleViewModel = hiltViewModel(),
    globalGraph: NavHostController
) {

    val settingsState by viewModel.settingsStateHolder.settingsState.collectAsState()
    val scheduleState by viewModel.scheduleStateHolder.scheduleState.collectAsState()
    val parseState by viewModel.parserStateHolder.groupParserState.collectAsState()

    ScheduleTheme {
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(top = 20.dp)) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(background)
            ) {
                if (parseState.loading) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(25.dp, 45.dp, 80.dp, 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = scheduleState.todayDate,
                            color = Color.Black,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    DefaultLoadingUnit()
                }
                else {
                    if (settingsState.fullWeekVisibility) {
                        WeekScheduleRender(viewModel)
                    }
                    else {
                        TodayScheduleRender(viewModel)
                    }
                }
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                SettingsButton(globalGraph)
            }
        }
    }
}

@Composable
fun SettingsButton(navController: NavHostController) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        shape = RoundedCornerShape(10.dp)
    ) {
        val navigationLink = {
            navController.navigate(route = Settings) {
                popUpTo(navController.graph.findStartDestination().id
                ) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }

        IconButton(
                onClick = navigationLink,
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                )
        ) {
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = "settings",
                tint = buttons
            )
        }
    }
}

@SuppressLint("MutableCollectionMutableState")
@Composable
fun WeekScheduleRender(viewModel: ScheduleViewModel) {

    val settingsState by viewModel.settingsStateHolder.settingsState.collectAsState()
    val scheduleState by viewModel.scheduleStateHolder.scheduleState.collectAsState()

    LaunchedEffect(Unit) {
        if (settingsState.weekCount) viewModel.changeWeekCountEvent() else viewModel.changeWeekCountEvent()
    }

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
                        color = Color.Black,
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
                                color = Color.Black,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    scheduleState.weekLessons[dayIndex]?.let { lessons ->

                        if (lessons.isEmpty()) {
                            WeekendUnit()
                        }

                        lessons.forEach { lesson ->
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
fun TodayScheduleRender(viewModel: ScheduleViewModel) {

    val settingsState by viewModel.settingsStateHolder.settingsState.collectAsState()
    val scheduleState by viewModel.scheduleStateHolder.scheduleState.collectAsState()

    LaunchedEffect(Unit) {
        if (settingsState.weekCount) viewModel.changeWeekCountEvent() else viewModel.changeWeekCountEvent()
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(25.dp, 45.dp, 80.dp, 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = scheduleState.todayDate,
            color = Color.Black,
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
            itemsIndexed(scheduleState.todayLessons) { _, lesson ->
                ScheduleUnit(lesson)
            }
        }
    }
}

@Composable
fun DefaultLoadingUnit() {
    val context: Context = LocalContext.current

    Text(
        context.getString(R.string.empty_screen),
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp, 50.dp, 0.dp, 10.dp),
        textAlign = TextAlign.Center,
        fontSize = 23.sp,
        fontWeight = FontWeight.Bold
    )
    Loader(resource = R.raw.error_animation, 270.dp)
    Spacer(modifier = Modifier.width(10.dp))
    /*
    * Text(
        text = context.getString(R.string.message),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 45.dp),
        textAlign = TextAlign.Left,
        color = Color.Black,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold
    )
    Text(
        text = context.getString(R.string.recommendations),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 45.dp),
        textAlign = TextAlign.Left,
        color = Color.Gray,
        fontSize = 13.sp
    )
    * */
}

@Composable
fun WeekendUnit() {
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
            Loader(resource = R.raw.weekend, 130.dp)
        }
    }
}

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
            text = lesson.name.toString(),
            color = Color.Black,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = lesson.teacher.toString(),
            color = Color.Black,
            fontSize = 15.sp,
            fontStyle = FontStyle.Italic
        )
        Text(
            text = lesson.location.toString(),
            modifier = Modifier.padding(top = 10.dp),
            color = Color.Black,
            fontSize = 15.sp,
            fontStyle = FontStyle.Italic
        )
    }
}

@Composable
fun Loader(resource: Int, height: Dp) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(resource))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    val isAnimationReady = composition != null

    Column(
        modifier = Modifier
            .height(height)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isAnimationReady) {
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 20.dp)
            )
        } else {
            Text(text = "Загрузка анимации...", color = Color.Gray)
        }
    }
}