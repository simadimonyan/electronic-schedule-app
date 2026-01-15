package com.mycollege.schedule.feature.settings.ui

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.appwidget.updateAll
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.my.tracker.MyTracker
import com.mycollege.schedule.R
import com.mycollege.schedule.app.activity.ui.state.AppState
import com.mycollege.schedule.app.navigation.Start
import com.mycollege.schedule.feature.settings.ui.components.AboutBottomSheet
import com.mycollege.schedule.feature.settings.ui.components.CardSettings
import com.mycollege.schedule.feature.settings.ui.components.ContactLabel
import com.mycollege.schedule.feature.settings.ui.components.CopyrightView
import com.mycollege.schedule.feature.settings.ui.components.SegmentedButton
import com.mycollege.schedule.feature.settings.ui.components.ThemeToggleButton
import com.mycollege.schedule.feature.settings.ui.state.SettingsEvent
import com.mycollege.schedule.feature.settings.ui.state.SettingsState
import com.mycollege.schedule.feature.settings.ui.state.SettingsViewModel
import com.mycollege.schedule.feature.widgets.ui.ScheduleLargeWidget
import com.mycollege.schedule.feature.widgets.ui.ScheduleSmallWidget
import com.mycollege.schedule.shared.ui.theme.LocalAppDarkTheme
import com.mycollege.schedule.shared.ui.theme.background
import com.mycollege.schedule.shared.ui.theme.backgroundDark
import com.mycollege.schedule.shared.ui.theme.buttons
import com.mycollege.schedule.shared.ui.theme.secondaryDark
import com.mycollege.schedule.shared.ui.theme.tertiaryDark
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Preview
@Composable
fun SettingsPreview() {
    SettingsContent(AppState(), SettingsState(), {}, {}, {}, {}, {})
}

@SuppressLint("ContextCastToActivity")
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    navController: NavHostController
) {

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val settingsState by viewModel.settingsStateHolder.settingsState.collectAsState()
    val appState by viewModel.appStateHolder.appState.collectAsState()
    var aboutBottomSheet by remember { mutableStateOf(false) }

    val handleEvent: (SettingsEvent) -> Unit = { event ->
        viewModel.handleEvent(event)
    }

    val onExit: () -> Unit = {
        MyTracker.trackEvent("Выйти из экрана настроек")
        navController.navigate(Start) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    LaunchedEffect(Unit) {
        viewModel.settingsStateHolder.networkSynchronizationIssues.collect {
            Toast.makeText(context, "Проблема подключения к серверу", Toast.LENGTH_LONG).show()
        }
    }

    val onThemeChange: () -> Unit = {
        scope.launch {
            viewModel.settingsStateHolder.sendThemeChange()
            viewModel.appStateHolder.updateDarkTheme(!appState.darkTheme)
            viewModel.handleEvent(SettingsEvent.SaveSettings)
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

    SettingsContent(appState, settingsState, handleEvent, onAboutToggle, openCopyrights, onThemeChange, onExit)

    if (showDialog) {
        CopyrightView(
            onDisposable = { showDialog = !showDialog }
        )
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(
    appState: AppState,
    settingsState: SettingsState,
    handleEvent: (SettingsEvent) -> Unit,
    onAboutToggle: () -> Unit,
    openCopyrights: () -> Unit,
    onThemeChange: () -> Unit,
    onExit: () -> Unit
) {

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val darkMode = LocalAppDarkTheme.current

    Scaffold(modifier = Modifier
        .fillMaxSize(), containerColor = if (darkMode) backgroundDark else background,
        bottomBar = {
            ContactLabel()
        },
        topBar = {
            TopAppBar(
                title = {
                    Text("Настройки", color = if (darkMode) Color.White else Color.Black, fontWeight = FontWeight.Medium)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (darkMode) backgroundDark else background
                ),
                navigationIcon = {
                    IconButton(onClick = {
                        onExit()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад", tint = if (darkMode) Color.White else Color.Black)
                    }
                },
                actions = {
                    ThemeToggleButton(
                        Modifier.padding(end = 20.dp), {
                            onThemeChange()
                            scope.launch {
                                ScheduleLargeWidget().updateAll(context)
                                ScheduleSmallWidget().updateAll(context)
                            }
                        },
                        appState.darkTheme
                    )
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
                if (it) MyTracker.trackEvent("Переключить расписание на вторую неделю")
                else MyTracker.trackEvent("Переключить расписание на первую неделю")
                scope.launch {
                    delay(500)
                    ScheduleLargeWidget().updateAll(context)
                    ScheduleSmallWidget().updateAll(context)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp, 0.dp, 20.dp, 0.dp)
                    .wrapContentHeight(),
                elevation = CardDefaults.cardElevation(2.dp),
                colors = CardDefaults.cardColors(containerColor = if (darkMode) secondaryDark else Color.White)
            ) {
                Column(modifier = Modifier.padding(10.dp, 0.dp)) {

                    Text(
                        text = "Расписание",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(start = 7.dp, top = 10.dp),
                        color = if (darkMode) Color.White else Color.Black
                    )

                    CardSettings(painterResource(R.drawable.sync), title = "Синхрон. неделю", checkedState = settingsState.synchronizeWeekParity) {
                        handleEvent(SettingsEvent.SynchronizeWeekParity(it))
                        handleEvent(SettingsEvent.SaveSettings)
                        if (settingsState.synchronizeWeekParity) MyTracker.trackEvent("Включить синхронизацию недели")
                        else MyTracker.trackEvent("Выключить синхронизацию недели")
                        scope.launch {
                            delay(500)
                            ScheduleLargeWidget().updateAll(context)
                            ScheduleSmallWidget().updateAll(context)
                        }
                    }

                    HorizontalDivider(Modifier.padding(start = 45.dp, end = 10.dp), color = if (darkMode) tertiaryDark else Color.LightGray)

                    CardSettings(Icons.Default.Notifications, title = "Уведомления", checkedState = settingsState.notificationsEnabled) {
                        handleEvent(SettingsEvent.MakeNotificationsEnabled(it))
                        handleEvent(SettingsEvent.SaveSettings)
                        if (settingsState.notificationsEnabled) MyTracker.trackEvent("Включить уведомления расписания")
                        else MyTracker.trackEvent("Выключить уведомления расписания")
                    }

                    HorizontalDivider(Modifier.padding(start = 45.dp, end = 10.dp), color = if (darkMode) tertiaryDark else Color.LightGray)

                    CardSettings(painterResource(R.drawable.week), title = "Показать неделю", checkedState = settingsState.fullWeekVisibility) {
                        handleEvent(SettingsEvent.MakeScheduleWeekFull(it))
                        handleEvent(SettingsEvent.SaveSettings)
                        if (settingsState.fullWeekVisibility) MyTracker.trackEvent("Включить расписание на неделю")
                        else MyTracker.trackEvent("Выключить расписание на неделю")
                    }

                    HorizontalDivider(Modifier.padding(start = 45.dp, end = 10.dp), color = if (darkMode) tertiaryDark else Color.LightGray)

                    CardSettings(Icons.Default.Menu, title = "Скрыть навигацию", checkedState = settingsState.navigationInvisibility) {
                        handleEvent(SettingsEvent.MakeNavigationInvisible(it))
                        handleEvent(SettingsEvent.SaveSettings)
                        if (settingsState.navigationInvisibility) MyTracker.trackEvent("Выключить видимость навигации")
                        else MyTracker.trackEvent("Включить видимость навигации")
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
                colors = CardDefaults.cardColors(containerColor = if (darkMode) secondaryDark else Color.White),
            ) {

                Text(
                    text = "Помощь",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(start = 17.dp, top = 10.dp),
                    color = if (darkMode) Color.White else Color.Black
                )

                Row(
                    modifier = Modifier
                        .clickable {
                            openCopyrights()
                            MyTracker.trackEvent("Открыть вкладку авторское право")
                        }
                        .height(60.dp)
                        .fillMaxWidth()
                        .padding(start = 15.dp, end = 15.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painterResource(R.drawable.copyright),
                        contentDescription = "Info",
                        tint = buttons,
                        modifier = Modifier.size(27.dp)
                    )
                    Spacer(modifier = Modifier.width(15.dp))
                    Text(
                        text = "Авторское право",
                        fontSize = 15.sp,
                        color = if (darkMode) Color.White else Color.Black
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(start = 55.dp, end = 20.dp), color = if (darkMode) tertiaryDark else Color.LightGray)

                Row(
                    modifier = Modifier
                        .clickable {
                            onAboutToggle()
                            MyTracker.trackEvent("Открыть вкладку о приложении")
                        }
                        .height(60.dp)
                        .fillMaxWidth()
                        .padding(start = 15.dp, end = 15.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.Info,
                        contentDescription = "Info",
                        tint = buttons,
                        modifier = Modifier.size(27.dp)
                    )
                    Spacer(modifier = Modifier.width(15.dp))
                    Text(
                        text = "О приложении",
                        fontSize = 15.sp,
                        color = if (darkMode) Color.White else Color.Black
                    )
                }
            }

        }
    }
}
