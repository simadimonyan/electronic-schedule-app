package com.mycollege.schedule.feature.surveys.ui.state

import android.util.Log
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.my.tracker.MyTracker
import com.mycollege.schedule.BuildConfig
import com.mycollege.schedule.core.network.dto.quizzes.Quiz
import com.mycollege.schedule.feature.surveys.data.models.QuestionType
import com.mycollege.schedule.feature.surveys.data.models.SurveyAnswers
import com.mycollege.schedule.feature.surveys.data.models.SURVEY_TEXT_LIMIT
import com.mycollege.schedule.feature.surveys.data.repository.SurveyApiException
import com.mycollege.schedule.feature.surveys.domain.usecase.GetActiveSurveyUseCase
import com.mycollege.schedule.feature.surveys.domain.usecase.SubmitSurveyUseCase
import com.mycollege.schedule.feature.surveys.domain.usecase.SurveyScheduleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@Stable
@HiltViewModel
class SurveyViewModel @Inject constructor(
    private val getActiveSurveyUseCase: GetActiveSurveyUseCase,
    private val submitSurveyUseCase: SubmitSurveyUseCase,
    private val surveyScheduleUseCase: SurveyScheduleUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SurveyState())
    val state: StateFlow<SurveyState> = _state

    private var job: Job? = null

    /** debug: показывать лист при старте, игнорируя лимиты */
    val forceShow: Boolean get() = surveyScheduleUseCase.forceShow

    /** форс-показ (debug) срабатывает один раз за жизнь экрана, а не на каждое пролистывание */
    private var forcedShown = false

    fun handleEvent(event: SurveyEvent) {
        when (event) {
            is SurveyEvent.AutoCheck -> autoCheck()
            is SurveyEvent.OpenManually -> openManually()
            is SurveyEvent.SelectSingle -> updateChoices(event.questionId) { setOf(event.answerId) }
            is SurveyEvent.ToggleMultiple -> updateChoices(event.questionId) {
                if (event.answerId in it) it - event.answerId else it + event.answerId
            }
            is SurveyEvent.ChangeValue -> changeValue(event.questionId, event.value)
            is SurveyEvent.Submit -> submit()
            is SurveyEvent.Retry -> openManually()
            is SurveyEvent.NotNow -> notNow()
            is SurveyEvent.Close -> close()
        }
    }

    private fun autoCheck() {
        if (_state.value.phase != SurveyPhase.Hidden || job?.isActive == true) return
        val force = surveyScheduleUseCase.forceShow
        if (force && forcedShown) return
        if (!surveyScheduleUseCase.shouldAutoCheck()) return
        // RemoteConfig ещё не пришёл — проверим при следующем открытии, не тратя суточный лимит
        if (!getActiveSurveyUseCase.isConfigured()) return

        job = viewModelScope.launch {
            try {
                val quiz = getActiveSurveyUseCase.getActiveSurvey()
                surveyScheduleUseCase.markChecked()
                if (quiz != null && _state.value.phase == SurveyPhase.Hidden) {
                    forcedShown = force
                    MyTracker.trackEvent("Показать опрос")
                    _state.value = SurveyState(phase = SurveyPhase.Form, quiz = quiz, automatic = true)
                    applyDemo(quiz)
                }
            } catch (e: SurveyApiException) {
                // тихо: автопоказ не должен мешать расписанию
                Log.w("Surveys", "Проверка опроса не удалась: ${e.message}")
            }
        }
    }

    private fun openManually() {
        job?.cancel()
        MyTracker.trackEvent("Открыть опрос из настроек")
        _state.value = SurveyState(phase = SurveyPhase.Loading)
        job = viewModelScope.launch {
            try {
                val quiz = getActiveSurveyUseCase.getActiveSurvey()
                _state.value = if (quiz == null) SurveyState(phase = SurveyPhase.Empty)
                else SurveyState(phase = SurveyPhase.Form, quiz = quiz)
                if (quiz != null) applyDemo(quiz)
            } catch (e: SurveyApiException) {
                _state.value = SurveyState(phase = SurveyPhase.LoadError, error = e.message)
            }
        }
    }

    /**
     * Только debug (surveys.debugDemoFill / surveys.debugDemoSubmit): демо-ответы для скриншотов —
     * кириллицу через adb input text не ввести
     */
    private fun applyDemo(quiz: Quiz) {
        if (!BuildConfig.DEBUG) return
        if (BuildConfig.SURVEYS_DEBUG_DEMO_FILL) {
            val texts = ArrayDeque(listOf(
                "Удобно, что расписание открывается сразу, без интернета",
                "Иногда виджет обновляется с задержкой"
            ))
            val choices = mutableMapOf<Long, Set<Long>>()
            val values = mutableMapOf<Long, String>()
            quiz.questions.orEmpty().forEach { q ->
                when (QuestionType.of(q)) {
                    QuestionType.RATING -> values[q.id] = (q.scale ?: 5).coerceIn(2, 10).toString()
                    QuestionType.TEXT -> texts.removeFirstOrNull()?.let { values[q.id] = it }
                    QuestionType.SINGLE, QuestionType.MULTIPLE ->
                        q.answers?.firstOrNull()?.let { choices[q.id] = setOf(it.id) }
                    QuestionType.UNKNOWN -> Unit
                }
            }
            _state.update { it.copy(answers = SurveyAnswers(choices, values), demoFilled = true) }
        }
        if (BuildConfig.SURVEYS_DEBUG_DEMO_SUBMIT) {
            viewModelScope.launch {
                delay(2000)
                submit()
            }
        }
    }

    private fun updateChoices(questionId: Long, transform: (Set<Long>) -> Set<Long>) {
        _state.update { state ->
            if (state.phase != SurveyPhase.Form) return@update state
            val current = state.answers.choices[questionId].orEmpty()
            state.copy(
                answers = state.answers.copy(choices = state.answers.choices + (questionId to transform(current))),
                error = null
            )
        }
    }

    private fun changeValue(questionId: Long, value: String) {
        _state.update { state ->
            if (state.phase != SurveyPhase.Form) return@update state
            state.copy(
                answers = state.answers.copy(values = state.answers.values + (questionId to value.take(SURVEY_TEXT_LIMIT))),
                error = null
            )
        }
    }

    private fun submit() {
        val state = _state.value
        val quiz = state.quiz ?: return
        if (!state.canSubmit) return

        _state.update { it.copy(phase = SurveyPhase.Submitting, error = null) }
        job = viewModelScope.launch {
            try {
                val message = submitSurveyUseCase.submit(quiz, state.answers)
                MyTracker.trackEvent("Отправить опрос")
                _state.update { it.copy(phase = SurveyPhase.Success, completionMessage = message) }
            } catch (e: SurveyApiException) {
                _state.update { it.copy(phase = SurveyPhase.Form, error = e.message) }
            }
        }
    }

    private fun notNow() {
        val state = _state.value
        // «Не сейчас» откладывает автопоказ на 3 дня; после отправки или в ручном режиме — просто закрыть
        if (state.phase == SurveyPhase.Form || state.phase == SurveyPhase.Submitting) {
            surveyScheduleUseCase.snooze()
            MyTracker.trackEvent("Отложить опрос")
        }
        close()
    }

    private fun close() {
        job?.cancel()
        _state.value = SurveyState()
    }

}
