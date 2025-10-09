package com.mycollege.schedule.feature.groups.ui

import android.annotation.SuppressLint
import android.widget.Toast
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
import com.mycollege.schedule.shared.ui.theme.ScheduleTheme
import com.mycollege.schedule.shared.ui.theme.background
import com.yandex.mobile.ads.banner.BannerAdSize
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.common.AdRequest
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
        false
    )
}

@SuppressLint("MutableCollectionMutableState")
@Composable
fun GroupScreen(
    viewModel: GroupViewModel = hiltViewModel(),
    pagerState: PagerState
) {
    val context = LocalContext.current
    val groupState by viewModel.groupStateHolder.groupState.collectAsState()
    val appState by viewModel.appStateHolder.appState.collectAsState()
    val parserState by viewModel.groupParserStateHolder.loadingState.collectAsState()

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

    GroupContent(handleEvent, updateAppStateIndex, groupState, appState, parserState, pagerState, showAds, changeStudentModeFlag)
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
    changeStudentModeFlag: Boolean
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    ScheduleTheme {
        Scaffold(modifier = Modifier.fillMaxSize(), contentWindowInsets = WindowInsets(0), containerColor = background) { innerPadding ->
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
                                handleEvent(GroupEvent.Display)
                                handleEvent(GroupEvent.ShowBottomSheet)
                                handleEvent(GroupEvent.SetSelectedIndex(0))
                            }
                        )

                        GroupCard(
                            icon = R.drawable.books,
                            title = LocalContext.current.getString(R.string.speciality),
                            subtitle = groupState.level,
                            onClick = {
                                handleEvent(GroupEvent.Display)
                                handleEvent(GroupEvent.ShowBottomSheet)
                                handleEvent(GroupEvent.SetSelectedIndex(1))
                            }
                        )

                        GroupCard(
                            icon = R.drawable.people,
                            title = LocalContext.current.getString(R.string.group),
                            subtitle = groupState.group,
                            onClick = {
                                handleEvent(GroupEvent.Display)
                                handleEvent(GroupEvent.ShowBottomSheet)
                                handleEvent(GroupEvent.SetSelectedIndex(2))
                            }
                        )
                    }
                    else {

                        GroupCard(
                            icon = R.drawable.department,
                            title = "Кафедра",
                            subtitle = groupState.department,
                            onClick = {
                                handleEvent(GroupEvent.Display)
                                handleEvent(GroupEvent.ShowBottomSheet)
                                handleEvent(GroupEvent.SetSelectedIndex(3))
                            }
                        )

                        GroupCard(
                            icon = R.drawable.teacher,
                            title = "Преподаватель",
                            subtitle = groupState.teacher,
                            onClick = {
                                handleEvent(GroupEvent.Display)
                                handleEvent(GroupEvent.ShowBottomSheet)
                                handleEvent(GroupEvent.SetSelectedIndex(4))
                            },
                        )

                    }

                }

                // ---

                ActionButton(
                    text = context.getString(R.string.choose),
                    icon = R.drawable.logo,
                    onClick = {
                        handleEvent(GroupEvent.ChooseGroup)
                        scope.launch {
                            updateAppStateIndex(1)
                            pagerState
                                .animateScrollToPage(1)
                        }
                    },
                    enabled = if (changeStudentModeFlag) groupState.group != "Выбрать" else groupState.teacher != "Выбрать преподавателя"
                )

                if (showAds) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.Center) {
                        AndroidView(factory = { context ->
                            BannerAdView(context).apply {
                                setAdUnitId(BuildConfig.ADVERTISEMENT_BANNER_ID)
                                setAdSize(BannerAdSize.stickySize(context, 370))
                                val adRequest = AdRequest.Builder().build()
                                setBannerAdEventListener(YandexAdsListener())
                                loadAd(adRequest)
                            }
                        })
                    }
                }

                if (groupState.showBottomSheet) {
                    BottomSheetContent(
                        loading = parserState.loading,
                        progress = parserState.progress,
                        groupState,
                        handleEvent,
                        selectedIndex = groupState.selectedIndex,
                        onDismiss = { handleEvent(GroupEvent.HideBottomSheet) }
                    )
                }

            }
        }
    }
}