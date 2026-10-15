package com.mycollege.schedule.feature.settings.ui

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.appwidget.updateAll
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.my.tracker.MyTracker
import com.mycollege.schedule.BuildConfig
import com.mycollege.schedule.R
import com.mycollege.schedule.app.activity.ui.state.AppState
import com.mycollege.schedule.app.navigation.MainTabs
import com.mycollege.schedule.app.navigation.Start
import com.mycollege.schedule.feature.groups.ui.state.GroupState
import com.mycollege.schedule.feature.settings.ui.components.AboutScreen
import com.mycollege.schedule.feature.settings.ui.components.CopyrightView
import com.mycollege.schedule.feature.settings.ui.components.WebViewScreen
import com.mycollege.schedule.feature.settings.ui.state.SettingsEvent
import com.mycollege.schedule.feature.settings.ui.state.SettingsState
import com.mycollege.schedule.feature.settings.ui.state.SettingsViewModel
import com.mycollege.schedule.feature.surveys.ui.SurveyHost
import com.mycollege.schedule.feature.surveys.ui.state.SurveyEvent
import com.mycollege.schedule.feature.surveys.ui.state.SurveyViewModel
import com.mycollege.schedule.feature.widgets.ui.ScheduleLargeWidget
import com.mycollege.schedule.feature.widgets.ui.ScheduleSmallWidget
import com.mycollege.schedule.shared.ui.design.FloatingNavBar
import com.mycollege.schedule.shared.ui.design.Glow
import com.mycollege.schedule.shared.ui.design.MaIcon
import com.mycollege.schedule.shared.ui.design.MaIcons
import com.mycollege.schedule.shared.ui.design.MaInsets
import com.mycollege.schedule.shared.ui.design.MaScreen
import com.mycollege.schedule.shared.ui.design.MaSegmented
import com.mycollege.schedule.shared.ui.design.MaSwitch
import com.mycollege.schedule.shared.ui.design.MainTab
import com.mycollege.schedule.shared.ui.design.MatIconButton
import com.mycollege.schedule.shared.ui.design.ScreenBackdrop
import com.mycollege.schedule.shared.ui.design.SectionLabel
import com.mycollege.schedule.shared.ui.design.SettingsRow
import com.mycollege.schedule.shared.ui.design.maCard
import com.mycollege.schedule.shared.ui.design.maClick
import com.mycollege.schedule.shared.ui.design.maShadow
import com.mycollege.schedule.shared.ui.design.navBarBottom
import com.mycollege.schedule.shared.ui.design.statusBarTop
import com.mycollege.schedule.shared.ui.theme.DarkMaColors
import com.mycollege.schedule.shared.ui.theme.LightMaColors
import com.mycollege.schedule.shared.ui.theme.Ma
import com.mycollege.schedule.shared.ui.theme.MaBrand
import com.mycollege.schedule.shared.ui.theme.MaShadow
import com.mycollege.schedule.shared.ui.theme.MaType
import com.mycollege.schedule.shared.ui.theme.ScheduleTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.rustore.sdk.remoteconfig.RemoteConfigClient

@Preview
@Composable
fun SettingsPreview() {
    ScheduleTheme(false) {
        SettingsContent(AppState(), SettingsState(), GroupState(), {}, {}, {}, {}, {}, rememberLazyListState())
    }
}

@SuppressLint("ContextCastToActivity")
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    navController: NavHostController,
    scrollState: LazyListState,
    groupState: GroupState,
    surveyViewModel: SurveyViewModel = hiltViewModel()
) {

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val settingsState by viewModel.settingsStateHolder.settingsState.collectAsState()
    val appState by viewModel.appStateHolder.appState.collectAsState()
    var showAbout by remember { mutableStateOf(false) }

    val handleEvent: (SettingsEvent) -> Unit = { event ->
        viewModel.handleEvent(event)
    }

    // вернуться на стартовый экран: страница пейджера 0 — выбор группы, 1 — расписание
    val goStart: (Int) -> Unit = { page ->
        viewModel.appStateHolder.updateIndex(page)
        navController.navigate(Start) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    val onSelectTab: (MainTab) -> Unit = { tab ->
        if (tab != MainTab.Settings) {
            MyTracker.trackEvent("Выйти из экрана настроек")
            MainTabs.scheduleTab = tab
            goStart(1)
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
        scope.launch {
            ScheduleLargeWidget().updateAll(context)
            ScheduleSmallWidget().updateAll(context)
        }
    }

    var showDialogCopyrights by remember { mutableStateOf(false) }
    var showDialogPrivacyPolicy by remember { mutableStateOf(false) }
    var showDialogUserAgreement by remember { mutableStateOf(false) }

    // «Оставить отзыв»: текущий опрос или «Сейчас опросов нет»
    val openFeedback: () -> Unit = {
        surveyViewModel.handleEvent(SurveyEvent.OpenManually)
    }

    val openAbout: () -> Unit = {
        MyTracker.trackEvent("Открыть вкладку о приложении")
        showAbout = true
    }

    SettingsContent(
        appState, settingsState, groupState, handleEvent, onThemeChange,
        onOpenSelect = { goStart(0) },
        openFeedback = openFeedback,
        openAbout = openAbout,
        scrollState = scrollState,
        onSelectTab = onSelectTab,
        onSearch = { goStart(0) },
        onBack = { MyTracker.trackEvent("Выйти из экрана настроек"); goStart(1) }
    )

    if (showAbout) {
        AboutScreen(
            onBack = { showAbout = false },
            openPrivacyPolicy = { showDialogPrivacyPolicy = true },
            openUserAgreement = { showDialogUserAgreement = true },
            openCopyrights = { showDialogCopyrights = true }
        )
    }

    SurveyHost(surveyViewModel)

    var privacyPolicy by remember { mutableStateOf("https://myimsit.ru") }
    var userAgreement by remember { mutableStateOf("https://myimsit.ru") }

    RemoteConfigClient.instance.getRemoteConfig().addOnSuccessListener { rc ->
        privacyPolicy = rc.getString("PrivacyPolicy")
        userAgreement = rc.getString("UserAgreement")
    }

    if (showDialogCopyrights) {
        CopyrightView(
            onDisposable = { showDialogCopyrights = !showDialogCopyrights }
        )
    }
    else if (showDialogPrivacyPolicy) {
        Box(Modifier.fillMaxSize()) {
            WebViewScreen("Политика конфиденциальности", privacyPolicy) {
                showDialogPrivacyPolicy = !showDialogPrivacyPolicy
            }
        }
    }
    else if (showDialogUserAgreement) {
        Box(Modifier.fillMaxSize()) {
            WebViewScreen("Пользовательское соглашение", userAgreement) {
                showDialogUserAgreement = !showDialogUserAgreement
            }
        }
    }

}

/**
 * Настройки (Settings.dc.html): профиль расписания, переключатели, уведомления, тема
 */
@Composable
fun SettingsContent(
    appState: AppState,
    settingsState: SettingsState,
    groupState: GroupState,
    handleEvent: (SettingsEvent) -> Unit,
    onThemeChange: () -> Unit,
    onOpenSelect: () -> Unit,
    openFeedback: () -> Unit,
    openAbout: () -> Unit,
    scrollState: LazyListState,
    onSelectTab: (MainTab) -> Unit = {},
    onSearch: () -> Unit = {},
    onBack: () -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val c = Ma.colors
    val navHidden = settingsState.navigationInvisibility

    val updateWidgets: () -> Unit = {
        scope.launch {
            delay(500)
            ScheduleLargeWidget().updateAll(context)
            ScheduleSmallWidget().updateAll(context)
        }
    }

    MaScreen {
        ScreenBackdrop(height = 300.dp, glows = listOf(Glow(1f, 0f, 0.7f, 0.6f, c.accentSoft2)), grid = false)

        LazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp, end = 16.dp,
                top = statusBarTop() + if (navHidden) MaInsets.contentTop else 18.dp,
                bottom = (if (navHidden) 48.dp else MaInsets.navScrollBottom) + navBarBottom()
            )
        ) {
            item {
                Text("Настройки", style = MaType.display(36.sp, FontWeight.ExtraBold, 38.sp), color = c.fg)
                ProfileCard(appState.studentMode, groupState, onOpenSelect)
            }

            item {
                SectionLabel("Расписание", Modifier.padding(top = 24.dp, bottom = 8.dp))
                Column(Modifier.fillMaxWidth().maCard(16.dp)) {
                    SettingsRow(MaIcons.refresh, "Определять неделю", sub = "Чётность приходит с сервера академии") {
                        MaSwitch(settingsState.synchronizeWeekParity, {
                            handleEvent(SettingsEvent.SynchronizeWeekParity(it))
                            handleEvent(SettingsEvent.SaveSettings)
                            if (settingsState.synchronizeWeekParity) MyTracker.trackEvent("Включить синхронизацию недели")
                            else MyTracker.trackEvent("Выключить синхронизацию недели")
                            updateWidgets()
                        })
                    }
                    SettingsRow(MaIcons.calendar, "Текущая неделя", divider = true, alpha = if (settingsState.synchronizeWeekParity) 0.45f else 1f) {
                        MaSegmented(
                            labels = listOf("1", "2"),
                            selected = if (settingsState.weekCount) 1 else 0,
                            onSelect = { i ->
                                val second = i == 1
                                handleEvent(SettingsEvent.MakeWeekCountDifferent(second))
                                handleEvent(SettingsEvent.SaveSettings)
                                if (second) MyTracker.trackEvent("Переключить расписание на вторую неделю")
                                else MyTracker.trackEvent("Переключить расписание на первую неделю")
                                updateWidgets()
                            },
                            enabled = !settingsState.synchronizeWeekParity,
                            segmentMinWidth = 34.dp
                        )
                    }
                    SettingsRow(MaIcons.grid, "Открывать сразу неделю", sub = "Вместо экрана «Сегодня»", divider = true) {
                        MaSwitch(settingsState.fullWeekVisibility, {
                            handleEvent(SettingsEvent.MakeScheduleWeekFull(it))
                            handleEvent(SettingsEvent.SaveSettings)
                            if (settingsState.fullWeekVisibility) MyTracker.trackEvent("Включить расписание на неделю")
                            else MyTracker.trackEvent("Выключить расписание на неделю")
                        })
                    }
                    SettingsRow(MaIcons.nav, "Нижняя панель", sub = if (navHidden) "Настройки — кнопкой в шапке расписания" else "Разделы и поиск внизу экрана", divider = true) {
                        MaSwitch(!navHidden, {
                            handleEvent(SettingsEvent.MakeNavigationInvisible(!it))
                            handleEvent(SettingsEvent.SaveSettings)
                            if (settingsState.navigationInvisibility) MyTracker.trackEvent("Выключить видимость навигации")
                            else MyTracker.trackEvent("Включить видимость навигации")
                        })
                    }
                }
            }

            item {
                SectionLabel("Уведомления", Modifier.padding(top = 24.dp, bottom = 8.dp))
                Column(Modifier.fillMaxWidth().maCard(16.dp)) {
                    SettingsRow(MaIcons.bell, "Напоминать о парах", sub = "За 5 минут до начала") {
                        MaSwitch(settingsState.notificationsEnabled, {
                            handleEvent(SettingsEvent.MakeNotificationsEnabled(it))
                            handleEvent(SettingsEvent.SaveSettings)
                            if (settingsState.notificationsEnabled) MyTracker.trackEvent("Включить уведомления расписания")
                            else MyTracker.trackEvent("Выключить уведомления расписания")
                        })
                    }
                }
            }

            item {
                SectionLabel("Ещё", Modifier.padding(top = 24.dp, bottom = 8.dp))
                Column(Modifier.fillMaxWidth().maCard(16.dp)) {
                    SettingsRow(MaIcons.message, "Оставить отзыв", onClick = openFeedback) {
                        MaIcon(MaIcons.chevRight, c.faint, 16.dp, 2f)
                    }
                    SettingsRow(MaIcons.info, "О приложении", divider = true, onClick = openAbout) {
                        Text("v${BuildConfig.VERSION_NAME}", style = MaType.mono(12.sp, FontWeight.Normal), color = c.faint)
                        MaIcon(MaIcons.chevRight, c.faint, 16.dp, 2f)
                    }
                }
            }

            item {
                SectionLabel("Оформление", Modifier.padding(top = 24.dp, bottom = 8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeCard("Светлая", dark = false, selected = !appState.darkTheme, Modifier.weight(1f)) { if (appState.darkTheme) onThemeChange() }
                    ThemeCard("Тёмная", dark = true, selected = appState.darkTheme, Modifier.weight(1f)) { if (!appState.darkTheme) onThemeChange() }
                }
                Text(
                    "Моя Академия ${BuildConfig.VERSION_NAME} · Академия ИМСИТ · данные с imsit.ru",
                    style = MaType.body(12.sp), color = c.faint, textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 20.dp)
                )
            }
        }

        if (navHidden) Row(Modifier.padding(start = 16.dp, top = statusBarTop() + MaInsets.headTop)) {
            MatIconButton(MaIcons.back, "Назад", onBack, iconSize = 20.dp, stroke = 2.2f)
        } else Box(Modifier.align(Alignment.BottomCenter)) {
            FloatingNavBar(current = MainTab.Settings, onSelect = onSelectTab, onSearch = onSearch)
        }
    }
}

/** Карточка «Моё расписание»: аватар-иллюстрация, группа или преподаватель, «Сменить» */
@Composable
private fun ProfileCard(student: Boolean, groupState: GroupState, onClick: () -> Unit) {
    val c = Ma.colors
    val chosen = if (student) groupState.group != "Выбрать группу" else groupState.teacher != "Выбрать преподавателя"
    val title = when {
        !chosen && student -> "Группа не выбрана"
        !chosen -> "Преподаватель не выбран"
        student -> groupState.group
        else -> groupState.teacher
    }
    val sub = when {
        !chosen -> "Нажмите, чтобы выбрать"
        student -> listOf(groupState.level, groupState.course).filter { it.isNotBlank() }.joinToString(" · ")
        else -> groupState.department
    }
    Row(
        Modifier
            .padding(top = 16.dp)
            .fillMaxWidth()
            .maCard(20.dp, c.shSm)
            .maClick(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Image(
            painterResource(if (student) R.drawable.ill_avatar_student else R.drawable.ill_avatar_teacher), null,
            contentScale = ContentScale.Crop,
            alignment = androidx.compose.ui.BiasAlignment(0f, -0.76f),
            modifier = Modifier.size(56.dp).clip(CircleShape).background(c.accentSoft)
        )
        Column(Modifier.weight(1f)) {
            Text("МОЁ РАСПИСАНИЕ", style = MaType.eyebrow(9.5.sp), color = c.faint)
            Text(title, style = MaType.displayFor(title, 18.sp), color = c.fg, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 2.dp))
            if (sub.isNotBlank()) Text(sub, style = MaType.body(12.5.sp), color = c.soft, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 1.dp))
        }
        Box(
            Modifier.height(32.dp).clip(CircleShape).border(1.dp, c.accentBorder, CircleShape).padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) { Text(if (chosen) "Сменить" else "Выбрать", style = MaType.body(12.5.sp, FontWeight.SemiBold), color = c.accent) }
    }
}

/** Карточка выбора темы с миниатюрой экрана */
@Composable
private fun ThemeCard(label: String, dark: Boolean, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val c = Ma.colors
    val preview = if (dark) DarkMaColors else LightMaColors
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier
            .then(if (selected) Modifier.maShadow(MaShadow(c.accentSoft, 0f, 0f, 3f), 16.dp) else Modifier.maShadow(c.shXs, 16.dp))
            .clip(shape)
            .background(c.surface)
            .border(if (selected) 2.dp else 1.dp, if (selected) MaBrand.primary else c.border, shape)
            .maClick(onClick = onClick)
            .padding(start = 6.dp, end = 6.dp, top = 6.dp, bottom = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            Modifier.fillMaxWidth().height(78.dp).clip(RoundedCornerShape(11.dp)).background(preview.bg)
                .border(1.dp, c.border, RoundedCornerShape(11.dp)).padding(8.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(Modifier.fillMaxWidth().height(22.dp).clip(RoundedCornerShape(7.dp)).background(Brush.linearGradient(listOf(MaBrand.primary, MaBrand.sky))))
                Box(Modifier.fillMaxWidth().padding(end = 14.dp).height(13.dp).clip(RoundedCornerShape(4.dp)).background(preview.surface))
                Box(Modifier.fillMaxWidth().padding(end = 26.dp).height(13.dp).clip(RoundedCornerShape(4.dp)).background(preview.surface))
            }
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaType.body(13.sp, FontWeight.SemiBold), color = c.fg, modifier = Modifier.weight(1f))
            Box(
                Modifier.size(18.dp).clip(CircleShape)
                    .border(if (selected) 5.dp else 1.5.dp, if (selected) MaBrand.primary else c.borderStrong, CircleShape)
            )
        }
    }
}

