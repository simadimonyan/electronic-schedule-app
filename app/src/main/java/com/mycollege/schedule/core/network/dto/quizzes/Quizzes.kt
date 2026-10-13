package com.mycollege.schedule.core.network.dto.quizzes

import androidx.compose.runtime.Immutable

/**
 * Ответ GET api/v1/quizzes/search — открытые опросы, ещё не пройденные этим respondent
 */
@Immutable
data class Quizzes(
    val quizzes: List<Quiz>?
)

/**
 * QuizResponse бэкенда (schedule-parse-system)
 *
 * recurrence — null (один раз) или "monthly";
 * period — для monthly текущий месяц по Москве ("2026-10"), иначе null
 */
@Immutable
data class Quiz(
    val id: Long,
    val name: String?,
    val description: String?,
    val completionMessage: String?,
    val status: String?,
    val groups: List<String>?,
    val questions: List<QuizQuestion>?,
    val startDate: String?,
    val endDate: String?,
    val applications: Long?,
    val recurrence: String?,
    val period: String?
)

/**
 * QuestionUnit: type — SINGLE | MULTIPLE | TEXT | RATING
 */
@Immutable
data class QuizQuestion(
    val id: Long,
    val label: String?,
    val description: String?,
    val type: String?,
    val required: Boolean?,
    val scale: Int?,
    val answers: List<QuizAnswer>?
)

@Immutable
data class QuizAnswer(
    val id: Long,
    val label: String?
)

/**
 * Тело POST api/v1/quizzes/submit
 */
@Immutable
data class QuizSubmitRequest(
    val quizId: Long,
    val respondent: String,
    val group: String?,
    val answers: List<QuizSubmitUnit>
)

/**
 * SINGLE → {questionId, answerId}; MULTIPLE → по строке на вариант;
 * TEXT → {questionId, value}; RATING → {questionId, value:"1..scale"}
 */
@Immutable
data class QuizSubmitUnit(
    val questionId: Long,
    val answerId: Long? = null,
    val value: String? = null
)

@Immutable
data class QuizSubmitResponse(
    val quizId: Long?,
    val message: String?
)
