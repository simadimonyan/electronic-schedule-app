package com.mycollege.schedule.feature.surveys.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mycollege.schedule.core.network.dto.quizzes.Quiz
import com.mycollege.schedule.core.network.dto.quizzes.QuizAnswer
import com.mycollege.schedule.core.network.dto.quizzes.QuizQuestion
import com.mycollege.schedule.feature.surveys.ui.components.SurveyQuestion
import com.mycollege.schedule.feature.surveys.ui.components.surveyErrorColor
import com.mycollege.schedule.feature.surveys.ui.components.surveySecondaryText
import com.mycollege.schedule.feature.surveys.ui.state.SurveyEvent
import com.mycollege.schedule.feature.surveys.ui.state.SurveyPhase
import com.mycollege.schedule.feature.surveys.ui.state.SurveyState
import com.mycollege.schedule.feature.surveys.ui.state.SurveyViewModel
import com.mycollege.schedule.shared.ui.theme.LocalAppDarkTheme
import com.mycollege.schedule.shared.ui.theme.backgroundDark
import com.mycollege.schedule.shared.ui.theme.buttons
import com.mycollege.schedule.shared.ui.theme.disabledBlue
import com.mycollege.schedule.shared.ui.theme.disabledDark
import com.mycollege.schedule.shared.ui.theme.disabledWhite
import com.mycollege.schedule.shared.ui.theme.tertiaryDark
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState

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
    val darkMode = LocalAppDarkTheme.current
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
        modifier = Modifier
            .wrapContentHeight()
            .padding(10.dp, 30.dp, 10.dp, 15.dp)
            .imePadding(),
        sheetState = sheetState,
        shape = RoundedCornerShape(17.dp),
        contentColor = if (darkMode) Color.White else Color.Black,
        containerColor = if (darkMode) backgroundDark else Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle(color = if (darkMode) Color.White else Color.Black) },
        onDismissRequest = { handleEvent(SurveyEvent.NotNow) }
    ) {
        SurveyContent(state, handleEvent, hideThen)
    }
}

@Composable
fun SurveyContent(
    state: SurveyState,
    handleEvent: (SurveyEvent) -> Unit,
    hideThen: (SurveyEvent) -> Unit
) {
    val darkMode = LocalAppDarkTheme.current
    val textColor = if (darkMode) Color.White else Color.Black
    val scrollState = rememberScrollState()

    // debug-демо: после автозаполнения показать кнопку «Отправить»
    LaunchedEffect(state.demoFilled) {
        if (state.demoFilled) {
            delay(400)
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
    ) {
        Text(
            text = "Пара вопросов о приложении",
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            textAlign = TextAlign.Center,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )

        when (state.phase) {
            SurveyPhase.Hidden -> Unit

            SurveyPhase.Loading -> Box(
                modifier = Modifier.fillMaxWidth().height(220.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = buttons)
            }

            SurveyPhase.Empty -> MessageBlock(
                icon = { Icon(Icons.Outlined.Info, null, tint = buttons, modifier = Modifier.size(52.dp)) },
                title = "Сейчас опросов нет",
                subtitle = "Загляните позже — когда появится новый опрос, мы спросим сами",
                primary = "Закрыть" to { hideThen(SurveyEvent.Close) }
            )

            SurveyPhase.LoadError -> MessageBlock(
                icon = { Icon(Icons.Outlined.Info, null, tint = surveyErrorColor, modifier = Modifier.size(52.dp)) },
                title = "Не удалось загрузить опрос",
                subtitle = state.error,
                primary = "Повторить" to { handleEvent(SurveyEvent.Retry) },
                secondary = "Закрыть" to { hideThen(SurveyEvent.Close) }
            )

            SurveyPhase.Success -> MessageBlock(
                icon = { Icon(Icons.Filled.CheckCircle, null, tint = buttons, modifier = Modifier.size(56.dp)) },
                title = state.completionMessage ?: "Спасибо за ответы!",
                subtitle = null,
                primary = "Готово" to { hideThen(SurveyEvent.Close) }
            )

            SurveyPhase.Form, SurveyPhase.Submitting -> SurveyForm(state, handleEvent, hideThen)
        }

        Spacer(Modifier.height(10.dp))
    }
}

@Composable
private fun SurveyForm(
    state: SurveyState,
    handleEvent: (SurveyEvent) -> Unit,
    hideThen: (SurveyEvent) -> Unit
) {
    val quiz = state.quiz ?: return
    val questions = quiz.questions.orEmpty()
    val darkMode = LocalAppDarkTheme.current
    val submitting = state.phase == SurveyPhase.Submitting

    if (!quiz.name.isNullOrBlank()) {
        Text(
            text = quiz.name,
            modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 6.dp),
            textAlign = TextAlign.Center,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = buttons
        )
    }
    if (!quiz.description.isNullOrBlank()) {
        Text(
            text = quiz.description,
            modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 4.dp),
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
            lineHeight = 18.sp,
            color = surveySecondaryText
        )
    }

    Spacer(Modifier.height(10.dp))

    questions.forEachIndexed { index, question ->
        if (index != 0) {
            HorizontalDivider(
                thickness = 1.dp,
                modifier = Modifier.padding(horizontal = 25.dp, vertical = 4.dp),
                color = if (darkMode) Color.DarkGray else disabledWhite
            )
        }
        SurveyQuestion(
            index = index + 1,
            question = question,
            answers = state.answers,
            enabled = !submitting,
            onSelectSingle = { handleEvent(SurveyEvent.SelectSingle(question.id, it)) },
            onToggleMultiple = { handleEvent(SurveyEvent.ToggleMultiple(question.id, it)) },
            onValueChange = { handleEvent(SurveyEvent.ChangeValue(question.id, it)) }
        )
    }

    if (questions.any { it.required == true }) {
        Text(
            text = "* — обязательный вопрос",
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
            fontSize = 12.sp,
            color = surveySecondaryText
        )
    }

    if (!state.error.isNullOrBlank()) {
        Text(
            text = state.error,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(surveyErrorColor.copy(alpha = 0.12f))
                .padding(12.dp),
            fontSize = 14.sp,
            color = surveyErrorColor,
            textAlign = TextAlign.Center
        )
    }

    Spacer(Modifier.height(8.dp))

    PrimaryButton(
        text = "Отправить",
        enabled = state.canSubmit,
        loading = submitting,
        onClick = { handleEvent(SurveyEvent.Submit) }
    )

    TextButton(
        onClick = { hideThen(SurveyEvent.NotNow) },
        enabled = !submitting,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
    ) {
        Text("Не сейчас", fontSize = 15.sp, color = if (darkMode) Color.White.copy(alpha = 0.7f) else surveySecondaryText)
    }
}

@Composable
private fun PrimaryButton(
    text: String,
    enabled: Boolean,
    loading: Boolean = false,
    onClick: () -> Unit
) {
    val darkMode = LocalAppDarkTheme.current
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(50.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = buttons,
            contentColor = Color.White,
            disabledContainerColor = if (loading) buttons else if (darkMode) disabledDark else disabledBlue.copy(alpha = 0.35f),
            disabledContentColor = if (loading) Color.White else if (darkMode) tertiaryDark else Color.White
        )
    ) {
        if (loading) {
            CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
        } else {
            Text(text, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun MessageBlock(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String?,
    primary: Pair<String, () -> Unit>,
    secondary: Pair<String, () -> Unit>? = null
) {
    val darkMode = LocalAppDarkTheme.current
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        icon()
        Text(
            text = title,
            modifier = Modifier.padding(horizontal = 24.dp),
            textAlign = TextAlign.Center,
            fontSize = 17.sp,
            lineHeight = 23.sp,
            fontWeight = FontWeight.Medium,
            color = if (darkMode) Color.White else Color.Black
        )
        if (!subtitle.isNullOrBlank()) {
            Text(
                text = subtitle,
                modifier = Modifier.padding(horizontal = 24.dp),
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                lineHeight = 18.sp,
                color = surveySecondaryText
            )
        }
        Spacer(Modifier.height(6.dp))
        PrimaryButton(text = primary.first, enabled = true, onClick = primary.second)
        if (secondary != null) {
            TextButton(onClick = secondary.second, modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                Text(secondary.first, fontSize = 15.sp, color = surveySecondaryText)
            }
        }
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
    Column(Modifier.background(Color.White)) {
        SurveyContent(SurveyState(phase = SurveyPhase.Form, quiz = quiz), {}, {})
    }
}
