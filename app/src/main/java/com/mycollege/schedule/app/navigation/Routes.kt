package com.mycollege.schedule.app.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.mycollege.schedule.feature.groups.ui.GroupScreen
import com.mycollege.schedule.feature.groups.ui.state.GroupViewModel
import com.mycollege.schedule.feature.onboarding.ui.OnboardingScreen
import com.mycollege.schedule.app.activity.ui.StartScreen
import com.mycollege.schedule.feature.schedule.ui.ScheduleScreen
import com.mycollege.schedule.feature.settings.ui.SettingsScreen
import com.mycollege.schedule.feature.schedule.ui.state.ScheduleViewModel
import com.mycollege.schedule.app.activity.ui.state.MainViewModel
import com.mycollege.schedule.app.activity.ui.state.StartViewModel

@Composable
fun AppPager(
    pagerState: PagerState,
    groupViewModel: GroupViewModel,
    scheduleViewModel: ScheduleViewModel,
    globalNavHostController: NavHostController
) {
    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
        userScrollEnabled = false
    ) { page ->
        when (page) {
            0 -> GroupScreen(groupViewModel, pagerState)
            1 -> ScheduleScreen(scheduleViewModel, globalNavHostController)
        }
    }
}

@Composable
fun AddNavGraph(
    navController: NavHostController,
    mainViewModel: MainViewModel,
    groupViewModel: GroupViewModel,
    scheduleViewModel: ScheduleViewModel
) {

    val appState by mainViewModel.appStateHolder.appState.collectAsState()
    val startViewModel: StartViewModel = hiltViewModel()

    // restore cache event
    startViewModel.init()
    
    NavHost(navController = navController, startDestination = if (appState.firstStartUp) Onboarding else Start) {
        composable<Onboarding>(
        ) {
            OnboardingScreen(hiltViewModel())
        }
        composable<Start> {
            StartScreen(startViewModel, navController, groupViewModel, scheduleViewModel)
        }
        composable<Settings>(
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(
                        durationMillis = 250
                    )
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(
                        durationMillis = 250
                    )
                )
            }
        ) {
            SettingsScreen(hiltViewModel(), navController)
        }
    }
}


