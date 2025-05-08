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
import com.mycollege.schedule.feature.groups.ui.state.GroupsViewModel
import com.mycollege.schedule.feature.onboarding.ui.OnboardingScreen
import com.mycollege.schedule.app.activity.components.StartScreen
import com.mycollege.schedule.feature.schedule.ui.ScheduleScreen
import com.mycollege.schedule.feature.settings.ui.Settings
import com.mycollege.schedule.feature.schedule.ui.state.ScheduleViewModel
import com.mycollege.schedule.app.activity.data.MainViewModel
import com.mycollege.schedule.app.activity.data.StartViewModel

@Composable
fun AppPager(
    pagerState: PagerState,
    groupsViewModel: GroupsViewModel,
    scheduleViewModel: ScheduleViewModel,
    globalNavHostController: NavHostController
) {
    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
        userScrollEnabled = false
    ) { page ->
        when (page) {
            0 -> GroupScreen(groupsViewModel, pagerState)
            1 -> ScheduleScreen(scheduleViewModel, globalNavHostController)
        }
    }
}

@Composable
fun AddNavGraph(
    navController: NavHostController,
    mainViewModel: MainViewModel,
    groupsViewModel: GroupsViewModel,
    scheduleViewModel: ScheduleViewModel
) {

    val firstStartup by mainViewModel.shared.firstStartup.collectAsState()
    val startViewModel: StartViewModel = hiltViewModel()

    // restore cache event
    startViewModel.init()
    
    NavHost(navController = navController, startDestination = if (firstStartup) Onboarding else Start) {
        composable<Onboarding>(
        ) {
            OnboardingScreen(hiltViewModel())
        }
        composable<Start> {
            StartScreen(startViewModel, navController, groupsViewModel, scheduleViewModel)
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
            Settings(hiltViewModel(), navController)
        }
    }
}


