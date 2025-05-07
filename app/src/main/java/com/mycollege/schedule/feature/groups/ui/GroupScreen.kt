package com.mycollege.schedule.feature.groups.ui

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.mycollege.schedule.BuildConfig
import com.mycollege.schedule.R
import com.mycollege.schedule.core.ads.YandexAdsListener
import com.mycollege.schedule.feature.groups.ui.components.BottomSheetContent
import com.mycollege.schedule.feature.groups.ui.state.GroupEvent
import com.mycollege.schedule.feature.groups.ui.state.GroupState
import com.mycollege.schedule.feature.groups.ui.state.GroupsViewModel
import com.mycollege.schedule.shared.ui.theme.ScheduleTheme
import com.mycollege.schedule.shared.ui.theme.background
import com.mycollege.schedule.shared.ui.theme.buttons
import com.yandex.mobile.ads.banner.BannerAdSize
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.common.AdRequest
import kotlinx.coroutines.launch
import ru.rustore.sdk.remoteconfig.RemoteConfigClient

@SuppressLint("MutableCollectionMutableState")
@Composable
fun GroupScreen(
    viewModel: GroupsViewModel = hiltViewModel(),
    pagerState: PagerState
) {
    MainFrame(viewModel = viewModel, pagerState)
}

@Composable
fun MainFrame(viewModel: GroupsViewModel, pagerState: PagerState) {
    val context = LocalContext.current
    val groupState by viewModel.groupState.collectAsState()
    val scope = rememberCoroutineScope()
    var showAds by remember { mutableStateOf(false) }

    RemoteConfigClient.instance
        .getRemoteConfig().addOnSuccessListener { rc -> showAds = rc.getBoolean("Advertisement")}

    ScheduleTheme {
        Scaffold(modifier = Modifier.fillMaxSize(), contentWindowInsets = WindowInsets(0), containerColor = background) { innerPadding ->
            Column(modifier = Modifier.fillMaxHeight()) {
                Text(
                    context.getString(R.string.choose_group),
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxWidth()
                        .padding(0.dp, 70.dp, 0.dp, 0.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold
                )

                // 480.dp is size of height when 80.dp is too big
                BoxWithConstraints {
                    val padding = if (maxHeight < 480.dp) 40.dp else 80.dp
                    Spacer(modifier = Modifier.padding(bottom = padding))
                }

                Body(viewModel, context, groupState)

                ActionButton(
                    text = context.getString(R.string.choose),
                    icon = R.drawable.logo,
                    onClick = {
                        viewModel.handleEvent(GroupEvent.CreateSchedule)
                        groupState.scheduleCreation
                        scope.launch {
                            viewModel.shared.updateIndex(1)
                            pagerState
                                .animateScrollToPage(1)
                        }
                    },
                    enabled = groupState.group != "Выбрать"
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

            }
        }
    }
}

@Composable
fun Body(
    viewModel: GroupsViewModel,
    context: Context,
    groupState: GroupState
) {
    val loading by viewModel.shared.loading.collectAsState(true)
    val progress by viewModel.shared.progress.collectAsState(0)

    Column {
        CardContent(
            icon = R.drawable.study,
            title = context.getString(R.string.course),
            subtitle = groupState.course,
            onClick = {
                viewModel.handleEvent(GroupEvent.DisplayCourses)
                viewModel.handleEvent(GroupEvent.ShowBottomSheet)
                viewModel.handleEvent(GroupEvent.SetSelectedIndex(0))
            }
        )

        CardContent(
            icon = R.drawable.books,
            title = context.getString(R.string.speciality),
            subtitle = groupState.speciality,
            onClick = {
                viewModel.handleEvent(GroupEvent.DisplaySpecialities(groupState.course))
                viewModel.handleEvent(GroupEvent.ShowBottomSheet)
                viewModel.handleEvent(GroupEvent.SetSelectedIndex(1))
            }
        )

        CardContent(
            icon = R.drawable.people,
            title = context.getString(R.string.group),
            subtitle = groupState.group,
            onClick = {
                viewModel.handleEvent(GroupEvent.DisplayGroups(groupState.course, groupState.speciality))
                viewModel.handleEvent(GroupEvent.ShowBottomSheet)
                viewModel.handleEvent(GroupEvent.SetSelectedIndex(2))
            }
        )
    }

    if (groupState.showBottomSheet) {
        BottomSheetContent(
            loading = loading,
            progress = progress,
            viewModel = viewModel,
            selectedIndex = groupState.selectedIndex,
            onDismiss = { viewModel.handleEvent(GroupEvent.HideBottomSheet) }
        )
    }
}

@Composable
fun CardContent(icon: Int, title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(20.dp, 0.dp, 20.dp, 20.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(start = 20.dp, top = 5.dp, bottom = 5.dp)
                .wrapContentHeight()
        ) {
            Image(
                painter = painterResource(id = icon),
                contentDescription = null,
                colorFilter = ColorFilter.tint(buttons),
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.CenterVertically)
            )
            Spacer(modifier = Modifier.width(20.dp))
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier
                    .wrapContentHeight()
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = subtitle,
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun ActionButton(text: String, icon: Int, onClick: () -> Unit, enabled: Boolean) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp, 30.dp, 20.dp, 0.dp)
            .size(0.dp, 65.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = buttons, disabledContainerColor = Color.LightGray),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
        enabled = enabled
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(15.dp, 0.dp)
                .fillMaxHeight()
        ) {
            Image(
                painter = painterResource(id = R.drawable.notification),
                contentDescription = null,
                modifier = Modifier.size(35.dp),
                colorFilter = ColorFilter.tint(Color.White),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = text, Modifier.padding(0.dp, 7.dp), color = Color.White)
        }
    }
}