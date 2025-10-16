package com.mycollege.schedule.shared.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.my.tracker.MyTracker
import com.mycollege.schedule.R

import com.mycollege.schedule.shared.ui.theme.buttons
import com.mycollege.schedule.feature.groups.ui.state.GroupViewModel
import kotlinx.coroutines.launch

@Composable
fun CustomAppBar(
    viewModel: GroupViewModel,
    pagerState: PagerState
) {
    val appState by viewModel.appStateHolder.appState.collectAsState()

    val imageSize = 62.dp

    val indicatorOffset by animateDpAsState(
        targetValue = if (appState.pagerIndex == 1) imageSize else -imageSize,
        animationSpec = tween(durationMillis = 200),
        label = "indicatorOffset"
    )

    var isClicked by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val navigateToGroupScreen = {
//        if (selectedIndex != 0 && !isClicked) {
//            isClicked = true
//            viewModel.shared.updateIndex(0)
//            navController.navigate(GroupScreen) {
//                popUpTo(navController.graph.findStartDestination().id) {
//                    saveState = true
//                }
//                launchSingleTop = true
//                restoreState = true
//            }
//
//            // Delay to reset the isClicked state after 500ms
//            scope.launch {
//                delay(200)  // Delay for half a second
//                isClicked = false
//            }
//        }
    }

    val navigateToScheduleScreen = {
//        if (selectedIndex != 1 && !isClicked) {
//            isClicked = true
//            viewModel.shared.updateIndex(1)
//            navController.navigate(ScheduleScreen) {
//                popUpTo(navController.graph.findStartDestination().id) {
//                    saveState = true
//                }
//                launchSingleTop = true
//               restoreState = true
//            }
//
//            // Delay to reset the isClicked state after 500ms
//            scope.launch {
//                delay(200)  // Delay for half a second
//                isClicked = false
//            }
//        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(60.dp, 0.dp, 60.dp, 30.dp)
                .size(width = 0.dp, height = 80.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 3.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Card(
                    modifier = Modifier
                        .size(44.dp, 4.dp)
                        .align(Alignment.BottomCenter)
                        .offset {
                            IntOffset(
                                x = indicatorOffset.roundToPx(),
                                y = -15.dp.roundToPx()
                            )
                        },
                    shape = RoundedCornerShape(100.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = buttons
                    )
                ) {}

                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    // Group
                    Image(
                        painter = painterResource(id = R.drawable.list),
                        contentDescription = "study",
                        colorFilter = ColorFilter.tint(if (appState.pagerIndex == 0) buttons else Color.Gray),
                        modifier = Modifier
                            .size(55.dp)
                            .padding(0.dp, 15.dp, 0.dp, 0.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    scope.launch {
                                        viewModel.appStateHolder.updateIndex(0)
                                        pagerState.animateScrollToPage(0)
                                    }
                                    MyTracker.trackEvent("GroupScreenPathEvent")
                                }
                            )
                    )

                    Spacer(modifier = Modifier.width(70.dp))

                    // Schedule
                    Image(
                        painter = painterResource(id = R.drawable.calendar),
                        contentDescription = "study",
                        colorFilter = ColorFilter.tint(if (appState.pagerIndex == 1) buttons else Color.Gray),
                        modifier = Modifier
                            .size(55.dp)
                            .padding(0.dp, 15.dp, 0.dp, 0.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    scope.launch {
                                        viewModel.appStateHolder.updateIndex(1)
                                        pagerState.animateScrollToPage(1)
                                    }
                                    MyTracker.trackEvent("ScheduleScreenPathEvent")
                                }
                            )
                    )
                }
            }
        }
    }
}