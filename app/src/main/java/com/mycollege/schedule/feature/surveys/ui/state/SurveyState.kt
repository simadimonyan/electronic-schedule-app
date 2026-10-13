package com.mycollege.schedule.feature.surveys.ui.state

import androidx.compose.runtime.Immutable
import com.mycollege.schedule.core.network.dto.quizzes.Quiz
import com.mycollege.schedule.feature.surveys.data.models.SurveyAnswers

/**
 * Фаза листа опроса
 */
enum class SurveyPhase {
    /** лист закрыт */
    Hidden,
    /** загрузка опроса (открыт вручную из настроек) */
    Loading,
    /** форма */
    Form,
    /** отправка анкеты */
    Submitting,
    /** спасибо — completionMessage */
    Success,
    /** открытых опросов нет */
    Empty,
    /** не удалось загрузить опрос */
    LoadError
}

@Immutable
data class SurveyState(
    val phase: SurveyPhase = SurveyPhase.Hidden,
    val quiz: Quiz? = null,
    val answers: SurveyAnswers = SurveyAnswers(),
    /** ошибка загрузки или отправки (текст из тела 400/409) */
    val error: String? = null,
    val completionMessage: String? = null,
    /** лист показан автоматически (закрытие = «Не сейчас») */
    val automatic: Boolean = false
) {
    val canSubmit: Boolean
        get() = phase == SurveyPhase.Form && quiz != null && answers.isComplete(quiz.questions.orEmpty())
}
