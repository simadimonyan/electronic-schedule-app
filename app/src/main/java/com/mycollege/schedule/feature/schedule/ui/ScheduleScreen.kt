package com.mycollege.schedule.feature.schedule.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.my.tracker.MyTracker
import com.mycollege.schedule.app.activity.domain.models.LoadingState
import com.mycollege.schedule.app.activity.ui.state.AppState
import com.mycollege.schedule.app.navigation.Settings
import com.mycollege.schedule.feature.schedule.ui.components.schedule.DefaultLoadingUnit
import com.mycollege.schedule.feature.schedule.ui.components.settings.SettingsButton
import com.mycollege.schedule.feature.schedule.ui.components.utils.TodayScheduleRender
import com.mycollege.schedule.feature.schedule.ui.components.utils.WeekScheduleRender
import com.mycollege.schedule.feature.schedule.ui.state.ScheduleEvent
import com.mycollege.schedule.feature.schedule.ui.state.ScheduleState
import com.mycollege.schedule.feature.schedule.ui.state.ScheduleViewModel
import com.mycollege.schedule.feature.settings.ui.state.SettingsState
import com.mycollege.schedule.shared.ui.theme.ScheduleTheme
import com.mycollege.schedule.shared.ui.theme.background

@Preview
@Composable
fun SchedulePreview() {
    ScheduleContent(SettingsState(), AppState(), ScheduleState(), LoadingState(scheduleLoading = true), {}) {}
}

@Composable
fun ScheduleScreen(
    viewModel: ScheduleViewModel = hiltViewModel(),
    globalGraph: NavHostController
) {

    val appState by viewModel.appStateHolder.appState.collectAsState()
    val settingsState by viewModel.settingsStateHolder.settingsState.collectAsState()
    val scheduleState by viewModel.scheduleStateHolder.scheduleState.collectAsState()
    val parseState by viewModel.parserStateHolder.loadingState.collectAsState()

    val handleEvent: (ScheduleEvent) -> Unit = { event ->
        viewModel.handleEvent(event)
    }

    val navigateToSettings = { // навигация в настройки
        MyTracker.trackEvent("Перейти в экран настроек")
        globalGraph.navigate(route = Settings) {
            popUpTo(globalGraph.graph.findStartDestination().id
            ) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    ScheduleContent(settingsState, appState, scheduleState, parseState, handleEvent, navigateToSettings)
}

@Composable
fun ScheduleContent(
    settingsState: SettingsState,
    appState: AppState,
    scheduleState: ScheduleState,
    parseState: LoadingState,
    handleEvent: (ScheduleEvent) -> Unit,
    navigateToSettings: () -> Unit
) {

    LaunchedEffect(Unit) {
        handleEvent(ScheduleEvent.ShowIfCachedSchedule)
    }

    ScheduleTheme {
        Scaffold(modifier = Modifier.fillMaxSize(), contentWindowInsets = WindowInsets(0), containerColor = background) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(top = 30.dp)) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(background)
                ) {
                    if (parseState.scheduleLoading) {

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

                        val shimmerColors = listOf(
                            Color.LightGray.copy(alpha = 0.2f),
                            Color.White.copy(alpha = 0.6f),
                            Color.LightGray.copy(alpha = 0.2f)
                        )

                        val infiniteTransition = rememberInfiniteTransition()
                        val translateAnim by infiniteTransition.animateFloat(
                            initialValue = -1000f,
                            targetValue = 1000f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1600, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Restart
                            )
                        )

                        for (i in 1..3) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .padding(20.dp, 0.dp, 20.dp, 15.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = shimmerColors,
                                            start = Offset(translateAnim, 0f),
                                            end = Offset(translateAnim + 500f, 0f)
                                        )
                                    )
                            )
                        }
                    }
                    else {
                        if (settingsState.fullWeekVisibility && ((appState.studentMode && scheduleState.buildScheduleGroupModeFlag)
                                    || (!appState.studentMode && scheduleState.buildScheduleTeacherModeFlag))) {
                            WeekScheduleRender(appState, scheduleState, settingsState, handleEvent)
                        }
                        else if (!settingsState.fullWeekVisibility && ((appState.studentMode && scheduleState.buildScheduleGroupModeFlag)
                                    || (!appState.studentMode && scheduleState.buildScheduleTeacherModeFlag))) {
                            TodayScheduleRender(appState, scheduleState, settingsState, handleEvent)
                        }
                        else {
                            DefaultLoadingUnit(scheduleState)
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    SettingsButton(navigateToSettings)
                }
            }
        }
    }
}