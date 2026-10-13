package com.mycollege.schedule.feature.surveys.domain.usecase

import androidx.compose.runtime.Immutable
import com.mycollege.schedule.core.network.dto.quizzes.Quiz
import com.mycollege.schedule.core.network.dto.quizzes.QuizSubmitRequest
import com.mycollege.schedule.core.network.dto.quizzes.QuizSubmitUnit
import com.mycollege.schedule.feature.surveys.data.models.QuestionType
import com.mycollege.schedule.feature.surveys.data.models.SURVEY_TEXT_LIMIT
import com.mycollege.schedule.feature.surveys.data.models.SurveyAnswers
import com.mycollege.schedule.feature.surveys.data.repository.SurveyPreferences
import com.mycollege.schedule.feature.surveys.data.repository.SurveyRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Отправка анкеты; возвращает текст благодарности
 */
@Singleton
@Immutable
class SubmitSurveyUseCase @Inject constructor(
    private val repository: SurveyRepository,
    private val preferences: SurveyPreferences,
    private val getActiveSurveyUseCase: GetActiveSurveyUseCase
) {

    suspend fun submit(quiz: Quiz, answers: SurveyAnswers): String {
        val request = QuizSubmitRequest(
            quizId = quiz.id,
            respondent = preferences.respondent(),
            group = getActiveSurveyUseCase.respondentGroup(),
            answers = buildUnits(quiz, answers)
        )
        val response = repository.submit(request)
        return response.message?.takeIf { it.isNotBlank() }
            ?: quiz.completionMessage?.takeIf { it.isNotBlank() }
            ?: "Спасибо за ответы!"
    }

    /**
     * Необязательный вопрос без ответа не передаётся вовсе
     */
    fun buildUnits(quiz: Quiz, answers: SurveyAnswers): List<QuizSubmitUnit> =
        quiz.questions.orEmpty().flatMap { question ->
            when (QuestionType.of(question)) {
                QuestionType.SINGLE -> answers.choices[question.id].orEmpty().take(1)
                    .map { QuizSubmitUnit(questionId = question.id, answerId = it) }
                QuestionType.MULTIPLE -> answers.choices[question.id].orEmpty().sorted()
                    .map { QuizSubmitUnit(questionId = question.id, answerId = it) }
                QuestionType.TEXT -> answers.values[question.id]?.trim()?.take(SURVEY_TEXT_LIMIT)
                    ?.takeIf { it.isNotBlank() }
                    ?.let { listOf(QuizSubmitUnit(questionId = question.id, value = it)) }
                    .orEmpty()
                QuestionType.RATING -> answers.values[question.id]?.takeIf { it.isNotBlank() }
                    ?.let { listOf(QuizSubmitUnit(questionId = question.id, value = it)) }
                    .orEmpty()
                QuestionType.UNKNOWN -> emptyList()
            }
        }

}
