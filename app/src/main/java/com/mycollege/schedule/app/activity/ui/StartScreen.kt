package com.mycollege.schedule.app.activity.ui

import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.mycollege.schedule.app.navigation.AppPager
import com.mycollege.schedule.shared.ui.components.CustomAppBar
import com.mycollege.schedule.feature.groups.ui.state.GroupViewModel
import com.mycollege.schedule.feature.schedule.ui.state.ScheduleViewModel
import com.mycollege.schedule.app.activity.ui.state.StartViewModel

@Composable
fun StartScreen(
    viewModel: StartViewModel = hiltViewModel(),
    globalGraph: NavHostController,
    groupsViewModel: GroupViewModel,
    scheduleViewModel: ScheduleViewModel
) {
    val settingsState by viewModel.settingsStateHolder.settingsState.collectAsState()
    val appState by viewModel.appStateHolder.appState.collectAsState()

    val pagerState = rememberPagerState(
        initialPage = if (appState.pagerIndex == 1) 1 else 0
    ) { 2 }

    AppPager(
        pagerState,
        groupViewModel = groupsViewModel,
        scheduleViewModel = scheduleViewModel,
        globalNavHostController = globalGraph
    )

    if (!settingsState.navigationVisibility) {
        CustomAppBar(groupsViewModel, pagerState)
    }

}