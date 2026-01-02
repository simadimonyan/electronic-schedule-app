package com.mycollege.schedule.feature.groups.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.my.tracker.MyTracker
import com.mycollege.schedule.BuildConfig
import com.mycollege.schedule.R
import com.mycollege.schedule.app.activity.domain.models.LoadingState
import com.mycollege.schedule.app.activity.ui.state.AppState
import com.mycollege.schedule.core.ads.YandexAdsListener
import com.mycollege.schedule.feature.groups.ui.components.ActionButton
import com.mycollege.schedule.feature.groups.ui.components.BottomSheetContent
import com.mycollege.schedule.feature.groups.ui.components.GroupCard
import com.mycollege.schedule.feature.groups.ui.components.ModeSegmentedButton
import com.mycollege.schedule.feature.groups.ui.state.GroupEvent
import com.mycollege.schedule.feature.groups.ui.state.GroupState
import com.mycollege.schedule.feature.groups.ui.state.GroupViewModel
import com.mycollege.schedule.shared.ui.theme.LocalAppDarkTheme
import com.mycollege.schedule.shared.ui.theme.ScheduleTheme
import com.mycollege.schedule.shared.ui.theme.background
import com.mycollege.schedule.shared.ui.theme.backgroundDark
import com.yandex.mobile.ads.banner.BannerAdSize
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.rustore.sdk.remoteconfig.RemoteConfigClient

@Preview
@Composable
fun GroupPreview() {

    val pagerState = rememberPagerState(
        initialPage = 0
    ) { 0 }

    GroupContent(
        {},
        {},
        GroupState(),
        AppState(0, false, false),
        LoadingState(),
        pagerState,
        false,
        false,
        emptyMap(),
        emptyMap()
    )

}

@SuppressLint("MutableCollectionMutableState")
@Composable
fun GroupScreen(
    viewModel: GroupViewModel = hiltViewModel(),
    pagerState: PagerState
) {
    val groupState by viewModel.groupStateHolder.groupState.collectAsState()
    val appState by viewModel.appStateHolder.appState.collectAsState()
    val parserState by viewModel.groupParserStateHolder.loadingState.collectAsState()

    val cachedGroups by viewModel.cacheManager.getGroupScheduleSyncFlow().collectAsState(emptyMap())
    val cachedTeachers by viewModel.cacheManager.getTeacherScheduleSyncFlow().collectAsState(emptyMap())

    var showAds by remember { mutableStateOf(false) }
    val changeStudentModeFlag by produceState(appState.studentMode, appState.studentMode) {
        if (value != appState.studentMode) {
            delay(700)
            value = appState.studentMode
        }
    }

    RemoteConfigClient.instance
        .getRemoteConfig().addOnSuccessListener { rc -> showAds = rc.getBoolean("Advertisement")}

    val handleEvent: (GroupEvent) -> Unit = {
        viewModel.handleEvent(it)
    }

    val updateAppStateIndex: (Int) -> Unit = {
        viewModel.appStateHolder.updateIndex(it)
    }

    GroupContent(handleEvent, updateAppStateIndex, groupState, appState, parserState, pagerState, showAds, changeStudentModeFlag, cachedGroups, cachedTeachers)
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun GroupContent(
    handleEvent: (GroupEvent) -> Unit,
    updateAppStateIndex: (Int) -> Unit,
    groupState: GroupState,
    appState: AppState,
    parserState: LoadingState,
    pagerState: PagerState,
    showAds: Boolean,
    changeStudentModeFlag: Boolean,
    cachedGroups: Map<String, Long>,
    cachedTeachers: Map<String, Long>
) {
    val scope = rememberCoroutineScope()
    val darkMode = LocalAppDarkTheme.current

    LaunchedEffect(Unit) {
        MyTracker.trackEvent("Просмотр рекламы")
    }

    Scaffold(modifier = Modifier.fillMaxSize(), contentWindowInsets = WindowInsets(0), containerColor = if (darkMode) backgroundDark else background) { innerPadding ->
        Column(modifier = Modifier.fillMaxHeight().padding(innerPadding).padding(0.dp, 70.dp, 0.dp, 0.dp)) {

            ModeSegmentedButton(appState.studentMode) {
                handleEvent(GroupEvent.ChangeStudentMode(it))
            }

            // 480.dp is size of height when 80.dp is too big
            BoxWithConstraints {
                val padding = if (maxHeight < 480.dp) 40.dp else 80.dp
                Spacer(modifier = Modifier.padding(bottom = padding))
            }

            // body cards
            Column {

                if (changeStudentModeFlag) {
                    GroupCard(
                        icon = R.drawable.study,
                        title = LocalContext.current.getString(R.string.course),
                        subtitle = groupState.course,
                        onClick = {
                            if (!parserState.networkIssues) handleEvent(GroupEvent.Display)
                            handleEvent(GroupEvent.ShowBottomSheet)
                            handleEvent(GroupEvent.SetSelectedIndex(0))
                        },
                        darkMode = darkMode
                    )

                    GroupCard(
                        icon = R.drawable.books,
                        title = LocalContext.current.getString(R.string.speciality),
                        subtitle = groupState.level,
                        onClick = {
                            if (!parserState.networkIssues) handleEvent(GroupEvent.Display)
                            handleEvent(GroupEvent.ShowBottomSheet)
                            handleEvent(GroupEvent.SetSelectedIndex(1))
                        },
                        darkMode = darkMode
                    )

                    GroupCard(
                        icon = R.drawable.people,
                        title = LocalContext.current.getString(R.string.group),
                        subtitle = groupState.group,
                        onClick = {
                            if (!parserState.networkIssues) handleEvent(GroupEvent.Display)
                            handleEvent(GroupEvent.ShowBottomSheet)
                            handleEvent(GroupEvent.SetSelectedIndex(2))
                        },
                        darkMode = darkMode
                    )
                }
                else {

                    GroupCard(
                        icon = R.drawable.department,
                        title = "Кафедра",
                        subtitle = groupState.department,
                        onClick = {
                            if (!parserState.networkIssues) handleEvent(GroupEvent.Display)
                            handleEvent(GroupEvent.ShowBottomSheet)
                            handleEvent(GroupEvent.SetSelectedIndex(3))
                        },
                        darkMode = darkMode
                    )

                    GroupCard(
                        icon = R.drawable.teacher,
                        title = "Преподаватель",
                        subtitle = groupState.teacher,
                        onClick = {
                            if (!parserState.networkIssues) handleEvent(GroupEvent.Display)
                            handleEvent(GroupEvent.ShowBottomSheet)
                            handleEvent(GroupEvent.SetSelectedIndex(4))
                        },
                        darkMode = darkMode
                    )

                }

            }

            // ---

            ActionButton(
                text = "Выбрать",
                icon = R.drawable.logo,
                onClick = {
                    handleEvent(GroupEvent.ChooseGroup)
                    scope.launch {
                        updateAppStateIndex(1)
                        pagerState
                            .animateScrollToPage(1)
                    }
                },
                enabled = if (changeStudentModeFlag) groupState.group != "Выбрать группу" else groupState.teacher != "Выбрать преподавателя",
                darkMode
            )

            if (showAds) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.Center) {
                    AndroidView(factory = { context ->
                        BannerAdView(context).apply {
                            setAdUnitId(BuildConfig.ADVERTISEMENT_BANNER_ID)
                            setAdSize(BannerAdSize.stickySize(context, 370))
                            val adRequest = AdRequest.Builder().setPreferredTheme(
                                if (darkMode) AdTheme.DARK else AdTheme.LIGHT
                            ).build()
                            setBannerAdEventListener(YandexAdsListener())
                            loadAd(adRequest)
                        }
                    })
                }
            }

            if (groupState.showBottomSheet) {
                BottomSheetContent(
                    loadingState = parserState,
                    progress = parserState.chooseConfigurationProgress,
                    groupState,
                    handleEvent,
                    selectedIndex = groupState.selectedIndex,
                    onDismiss = { handleEvent(GroupEvent.HideBottomSheet) },
                    cachedGroups,
                    cachedTeachers
                )
            }

        }
    }

}