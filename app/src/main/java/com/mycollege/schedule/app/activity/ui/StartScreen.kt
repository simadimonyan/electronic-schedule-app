package com.mycollege.schedule.app.activity.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.mycollege.schedule.app.activity.ui.state.StartViewModel
import com.mycollege.schedule.app.navigation.AppPager
import com.mycollege.schedule.feature.groups.ui.state.GroupViewModel
import com.mycollege.schedule.feature.schedule.ui.state.ScheduleViewModel
import com.mycollege.schedule.shared.ui.components.ModeTransition
import com.mycollege.schedule.shared.ui.components.CustomAppBar
import kotlinx.coroutines.delay

@Composable
fun StartScreen(
    viewModel: StartViewModel = hiltViewModel(),
    globalGraph: NavHostController,
    groupsViewModel: GroupViewModel,
    scheduleViewModel: ScheduleViewModel
) {
    val settingsState by viewModel.settingsStateHolder.settingsState.collectAsState()
    val appState by viewModel.appStateHolder.appState.collectAsState()

    var previousMode by remember { mutableStateOf(appState.studentMode) }
    var transitionModeFlag by remember { mutableStateOf(false) }

    val pagerState = rememberPagerState(
        initialPage = if (appState.pagerIndex == 1) 1 else 0
    ) { 2 }

    AppPager(
        pagerState,
        groupViewModel = groupsViewModel,
        scheduleViewModel = scheduleViewModel,
        globalNavHostController = globalGraph
    )

    LaunchedEffect(appState.studentMode) {
        if (previousMode != appState.studentMode) {

            transitionModeFlag = true
            delay(2000)
            transitionModeFlag = false

            previousMode = appState.studentMode
        }
    }

    if (!settingsState.navigationVisibility) {
        CustomAppBar(groupsViewModel, pagerState)
    }

    if (transitionModeFlag) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(enabled = false) {}
        ) {
            ModeTransition(appState.studentMode)
        }
    }

}