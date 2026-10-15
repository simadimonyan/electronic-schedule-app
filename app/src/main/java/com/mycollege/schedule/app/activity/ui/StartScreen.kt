package com.mycollege.schedule.app.activity.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.my.tracker.MyTracker
import com.mycollege.schedule.app.activity.ui.state.StartViewModel
import com.mycollege.schedule.app.navigation.AppPager
import com.mycollege.schedule.app.navigation.MainTabs
import com.mycollege.schedule.app.navigation.Settings
import com.mycollege.schedule.feature.groups.ui.state.GroupEvent
import com.mycollege.schedule.feature.groups.ui.state.GroupViewModel
import com.mycollege.schedule.feature.schedule.ui.ScheduleActions
import com.mycollege.schedule.feature.schedule.ui.state.ScheduleViewModel
import com.mycollege.schedule.feature.surveys.ui.SurveyHost
import com.mycollege.schedule.feature.surveys.ui.state.SurveyEvent
import com.mycollege.schedule.feature.surveys.ui.state.SurveyViewModel
import com.mycollege.schedule.shared.ui.components.ModeTransition
import com.mycollege.schedule.shared.ui.design.FloatingNavBar
import com.mycollege.schedule.shared.ui.design.MainTab
import kotlinx.coroutines.delay

/** Переход на экран настроек (общий для экрана расписания и нижней навигации) */
fun NavHostController.openSettings() {
    MyTracker.trackEvent("Перейти в экран настроек")
    navigate(route = Settings) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun StartScreen(
    viewModel: StartViewModel = hiltViewModel(),
    globalGraph: NavHostController,
    groupsViewModel: GroupViewModel,
    scheduleViewModel: ScheduleViewModel,
    surveyViewModel: SurveyViewModel = hiltViewModel()
) {
    val settingsState by viewModel.settingsStateHolder.settingsState.collectAsState()
    val appState by viewModel.appStateHolder.appState.collectAsState()

    var previousMode by remember { mutableStateOf(appState.studentMode) }
    var transitionModeFlag by remember { mutableStateOf(false) }

    val pagerState = rememberPagerState(
        initialPage = if (appState.pagerIndex == 1) 1 else 0
    ) { 2 }

    // главный экран нового дизайна — «Сегодня»: если расписание уже выбрано, при запуске открываем его,
    // а не экран выбора группы
    var startPageResolved by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!startPageResolved) {
            startPageResolved = true
            val g = groupsViewModel.groupStateHolder.groupState.value
            val chosen = if (appState.studentMode) g.group != "Выбрать группу" else g.teacher != "Выбрать преподавателя"
            if (chosen && appState.pagerIndex == 0) groupsViewModel.appStateHolder.updateIndex(1)
        }
    }

    // страница пейджера следует за pagerIndex (выбор группы ↔ расписание)
    LaunchedEffect(appState.pagerIndex) {
        val target = if (appState.pagerIndex == 1) 1 else 0
        if (pagerState.currentPage != target) pagerState.animateScrollToPage(target)
    }

    val tab = MainTabs.scheduleTab ?: if (settingsState.fullWeekVisibility) MainTab.Week else MainTab.Today

    val openSelect = {
        MyTracker.trackEvent("Перейти на главный экран")
        groupsViewModel.appStateHolder.updateIndex(0)
    }
    val selectTab: (MainTab) -> Unit = { t ->
        when (t) {
            MainTab.Settings -> globalGraph.openSettings()
            else -> {
                if (pagerState.currentPage != 1) groupsViewModel.appStateHolder.updateIndex(1)
                MainTabs.scheduleTab = t
                MyTracker.trackEvent(if (t == MainTab.Week) "Открыть вкладку недели" else "Открыть вкладку сегодня")
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        AppPager(
            pagerState,
            groupViewModel = groupsViewModel,
            scheduleViewModel = scheduleViewModel,
            tab = tab,
            scheduleActions = ScheduleActions(
                onOpenSelect = openSelect,
                onOpenSettings = { globalGraph.openSettings() },
                onSelectTab = selectTab,
                onRetry = { groupsViewModel.handleEvent(GroupEvent.ChooseGroup) },
                onRetryNetwork = { groupsViewModel.handleEvent(GroupEvent.Display) },
            )
        )

        AnimatedVisibility(
            visible = !settingsState.navigationInvisibility && pagerState.targetPage == 1,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 },
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            FloatingNavBar(current = tab, onSelect = selectTab, onSearch = openSelect)
        }
    }

    LaunchedEffect(appState.studentMode) {
        if (previousMode != appState.studentMode) {

            transitionModeFlag = true
            delay(2000)
            transitionModeFlag = false

            previousMode = appState.studentMode
        }
    }

    // опрос: проверка при открытии экрана расписания (лимиты — в SurveyScheduleUseCase),
    // в debug с surveys.debugForceShow — сразу при старте
    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage == 1 || surveyViewModel.forceShow) {
            surveyViewModel.handleEvent(SurveyEvent.AutoCheck)
        }
    }

    SurveyHost(surveyViewModel)

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
