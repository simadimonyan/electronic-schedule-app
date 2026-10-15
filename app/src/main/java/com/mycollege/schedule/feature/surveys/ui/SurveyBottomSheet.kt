package com.mycollege.schedule.feature.surveys.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mycollege.schedule.R
import com.mycollege.schedule.core.network.dto.quizzes.Quiz
import com.mycollege.schedule.core.network.dto.quizzes.QuizAnswer
import com.mycollege.schedule.core.network.dto.quizzes.QuizQuestion
import com.mycollege.schedule.feature.surveys.ui.components.SurveyQuestion
import com.mycollege.schedule.feature.surveys.ui.components.surveyErrorColor
import com.mycollege.schedule.feature.surveys.ui.state.SurveyEvent
import com.mycollege.schedule.feature.surveys.ui.state.SurveyPhase
import com.mycollege.schedule.feature.surveys.ui.state.SurveyState
import com.mycollege.schedule.feature.surveys.ui.state.SurveyViewModel
import com.mycollege.schedule.shared.ui.design.EyebrowPill
import com.mycollege.schedule.shared.ui.design.MaIcon
import com.mycollege.schedule.shared.ui.design.MaIcons
import com.mycollege.schedule.shared.ui.design.OutlineButton
import com.mycollege.schedule.shared.ui.design.PrimaryButton
import com.mycollege.schedule.shared.ui.design.maClick
import com.mycollege.schedule.shared.ui.theme.Ma
import com.mycollege.schedule.shared.ui.theme.MaBrand
import com.mycollege.schedule.shared.ui.theme.MaType
import com.mycollege.schedule.shared.ui.theme.ScheduleTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Хост листа опроса: рисует лист, пока фаза не Hidden
 */
@Composable
fun SurveyHost(viewModel: SurveyViewModel) {
    val state by viewModel.state.collectAsState()
    if (state.phase != SurveyPhase.Hidden) {
        SurveyBottomSheet(state, viewModel::handleEvent)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurveyBottomSheet(
    state: SurveyState,
    handleEvent: (SurveyEvent) -> Unit
) {
    val c = Ma.colors
    val scope = rememberCoroutineScope()
    val currentPhase by rememberUpdatedState(state.phase)

    // во время отправки лист не смахивается
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { it != SheetValue.Hidden || currentPhase != SurveyPhase.Submitting }
    )

    val hideThen: (SurveyEvent) -> Unit = { event ->
        scope.launch { sheetState.hide() }.invokeOnCompletion { handleEvent(event) }
    }

    ModalBottomSheet(
        modifier = Modifier.statusBarsPadding().imePadding(),
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = c.sheet,
        contentColor = c.fg,
        scrimColor = Color(0x570F172A),
        dragHandle = {
            Box(Modifier.padding(top = 8.dp).size(36.dp, 5.dp).clip(RoundedCornerShape(3.dp)).background(c.borderStrong))
        },
        onDismissRequest = { handleEvent(SurveyEvent.NotNow) }
    ) {
        SurveyContent(state, handleEvent, hideThen)
    }
}

private val monthNom = listOf("январь", "февраль", "март", "апрель", "май", "июнь", "июль", "август", "сентябрь", "октябрь", "ноябрь", "декабрь")
private val monthPrep = listOf("январе", "феврале", "марте", "апреле", "мае", "июне", "июле", "августе", "сентябре", "октябре", "ноябре", "декабре")

/** месяц опроса из period «2026-10» */
private fun periodMonth(period: String?): Int? = period?.split("-")?.getOrNull(1)?.toIntOrNull()?.takeIf { it in 1..12 }

/**
 * Содержимое листа (Survey.dc.html): форма, «спасибо», «опросов нет», ошибка
 */
@Composable
fun SurveyContent(
    state: SurveyState,
    handleEvent: (SurveyEvent) -> Unit,
    hideThen: (SurveyEvent) -> Unit
) {
    val c = Ma.colors
    val month = periodMonth(state.quiz?.period)
    val formPhase = state.phase == SurveyPhase.Form || state.phase == SurveyPhase.Submitting

    Column(Modifier.fillMaxWidth().navigationBarsPadding()) {
        // шапка: «Опрос · месяц» и закрыть
        Row(Modifier.fillMaxWidth().padding(start = 22.dp, end = 14.dp, top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            EyebrowPill(if (month != null) "Опрос · ${monthNom[month - 1]}" else "Опрос", size = 10.sp)
            Box(Modifier.weight(1f))
            Box(
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(c.sunk)
                    .maClick(enabled = state.phase != SurveyPhase.Submitting) { hideThen(if (formPhase) SurveyEvent.NotNow else SurveyEvent.Close) },
                contentAlignment = Alignment.Center
            ) { MaIcon(MaIcons.close, c.fg, 16.dp, 2.2f) }
        }

        when (state.phase) {
            SurveyPhase.Hidden -> Unit

            SurveyPhase.Loading -> Box(Modifier.fillMaxWidth().height(260.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaBrand.primary, strokeWidth = 3.dp, modifier = Modifier.size(36.dp))
            }

            SurveyPhase.Empty -> InfoBlock(
                image = R.drawable.ill_survey_empty,
                title = "Сейчас опросов нет",
                text = "Загляните позже — когда появится новый опрос, мы спросим сами.",
                primary = "Понятно" to { hideThen(SurveyEvent.Close) }
            )

            SurveyPhase.LoadError -> InfoBlock(
                image = R.drawable.ill_survey_error,
                title = "Не удалось загрузить опрос",
                text = state.error?.takeIf { it.isNotBlank() } ?: "Проверьте подключение к интернету и попробуйте ещё раз.",
                primary = "Повторить" to { handleEvent(SurveyEvent.Retry) },
                secondary = "Закрыть" to { hideThen(SurveyEvent.Close) }
            )

            SurveyPhase.Success -> {
                val next = if (state.quiz?.recurrence == "monthly" && month != null) "Следующий короткий опрос — в ${monthPrep[month % 12]}." else null
                InfoBlock(
                    image = R.drawable.ill_survey_done,
                    title = state.completionMessage?.takeIf { it.isNotBlank() } ?: "Спасибо! Читаем каждый отзыв",
                    text = next,
                    primary = "Готово" to { hideThen(SurveyEvent.Close) },
                    big = true
                )
            }

            SurveyPhase.Form, SurveyPhase.Submitting -> SurveyForm(state, handleEvent, hideThen)
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.ColumnScope.SurveyForm(
    state: SurveyState,
    handleEvent: (SurveyEvent) -> Unit,
    hideThen: (SurveyEvent) -> Unit
) {
    val quiz = state.quiz ?: return
    val c = Ma.colors
    val questions = quiz.questions.orEmpty()
    val submitting = state.phase == SurveyPhase.Submitting
    val scrollState = rememberScrollState()

    // debug-демо: после автозаполнения показать кнопку «Отправить»
    LaunchedEffect(state.demoFilled) {
        if (state.demoFilled) {
            delay(400)
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    Column(
        Modifier
            .fillMaxWidth()
            .weight(1f, fill = false)
            .verticalScroll(scrollState)
            .padding(start = 22.dp, end = 22.dp, top = 12.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        Column {
            Text(quiz.name.orEmpty().ifBlank { "Пара вопросов о приложении" }, style = MaType.display(25.sp, FontWeight.ExtraBold, 29.sp), color = c.fg)
            if (!quiz.description.isNullOrBlank()) {
                Text(quiz.description, style = MaType.body(13.5.sp, lineHeight = 20.sp), color = c.soft, modifier = Modifier.padding(top = 6.dp))
            }
        }
        questions.forEach { question ->
            SurveyQuestion(
                question = question,
                answers = state.answers,
                enabled = !submitting,
                onSelectSingle = { handleEvent(SurveyEvent.SelectSingle(question.id, it)) },
                onToggleMultiple = { handleEvent(SurveyEvent.ToggleMultiple(question.id, it)) },
                onValueChange = { handleEvent(SurveyEvent.ChangeValue(question.id, it)) }
            )
        }
        if (!state.error.isNullOrBlank()) {
            Text(
                state.error,
                style = MaType.body(13.5.sp, lineHeight = 19.sp),
                color = surveyErrorColor,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(surveyErrorColor.copy(alpha = 0.12f))
                    .padding(12.dp)
            )
        }
    }

    // подвал: «Не сейчас» и «Отправить»
    Box(Modifier.fillMaxWidth().height(1.dp).background(c.border))
    Row(
        Modifier.fillMaxWidth().padding(start = 22.dp, end = 22.dp, top = 10.dp, bottom = 30.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlineButton(
            "Не сейчас", { hideThen(SurveyEvent.NotNow) },
            height = 50.dp, fontSize = 14.5.sp, color = c.muted, background = c.surface, enabled = !submitting
        )
        PrimaryButton(
            text = "Отправить",
            onClick = { handleEvent(SurveyEvent.Submit) },
            enabled = state.canSubmit || submitting,
            height = 50.dp,
            fontSize = 15.sp,
            modifier = Modifier.weight(1f),
            content = if (submitting) ({
                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
            }) else null
        )
    }
}

@Composable
private fun InfoBlock(
    image: Int,
    title: String,
    text: String?,
    primary: Pair<String, () -> Unit>,
    secondary: Pair<String, () -> Unit>? = null,
    big: Boolean = false,
) {
    val c = Ma.colors
    Column(
        Modifier.fillMaxWidth().heightIn(min = 300.dp).padding(horizontal = 30.dp).padding(top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Image(painterResource(image), null, Modifier.size(if (big) 150.dp else 132.dp))
        Text(title, style = MaType.display(if (big) 23.sp else 22.sp, FontWeight.ExtraBold, 27.sp), color = c.fg, textAlign = TextAlign.Center)
        if (!text.isNullOrBlank()) Text(text, style = MaType.body(14.sp, lineHeight = 21.7.sp), color = c.soft, textAlign = TextAlign.Center)
    }
    Column(
        Modifier.fillMaxWidth().padding(start = 22.dp, end = 22.dp, top = 16.dp, bottom = 30.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        PrimaryButton(primary.first, primary.second, Modifier.fillMaxWidth(), height = 52.dp, fontSize = 15.sp)
        if (secondary != null) Box(
            Modifier.fillMaxWidth().height(44.dp).clip(CircleShape).maClick(onClick = secondary.second),
            contentAlignment = Alignment.Center
        ) { Text(secondary.first, style = MaType.body(14.sp, FontWeight.SemiBold), color = c.muted) }
    }
}

@Preview
@Composable
fun SurveyPreview() {
    val quiz = Quiz(
        id = 1, name = "Как вам «Моя Академия»?", description = null, completionMessage = "Спасибо! Читаем каждый отзыв 💙",
        status = "PUBLISHED", groups = emptyList(), startDate = null, endDate = null, applications = 0,
        recurrence = "monthly", period = "2026-10",
        questions = listOf(
            QuizQuestion(1, "Как вам «Моя Академия» в этом месяце?", null, "RATING", true, 5, emptyList()),
            QuizQuestion(2, "Чем пользуетесь чаще?", null, "SINGLE", false, null, listOf(QuizAnswer(1, "Расписание"), QuizAnswer(2, "Виджеты"))),
            QuizQuestion(3, "Что нравится? Похвалите нас 🙂", null, "TEXT", false, null, emptyList())
        )
    )
    ScheduleTheme(false) {
        Column(Modifier.background(Ma.colors.sheet)) {
            SurveyContent(SurveyState(phase = SurveyPhase.Form, quiz = quiz), {}, {})
        }
    }
}
