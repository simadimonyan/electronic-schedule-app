package com.mycollege.schedule.feature.settings.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.mycollege.schedule.app.navigation.Start
import com.mycollege.schedule.feature.settings.ui.components.AboutBottomSheet
import com.mycollege.schedule.feature.settings.ui.components.CardSettings
import com.mycollege.schedule.feature.settings.ui.components.ContactLabel
import com.mycollege.schedule.feature.settings.ui.components.CopyrightView
import com.mycollege.schedule.feature.settings.ui.components.SegmentedButton
import com.mycollege.schedule.feature.settings.ui.state.SettingsEvent
import com.mycollege.schedule.feature.settings.ui.state.SettingsState
import com.mycollege.schedule.feature.settings.ui.state.SettingsViewModel
import com.mycollege.schedule.shared.ui.theme.ScheduleTheme
import com.mycollege.schedule.shared.ui.theme.background

@Preview
@Composable
fun SettingsPreview() {
    SettingsContent(SettingsState(), {}, {}, {}, {})
}

@SuppressLint("ContextCastToActivity")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    navController: NavHostController
) {

    val settingsState by viewModel.settingsStateHolder.settingsState.collectAsState()
    var aboutBottomSheet by remember { mutableStateOf(false) }

    val handleEvent: (SettingsEvent) -> Unit = { event ->
        viewModel.handleEvent(event)
    }

    val onExit: () -> Unit = {
        navController.navigate(Start) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    var showDialog by remember { mutableStateOf(false) }

    val openCopyrights: () -> Unit = {
        showDialog = true
    }

    val onAboutToggle: () -> Unit = {
        aboutBottomSheet = !aboutBottomSheet
    }

    if (aboutBottomSheet) {
        AboutBottomSheet(onAboutToggle)
    }

    SettingsContent(settingsState, handleEvent, onAboutToggle, openCopyrights, onExit)

    if (showDialog) {
        CopyrightView(
            onDisposable = { showDialog = !showDialog }
        )
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(
    settingsState: SettingsState,
    handleEvent: (SettingsEvent) -> Unit,
    onAboutToggle: () -> Unit,
    openCopyrights: () -> Unit,
    onExit: () -> Unit
) {
    ScheduleTheme {
        Scaffold(modifier = Modifier
            .fillMaxSize(), containerColor = background,
            bottomBar = {
                ContactLabel()
            },
            topBar = {
                TopAppBar(
                    title = {
                        Text("Настройки", color = Color.Black, fontWeight = FontWeight.Medium)
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = background
                    ),
                    navigationIcon = {
                        IconButton(onClick = {
                            onExit()
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад", tint = Color.Black)
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {

                SegmentedButton(settingsState.weekCount, !settingsState.synchronizeWeekParity) {
                    handleEvent(SettingsEvent.MakeWeekCountDifferent(it))
                    handleEvent(SettingsEvent.SaveSettings)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp, 0.dp, 20.dp, 0.dp)
                        .wrapContentHeight(),
                    elevation = CardDefaults.cardElevation(2.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(10.dp, 0.dp)) {

                        CardSettings(title = "Синхронизировать неделю", checkedState = settingsState.synchronizeWeekParity) {
                            handleEvent(SettingsEvent.SynchronizeWeekParity(it))
                            handleEvent(SettingsEvent.SaveSettings)
                        }

                        HorizontalDivider(Modifier.padding(10.dp, 0.dp))
                        
                        CardSettings(title = "Уведомления", checkedState = settingsState.notificationsEnabled) {
                            handleEvent(SettingsEvent.MakeNotificationsEnabled(it))
                            handleEvent(SettingsEvent.SaveSettings)
                        }

                        HorizontalDivider(Modifier.padding(10.dp, 0.dp))

                        CardSettings(title = "Показать неделю", checkedState = settingsState.fullWeekVisibility) {
                            handleEvent(SettingsEvent.MakeScheduleWeekFull(it))
                            handleEvent(SettingsEvent.SaveSettings)
                        }

                        HorizontalDivider(Modifier.padding(10.dp, 0.dp))

                        CardSettings(title = "Скрыть навигацию", checkedState = settingsState.navigationVisibility) {
                            handleEvent(SettingsEvent.MakeNavigationInvisible(it))
                            handleEvent(SettingsEvent.SaveSettings)
                        }

                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp, 0.dp, 20.dp, 0.dp)
                        .wrapContentHeight(),
                    elevation = CardDefaults.cardElevation(2.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                ) {

                    Row(
                        modifier = Modifier
                            .clickable {
                                openCopyrights()
                            }
                            .height(60.dp)
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.width(20.dp))
                        Text(
                            text = "Авторские права",
                            fontSize = 16.sp
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp))

                    Row(
                        modifier = Modifier
                            .clickable {
                                onAboutToggle()
                            }
                            .height(60.dp)
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.Info, contentDescription = "Info", tint = Color.LightGray)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "О приложении",
                            fontSize = 16.sp
                        )
                    }
                }

            }
        }
    }
}
