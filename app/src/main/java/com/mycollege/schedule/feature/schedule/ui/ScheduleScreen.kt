package com.mycollege.schedule.feature.schedule.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.mycollege.schedule.app.activity.domain.models.GroupParserState
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
    ScheduleContent(SettingsState(), AppState(), ScheduleState(), GroupParserState(), {}) {}
}

@Composable
fun ScheduleScreen(
    viewModel: ScheduleViewModel = hiltViewModel(),
    globalGraph: NavHostController
) {

    val appState by viewModel.appStateHolder.appState.collectAsState()
    val settingsState by viewModel.settingsStateHolder.settingsState.collectAsState()
    val scheduleState by viewModel.scheduleStateHolder.scheduleState.collectAsState()
    val parseState by viewModel.parserStateHolder.groupParserState.collectAsState()

    val handleEvent: (ScheduleEvent) -> Unit = { event ->
        viewModel.handleEvent(event)
    }

    val navigateToSettings = { // навигация в настройки
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
    parseState: GroupParserState,
    handleEvent: (ScheduleEvent) -> Unit,
    navigateToSettings: () -> Unit
) {
    ScheduleTheme {
        Scaffold(modifier = Modifier.fillMaxSize(), contentWindowInsets = WindowInsets(0), containerColor = background) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(top = 30.dp)) {
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
                            WeekScheduleRender(appState, scheduleState, settingsState, handleEvent)
                        }
                        else {
                            TodayScheduleRender(appState, scheduleState, settingsState, handleEvent)
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