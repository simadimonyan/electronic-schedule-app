package com.mycollege.schedule.feature.surveys.data.models

import androidx.compose.runtime.Immutable
import com.mycollege.schedule.core.network.dto.quizzes.QuizQuestion

/**
 * Типы вопросов бэкенда
 */
enum class QuestionType {
    SINGLE, MULTIPLE, TEXT, RATING, UNKNOWN;

    companion object {
        fun of(question: QuizQuestion): QuestionType =
            entries.firstOrNull { it.name.equals(question.type, ignoreCase = true) } ?: UNKNOWN
    }
}

/**
 * Ответы пользователя на форму опроса
 *
 * choices — выбранные варианты (SINGLE — ровно один, MULTIPLE — любые);
 * values — значения TEXT и RATING ("1..scale")
 */
@Immutable
data class SurveyAnswers(
    val choices: Map<Long, Set<Long>> = emptyMap(),
    val values: Map<Long, String> = emptyMap()
) {

    fun isAnswered(question: QuizQuestion): Boolean = when (QuestionType.of(question)) {
        QuestionType.SINGLE, QuestionType.MULTIPLE -> !choices[question.id].isNullOrEmpty()
        QuestionType.TEXT, QuestionType.RATING -> !values[question.id].isNullOrBlank()
        QuestionType.UNKNOWN -> true
    }

    /**
     * Все обязательные вопросы заполнены
     */
    fun isComplete(questions: List<QuizQuestion>): Boolean =
        questions.filter { it.required == true }.all { isAnswered(it) }

}

const val SURVEY_TEXT_LIMIT = 2000
