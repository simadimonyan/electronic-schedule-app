package com.mycollege.schedule.feature.groups.ui

import android.annotation.SuppressLint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.glance.appwidget.updateAll
import androidx.hilt.navigation.compose.hiltViewModel
import com.my.tracker.MyTracker
import com.mycollege.schedule.BuildConfig
import com.mycollege.schedule.R
import com.mycollege.schedule.app.activity.domain.models.LoadingState
import com.mycollege.schedule.app.activity.ui.state.AppState
import com.mycollege.schedule.core.ads.YandexAdsListener
import com.mycollege.schedule.feature.groups.ui.state.GroupEvent
import com.mycollege.schedule.feature.groups.ui.state.GroupState
import com.mycollege.schedule.feature.groups.ui.state.GroupViewModel
import com.mycollege.schedule.feature.schedule.ui.components.dashedBorder
import com.mycollege.schedule.feature.widgets.ui.ScheduleLargeWidget
import com.mycollege.schedule.feature.widgets.ui.ScheduleSmallWidget
import com.mycollege.schedule.shared.ui.design.EyebrowPill
import com.mycollege.schedule.shared.ui.design.MaIcon
import com.mycollege.schedule.shared.ui.design.MaIcons
import com.mycollege.schedule.shared.ui.design.MaInsets
import com.mycollege.schedule.shared.ui.design.MaScreen
import com.mycollege.schedule.shared.ui.design.MaSegmented
import com.mycollege.schedule.shared.ui.design.MatIconButton
import com.mycollege.schedule.shared.ui.design.ScreenBackdrop
import com.mycollege.schedule.shared.ui.design.Glow
import com.mycollege.schedule.shared.ui.design.SectionLabel
import com.mycollege.schedule.shared.ui.design.maCard
import com.mycollege.schedule.shared.ui.design.maClick
import com.mycollege.schedule.shared.ui.design.maShadow
import com.mycollege.schedule.shared.ui.design.navBarBottom
import com.mycollege.schedule.shared.ui.design.statusBarTop
import com.mycollege.schedule.shared.ui.theme.LocalAppDarkTheme
import com.mycollege.schedule.shared.ui.theme.Ma
import com.mycollege.schedule.shared.ui.theme.MaBrand
import com.mycollege.schedule.shared.ui.theme.MaShadow
import com.mycollege.schedule.shared.ui.theme.MaType
import com.mycollege.schedule.shared.ui.theme.ScheduleTheme
import com.yandex.mobile.ads.banner.BannerAdSize
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.rustore.sdk.remoteconfig.RemoteConfigClient

private const val NO_GROUP = "Выбрать группу"
private const val NO_TEACHER = "Выбрать преподавателя"
private const val ALL_LEVELS = "Все уровни"
private const val ALL_DEPARTMENTS = "Все кафедры"

@Preview
@Composable
fun GroupPreview() {
    val pagerState = rememberPagerState(initialPage = 0) { 2 }
    ScheduleTheme(false) {
        GroupContent(
            {}, {}, GroupState(groupsToDisplay = listOf("24-Бк-ПИ-01", "24-Бк-ПИ-02"), coursesToDisplay = listOf("1", "2", "3")),
            AppState(0, false, true), LoadingState(), pagerState, false, true, emptyMap(), emptyMap()
        )
    }
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

/**
 * Выбор группы или преподавателя (Select.dc.html): поиск, режим, фильтры уровня/кафедры, курс,
 * недавние (сохранённые на устройстве) и список
 */
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
    val context = LocalContext.current
    val c = Ma.colors
    val student = changeStudentModeFlag
    var query by rememberSaveable { mutableStateOf("") }
    val q = query.trim()

    LaunchedEffect(Unit) {
        MyTracker.trackEvent("Просмотр рекламы")
    }

    // подгрузка списков при открытии экрана и смене режима (с дневным кешем во ViewModel)
    LaunchedEffect(appState.studentMode) {
        handleEvent(GroupEvent.Display)
    }

    val updateWidgets: () -> Unit = {
        scope.launch {
            delay(500)
            ScheduleLargeWidget().updateAll(context)
            ScheduleSmallWidget().updateAll(context)
        }
    }

    val chosenName = if (student) groupState.group else groupState.teacher
    val hasChoice = if (student) groupState.group != NO_GROUP else groupState.teacher != NO_TEACHER

    val pick: (String) -> Unit = { name ->
        if (student) handleEvent(GroupEvent.UpdateGroup(name)) else handleEvent(GroupEvent.UpdateTeacher(name))
        handleEvent(GroupEvent.ChooseGroup)
        query = ""
        updateAppStateIndex(1)
        updateWidgets()
    }

    val items: List<String> = if (student) groupState.groupsToDisplay else groupState.teachersToDisplay
    val cached = if (student) cachedGroups else cachedTeachers
    val filtered = remember(items, q) { if (q.isEmpty()) items else items.filter { it.contains(q, ignoreCase = true) } }

    MaScreen {
        ScreenBackdrop(height = 320.dp, glows = listOf(Glow(1f, 0f, 0.7f, 0.6f, c.accentSoft2)), grid = false)

        LazyColumn(
            Modifier.fillMaxSize().imePadding(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp, end = 16.dp, top = statusBarTop() + MaInsets.contentTop - 4.dp, bottom = 48.dp + navBarBottom()
            )
        ) {
            item {
                EyebrowPill("Академия ИМСИТ")
                Text("Чьё расписание\nоткрыть?", style = MaType.display(34.sp, FontWeight.ExtraBold, 36.sp), color = c.fg, modifier = Modifier.padding(top = 10.dp))
                SearchBox(query, { query = it }, if (student) "Группа, например ПИ-01" else "Фамилия преподавателя", Modifier.padding(top = 18.dp))
                MaSegmented(
                    labels = listOf("Студент", "Преподаватель"),
                    selected = if (appState.studentMode) 0 else 1,
                    onSelect = { i ->
                        val toStudent = i == 0
                        if (toStudent != appState.studentMode) {
                            query = ""
                            handleEvent(GroupEvent.ChangeStudentMode(toStudent))
                            updateWidgets()
                        }
                    },
                    modifier = Modifier.padding(top = 12.dp).fillMaxWidth(),
                    height = 38.dp, radius = 12.dp, fontSize = 14.sp, fill = true,
                    icons = listOf(MaIcons.cap, MaIcons.user)
                )
            }

            if (q.isEmpty()) item {
                if (student) {
                    val levels = listOf(ALL_LEVELS) + groupState.levelsToDisplay.filter { it.isNotBlank() && it != "null" && it != ALL_LEVELS }
                    FilterTitle("Уровень образования", 16.dp)
                    ChipRow(levels, groupState.level, primary = false) { level ->
                        handleEvent(GroupEvent.UpdateSpeciality(level))
                        handleEvent(GroupEvent.Display)
                        updateWidgets()
                    }
                    if (groupState.coursesToDisplay.isNotEmpty()) {
                        FilterTitle("Курс", 14.dp)
                        ChipRow(groupState.coursesToDisplay.map { "$it курс" }, groupState.course, primary = true) { course ->
                            handleEvent(GroupEvent.UpdateCourse(course))
                            handleEvent(GroupEvent.Display)
                            updateWidgets()
                        }
                    }
                } else {
                    val departments = listOf(ALL_DEPARTMENTS) + groupState.departmentsToDisplay.filter { it.isNotBlank() && it != "null" && it != ALL_DEPARTMENTS }
                    FilterTitle("Кафедра", 16.dp)
                    ChipRow(departments, groupState.department, primary = false) { d ->
                        handleEvent(GroupEvent.UpdateDepartment(d))
                        handleEvent(GroupEvent.Display)
                        updateWidgets()
                    }
                }

                // недавние: расписания, сохранённые на устройстве
                val recents = cached.entries.sortedByDescending { it.value }.map { it.key }.take(2)
                if (recents.isNotEmpty()) {
                    SectionLabel("Недавние", Modifier.padding(top = 22.dp, bottom = 8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        recents.forEach { name ->
                            Column(
                                Modifier.weight(1f).maCard(16.dp).maClick { pick(name) }.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(name, style = MaType.displayFor(name, 14.sp, FontWeight.Bold, tracking = -0.01f), color = c.fg, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(if (name == chosenName && hasChoice) (if (student) "Моя группа" else "Моё расписание") else "Сохранено офлайн", style = MaType.body(11.5.sp), color = c.faint)
                            }
                        }
                        if (recents.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }

            when {
                parserState.chooseConfigurationLoading -> item { LoadingCard(parserState.chooseConfigurationProgress) }
                filtered.isEmpty() -> item { NothingFound(q.isNotEmpty() || items.isNotEmpty()) }
                student -> {
                    val title = if (q.isNotEmpty()) "Найдено" else groupState.level
                    item { SectionLabel(title, Modifier.padding(top = 22.dp, bottom = 8.dp), trailing = "${filtered.size} гр.") }
                    item { ChoiceList(filtered, chosenName.takeIf { hasChoice }, cached, student = true, sub = { if (cached.containsKey(it)) "Сохранено на устройстве" else groupState.course }, onPick = pick) }
                }
                else -> {
                    val byLetter = filtered.groupBy { it.firstOrNull()?.uppercaseChar() ?: '#' }.toSortedMap()
                    byLetter.forEach { (letter, names) ->
                        item(key = "letter-$letter") {
                            SectionLabel(letter.toString(), Modifier.padding(top = 22.dp, bottom = 8.dp))
                            ChoiceList(names, chosenName.takeIf { hasChoice }, cached, student = false, sub = {
                                if (cached.containsKey(it)) "Сохранено на устройстве" else groupState.department.takeIf { d -> d != ALL_DEPARTMENTS } ?: "Преподаватель"
                            }, onPick = pick)
                        }
                    }
                }
            }

            if (showAds) item { AdsBanner(pagerState) }
        }

        // шапка: «назад» к расписанию, если оно уже выбрано
        if (hasChoice) Row(Modifier.padding(start = 16.dp, top = statusBarTop() + MaInsets.headTop)) {
            MatIconButton(MaIcons.back, "Назад", { updateAppStateIndex(1) }, iconSize = 20.dp, stroke = 2.2f)
        }
    }
}

@Composable
private fun SearchBox(value: String, onChange: (String) -> Unit, placeholder: String, modifier: Modifier = Modifier) {
    val c = Ma.colors
    val focus = LocalFocusManager.current
    val active = value.isNotBlank()
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier
            .fillMaxWidth()
            .height(52.dp)
            .then(if (active) Modifier.maShadow(MaShadow(c.accentSoft, 0f, 0f, 3f), 16.dp) else Modifier.maShadow(c.shXs, 16.dp))
            .clip(shape)
            .background(c.surface)
            .border(1.5.dp, if (active) c.accent else c.borderStrong, shape)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MaIcon(MaIcons.search, c.soft, 19.dp, 2f)
        Box(Modifier.weight(1f)) {
            if (value.isEmpty()) Text(placeholder, style = MaType.body(15.5.sp), color = c.faint, maxLines = 1)
            BasicTextField(
                value = value,
                onValueChange = onChange,
                singleLine = true,
                textStyle = MaType.body(15.5.sp).copy(color = c.fg),
                cursorBrush = SolidColor(c.accent),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focus.clearFocus() }),
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (active) Box(
            Modifier.size(26.dp).clip(CircleShape).background(c.sunk).maClick { onChange("") },
            contentAlignment = Alignment.Center
        ) { MaIcon(MaIcons.close, c.soft, 13.dp, 2.4f) }
    }
}

@Composable
private fun FilterTitle(text: String, top: androidx.compose.ui.unit.Dp) {
    Text(text.uppercase(), style = MaType.eyebrow(10.sp), color = Ma.colors.faint, modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = top))
}

/** Горизонтальный ряд чипов: курс (синие пилюли) или фильтр уровня/кафедры (мягкие) */
@Composable
private fun ChipRow(options: List<String>, selected: String, primary: Boolean, onPick: (String) -> Unit) {
    val c = Ma.colors
    Row(
        Modifier
            .padding(top = 8.dp)
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        options.forEach { o ->
            val on = o == selected
            val shape = if (primary) CircleShape else RoundedCornerShape(10.dp)
            Box(
                Modifier
                    .height(if (primary) 34.dp else 32.dp)
                    .then(if (primary && on) Modifier.maShadow(MaBrand.primaryShadow, 17.dp) else Modifier)
                    .clip(shape)
                    .background(
                        when {
                            primary && on -> MaBrand.primary
                            on -> c.accentSoft
                            else -> c.surface
                        }
                    )
                    .border(
                        1.dp,
                        when {
                            primary && on -> Color.Transparent
                            primary -> c.borderStrong
                            on -> c.accentBorder
                            else -> c.border
                        },
                        shape
                    )
                    .maClick { if (!on) onPick(o) }
                    .padding(horizontal = if (primary) 14.dp else 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    // «Кафедра информационных систем» → «Информационных систем»
                    if (!primary && o.startsWith("Кафедра ")) o.removePrefix("Кафедра ").replaceFirstChar { it.uppercase() } else o,
                    style = MaType.body(if (primary) 13.sp else 12.5.sp, FontWeight.SemiBold),
                    color = when {
                        primary && on -> Color.White
                        on -> c.accent
                        else -> c.muted
                    },
                    maxLines = 1
                )
            }
        }
    }
}

/** Карточка-список групп или преподавателей */
@Composable
private fun ChoiceList(
    names: List<String>,
    current: String?,
    cached: Map<String, Long>,
    student: Boolean,
    sub: (String) -> String,
    onPick: (String) -> Unit,
) {
    val c = Ma.colors
    Column(Modifier.fillMaxWidth().maCard(16.dp)) {
        names.forEachIndexed { i, name ->
            val sel = name == current
            if (i > 0) Box(Modifier.fillMaxWidth().height(1.dp).background(c.border))
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(if (sel) c.accentSoft else Color.Transparent)
                    .maClick { onPick(name) }
                    .padding(horizontal = 14.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    Modifier.size(38.dp).clip(CircleShape).background(if (sel) MaBrand.primary else c.accentSoft),
                    contentAlignment = Alignment.Center
                ) {
                    Text(badgeOf(name, student), style = MaType.display(12.5.sp, FontWeight.ExtraBold, tracking = 0f), color = if (sel) Color.White else c.accent, maxLines = 1)
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text(name, style = MaType.displayFor(name, 15.sp, FontWeight.Bold, tracking = -0.01f), color = c.fg, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(sub(name), style = MaType.body(12.5.sp), color = c.soft, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                if (sel) Box(Modifier.size(24.dp).clip(CircleShape).background(MaBrand.primary), contentAlignment = Alignment.Center) {
                    MaIcon(MaIcons.check, Color.White, 14.dp, 3f)
                } else if (cached.containsKey(name)) MaIcon(MaIcons.cloudCheck, c.faint, 16.dp, 2f)
                else MaIcon(MaIcons.chevRight, c.faint, 16.dp, 2f)
            }
        }
    }
}

/** Значок в кружке: для группы — буквы направления (ПИ из 24-Бк-ПИ-01), для преподавателя — инициалы */
private fun badgeOf(name: String, student: Boolean): String {
    if (!student) return name.split(' ', '.').filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }
    val parts = name.split('-', ' ').filter { it.isNotBlank() }
    val letters = parts.filter { p -> p.any { it.isLetter() } }
    val pick = letters.getOrNull(1) ?: letters.firstOrNull() ?: name
    return pick.filter { it.isLetter() }.take(2).uppercase().ifEmpty { name.take(2) }
}

@Composable
private fun LoadingCard(progress: Int) {
    val c = Ma.colors
    val p by animateFloatAsState((progress / 100f).coerceIn(0.03f, 1f), tween(300), label = "p")
    Column(Modifier.padding(top = 22.dp).fillMaxWidth().maCard(16.dp).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Обновляем данные…", style = MaType.body(14.sp, FontWeight.SemiBold), color = c.fg, modifier = Modifier.weight(1f))
            Text("$progress%", style = MaType.mono(11.5.sp), color = c.faint)
        }
        Box(Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)).background(c.sunk)) {
            Box(Modifier.fillMaxWidth(p).height(4.dp).clip(RoundedCornerShape(2.dp)).background(MaBrand.primary))
        }
        Text("Получаем список с сервера академии", style = MaType.body(12.5.sp), color = c.soft)
    }
}

@Composable
private fun NothingFound(searching: Boolean) {
    val c = Ma.colors
    Column(
        Modifier.padding(top = 22.dp).fillMaxWidth().dashedBorder(c.borderStrong, 16.dp).padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 26.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(painterResource(R.drawable.ill_search_empty), null, Modifier.size(140.dp))
        Text(if (searching) "Ничего не нашли" else "Список пуст", style = MaType.display(16.sp, FontWeight.Bold), color = c.fg)
        Text(
            if (searching) "Попробуйте «ПИ-01» или фамилию преподавателя" else "Выберите другой курс или уровень образования",
            style = MaType.body(13.sp), color = c.soft, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 4.dp)
        )
    }
}

/** Рекламный баннер Яндекса — как и раньше, по флагу Advertisement из RemoteConfig */
@Composable
private fun AdsBanner(pagerState: PagerState) {
    val darkMode = LocalAppDarkTheme.current
    val pagerAdsState = rememberPagerState(1, 0f, pageCount = { 3 })

    LaunchedEffect(pagerAdsState) {
        while (true) {
            delay(4000)
            if (!pagerState.isScrollInProgress) {
                val next = (pagerAdsState.currentPage + 1) % 3
                pagerAdsState.animateScrollToPage(next, animationSpec = tween(durationMillis = 500))
            }
        }
    }

    val banner = when (pagerState.currentPage) {
        1 -> BuildConfig.ADVERTISEMENT_BANNER_ID
        2 -> BuildConfig.ADVERTISEMENT_BANNER_ID2
        3 -> BuildConfig.ADVERTISEMENT_BANNER_ID3
        else -> BuildConfig.ADVERTISEMENT_BANNER_ID
    }

    HorizontalPager(pagerAdsState, Modifier.padding(top = 22.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            AndroidView(factory = { context ->
                BannerAdView(context).apply {
                    setAdUnitId(banner)
                    setAdSize(BannerAdSize.fixedSize(context, 400, 90))
                    val adRequest = AdRequest.Builder().setPreferredTheme(
                        if (darkMode) AdTheme.DARK else AdTheme.LIGHT
                    ).build()
                    setBannerAdEventListener(YandexAdsListener())
                    loadAd(adRequest)
                }
            })
        }
    }
}
