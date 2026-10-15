package com.mycollege.schedule.app.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.mycollege.schedule.app.activity.ui.StartScreen
import com.mycollege.schedule.app.activity.ui.state.MainViewModel
import com.mycollege.schedule.app.activity.ui.state.StartViewModel
import com.mycollege.schedule.feature.groups.ui.GroupScreen
import com.mycollege.schedule.feature.groups.ui.state.GroupViewModel
import com.mycollege.schedule.feature.onboarding.ui.OnboardingScreen
import com.mycollege.schedule.feature.schedule.ui.ScheduleScreen
import com.mycollege.schedule.feature.schedule.ui.state.ScheduleViewModel
import com.mycollege.schedule.feature.settings.ui.SettingsScreen
import com.mycollege.schedule.feature.settings.ui.components.CircleThemeTransition
import com.mycollege.schedule.feature.settings.ui.state.SettingsViewModel
import com.mycollege.schedule.shared.ui.theme.LocalAppDarkTheme
import com.mycollege.schedule.feature.schedule.ui.ScheduleActions
import com.mycollege.schedule.shared.ui.design.MainTab
import com.mycollege.schedule.shared.ui.theme.DarkMaColors
import com.mycollege.schedule.shared.ui.theme.LightMaColors

@Composable
fun AppPager(
    pagerState: PagerState,
    groupViewModel: GroupViewModel,
    scheduleViewModel: ScheduleViewModel,
    tab: MainTab,
    scheduleActions: ScheduleActions
) {
    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
        userScrollEnabled = false
    ) { page ->
        when (page) {
            0 -> GroupScreen(groupViewModel, pagerState)
            1 -> ScheduleScreen(scheduleViewModel, tab, scheduleActions)
        }
    }
}

@Composable
fun AddNavGraph(
    navController: NavHostController,
    mainViewModel: MainViewModel,
    startViewModel: StartViewModel,
    groupViewModel: GroupViewModel,
    scheduleViewModel: ScheduleViewModel,
    settingsViewModel: SettingsViewModel
) {

    val darkMode = LocalAppDarkTheme.current

    NavHost(
        navController = navController,
        modifier = Modifier.fillMaxSize()//.windowInsetsPadding(WindowInsets.navigationBars)
            .background(if (darkMode) DarkMaColors.bg else LightMaColors.bg),
        startDestination = Start // if (appState.firstStartUp) Onboarding else Start
    ) {
        composable<Onboarding>() {
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

            val scrollState = rememberLazyListState()

            CircleThemeTransition(settingsViewModel) {
                SettingsScreen(settingsViewModel, navController, scrollState)
            }
        }
    }
}


