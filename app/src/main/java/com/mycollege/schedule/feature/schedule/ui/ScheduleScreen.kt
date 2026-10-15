package com.mycollege.schedule.feature.schedule.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.my.tracker.MyTracker
import com.mycollege.schedule.app.activity.domain.models.LoadingState
import com.mycollege.schedule.app.activity.ui.state.AppState
import com.mycollege.schedule.feature.groups.ui.state.GroupState
import com.mycollege.schedule.feature.schedule.data.models.DataClasses
import com.mycollege.schedule.feature.schedule.domain.calendar.WeekParity
import com.mycollege.schedule.feature.schedule.ui.components.CalendarSheet
import com.mycollege.schedule.feature.schedule.ui.components.DatePickerSheet
import com.mycollege.schedule.feature.schedule.ui.components.ParityChip
import com.mycollege.schedule.feature.schedule.ui.components.ScheduleHeader
import com.mycollege.schedule.feature.schedule.ui.components.TodayContent
import com.mycollege.schedule.feature.schedule.ui.components.WeekContent
import com.mycollege.schedule.feature.schedule.ui.components.ScheduleErrorContent
import com.mycollege.schedule.feature.schedule.ui.components.NoChoiceContent
import com.mycollege.schedule.feature.schedule.ui.state.ScheduleEvent
import com.mycollege.schedule.feature.schedule.ui.state.ScheduleState
import com.mycollege.schedule.feature.schedule.ui.state.ScheduleViewModel
import com.mycollege.schedule.feature.settings.ui.state.SettingsState
import com.mycollege.schedule.shared.ui.design.MaIcons
import com.mycollege.schedule.shared.ui.design.MaInsets
import com.mycollege.schedule.shared.ui.design.MaScreen
import com.mycollege.schedule.shared.ui.design.MaToastHost
import com.mycollege.schedule.shared.ui.design.MainTab
import com.mycollege.schedule.shared.ui.design.MatIconButton
import com.mycollege.schedule.shared.ui.design.ScreenBackdrop
import com.mycollege.schedule.shared.ui.design.navBarBottom
import com.mycollege.schedule.shared.ui.design.rememberMaToast
import com.mycollege.schedule.shared.ui.design.statusBarTop
import com.mycollege.schedule.shared.ui.theme.ScheduleTheme
import java.time.LocalDate

/** Действия экрана расписания, которые ведут в другие разделы */
class ScheduleActions(
    val onOpenSelect: () -> Unit = {},
    val onOpenSettings: () -> Unit = {},
    val onSelectTab: (MainTab) -> Unit = {},
    /** повторная загрузка расписания выбранной группы (GroupEvent.ChooseGroup) */
    val onRetry: () -> Unit = {},
    /** повторная проверка связи (GroupEvent.Display) */
    val onRetryNetwork: () -> Unit = {},
)

@Preview
@Composable
fun SchedulePreview() {
    ScheduleTheme(false) {
        ScheduleContent(SettingsState(), AppState(), ScheduleState(), LoadingState(), GroupState(), MainTab.Today, ScheduleActions()) {}
    }
}

@Composable
fun ScheduleScreen(
    viewModel: ScheduleViewModel = hiltViewModel(),
    tab: MainTab,
    actions: ScheduleActions,
) {
    val appState by viewModel.appStateHolder.appState.collectAsState()
    val settingsState by viewModel.settingsStateHolder.settingsState.collectAsState()
    val scheduleState by viewModel.scheduleStateHolder.scheduleState.collectAsState()
    val parseState by viewModel.parserStateHolder.loadingState.collectAsState()
    val groupState by viewModel.groupStateHolder.groupState.collectAsState()

    ScheduleContent(settingsState, appState, scheduleState, parseState, groupState, tab, actions) { viewModel.handleEvent(it) }
}

@Composable
fun ScheduleContent(
    settingsState: SettingsState,
    appState: AppState,
    scheduleState: ScheduleState,
    parseState: LoadingState,
    groupState: GroupState,
    tab: MainTab,
    actions: ScheduleActions,
    handleEvent: (ScheduleEvent) -> Unit,
) {
    val context = LocalContext.current
    val toast = rememberMaToast()

    // пока не проверен кеш, показываем загрузку, а не «ошибку» (иначе она мелькает при запуске)
    var cacheChecked by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        handleEvent(ScheduleEvent.ShowIfCachedSchedule)
        cacheChecked = true
    }

    val student = appState.studentMode
    val built = (student && scheduleState.buildScheduleGroupModeFlag) || (!student && scheduleState.buildScheduleTeacherModeFlag)
    val owner = if (student) groupState.group else groupState.teacher
    val chosen = if (student) owner != "Выбрать группу" else owner != "Выбрать преподавателя"
    val parity = if (settingsState.weekCount) 2 else 1

    // единая точка обновления: расписание на сегодня (как раньше) и неделя для полосы дней и вкладки «Неделя»
    LaunchedEffect(built, settingsState.weekCount, owner, student) {
        if (built) {
            handleEvent(ScheduleEvent.WeekCountChanged)
            handleEvent(ScheduleEvent.ShowWeekSchedule)
            handleEvent(ScheduleEvent.LoadCalendar)
        }
    }

    // пары на любую дату: текущая неделя — из актуальных «Сегодня»/«Неделя», остальное — из календаря
    // (дата → чётность недели → день недели → пары шаблона; у заочников — занятия этой даты)
    val today = LocalDate.now()
    val thisMonday = WeekParity.monday(today)
    val calendar = scheduleState.calendar
    val lessonsOn: (LocalDate) -> List<DataClasses.Lesson> = { d ->
        when {
            d == today -> scheduleState.todayLessons
            WeekParity.monday(d) == thisMonday && d.dayOfWeek.value <= 6 -> scheduleState.weekLessons[d.dayOfWeek.value].orEmpty()
            else -> calendar.lessonsOn(d, today, parity)
        }
    }
    val parityOf: (LocalDate) -> Int = { d -> WeekParity.of(d, today, parity) }
    val nextStudyDate: (LocalDate) -> LocalDate? = { from ->
        (1L..120L).asSequence().map { from.plusDays(it) }.firstOrNull { lessonsOn(it).isNotEmpty() }
    }

    val copyEios: (String) -> Unit = { link ->
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("eios", link))
        toast.show("Ссылка eios скопирована", "вставьте в браузер")
    }

    var selectedEpoch by rememberSaveable { mutableLongStateOf(LocalDate.now().toEpochDay()) }
    val selectedDate = LocalDate.ofEpochDay(selectedEpoch)
    // переход к дате из календаря на вкладке «Неделя»: не сбрасывать выбранную дату при смене вкладки
    var keepDate by rememberSaveable { mutableStateOf(false) }
    // вкладка «Сегодня» всегда открывается на текущем дне
    LaunchedEffect(tab) {
        if (tab == MainTab.Today) {
            if (!keepDate) selectedEpoch = LocalDate.now().toEpochDay()
            keepDate = false
        }
    }
    val openDate: (LocalDate) -> Unit = { d ->
        selectedEpoch = d.toEpochDay()
        if (tab != MainTab.Today) {
            keepDate = true
            actions.onSelectTab(MainTab.Today)
        }
    }
    var calendarOpen by rememberSaveable { mutableStateOf(false) }
    var datePickerOpen by rememberSaveable { mutableStateOf(false) }

    MaScreen {
        ScreenBackdrop(height = if (tab == MainTab.Week) 360.dp else 380.dp, gridHeight = if (tab == MainTab.Week) 320.dp else 340.dp)

        val showContent = built || parseState.scheduleLoading || !cacheChecked
        AnimatedContent(
            targetState = if (!showContent) -1 else tab.ordinal,
            transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(150)) },
            label = "tab"
        ) { target ->
            val scroll = rememberScrollState()
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(scroll)
                    .padding(start = 16.dp, end = 16.dp, top = statusBarTop() + MaInsets.contentTop, bottom = MaInsets.navScrollBottom + navBarBottom())
            ) {
                when (target) {
                    -1 -> if (chosen) ScheduleErrorContent(actions.onRetry) else NoChoiceContent(student, actions.onOpenSelect)
                    MainTab.Week.ordinal -> WeekContent(
                        weekLessons = scheduleState.weekLessons,
                        todayLessons = scheduleState.todayLessons,
                        parity = parity,
                        loading = parseState.scheduleLoading || !built,
                        onCopyEios = copyEios,
                        onOpenDatePicker = { datePickerOpen = true }
                    )
                    else -> TodayContent(
                        lessonsOn = lessonsOn,
                        parityOf = parityOf,
                        teacherMode = !student,
                        loading = parseState.scheduleLoading || !built,
                        offline = parseState.networkIssues && built,
                        selectedDate = selectedDate,
                        onSelectDate = { selectedEpoch = it.toEpochDay() },
                        onOpenDatePicker = { datePickerOpen = true },
                        nextStudyDate = nextStudyDate,
                        onOpenWeek = { actions.onSelectTab(MainTab.Week) },
                        onRetry = actions.onRetryNetwork,
                        onCopyEios = copyEios
                    )
                }
            }
        }

        // шапка
        ScheduleHeader(
            title = if (chosen) owner else if (student) "Выбрать группу" else "Выбрать преподавателя",
            teacher = !student && chosen,
            onOpenSelect = {
                MyTracker.trackEvent("Перейти на главный экран")
                actions.onOpenSelect()
            },
            modifier = Modifier.padding(top = statusBarTop() + MaInsets.headTop)
        ) {
            if (built && tab == MainTab.Today) ParityChip(parityOf(selectedDate)) { actions.onSelectTab(MainTab.Week) }
            if (built) MatIconButton(MaIcons.calendar, "Календарь", { calendarOpen = true })
            if (settingsState.navigationInvisibility) MatIconButton(MaIcons.settings, "Настройки", actions.onOpenSettings)
        }

        MaToastHost(toast)
    }

    if (calendarOpen) CalendarSheet(
        initial = if (tab == MainTab.Today) selectedDate else today,
        today = today,
        lessonsOn = lessonsOn,
        sessionMode = calendar.datedOnly,
        onShow = openDate,
        onDismiss = { calendarOpen = false }
    )
    if (datePickerOpen) DatePickerSheet(
        initial = selectedDate,
        today = today,
        hasLessons = { lessonsOn(it).isNotEmpty() },
        onPick = openDate,
        onDismiss = { datePickerOpen = false }
    )
}
