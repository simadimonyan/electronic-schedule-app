package com.mycollege.schedule.feature.settings.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.mycollege.schedule.app.navigation.Start
import com.mycollege.schedule.feature.settings.ui.components.CardSettings
import com.mycollege.schedule.feature.settings.ui.components.ContactLabel
import com.mycollege.schedule.feature.settings.ui.components.SegmentedButton
import com.mycollege.schedule.feature.settings.ui.state.SettingsEvent
import com.mycollege.schedule.feature.settings.ui.state.SettingsState
import com.mycollege.schedule.feature.settings.ui.state.SettingsViewModel
import com.mycollege.schedule.shared.ui.theme.ScheduleTheme
import com.mycollege.schedule.shared.ui.theme.background

@Preview
@Composable
fun SettingsPreview() {
    SettingsContent(SettingsState(), {}, {})
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    navController: NavHostController
) {

    val settingsState by viewModel.settingsStateHolder.settingsState.collectAsState()

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

    SettingsContent(settingsState, handleEvent, onExit)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(
    settingsState: SettingsState,
    handleEvent: (SettingsEvent) -> Unit,
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
                    title = { Text("Настройки", color = Color.Black, fontWeight = FontWeight.Medium) },
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

                SegmentedButton(settingsState.weekCount) {
                    handleEvent(SettingsEvent.MakeWeekCountDifferent(it))
                    handleEvent(SettingsEvent.SaveSettings)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp, 0.dp, 20.dp, 0.dp)
                        .wrapContentHeight(),
                    //.size(width = 0.dp, height = 140.dp),
                    elevation = CardDefaults.cardElevation(2.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(20.dp, 0.dp)) {

                        CardSettings(title = "Уведомления", checkedState = settingsState.notificationsEnabled) {
                            handleEvent(SettingsEvent.MakeNotificationsEnabled(it))
                            handleEvent(SettingsEvent.SaveSettings)
                        }

                        HorizontalDivider(Modifier.padding(horizontal = 10.dp))

                        CardSettings(title = "Показать неделю", checkedState = settingsState.fullWeekVisibility) {
                            handleEvent(SettingsEvent.MakeScheduleWeekFull(it))
                            handleEvent(SettingsEvent.SaveSettings)
                        }

                        HorizontalDivider(Modifier.padding(horizontal = 10.dp))

                        CardSettings(title = "Скрыть навигацию", checkedState = settingsState.navigationVisibility) {
                            handleEvent(SettingsEvent.MakeNavigationInvisible(it))
                            handleEvent(SettingsEvent.SaveSettings)
                        }

                    }
                }

            }
        }
    }
}
